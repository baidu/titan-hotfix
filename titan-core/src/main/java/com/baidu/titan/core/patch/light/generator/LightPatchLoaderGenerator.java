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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.light.LightPatchClassPools;
import com.baidu.titan.core.patch.light.generator.changed.InterceptorProcessor;
import com.baidu.titan.core.patch.light.generator.changed.LightChangedClassGenerator;
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
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 *
 * 用于生成PatchLoader
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/9/28
 */
public class LightPatchLoaderGenerator {

    private final LightPatchClassPools mClassPools;

    private final TitanDexItemFactory mDexItemFactory;

    private DexClassNode mPatchLoaderClassNode;

    private LightPatchLoaderGenerator(LightPatchClassPools classPools,
                                      TitanDexItemFactory dexItemFactory) {
        this.mClassPools = classPools;
        this.mDexItemFactory = dexItemFactory;
    }

    public static void generate(LightPatchClassPools classPools,
                                TitanDexItemFactory dexItemFactory) {
        LightPatchLoaderGenerator generator =
                new LightPatchLoaderGenerator(
                        classPools,
                        dexItemFactory);
        generator.doGenerate();
    }

    private void doGenerate() {

        DexClassNode patchLoaderClass = new DexClassNode(
                mDexItemFactory.patchLoaderClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                mDexItemFactory.patchBaseLoaderClass.type,
                DexTypeList.empty());

        this.mPatchLoaderClassNode = patchLoaderClass;

        mClassPools.patchLoaderClassPool.addProgramClass(patchLoaderClass);
        DexClassVisitor loaderClassVisitor = patchLoaderClass.asVisitor();
        loaderClassVisitor.visitBegin();

        generateLoaderInitMethod(patchLoaderClass, loaderClassVisitor, mDexItemFactory);

        generateApplyMethod(loaderClassVisitor);

        generateApplyInTimeMethod(loaderClassVisitor);

        loaderClassVisitor.visitEnd();

    }

    /**
     * 生成apply()方法
     *
     * @param loaderClassVisitor patchloader类的class visitor
     */
    private void generateApplyMethod(DexClassVisitor loaderClassVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                mDexItemFactory.patchLoaderClass.type,
                mDexItemFactory.patchLoaderClass.applyMethod.getName(),
                mDexItemFactory.patchLoaderClass.applyMethod.getParameterTypes(),
                mDexItemFactory.patchLoaderClass.applyMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor applyMethodVisitor = loaderClassVisitor.visitMethod(methodVisitorInfo);

        applyMethodVisitor.visitBegin();

        generateApplyCode(applyMethodVisitor);

        applyMethodVisitor.visitEnd();
    }


    /**
     * 生成apply方法中的指令
     *
     * @param applyMethodVisitor method visitor
     */
    private void generateApplyCode(DexMethodVisitor applyMethodVisitor) {
        DexCodeRegisterCalculator applyCodeVisitor = new DexCodeRegisterCalculator(false,
                mDexItemFactory.patchLoaderClass.applyMethod.getParameterTypes(),
                new DexCodeFormatVerifier(applyMethodVisitor.visitCode()));

        applyCodeVisitor.visitBegin();

        generateInitInterceptorCode(applyCodeVisitor, true);

        generateLazyLoadApplyCode(applyCodeVisitor);

        // 调用类的$staticInit方法，需要在lazy load之后，防止这些类的$staticInit方法中调用到lazy load类时出现问题
        generateInvokeClinitCode(applyCodeVisitor);

        applyCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        applyCodeVisitor.fillRegisterCount();
        applyCodeVisitor.visitEnd();

    }

    /**
     * 生成applyInTime()方法
     *
     * @param loaderClassVisitor patchloader类的class visitor
     */
    private void generateApplyInTimeMethod(DexClassVisitor loaderClassVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                mDexItemFactory.patchLoaderClass.type,
                mDexItemFactory.patchLoaderClass.applyInTimeMethod.getName(),
                mDexItemFactory.patchLoaderClass.applyInTimeMethod.getParameterTypes(),
                mDexItemFactory.patchLoaderClass.applyInTimeMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor applyInTimeMethodVisitor = loaderClassVisitor.visitMethod(methodVisitorInfo);

        applyInTimeMethodVisitor.visitBegin();

        generateApplyInTimeCode(applyInTimeMethodVisitor);

        applyInTimeMethodVisitor.visitEnd();
    }

    /**
     * 生成apply方法中的指令
     *
     * @param applyInTimeMethodVisitor method visitor
     */
    private void generateApplyInTimeCode(DexMethodVisitor applyInTimeMethodVisitor) {
        DexCodeRegisterCalculator applyInTimeCodeVisitor = new DexCodeRegisterCalculator(false,
                mDexItemFactory.patchLoaderClass.applyInTimeMethod.getParameterTypes(),
                new DexCodeFormatVerifier(applyInTimeMethodVisitor.visitCode()));

        applyInTimeCodeVisitor.visitBegin();

        generateInitInterceptorCode(applyInTimeCodeVisitor, false);

        // applyInTime中不对ClassClinitInterceptorStorage.$ic赋值，即不进行懒加载
//        generateLazyLoadApplyCode(applyInTimeCodeVisitor);

        // applyInTime中调用staticInit，避免出现错误
        // 调用类的$staticInit方法，需要在lazy load之后，防止这些类的$staticInit方法中调用到lazy load类时出现问题
//        generateInvokeClinitCode(applyInTimeCodeVisitor);

        applyInTimeCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        applyInTimeCodeVisitor.fillRegisterCount();
        applyInTimeCodeVisitor.visitEnd();

    }

    /**
     * 对需要lazy load的类，生成lazy load指令。原来有<clinit>方法的类被认为可进行lazy load,在类初始化调用<clinit>时，
     * 从ClassClinitInterceptorStorage.$ic中初始化对应类的$iter实例并保存到对应类的$ic字段中
     * 该方法加入的指令伪代码如下：
     * new-instance v0, Lcom/baidu/titan/patch/ClassClinitInterceptor;
     * invoke-direct v0, Lcom/baidu/titan/patch/ClassClinitInterceptor;-><init>
     * sput-object v0, ClassClinitInterceptorStorage -> $ic
     *
     * @param applyCodeVisitor
     */
    private void generateLazyLoadApplyCode(DexCodeVisitor applyCodeVisitor) {
        int vClassClinitIter = 0;
        // new-instance v0, Lcom/baidu/titan/patch/ClassClinitInterceptor;
        applyCodeVisitor.visitConstInsn(Dops.NEW_INSTANCE,
                DexRegisterList.make(DexRegister.makeLocalReg(vClassClinitIter)),
                DexConst.ConstType.make(mDexItemFactory.classInitInterceptorClass.type));

        // invoke-direct Lcom/baidu/titan/patch/ClassClinitInterceptor;-><init>
        applyCodeVisitor.visitConstInsn(Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vClassClinitIter)),
                DexConst.ConstMethodRef.make(
                        mDexItemFactory.classInitInterceptorClass.type,
                        mDexItemFactory.methods.initMethodName,
                        mDexItemFactory.voidClass.primitiveType,
                        DexTypeList.empty()));

        // sput-object v0, ClassClinitInterceptorStorage -> $ic
        applyCodeVisitor.visitConstInsn(
                Dops.SPUT_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vClassClinitIter)),
                mDexItemFactory.classClinitInterceptorStorageClass.interceptorField);
    }

    /**
     * 生成初始化interceptor代码，对于原来没有<clinit>方法的类，无法通过lazy load加载patch，
     * 需要在patch loader apply中直接创建对应的$iter类实例并保存到对应类的$ic字段中
     * 指令伪代码如下：
     * new-instance v0, $iter
     * invoke-direct v0, $iter-><init>()V
     * sput-object v0, orgclass->$ic;
     *
     * @param applyCodeVisitor code visitor
     * @param checkLazy 是否需要检查懒加载，false不检查，所有interceptor在这里初始化；
     *                  true为检查，可以lazy init的interceptor在对应类初始化方法中初始化
     */
    private void generateInitInterceptorCode(DexCodeRegisterCalculator applyCodeVisitor, boolean checkLazy) {
        final int vInterceptorReg = 0;
        mClassPools.interceptorClassPool.getProgramClassPool().stream()
                .filter(dcn -> !checkLazy || !LightClassClinitInterceptorGenerator.isInterceptorLazyInitAble(dcn))
                .forEach(dcn -> {

                    // new interceptor
                    applyCodeVisitor.visitConstInsn(
                            Dops.NEW_INSTANCE,
                            DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                            DexConst.ConstType.make(dcn.type));
                    applyCodeVisitor.visitConstInsn(
                            Dops.INVOKE_DIRECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                            DexConst.ConstMethodRef.make(
                                    dcn.type,
                                    mDexItemFactory.methods.initMethodName,
                                    mDexItemFactory.voidClass.primitiveType,
                                    DexTypeList.empty()));
                    applyCodeVisitor.visitConstInsn(
                            Dops.SPUT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vInterceptorReg)),
                            DexConst.ConstFieldRef.make(
                                    PatchUtils.getOrgTypeFromInterceptorType(dcn.type, mDexItemFactory),
                                    mDexItemFactory.interceptableClass.type,
                                    mDexItemFactory.instrumentedClass.interceptorFieldName));

                });
    }

    /**
     * 类新增了<clinit>方法，需要在patchloader中调用
     * 指令伪代码如下：
     * const v1, 0
     * new-array v0, v1, Ljava.lang.Object;
     * invoke-static v0, $chg->$staticInit([Ljava.lang.Object;)V
     *
     * @param applyCodeVisitor code visitor
     */
    private void generateInvokeClinitCode(DexCodeRegisterCalculator applyCodeVisitor) {
        mClassPools.oldOrgClassPool.getProgramClassPool().stream()
                .filter(LightClassClinitInterceptorGenerator::shouldCallStaticInit)
                .forEach(dcn -> {
                    int vParaArrayReg = 0;
                    int vParaSizeReg = 1;
                    DexType changeType = PatchUtils.getChangeType(dcn.type, mDexItemFactory);

                    DexConst.ConstMethodRef staticInitMethodRef = mDexItemFactory.changedClass
                            .staticInitMethodForType(changeType);

                    applyCodeVisitor.visitConstInsn(
                            Dops.CONST,
                            DexRegisterList.make(DexRegister.makeLocalReg(vParaSizeReg)),
                            DexConst.LiteralBits32.make(0));

                    applyCodeVisitor.visitConstInsn(
                            Dops.NEW_ARRAY,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vParaArrayReg),
                                    DexRegister.makeLocalReg(vParaSizeReg)),
                            DexConst.ConstType.make(
                                    mDexItemFactory.createArrayType(mDexItemFactory.objectClass.type)));

                    applyCodeVisitor.visitConstInsn(
                            Dops.INVOKE_STATIC,
                            DexRegisterList.make(DexRegister.makeLocalReg(vParaArrayReg)),
                            staticInitMethodRef);
                });
    }


    /**
     * 生成patchloader类的<init>方法
     *
     * @param patchLoaderClass patchloader类的classnode
     * @param loaderClassVisitor patchloader class visitor
     * @param factory dex item factory
     */
    private void generateLoaderInitMethod(DexClassNode patchLoaderClass,
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

}
