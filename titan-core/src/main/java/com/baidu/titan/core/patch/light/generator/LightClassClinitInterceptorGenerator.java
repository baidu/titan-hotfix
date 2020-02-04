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

package com.baidu.titan.core.patch.light.generator;

import com.baidu.titan.core.Constant;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.light.LightPatchClassPools;
import com.baidu.titan.core.patch.light.generator.changed.InterceptorProcessor;
import com.baidu.titan.core.util.TitanHashs;
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
 * @author shanghuibo
 * @since 2018/9/28
 */
public class LightClassClinitInterceptorGenerator {

    private final LightPatchClassPools mClassPools;

    private final TitanDexItemFactory mDexItemFactory;

    /** 标记是否可以懒加载*/
    private static final String KEY_EXTRA_INTERCRTOR_LAZY_INIT_ABLE = "_extra_intercetor_lazy_init_able";
    /** 标记是否需要调用staic init*/
    private static final String KEY_EXTRA_INTERCRTOR_CALL_STATIC_INIT = "_extra_intercetor_call_static_init";
    /** 标记clinit发生了修改 */
    public static final String EXTRA_CLINIT_CHANGED = "clinit_changed";

    private LightClassClinitInterceptorGenerator(LightPatchClassPools classPools,
                                                 TitanDexItemFactory dexItemFactory) {
        this.mClassPools = classPools;
        this.mDexItemFactory = dexItemFactory;
    }

    public static void generate(LightPatchClassPools classPools,
                                TitanDexItemFactory dexItemFactory) {
        LightClassClinitInterceptorGenerator generator =
                new LightClassClinitInterceptorGenerator(classPools, dexItemFactory);
        generator.doGenerate();
    }

    private void doGenerate() {
        generateClinitInterceptor(mClassPools,
                mDexItemFactory);
    }

    /**
     * interceptor类对应的class需要有<clinit>方法才能做到lazy init
     *
     * @param dcn
     * @return
     */
    public static boolean isInterceptorLazyInitAble(DexClassNode dcn) {
        return dcn.getExtraInfo(KEY_EXTRA_INTERCRTOR_LAZY_INIT_ABLE, false);
    }

    public static void setInterceptorLazyInitAble(DexClassNode dcn, boolean lazyInitAble) {
        dcn.setExtraInfo(KEY_EXTRA_INTERCRTOR_LAZY_INIT_ABLE, lazyInitAble);
    }

    /**
     * 是否需要在patchloader中调用static init
     *
     * @param dcn  获取extra info的class node
     * @return 是否需要在patchloader中调用static init
     */
    public static boolean shouldCallStaticInit(DexClassNode dcn) {
        return dcn.getExtraInfo(KEY_EXTRA_INTERCRTOR_CALL_STATIC_INIT, false);
    }

    /**
     * 设置是否需要在patchloader中调用static init
     *
     * @param dcn 设置extra info的class node
     * @param callStaticInit 是否调用static init
     */
    public static void setCallStaticInit(DexClassNode dcn, boolean callStaticInit) {
        dcn.setExtraInfo(KEY_EXTRA_INTERCRTOR_CALL_STATIC_INIT, callStaticInit);
    }

    /**
     * clinit 是否发生了修改
     *
     * @param dcn 被标记是否修改的class node
     * @return clinit是否发生了修改
     */
    public static boolean isClinitChanged(DexClassNode dcn) {
        return dcn.getExtraInfo(EXTRA_CLINIT_CHANGED, false);
    }

    /**
     * 标记clinit发生了修改
     *
     * @param dcn 被标记为发生修改的class node
     */
    public static void setClinitChanged(DexClassNode dcn) {
        dcn.setExtraInfo(EXTRA_CLINIT_CHANGED, true);
    }

    static class LazyLoadInterceptorHashCode {

        public final int typeHashCode;

        public final Set<DexType> types = new HashSet<>();

        public LazyLoadInterceptorHashCode(int hashCode) {
            this.typeHashCode = hashCode;
        }

    }

    private static List<LazyLoadInterceptorHashCode> reduceAndOrderInterceptorHashCode(
            LightPatchClassPools classPools, TitanDexItemFactory factory) {
        List<LazyLoadInterceptorHashCode> result =
                classPools.lazyInitClassPool.getProgramClassPool().stream()
                        .map(dcn -> dcn.type)
                        .collect(
                                Collectors.groupingBy(type -> TitanHashs.type2HashCode(type),
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

    private static List<LazyLoadInterceptorHashCode> reduceAndOrderInterceptorHashCodeForPostInit(
            LightPatchClassPools classPools, TitanDexItemFactory factory) {
        List<LazyLoadInterceptorHashCode> result =
                classPools.lazyInitClassPool.getProgramClassPool().stream()
                        .filter(LightClassClinitInterceptorGenerator::isClinitChanged)
                        .map(dcn -> dcn.type)
                        .collect(
                                Collectors.groupingBy(type -> TitanHashs.type2HashCode(type),
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

    private DexClassNode generateClinitInterceptor(
            LightPatchClassPools classPools,
            TitanDexItemFactory factory) {

        DexClassNode classClinitInterceptor = new DexClassNode(
                mDexItemFactory.classInitInterceptorClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                mDexItemFactory.simpleClassClinitInterceptorClass.type,
                DexTypeList.empty());

        mClassPools.clinitIntercepotroClassPool.addProgramClass(classClinitInterceptor);

        DexClassVisitor classClinitInterceptorVisitor = classClinitInterceptor.asVisitor();

        classClinitInterceptorVisitor.visitBegin();

        generateInvokeClinitMethod(classPools, factory, classClinitInterceptor, classClinitInterceptorVisitor);
        generateInvokePostClinitMethod(classPools, factory, classClinitInterceptor, classClinitInterceptorVisitor);
        generateInitMethod(factory, classClinitInterceptor, classClinitInterceptorVisitor);

        classClinitInterceptorVisitor.visitEnd();

        return classClinitInterceptor;
    }

    private void generateInvokeClinitMethod(LightPatchClassPools classPools,
                                             TitanDexItemFactory factory,
                                             DexClassNode classClinitInterceptor,
                                             DexClassVisitor classClinitInterceptorVisitor) {
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

        int vInterceptResultReg = 0;

        int vTypeConstReg = 1;

        int vEqualsResultReg = 2;

        int vInterceptorReg = 3;

        int vInterceptFlagReg = 4;

        DexLabel returnLabel = new DexLabel();

        DexLabel interceptLabel = new DexLabel();

        List<LazyLoadInterceptorHashCode> classHashCodes =
                reduceAndOrderInterceptorHashCode(mClassPools, factory);

        if (classHashCodes.isEmpty()) {
            invokeClinitCodeVisitor.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.LiteralBits32.make(0));

            invokeClinitCodeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));
        } else {

            int[] keys = classHashCodes.stream()
                    .mapToInt(h -> h.typeHashCode)
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

            invokeClinitCodeVisitor.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.LiteralBits32.make(0));

            // : return label
            invokeClinitCodeVisitor.visitLabel(returnLabel);

            invokeClinitCodeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));

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


                    DexClassNode dcn = classPools.lazyInitClassPool.getProgramClassPool().getClass(type);
                    if (LightClassClinitInterceptorGenerator.isInterceptorLazyInitAble(dcn)) {
                        // 对于有interceptor class node的情况，需要创建$iter类实例
                        // new interceptor
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
                    } else {
                        invokeClinitCodeVisitor.visitConstInsn(
                                Dops.CONST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                                DexConst.LiteralBits32.make(0));
                    }

                    // 若clinit发生了变化，需要设置interceptFlag，以调用postClinit方法
                    boolean clinitChanged = isClinitChanged(dcn);
                    invokeClinitCodeVisitor.visitConstInsn(
                            Dops.CONST,
                            DexRegisterList.make(DexRegister.makeLocalReg(vInterceptFlagReg)),
                            DexConst.LiteralBits32.make(
                                    clinitChanged ? Constant.INIT_CONTEXT_FLAG_INTERCEPTED : 0));


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

            // InterceptResult interceptResult = new InterceptResult();
            // interceptResult.interceptor = interceptor;
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


            invokeClinitCodeVisitor.visitTargetInsn(
                    Dops.IF_EQZ,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptFlagReg)),
                    returnLabel);

            // interceptableResult.flags |= Constant.INIT_CONTEXT_FLAG_INTERCEPTED
            invokeClinitCodeVisitor.visitConstInsn(
                    Dops.IGET,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vInterceptFlagReg),
                            DexRegister.makeLocalReg(vInterceptResultReg)),
                    factory.interceptResultClass.flagsField);

            invokeClinitCodeVisitor.visitConstInsn(Dops.OR_INT_LIT8,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vInterceptFlagReg),
                            DexRegister.makeLocalReg(vInterceptFlagReg)),
                    DexConst.LiteralBits32.make(Constant.INIT_CONTEXT_FLAG_INTERCEPTED));

            invokeClinitCodeVisitor.visitConstInsn(
                    Dops.IPUT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vInterceptFlagReg),
                            DexRegister.makeLocalReg(vInterceptResultReg)),
                    factory.interceptResultClass.flagsField);

            invokeClinitCodeVisitor.visitTargetInsn(
                    Dops.GOTO,
                    DexRegisterList.empty(),
                    returnLabel);
        }


        invokeClinitCodeVisitor.fillRegisterCount();

        invokeClinitCodeVisitor.visitEnd();
    }

    private void generateInitMethod(TitanDexItemFactory factory, DexClassNode classClinitInterceptor, DexClassVisitor classClinitInterceptorVisitor) {
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
    }

    /**
     * 调用对应chg类中的$staticInit方法
     *
     * @param classPools
     * @param factory
     * @return
     */
    private void generateInvokePostClinitMethod(LightPatchClassPools classPools,
                                                  TitanDexItemFactory factory,
                                                  DexClassNode classClinitInterceptor,
                                                  DexClassVisitor classClinitInterceptorVisitor) {

        DexMethodVisitorInfo inovkePostClinitMethodVisitorInfo = new DexMethodVisitorInfo(
                classClinitInterceptor.type,
                factory.classClinitInterceptableClass.invokePostClinitMethod.getName(),
                factory.classClinitInterceptableClass.invokePostClinitMethod.getParameterTypes(),
                factory.classClinitInterceptableClass.invokePostClinitMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor invokePostClinitMethodVisitor = classClinitInterceptorVisitor.visitMethod(
                inovkePostClinitMethodVisitorInfo);

        invokePostClinitMethodVisitor.visitBegin();

        DexCodeRegisterCalculator invokePostClinitCodeVisitor = new DexCodeRegisterCalculator(
                false,
                inovkePostClinitMethodVisitorInfo.parameters,
                new DexCodeFormatVerifier(invokePostClinitMethodVisitor.visitCode()));

        invokePostClinitCodeVisitor.visitBegin();

        int pHashCodeReg = 1;

        int pTypeDescReg = 2;

        int vInterceptResultReg = 0;

        int vEqualsResultReg = 1;

        int vTypeConstReg = 2;

        int vParaSizeReg = 3;

        int vParaArrayReg = 4;

        DexLabel returnLabel = new DexLabel();

        DexLabel interceptLabel = new DexLabel();

        List<LazyLoadInterceptorHashCode> classHashCodes =
                reduceAndOrderInterceptorHashCodeForPostInit(classPools, factory);

        if (classHashCodes.size() > 0) {
            int[] keys = classHashCodes.stream()
                    .mapToInt(h -> h.typeHashCode)
                    .toArray();

            DexLabel[] targets = new DexLabel[keys.length];
            for (int i = 0; i < targets.length; i++) {
                targets[i] = new DexLabel();
            }

            invokePostClinitCodeVisitor.visitSwitch(
                    Dops.SPARSE_SWITCH,
                    DexRegisterList.make(DexRegister.makeParameterReg(pHashCodeReg)),
                    keys,
                    targets);

            // return null
            invokePostClinitCodeVisitor.visitLabel(interceptLabel);
            invokePostClinitCodeVisitor.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.LiteralBits32.make(0));

            invokePostClinitCodeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));

            for (int i = 0; i < classHashCodes.size(); i++) {
                LazyLoadInterceptorHashCode hashCode = classHashCodes.get(i);
                invokePostClinitCodeVisitor.visitLabel(targets[i]);

                DexLabel nextCompareLabel = new DexLabel();

                for (DexType type : hashCode.types) {

                    invokePostClinitCodeVisitor.visitLabel(nextCompareLabel);

                    nextCompareLabel = new DexLabel();

                    invokePostClinitCodeVisitor.visitConstInsn(
                            Dops.CONST_STRING,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTypeConstReg)),
                            DexConst.ConstString.make(type.toTypeDescriptor()));

                    invokePostClinitCodeVisitor.visitConstInsn(
                            Dops.INVOKE_VIRTUAL,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTypeConstReg),
                                    DexRegister.makeParameterReg(pTypeDescReg)),
                            factory.stringClass.equalsMethod);

                    invokePostClinitCodeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vEqualsResultReg)));

                    invokePostClinitCodeVisitor.visitTargetInsn(
                            Dops.IF_EQZ,
                            DexRegisterList.make(DexRegister.makeLocalReg(vEqualsResultReg)),
                            nextCompareLabel);

                    DexType changeType = PatchUtils.getChangeType(type, factory);

                    DexConst.ConstMethodRef staticInitMethodRef = mDexItemFactory.changedClass
                            .staticInitMethodForType(changeType);

                    invokePostClinitCodeVisitor.visitConstInsn(
                            Dops.CONST,
                            DexRegisterList.make(DexRegister.makeLocalReg(vParaSizeReg)),
                            DexConst.LiteralBits32.make(0));

                    invokePostClinitCodeVisitor.visitConstInsn(
                            Dops.NEW_ARRAY,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vParaArrayReg),
                                    DexRegister.makeLocalReg(vParaSizeReg)),
                            DexConst.ConstType.make(
                                    factory.createArrayType(factory.objectClass.type)));

                    invokePostClinitCodeVisitor.visitConstInsn(
                            Dops.INVOKE_STATIC,
                            DexRegisterList.make(DexRegister.makeLocalReg(vParaArrayReg)),
                            staticInitMethodRef);

                    invokePostClinitCodeVisitor.visitTargetInsn(
                            Dops.GOTO,
                            DexRegisterList.empty(),
                            interceptLabel);
                }

                // for the last compare
                invokePostClinitCodeVisitor.visitLabel(nextCompareLabel);
                // 没有match equals compare:

                invokePostClinitCodeVisitor.visitTargetInsn(
                        Dops.GOTO,
                        DexRegisterList.empty(),
                        interceptLabel);
            }
        } else {
            // InterceptResult interceptResult = new InterceptResult();
            // interceptResult.interceptor = interceptor;
            invokePostClinitCodeVisitor.visitLabel(interceptLabel);
            invokePostClinitCodeVisitor.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.LiteralBits32.make(0));

            invokePostClinitCodeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));
        }



        invokePostClinitCodeVisitor.fillRegisterCount();

        invokePostClinitCodeVisitor.visitEnd();

    }

}
