/*
 * Copyright (C) Baidu Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.titan.core.patch.full;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/5/17
 */
public class PatchLoaderGenerator {


    static class LazyLoadInterceptorHashCode {

        public final int hashCode;

        public final Set<DexType> types = new HashSet<>();

        public LazyLoadInterceptorHashCode(int hashCode) {
            this.hashCode = hashCode;
        }

    }

    static int hashCode(String str) {
        int hash = 0;
        char[] value = str.toCharArray();
        if (value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                hash = 31 * hash + val[i];
            }
        }
        return hash;
    }


    private static List<LazyLoadInterceptorHashCode> reduceAndOrderInterceptorHashCode(
            ApplicationDexPool appPool, TitanDexItemFactory factory) {
        List<LazyLoadInterceptorHashCode> result =
                appPool.getProgramClassPool().stream()
                        .map(dcn -> PatchUtils.getOrgTypeFromInterceptorType(dcn.type, factory))
                        .collect(
                                Collectors.groupingBy(type ->
                                        hashCode(type.toTypeDescriptor()),
                                        Collectors.toList()))
                        .entrySet().stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                        .map(entry -> {
                            LazyLoadInterceptorHashCode hashCode =
                            new LazyLoadInterceptorHashCode(entry.getKey());
                            entry.getValue().forEach(type -> hashCode.types.add(type));
                            return hashCode;
                        })
                        .collect(Collectors.toList());
        return result;
    }

    private static DexClassNode generateClinitInterceptor(
            ApplicationDexPool interceptorPool,
            TitanDexItemFactory factory) {

        DexClassNode classClinitInterceptor = new DexClassNode(
                factory.classInitInterceptorClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                factory.simpleClassClinitInterceptorClass.type,
                DexTypeList.empty());

        DexClassVisitor classClinitInterceptorVisitor = classClinitInterceptor.asVisitor();

        classClinitInterceptorVisitor.visitBegin();

        DexMethodVisitorInfo inovkeClinitMethodVisitorInfo = new DexMethodVisitorInfo(
                classClinitInterceptor.type,
                factory.classClinitInterceptableClass.invokeClinitMethod.getName(),
                factory.classClinitInterceptableClass.invokeClinitMethod.getParameterTypes(),
                factory.classClinitInterceptableClass.invokeClinitMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor invokeClinitMethodVisitor = classClinitInterceptorVisitor.visitMethod(
                inovkeClinitMethodVisitorInfo);

        invokeClinitMethodVisitor.visitBegin();

        DexCodeRegisterCalculator invokeClinitCodeVisitor = new DexCodeRegisterCalculator(
                false,
                inovkeClinitMethodVisitorInfo.parameters,
                new DexCodeFormatVerifier(invokeClinitMethodVisitor.visitCode()));

        invokeClinitCodeVisitor.visitBegin();

        int pHashCodeReg = 1;

        int pTypeDescReg = 2;

        int vInterceptorReg = 0;

        int vTypeConstReg = 1;

        int vEqualsResultReg = 2;

        int vInterceptResultReg = 3;

        DexLabel returnLabel = new DexLabel();

        DexLabel interceptLabel = new DexLabel();

        List<LazyLoadInterceptorHashCode> classHashCodes =
                reduceAndOrderInterceptorHashCode(interceptorPool, factory);

        int[] keys = classHashCodes.stream()
                .mapToInt(h -> h.hashCode)
                .toArray();

        DexLabel[] targets = new DexLabel[keys.length];
        for (int i = 0; i < targets.length; i++) {
            targets[i] = new DexLabel();
        }

        invokeClinitCodeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pHashCodeReg)),
                keys,
                targets);

        for (int i = 0; i < classHashCodes.size(); i++) {
            LazyLoadInterceptorHashCode hashCode = classHashCodes.get(i);
            invokeClinitCodeVisitor.visitLabel(targets[i]);

            DexLabel nextCompareLabel = new DexLabel();

            for (DexType type : hashCode.types) {

                invokeClinitCodeVisitor.visitLabel(nextCompareLabel);

                nextCompareLabel = new DexLabel();

                invokeClinitCodeVisitor.visitConstInsn(
                        Dops.CONST_STRING,
                        DexRegisterList.make(DexRegister.makeLocalReg(vTypeConstReg)),
                        DexConst.ConstString.make(type.toTypeDescriptor()));

                invokeClinitCodeVisitor.visitConstInsn(
                        Dops.INVOKE_VIRTUAL,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vTypeConstReg),
                                DexRegister.makeParameterReg(pTypeDescReg)),
                        factory.stringClass.equalsMethod);

                invokeClinitCodeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vEqualsResultReg)));

                invokeClinitCodeVisitor.visitTargetInsn(
                        Dops.IF_EQZ,
                        DexRegisterList.make(DexRegister.makeLocalReg(vEqualsResultReg)),
                        nextCompareLabel);

                DexType interceptorType = PatchUtils.getInterceptorType(type, factory);

                invokeClinitCodeVisitor.visitConstInsn(
                        Dops.NEW_INSTANCE,
                        DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                        DexConst.ConstType.make(interceptorType));
                invokeClinitCodeVisitor.visitConstInsn(
                        Dops.INVOKE_DIRECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                        DexConst.ConstMethodRef.make(
                                interceptorType,
                                factory.methods.initMethodName,
                                factory.voidClass.primitiveType,
                                DexTypeList.empty()));

                invokeClinitCodeVisitor.visitTargetInsn(
                        Dops.GOTO,
                        DexRegisterList.empty(),
                        interceptLabel);
            }

            // for the last compare
            invokeClinitCodeVisitor.visitLabel(nextCompareLabel);
            // 没有match equals compare:

            invokeClinitCodeVisitor.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.LiteralBits32.make(0));

            invokeClinitCodeVisitor.visitTargetInsn(
                    Dops.GOTO,
                    DexRegisterList.empty(),
                    returnLabel);
        }

        invokeClinitCodeVisitor.visitLabel(interceptLabel);
        invokeClinitCodeVisitor.visitConstInsn(
                Dops.NEW_INSTANCE,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                DexConst.ConstType.make(factory.interceptResultClass.type));

        invokeClinitCodeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                factory.interceptResultClass.initMethod);

        invokeClinitCodeVisitor.visitConstInsn(
                Dops.IPUT_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vInterceptorReg),
                        DexRegister.makeLocalReg(vInterceptResultReg)),
                factory.interceptResultClass.interceptorField);

        // : return label
        invokeClinitCodeVisitor.visitLabel(returnLabel);

        invokeClinitCodeVisitor.visitSimpleInsn(
                Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));

        invokeClinitCodeVisitor.fillRegisterCount();

        invokeClinitCodeVisitor.visitEnd();


        // <init> method

        DexMethodVisitorInfo initMethodVisitorInfo = new DexMethodVisitorInfo(
                classClinitInterceptor.type,
                factory.methods.initMethodName,
                DexTypeList.empty(),
                factory.voidClass.primitiveType,
                new DexAccessFlags(
                        DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_CONSTRUCTOR));


        DexMethodVisitor initMethodVisitor = classClinitInterceptorVisitor.visitMethod(
                initMethodVisitorInfo);

        initMethodVisitor.visitBegin();

        DexCodeRegisterCalculator initCodeVisitor = new DexCodeRegisterCalculator(
                false,
                initMethodVisitorInfo.parameters,
                new DexCodeFormatVerifier(initMethodVisitor.visitCode()));

        initCodeVisitor.visitBegin();

        int pThisReg = 0;

        initCodeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisReg)),
                DexConst.ConstMethodRef.make(
                        factory.simpleClassClinitInterceptorClass.type,
                        factory.createString("<init>"),
                        factory.voidClass.primitiveType,
                        DexTypeList.empty()));

        initCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        initCodeVisitor.fillRegisterCount();

        initCodeVisitor.visitEnd();

        initMethodVisitor.visitEnd();

        classClinitInterceptorVisitor.visitEnd();

        return classClinitInterceptor;
    }

    private static void generateLoaderInitMethod(DexClassNode patchLoaderClass,
                                                 DexClassVisitor loaderClassVisitor,
                                                 TitanDexItemFactory factory) {
        DexMethodVisitor initMethodVisitor = loaderClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        patchLoaderClass.type,
                        factory.methods.initMethodName,
                        DexTypeList.empty(),
                        factory.voidClass.primitiveType,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC,
                                DexAccessFlags.ACC_CONSTRUCTOR)));

        initMethodVisitor.visitBegin();

        DexCodeRegisterCalculator initCodeVisitor = new DexCodeRegisterCalculator(
                false,
                DexTypeList.empty(),
                new DexCodeFormatVerifier(initMethodVisitor.visitCode()));

        initCodeVisitor.visitBegin();
        int pThisObjReg = 0;
        initCodeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisObjReg)),
                DexConst.ConstMethodRef.make(
                        patchLoaderClass.superType,
                        factory.methods.initMethodName,
                        factory.voidClass.primitiveType,
                        DexTypeList.empty()));

        initCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        initCodeVisitor.fillRegisterCount();

        initCodeVisitor.visitEnd();
        initMethodVisitor.visitEnd();
    }

    public static void generateLoader(
            ApplicationDexPool interceptorPool,
            ApplicationDexPool clinitInterceptorPool,
            ApplicationDexPool loaderPool,
            TitanDexItemFactory factory) {

        DexClassNode clinitInterceptor = generateClinitInterceptor(interceptorPool, factory);
        clinitInterceptorPool.addProgramClass(clinitInterceptor);

        DexClassNode patchLoaderClass = new DexClassNode(
                factory.patchLoaderClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                factory.patchBaseLoaderClass.type,
               DexTypeList.empty());

        DexClassVisitor loaderClassVisitor = patchLoaderClass.asVisitor();

        loaderClassVisitor.visitBegin();

        generateLoaderInitMethod(patchLoaderClass, loaderClassVisitor, factory);

        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                patchLoaderClass.type,
                factory.createString("apply"),
                DexTypeList.empty(),
                factory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor applyMethodVisitor = loaderClassVisitor.visitMethod(methodVisitorInfo);

        applyMethodVisitor.visitBegin();

        DexCodeRegisterCalculator applyCodeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(applyMethodVisitor.visitCode()));

        int vClinitInterceptorReg = 0;

        applyCodeVisitor.visitBegin();

        applyCodeVisitor.visitConstInsn(
                Dops.NEW_INSTANCE,
                DexRegisterList.make(DexRegister.makeLocalReg(vClinitInterceptorReg)),
                DexConst.ConstType.make(clinitInterceptor.type));

        applyCodeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vClinitInterceptorReg)),
                DexConst.ConstMethodRef.make(
                        clinitInterceptor.type,
                        factory.createString("<init>"),
                        factory.voidClass.primitiveType,
                        DexTypeList.empty()));

        applyCodeVisitor.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.make(DexRegister.makeLocalReg(vClinitInterceptorReg)),
                factory.titanRuntimeClass.setClassClinitInterceptorMethod);

        applyCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        applyCodeVisitor.fillRegisterCount();

        applyCodeVisitor.visitEnd();

        applyMethodVisitor.visitEnd();

        loaderClassVisitor.visitEnd();

        loaderPool.addProgramClass(patchLoaderClass);

    }

}
