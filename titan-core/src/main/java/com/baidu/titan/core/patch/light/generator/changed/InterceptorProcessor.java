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

package com.baidu.titan.core.patch.light.generator.changed;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.light.generator.LightClassClinitInterceptorGenerator;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;
import com.baidu.titan.sdk.common.TitanConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * 存储管理所有需要拦截的方法，负责分类、排序，并生成对应的拦截器类
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/12/12
 */
public class InterceptorProcessor {

    private boolean mFinished = false;

    private TitanDexItemFactory mDexItemFactory;

    /** $itor */
    private DexClassNode mInterceptorClassNode;

    private boolean mStaticInitChanged = false;

    /**
     * normal method: 除了<init>和<clinit>之外的方法
     */
    public Map<InterceptInvokeKind, List<DispatchMethodInfo>> normalMethods = new HashMap<>();
    public List<DispatchMethodInfo> instanceInitMethods = new ArrayList<>();

    private LightChangedClassGenerator mHost;


    public InterceptorProcessor(TitanDexItemFactory dexItemFactory,
                                LightChangedClassGenerator host) {
        this.mDexItemFactory = dexItemFactory;
        this.mHost = host;
    }

    /**
     * 标识一个被拦截的方法，从属于一个#InterceptInvokeKind
     */
    private class DispatchMethodInfo implements Comparable {

        public DispatchMethodInfo(boolean specialMethod,
                                  DexNamedProtoNode method,
                                  int methodId,
                                  boolean staticMethod) {
            this.specialMethod = specialMethod;
            this.method = method;
            this.methodId = methodId;
            this.staticMethod = staticMethod;
        }

        public final boolean specialMethod;

        public final DexNamedProtoNode method;

        public final int methodId;

        public final boolean staticMethod;

        /**
         * 根据method id 进行排序，因为sparse-switch要求switch-key必须是递增有序的
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Object o) {
            return this.methodId - ((DispatchMethodInfo)o).methodId;
        }

    }

    /**
     * 标志一个Interceptable中的一个方法，包含一到多个#DispatchMethodInfo
     */
    private class InterceptInvokeKind {

        public final boolean specialMethod;

        public final String desc;

        public final DexTypeList invokeParaTypes;

        public final String invokeMethodName;

        public InterceptInvokeKind(boolean specialMethod,
                                   String desc,
                                   DexTypeList invokeParaTypes,
                                   String invokeMethodName) {
            this.specialMethod = specialMethod;
            this.desc = desc;
            this.invokeParaTypes = invokeParaTypes;
            this.invokeMethodName = invokeMethodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InterceptInvokeKind that = (InterceptInvokeKind) o;
            return specialMethod == that.specialMethod &&
                    Objects.equals(desc, that.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(specialMethod, desc);
        }

    }

    /**
     * 添加一个普通方法
     *
     * @param methodNode
     */
    public void addNormalMethod(DexMethodNode methodNode) {
        if (mFinished) {
            throw new IllegalStateException("finished already");
        }
        // invoke-xxx 方法参数列表
        DexTypeList.Builder paraTypesBuilder = DexTypeList.newBuilder();
        // method id
        paraTypesBuilder.addType(mDexItemFactory.integerClass.primitiveType);
        // thisObj
        paraTypesBuilder.addType(mDexItemFactory.objectClass.type);

        // 首先先根据方法参数列表，判断属于哪一种InvokeKind
        StringBuilder descBuilder = new StringBuilder();
        if (methodNode.parameters.count() == 0) {
            descBuilder.append(DexItemFactory.VoidClass.SHORT_DESCRIPTOR);
        } else {
            methodNode.parameters.forEach(type -> {
                char shortType = type.toShortDescriptor();
                switch (shortType) {
                    case DexItemFactory.ArrayType.SHORT_DESCRIPTOR:
                    case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR: {
                        shortType = DexItemFactory.ReferenceType.SHORT_DESCRIPTOR;
                    }
                }
                descBuilder.append(shortType);
            });
        }

        String desc = descBuilder.toString();

        InterceptInvokeKind interceptInvokeKind;
        Set<String> specialParas = mHost.patchArgument.getSpecialInterceptParas();
        boolean specialMethod = specialParas.contains(desc);
        // invokeXX
        if (specialMethod) {
            methodNode.parameters.forEach(type -> {
                // 对引用类型，包括数组类型，会转换成Object类型
                if (type.isReferenceType() || type.isArrayType()) {
                    paraTypesBuilder.addType(mDexItemFactory.objectClass.type);
                } else {
                    paraTypesBuilder.addType(type);
                }
            });
            interceptInvokeKind = new InterceptInvokeKind(true, desc, paraTypesBuilder.build(),
                    "invoke" + desc);
        } else {
            paraTypesBuilder.addType(
                    mDexItemFactory.createArrayType(mDexItemFactory.objectClass.type));
            interceptInvokeKind = new InterceptInvokeKind(false, "Common", paraTypesBuilder.build(),
                    "invokeCommon");
        }

        int methodId = MethodIdAssigner.getMethodId(methodNode);

        // 相关信息保存在DispatchMethodInfo中，并放入列表中，后继调用finish方法后统一处理
        DispatchMethodInfo dispatchMethodInfo = new DispatchMethodInfo(
                specialMethod,
                new DexNamedProtoNode(methodNode),
                methodId,
                methodNode.isStatic());

        List<DispatchMethodInfo> dispatchMethodInfos =
                normalMethods.computeIfAbsent(interceptInvokeKind, k -> new ArrayList<>());

        dispatchMethodInfos.add(dispatchMethodInfo);
    }

    public void addStaticInitMethod(DexMethodNode methodNode) {
        mStaticInitChanged = true;
    }

    public void addInstanceInitMethod(DexMethodNode methodNode) {
        int methodId = MethodIdAssigner.getMethodId(methodNode);
        DispatchMethodInfo dispatchMethodInfo = new DispatchMethodInfo(
                false,
                new DexNamedProtoNode(methodNode),
                methodId,
                methodNode.isStatic());
        instanceInitMethods.add(dispatchMethodInfo);
    }

    /**
     * 是否生成拦截器
     *
     * @return
     */
    public boolean hasInterceptor() {
        if (!mFinished) {
            throw new IllegalStateException("not finished yet");
        }
        return !normalMethods.isEmpty() || !instanceInitMethods.isEmpty();
    }

    private void finishNormalMethods(DexClassVisitor interceptorClassVisitor) {
        if (normalMethods.isEmpty()) {
            return;
        }
        // 因为sparse-switch opcode的key[]操作数需要递增排序，
        // 对每一种Intercept Kind，根据methodId进行排序
        normalMethods.forEach((interceptInvokeKind, methods) -> {
            Collections.sort(methods);
        });

        this.normalMethods.forEach((interceptInvokeKind, methods) -> {
            if (interceptInvokeKind.specialMethod) {
                generateSpecialMethodInterceptor(
                        interceptInvokeKind,
                        methods,
                        mInterceptorClassNode,
                        interceptorClassVisitor);
            } else {
                generateCommonMethodInterceptor(
                        interceptInvokeKind,
                        methods,
                        mInterceptorClassNode,
                        interceptorClassVisitor);
            }
        });
    }

    private void finishStaticInitMethod(DexClassVisitor interceptorClassVisitor) {

    }

    private void finishInstanceInitMethods(DexClassVisitor interceptorClassVisitor) {
        if (instanceInitMethods.isEmpty()) {
            return;
        }

        Collections.sort(instanceInitMethods);
        generateUninitMethodInterceptor(instanceInitMethods, mInterceptorClassNode, interceptorClassVisitor);
        generateInitBodyMethodInterceptor(instanceInitMethods, mInterceptorClassNode, interceptorClassVisitor);

    }



    /**
     * 生成最终的Interceptor
     */
    public void finish() {
        this.mFinished = true;
        if (!hasInterceptor()) {
            checkLazyInitableIfNoItor();
            return;
        }

        mInterceptorClassNode = new DexClassNode(
                mDexItemFactory.interceptorClass.getInterceptorType(mHost.mNewOrgClassNode.type),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                mDexItemFactory.simpleInterceptorClass.type,
                DexTypeList.empty());
        mHost.mClassPools.interceptorClassPool.addProgramClass(mInterceptorClassNode);

        DexClassVisitor interceptorClassVisitor = mInterceptorClassNode.asVisitor();
        interceptorClassVisitor.visitBegin();
        // normal methods
        finishNormalMethods(interceptorClassVisitor);
        // <clint> methods
        finishStaticInitMethod(interceptorClassVisitor);
        // instance methods
        finishInstanceInitMethods(interceptorClassVisitor);
        // generate $iter.<init> method
        generateInitMethod(mInterceptorClassNode, interceptorClassVisitor);

        interceptorClassVisitor.visitEnd();

        checkLazyInitable();
    }

    /**
     * 生成<init>方法
     *
     * @param interceptorClassNode 要生成<init>方法的interceptor class node
     * @param interceptorClassVisitor interceptor class visitor
     */
    private void generateInitMethod( DexClassNode interceptorClassNode,
                                     DexClassVisitor interceptorClassVisitor) {
        // <init> method

        DexMethodVisitorInfo initMethodVisitorInfo = new DexMethodVisitorInfo(
                interceptorClassNode.type,
                mDexItemFactory.methods.initMethodName,
                DexTypeList.empty(),
                mDexItemFactory.voidClass.primitiveType,
                new DexAccessFlags(
                        DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_CONSTRUCTOR));


        DexMethodVisitor initMethodVisitor = interceptorClassVisitor.visitMethod(
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
                        mDexItemFactory.simpleInterceptorClass.type,
                        mDexItemFactory.createString("<init>"),
                        mDexItemFactory.voidClass.primitiveType,
                        DexTypeList.empty()));

        initCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        initCodeVisitor.fillRegisterCount();

        initCodeVisitor.visitEnd();

        initMethodVisitor.visitEnd();
    }

    /**
     * 有interceptor情况下检查是否可以懒加载
     */
    private void checkLazyInitable() {
        boolean clinitAdded = mHost.mOldOrgClassNode.getExtraInfo(LightChangedClassGenerator.EXTRA_CLINIT_ADDED,
                false);

        mHost.mOldOrgClassNode.getMethods().stream()
                .filter(DexMethodNode::isStaticInitMethod)
                .findAny()
                .map(dmn -> {
                    LightClassClinitInterceptorGenerator.setInterceptorLazyInitAble(mInterceptorClassNode, true);
                    LightClassClinitInterceptorGenerator.setInterceptorLazyInitAble(mHost.mOldOrgClassNode, true);
                    if (mStaticInitChanged) {
                        LightClassClinitInterceptorGenerator.setClinitChanged(mHost.mOldOrgClassNode);
                    }
                    // 由于old node中存在static init方法，该类可以进行懒加载，将old node添加到lazy init class pool中
                    mHost.mClassPools.lazyInitClassPool.addProgramClass(mHost.mOldOrgClassNode);
                    return dmn;
                }).orElseGet(() -> {
            LightClassClinitInterceptorGenerator.setInterceptorLazyInitAble(mInterceptorClassNode, false);
            LightClassClinitInterceptorGenerator.setInterceptorLazyInitAble(mHost.mOldOrgClassNode, false);
            return null;
        });

        // 新增clinit方法的请况，标记需要在patchLoader中调用$chg.staticInit
        LightClassClinitInterceptorGenerator.setCallStaticInit(mHost.mOldOrgClassNode, clinitAdded);
    }

    /**
     * 在无interceptor情况下检查是否可以懒加载
     */
    private void checkLazyInitableIfNoItor() {
        boolean clinitAdded = mHost.mOldOrgClassNode.getExtraInfo(LightChangedClassGenerator.EXTRA_CLINIT_ADDED,
                false);
        // 只有<clinit>方法发生了变化，此时不会生成$iter类
        // 由于interceptor node未创建，此处应该将old node标记为不可对$itor进行懒加载
        LightClassClinitInterceptorGenerator.setInterceptorLazyInitAble(mHost.mOldOrgClassNode, false);
        if (mStaticInitChanged) {
            LightClassClinitInterceptorGenerator.setClinitChanged(mHost.mOldOrgClassNode);
            // 由于old node中存在static init方法，且新类中对clinit进行了修改，需要将其加到lazy init class pool中
            // 这样可以在类初始化时，调用$chg.staticInit
            mHost.mClassPools.lazyInitClassPool.addProgramClass(mHost.mOldOrgClassNode);
        }

        // 新增clinit方法的请况，标记需要在patchLoader中调用$chg.staticInit
        LightClassClinitInterceptorGenerator.setCallStaticInit(mHost.mOldOrgClassNode, clinitAdded);
    }

    /**
     *
     * invoke-special(int methodid, Object thisObj, ...)
     *
     * @param invokeKind
     * @param methods
     * @param interceptorClassNode
     * @param interceptorClassVisitor
     */
    private void generateSpecialMethodInterceptor(InterceptInvokeKind invokeKind,
                                                  List<DispatchMethodInfo> methods,
                                                  DexClassNode interceptorClassNode,
                                                  DexClassVisitor interceptorClassVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorClassNode.type,
                mDexItemFactory.createString(invokeKind.invokeMethodName),
                invokeKind.invokeParaTypes,
                mDexItemFactory.interceptResultClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor methodVisitor = interceptorClassVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        final int pIntercptorThisReg = 0;
        final int pMethodIdReg = 1;
        final int pThisObjReg = 2;
        final int pFirstParaReg = 3;

        final int vInterceptResultReg = 0;
        final int vReturnValueMayWideReg = 1;

        // 通过sparse-switch key为method id进行方法派发到$chg类的对应方法
        DexLabel[] labels = methods.stream()
                .map(m -> new DexLabel())
                .toArray(size -> new DexLabel[size]);

        int[] keys = methods.stream()
                .mapToInt(m -> m.methodId)
                .toArray();

        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodIdReg)),
                keys,
                labels);

        // 当sparse switch没有匹配的方法时，运行到这里，return null
        codeVisitor.visitConstInsn(Dops.CONST,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                DexConst.LiteralBits32.make(0));
        codeVisitor.visitSimpleInsn(Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));

        // intercept dispatch
        for (int i = 0; i < methods.size(); i++) {
            DispatchMethodInfo method = methods.get(i);

            codeVisitor.visitLabel(labels[i]);

            DexRegisterList.Builder callChangedRegsBuilder = DexRegisterList.newBuilder();
            DexTypeList.Builder callChangedParaBuilder = DexTypeList.newBuilder();

            // 判断是否是static方法
            if (!method.staticMethod) {
                callChangedParaBuilder.addType(mHost.mNewOrgClassNode.type);
                callChangedRegsBuilder.addReg(DexRegister.makeParameterReg(pThisObjReg));

                codeVisitor.visitConstInsn(
                        Dops.CHECK_CAST,
                        DexRegisterList.make(DexRegister.makeParameterReg(pThisObjReg)),
                        DexConst.ConstType.make(mHost.mNewOrgClassNode.type));
            }

            int pNextCallChangedParaReg = pFirstParaReg;
            for (int j = 0; j < method.method.parameters.count(); j++) {
                DexType type = method.method.parameters.getType(j);
                callChangedParaBuilder.addType(type);
                if (type.isReferenceType() || type.isArrayType()) {
                    codeVisitor.visitConstInsn(
                            Dops.CHECK_CAST,
                            DexRegisterList.make(DexRegister.makeParameterReg(pNextCallChangedParaReg)),
                            DexConst.ConstType.make(type));
                }
                callChangedRegsBuilder.addReg(
                        DexRegister.makeParameterRegWithWide(pNextCallChangedParaReg, type.isWideType()));
                pNextCallChangedParaReg += (type.isWideType() ? 2 : 1);
            }
            codeVisitor.visitConstInsn(
                    Dops.INVOKE_STATIC_RANGE,
                    callChangedRegsBuilder.build(),
                    DexConst.ConstMethodRef.make(
                            mHost.mChangedClassNode.type,
                            method.method.name,
                            method.method.returnType,
                            callChangedParaBuilder.build()));

            DexType returnType = method.method.returnType;
            if (!returnType.isVoidType()) {
                if (returnType.isArrayType() ||
                        method.method.returnType.isReferenceType()) {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vReturnValueMayWideReg)));
                } else if (returnType.isPrimitiveType()) {
                    if (returnType.isWideType()) {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(
                                        DexRegister.makeDoubleLocalReg(vReturnValueMayWideReg)));
                    } else {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vReturnValueMayWideReg)));
                    }
                }
            }

            // 暂时先用new InterceptResult的形式，后继考虑使用obtain和recycle机制
            codeVisitor.visitConstInsn(
                    Dops.NEW_INSTANCE,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.ConstType.make(mDexItemFactory.interceptResultClass.type));
            codeVisitor.visitConstInsn(
                    Dops.INVOKE_DIRECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    mDexItemFactory.interceptResultClass.initMethod);

            if (!returnType.isVoidType()) {
                // 非Void返回值，需要将结果保存到InterceptResult对应的类型字段上
                codeVisitor.visitConstInsn(
                        mDexItemFactory.dops.getFieldPutOpForType(returnType, false).opcode,
                        DexRegisterList.make(
                                DexRegister.makeLocalRegWithWide(vReturnValueMayWideReg, returnType.isWideType()),
                                DexRegister.makeLocalReg(vInterceptResultReg)),
                        mDexItemFactory.interceptResultClass.getValueFieldForType(returnType));
            }

            codeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }

    /**
     *
     * invoke-common(int methodid, Object thisObj, Object[] args)
     *
     * @param interceptInvokeKind
     * @param methods
     * @param interceptorClassNode
     * @param interceptorClassVisitor
     */
    private void generateCommonMethodInterceptor(InterceptInvokeKind interceptInvokeKind,
                                                 List<DispatchMethodInfo> methods,
                                                 DexClassNode interceptorClassNode,
                                                 DexClassVisitor interceptorClassVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorClassNode.type,
                mDexItemFactory.createString(interceptInvokeKind.invokeMethodName),
                interceptInvokeKind.invokeParaTypes,
                mDexItemFactory.interceptResultClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor methodVisitor = interceptorClassVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        final int pIntercptorThisReg = 0;
        final int pMethodIdReg = 1;
        final int pThisObjReg = 2;
        final int pArgArrayReg = 3;

        final int vInterceptResultReg = 0;
        final int vReturnValueMayWideReg = 1;
        // 预留v2寄存器，如果return value是wide类型
        final int vParaArrayIdxReg = 3;

        final int vFirstCallChangedParaReg = 4;
        // 通过sparse-switch key为method id进行方法派发到$chg类的对应方法
        DexLabel[] labels = methods.stream()
                .map(m -> new DexLabel())
                .toArray(size -> new DexLabel[size]);

        int[] keys = methods.stream()
                .mapToInt(m -> m.methodId)
                .toArray();

        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodIdReg)),
                keys,
                labels);

        // 当sparse switch没有匹配的方法时，运行到这里，return null
        codeVisitor.visitConstInsn(Dops.CONST,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                DexConst.LiteralBits32.make(0));

        codeVisitor.visitSimpleInsn(Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));

        // intercept dispatch
        for (int i = 0; i < methods.size(); i++) {
            DispatchMethodInfo method = methods.get(i);

            codeVisitor.visitLabel(labels[i]);

            DexRegisterList.Builder callChangedRegsBuilder = DexRegisterList.newBuilder();
            DexTypeList.Builder callChangedParaBuilder = DexTypeList.newBuilder();

            int vNextCallChangedParaReg = vFirstCallChangedParaReg;

            // 判断是否是static方法
            if (!method.staticMethod) {
                callChangedParaBuilder.addType(mHost.mNewOrgClassNode.type);

                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallChangedParaReg),
                                DexRegister.makeParameterReg(pThisObjReg)));

                callChangedRegsBuilder.addReg(DexRegister.makeLocalReg(vNextCallChangedParaReg));
                codeVisitor.visitConstInsn(
                        Dops.CHECK_CAST,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallChangedParaReg)),
                        DexConst.ConstType.make(mHost.mNewOrgClassNode.type));

                vNextCallChangedParaReg++;
            }

            for (int j = 0; j < method.method.parameters.count(); j++) {
                DexType type = method.method.parameters.getType(j);
                codeVisitor.visitConstInsn(
                        Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vParaArrayIdxReg)),
                        DexConst.LiteralBits32.make(j));

                // arrayop vAA, vBB, vCC
                // A: 值寄存器或寄存器对；可以是源寄存器，也可以是目标寄存器（8 位）
                // B: 数组寄存器（8 位）
                // C: 索引寄存器（8 位）
                codeVisitor.visitSimpleInsn(
//                        mDexItemFactory.dops.getArrayGetOpForType(type).opcode,
                        Dops.AGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallChangedParaReg),
                                DexRegister.makeParameterReg(pArgArrayReg),
                                DexRegister.makeLocalReg(vParaArrayIdxReg)));

                DexType checkCastType = null;
                if (type.isPrimitiveType()) {
                    checkCastType =
                            mDexItemFactory.boxTypes.getBoxedTypeForPrimitiveType(type);
                } else if (!type.equals(mDexItemFactory.objectClass.type)) {
                    checkCastType = type;
                }
                if (checkCastType != null) {
                    codeVisitor.visitConstInsn(
                            Dops.CHECK_CAST,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vNextCallChangedParaReg)),
                            DexConst.ConstType.make(checkCastType));
                }

                DexRegister dstReg = type.isWideType() ?
                        DexRegister.makeDoubleLocalReg(vNextCallChangedParaReg) :
                        DexRegister.makeLocalReg(vNextCallChangedParaReg);

                if (type.isPrimitiveType()) {
                    codeVisitor.visitConstInsn(
                            Dops.INVOKE_VIRTUAL,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vNextCallChangedParaReg)),
                            mDexItemFactory.methods.primitiveValueMethodForType(type));

                    if (type.isWideType()) {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(dstReg));
                        vNextCallChangedParaReg += 2;
                    } else {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(dstReg));
                        vNextCallChangedParaReg += 1;
                    }
                } else {
                    vNextCallChangedParaReg += 1;
                }
                callChangedRegsBuilder.addReg(dstReg);
                callChangedParaBuilder.addType(type);
            }

            codeVisitor.visitConstInsn(
                    Dops.INVOKE_STATIC_RANGE,
                    callChangedRegsBuilder.build(),
                    DexConst.ConstMethodRef.make(
                            mHost.mChangedClassNode.type,
                            method.method.name,
                            method.method.returnType,
                            callChangedParaBuilder.build()));

            DexType returnType = method.method.returnType;
            if (!returnType.isVoidType()) {
                if (returnType.isArrayType() ||
                        method.method.returnType.isReferenceType()) {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vReturnValueMayWideReg)));
                } else if (returnType.isPrimitiveType()) {
                    if (returnType.isWideType()) {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(
                                        DexRegister.makeDoubleLocalReg(vReturnValueMayWideReg)));
                    } else {
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vReturnValueMayWideReg)));
                    }
                }
            }

            // 暂时先用new InterceptResult的形式，后继考虑使用obtain和recycle机制
            codeVisitor.visitConstInsn(
                    Dops.NEW_INSTANCE,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    DexConst.ConstType.make(mDexItemFactory.interceptResultClass.type));
            codeVisitor.visitConstInsn(
                    Dops.INVOKE_DIRECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                    mDexItemFactory.interceptResultClass.initMethod);

            if (!returnType.isVoidType()) {
                // 非Void返回值，需要将结果保存到InterceptResult对应的类型字段上
                codeVisitor.visitConstInsn(
                        mDexItemFactory.dops.getFieldPutOpForType(returnType, false).opcode,
                        DexRegisterList.make(
                                DexRegister.makeLocalRegWithWide(vReturnValueMayWideReg, returnType.isWideType()),
                                DexRegister.makeLocalReg(vInterceptResultReg)),
                        mDexItemFactory.interceptResultClass.getValueFieldForType(returnType));
            }

            codeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

    }

    private void generateUninitMethodInterceptor(List<DispatchMethodInfo> methods,
                                                 DexClassNode interceptorClassNode,
                                                 DexClassVisitor interceptorClassVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorClassNode.type,
                mDexItemFactory.interceptableClass.invokeUnInitMethod.getName(),
                mDexItemFactory.interceptableClass.invokeUnInitMethod.getParameterTypes(),
                mDexItemFactory.interceptableClass.invokeUnInitMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));



        DexMethodVisitor methodVisitor = interceptorClassVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        final int pIntercptorThisReg = 0;
        final int pMethodIdReg = 1;
        final int pInitContextReg = 2;

        // 通过sparse-switch key为method id进行方法派发到$chg类的对应方法
        DexLabel[] labels = methods.stream()
                .map(m -> new DexLabel())
                .toArray(DexLabel[]::new);

        int[] keys = methods.stream()
                .mapToInt(m -> m.methodId)
                .toArray();

        // 根据methodId跳到指定的方法调用
        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodIdReg)),
                keys,
                labels);

        // 当sparse switch没有匹配的方法时，运行到这里，return void

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        // intercept dispatch
        for (int i = 0; i < methods.size(); i++) {
            DispatchMethodInfo method = methods.get(i);

            codeVisitor.visitLabel(labels[i]);

            // 获取this变量，该变量是org class实例
            final int vThisObjReg = 2;
            codeVisitor.visitConstInsn(Dops.IGET_OBJECT, DexRegisterList.make(
                    DexRegister.makeLocalReg(vThisObjReg),
                    DexRegister.makeParameterReg(pInitContextReg)),
                    mDexItemFactory.initContextClass.thisArgField);

            codeVisitor.visitConstInsn(Dops.CHECK_CAST,
                    DexRegisterList.make( DexRegister.makeLocalReg(vThisObjReg)),
                    DexConst.ConstType.make(mHost.mNewOrgClassNode.type));


            DexTypeList.Builder callChangedParaBuilder = DexTypeList.newBuilder();

            DexTypeList initMethodParaTypes = method.method.parameters;

            final int callInitRegCount = 1 + initMethodParaTypes.count();
            // 参数第一个为this实例
            callChangedParaBuilder.addType(mHost.mNewOrgClassNode.type);

            DexRegisterList.Builder callInitRegListBuilder = DexRegisterList.newBuilder();

            int nextCallInitParaReg = 5;

            boolean callInitRange = false;

            if (callInitRegCount <= 5) {
                callInitRegListBuilder.addReg(DexRegister.makeLocalReg(vThisObjReg));
            } else { // invoke-range, regs require continuous
                callInitRange = true;
                // this obj
                codeVisitor.visitSimpleInsn(Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(nextCallInitParaReg),
                                DexRegister.makeLocalReg(vThisObjReg)));
                callInitRegListBuilder.addReg(DexRegister.makeLocalReg(nextCallInitParaReg));
                nextCallInitParaReg++;
            }

            // 从InitContext.callArgs中取出参数，最好必要的拆箱(unbox)工作
            if (initMethodParaTypes.count() > 0) {
                final int vTmpInitArgReg = 3;
                codeVisitor.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vTmpInitArgReg),
                                DexRegister.makeParameterReg(pInitContextReg)),
                        mDexItemFactory.initContextClass.initArgsField);

                final int vCallArgIdxReg = 4;
                for (int initParaIdx = 0; initParaIdx < initMethodParaTypes.count(); initParaIdx++) {
                    DexType type = initMethodParaTypes.getType(initParaIdx);
                    codeVisitor.visitConstInsn(Dops.CONST_16,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vCallArgIdxReg)),
                            DexConst.LiteralBits32.make(initParaIdx));

                    codeVisitor.visitSimpleInsn(Dops.AGET_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(nextCallInitParaReg),
                                    DexRegister.makeLocalReg(vTmpInitArgReg),
                                    DexRegister.makeLocalReg(vCallArgIdxReg)));

                    // callArgs的类型是Object[]，通过aget-object之后的类型是Object，需要做一下check-cast
                    DexType checkCastType = type.isPrimitiveType()
                            ? mDexItemFactory.boxTypes.getBoxedTypeForPrimitiveType(type) : type;
                    codeVisitor.visitConstInsn(Dops.CHECK_CAST,
                            DexRegisterList.make(DexRegister.makeLocalReg(nextCallInitParaReg)),
                            DexConst.ConstType.make(checkCastType));

                    if (type.isPrimitiveType()) {
                        codeVisitor.visitConstInsn(Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(nextCallInitParaReg)),
                                mDexItemFactory.methods.primitiveValueMethodForType(type));

                        codeVisitor.visitSimpleInsn(mDexItemFactory.dops.getMoveResultOpForType(type).opcode,
                                DexRegisterList.make(
                                        DexRegister.makeLocalRegWithWide(
                                                nextCallInitParaReg, type.isWideType())));
                    }

                    callInitRegListBuilder.addReg(
                            DexRegister.makeLocalRegWithWide(nextCallInitParaReg, type.isWideType()));
                    callChangedParaBuilder.addType(type);
                    /** reg num is 0-based */
                    nextCallInitParaReg += (type.isWideType() ? 2 : 1);
                }


            }

            codeVisitor.visitSimpleInsn(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(nextCallInitParaReg),
                            DexRegister.makeParameterReg(pInitContextReg)));
            callInitRegListBuilder.addReg(DexRegister.makeLocalReg(nextCallInitParaReg));
            nextCallInitParaReg++;
            callChangedParaBuilder.addType(mDexItemFactory.initContextClass.type);

            // invoke_static/ invoke_static_range {vx .. vxx} L$chg->$instanceInitBody;
            codeVisitor.visitConstInsn(
                    callInitRange ? Dops.INVOKE_STATIC_RANGE : Dops.INVOKE_STATIC,
                    callInitRegListBuilder.build(),
                    DexConst.ConstMethodRef.make(
                            mHost.mChangedClassNode.type,
                            mDexItemFactory.changedClass.instanceUnInitMethodName,
                            mDexItemFactory.voidClass.primitiveType,
                            callChangedParaBuilder.build()));

            codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

    }

    private void generateInitBodyMethodInterceptor(List<DispatchMethodInfo> methods,
                                                   DexClassNode interceptorClassNode,
                                                   DexClassVisitor interceptorClassVisitor) {

        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorClassNode.type,
                mDexItemFactory.interceptableClass.invokeInitBodyMethod.getName(),
                mDexItemFactory.interceptableClass.invokeInitBodyMethod.getParameterTypes(),
                mDexItemFactory.interceptableClass.invokeInitBodyMethod.getReturnType(),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor methodVisitor = interceptorClassVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        final int pIntercptorThisReg = 0;
        final int pMethodIdReg = 1;
        final int pInitContextReg = 2;

        // 通过sparse-switch key为method id进行方法派发到$chg类的对应方法
        DexLabel[] labels = methods.stream()
                .map(m -> new DexLabel())
                .toArray(DexLabel[]::new);

        int[] keys = methods.stream()
                .mapToInt(m -> m.methodId)
                .toArray();

        // 根据methodId跳到指定的方法调用
        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodIdReg)),
                keys,
                labels);

        // 当sparse switch没有匹配的方法时，运行到这里，return void

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        // intercept dispatch
        for (int i = 0; i < methods.size(); i++) {
            DispatchMethodInfo method = methods.get(i);

            codeVisitor.visitLabel(labels[i]);

            // 获取this变量，该变量是org class实例
            final int vThisObjReg = 2;
            codeVisitor.visitConstInsn(Dops.IGET_OBJECT, DexRegisterList.make(
                    DexRegister.makeLocalReg(vThisObjReg),
                    DexRegister.makeParameterReg(pInitContextReg)),
                    mDexItemFactory.initContextClass.thisArgField);

            codeVisitor.visitConstInsn(Dops.CHECK_CAST,
                    DexRegisterList.make( DexRegister.makeLocalReg(vThisObjReg)),
                    DexConst.ConstType.make(mHost.mNewOrgClassNode.type));

            DexTypeList.Builder callChangedParaBuilder = DexTypeList.newBuilder();

            DexTypeList initMethodParaTypes = method.method.parameters;

            final int callInitRegCount = 1 + initMethodParaTypes.count();
            // 参数第一个为this实例
            callChangedParaBuilder.addType(mHost.mNewOrgClassNode.type);

            DexRegisterList.Builder callInitRegListBuilder = DexRegisterList.newBuilder();

            int nextCallInitParaReg = 5;

            boolean callInitRange = false;

            if (callInitRegCount <= 5) {
                callInitRegListBuilder.addReg(DexRegister.makeLocalReg(vThisObjReg));
            } else { // invoke-range, regs require continuous
                callInitRange = true;
                // this obj
                codeVisitor.visitSimpleInsn(Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(nextCallInitParaReg),
                                DexRegister.makeLocalReg(vThisObjReg)));
                callInitRegListBuilder.addReg(DexRegister.makeLocalReg(nextCallInitParaReg));
                nextCallInitParaReg++;
            }

            // 从InitContext.callArgs中取出参数，最好必要的拆箱(unbox)工作
            if (initMethodParaTypes.count() > 0) {
                final int vTmpInitArgReg = 3;
                codeVisitor.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vTmpInitArgReg),
                                DexRegister.makeParameterReg(pInitContextReg)),
                        mDexItemFactory.initContextClass.initArgsField);

                final int vCallArgIdxReg = 4;
                for (int initParaIdx = 0; initParaIdx < initMethodParaTypes.count(); initParaIdx++) {
                    DexType type = initMethodParaTypes.getType(initParaIdx);
                    codeVisitor.visitConstInsn(Dops.CONST_16,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vCallArgIdxReg)),
                            DexConst.LiteralBits32.make(initParaIdx));

                    codeVisitor.visitSimpleInsn(Dops.AGET_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(nextCallInitParaReg),
                                    DexRegister.makeLocalReg(vTmpInitArgReg),
                                    DexRegister.makeLocalReg(vCallArgIdxReg)));

                    // callArgs的类型是Object[]，通过aget-object之后的类型是Object，需要做一下check-cast
                    DexType checkCastType = type.isPrimitiveType()
                            ? mDexItemFactory.boxTypes.getBoxedTypeForPrimitiveType(type) : type;
                    codeVisitor.visitConstInsn(Dops.CHECK_CAST,
                            DexRegisterList.make(DexRegister.makeLocalReg(nextCallInitParaReg)),
                            DexConst.ConstType.make(checkCastType));

                    if (type.isPrimitiveType()) {
                        codeVisitor.visitConstInsn(Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(nextCallInitParaReg)),
                                mDexItemFactory.methods.primitiveValueMethodForType(type));

                        codeVisitor.visitSimpleInsn(mDexItemFactory.dops.getMoveResultOpForType(type).opcode,
                                DexRegisterList.make(
                                        DexRegister.makeLocalRegWithWide(
                                                nextCallInitParaReg, type.isWideType())));
                    }

                    callInitRegListBuilder.addReg(
                            DexRegister.makeLocalRegWithWide(nextCallInitParaReg, type.isWideType()));
                    callChangedParaBuilder.addType(type);
                    /** reg num is 0-based */
                    nextCallInitParaReg += (type.isWideType() ? 2 : 1);
                }


            }

            codeVisitor.visitSimpleInsn(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(nextCallInitParaReg),
                            DexRegister.makeParameterReg(pInitContextReg)));
            callInitRegListBuilder.addReg(DexRegister.makeLocalReg(nextCallInitParaReg));
            nextCallInitParaReg++;
            callChangedParaBuilder.addType(mDexItemFactory.initContextClass.type);

            // invoke_static/ invoke_static_range {vx .. vxx} L$chg->$instanceInitBody;
            codeVisitor.visitConstInsn(
                    callInitRange ? Dops.INVOKE_STATIC_RANGE : Dops.INVOKE_STATIC,
                    callInitRegListBuilder.build(),
                    DexConst.ConstMethodRef.make(
                            mHost.mChangedClassNode.type,
                            mDexItemFactory.changedClass.instanceInitBodyMethodName,
                            mDexItemFactory.voidClass.primitiveType,
                            callChangedParaBuilder.build()));

            codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
        }

        // init instruction end

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }
}
