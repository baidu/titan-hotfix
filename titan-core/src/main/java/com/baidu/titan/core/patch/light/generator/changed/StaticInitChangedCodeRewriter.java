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
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.common.TitanReflectionHelper;
import com.baidu.titan.core.patch.light.diff.ChangedClassDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffMode;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;

/**
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/12/14
 */
public class StaticInitChangedCodeRewriter extends DexCodeVisitor {

    private DexClassLoader mClassLoaderFromNewPool;

    private DexClassLoader mClassLoaderFromOldInstrumentPool;

    private TitanReflectionHelper mTitanReflectionHelper;

    private TitanDexItemFactory mDexItemFactory;

    private LightChangedClassGenerator mHost;

    private DexCodeNode mNewCodeNode;

    public StaticInitChangedCodeRewriter(DexCodeVisitor delegate,
                                         DexClassLoader classLoaderFromNewPool,
                                         DexClassLoader classLoaderFromOldInstrumentPool,
                                         TitanDexItemFactory dexItemFactory,
                                         LightChangedClassGenerator host,
                                         DexCodeNode newCodeNode) {
        super(delegate);
        this.mDexItemFactory = dexItemFactory;
        this.mClassLoaderFromNewPool = classLoaderFromNewPool;
        this.mClassLoaderFromOldInstrumentPool = classLoaderFromOldInstrumentPool;
        this.mTitanReflectionHelper = new TitanReflectionHelper(mDexItemFactory, delegate);
        this.mNewCodeNode = newCodeNode;
        this.mHost = host;
    }

    @Override
    public void visitBegin() {
        super.visitBegin();
    }

    @Override
    public void visitRegisters(int localRegCount, int parameterRegCount) {
        if (mTitanReflectionHelper.visitRegisters(localRegCount, parameterRegCount)) {
            return;
        }
        super.visitRegisters(localRegCount, parameterRegCount);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexTypeList types,
                              DexLabel[] handlers, DexLabel catchAllHandler) {

        super.visitTryCatch(start, end, types, handlers, catchAllHandler);
    }

    @Override
    public void visitLabel(DexLabel label) {
        super.visitLabel(label);
    }

    @Override
    public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
        if (mTitanReflectionHelper.visitConstInsn(op, regs, dexConst)) {
            return;
        }
        Dop dop = Dops.dopFor(op);
        if (dop.isInvokeKind()) {
            processInvokeInsn(dop, regs, (DexConst.ConstMethodRef)dexConst);
        } else if (dop.isFieldAccessKind()) {
            processFieldAccess(dop, regs, (DexConst.ConstFieldRef)dexConst);
        } else {
            super.visitConstInsn(op, regs, dexConst);
        }
    }

    @Override
    public void visitTargetInsn(int op, DexRegisterList regs, DexLabel label) {
        if (mTitanReflectionHelper.visitTargetInsn(op, regs, label)) {
            return;
        }
        super.visitTargetInsn(op, regs, label);
    }

    @Override
    public void visitSimpleInsn(int op, DexRegisterList regs) {
        if (mTitanReflectionHelper.visitSimpleInsn(op, regs)) {
            return;
        }
        super.visitSimpleInsn(op, regs);
    }

    @Override
    public void visitSwitch(int op, DexRegisterList regs, int[] keys, DexLabel[] targets) {
        if (mTitanReflectionHelper.visitSwitch(op, regs, keys, targets)) {
            return;
        }
        super.visitSwitch(op, regs, keys, targets);
    }

    @Override
    public void visitParameters(DexString[] parameters) {
        super.visitParameters(parameters);
    }

    @Override
    public void visitLocal(int reg, DexString name, DexType type, DexString signature,
                           DexLabel start, DexLabel end) {
        super.visitLocal(reg, name, type, signature, start, end);
    }

    @Override
    public void visitLineNumber(int line, DexLabel start) {
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    /**
     * 处理invoke-kind指令
     *
     * @param dop
     * @param regs
     * @param calledMethodRef
     */
    private void processInvokeInsn(
            Dop dop,
            DexRegisterList regs,
            DexConst.ConstMethodRef calledMethodRef) {

        int newOp = dop.opcode;
        DexRegisterList newRegs = regs;
        DexConst.ConstMethodRef newCalledMethodRef = calledMethodRef;

        ClassLinker linker = new ClassLinker(mDexItemFactory);

        DexClassLoader loader = mClassLoaderFromNewPool;
        DexClassLoader instruementedLoader = mClassLoaderFromOldInstrumentPool;

        DexMethodNode calledMethodNode = null;
        DexMethodNode instrementedCalledMethodNode = null;

        if (dop.isInvokeDirect() || dop.isInvokeStatic()) {
            calledMethodNode = linker.findDirectMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    loader);
            instrementedCalledMethodNode = linker.findDirectMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    instruementedLoader);
        } else if (dop.isInvokeVirtual() || dop.isInvokeSuper()) {
            calledMethodNode = linker.findVirtualMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    loader);
            instrementedCalledMethodNode = linker.findVirtualMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    instruementedLoader);
        } else if (dop.isInvokeInterface()) {
            calledMethodNode = linker.findInterfaceMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    loader);
            instrementedCalledMethodNode = linker.findInterfaceMethod(
                    calledMethodRef.getOwner(),
                    calledMethodRef.getParameterTypes(),
                    calledMethodRef.getReturnType(),
                    calledMethodRef.getName(),
                    instruementedLoader);
        } else {
            // impossible
        }

        if (calledMethodNode != null) {

            DiffMode calledMethodDiffMode = ChangedClassDiffMarker.getMethodDiffMode(calledMethodNode);

            if (calledMethodDiffMode.isAdded()) {
                // 如果是新增方法，那么调用其$chg方法
                DexType changedType = PatchUtils.getChangeType(calledMethodNode.owner,
                        mDexItemFactory);

                DexTypeList paraTypes;

                newOp = dop.isInvokeRange() ? Dops.INVOKE_STATIC_RANGE : Dops.INVOKE_STATIC;

                if (dop.isInvokeStatic()) {
                    paraTypes = calledMethodNode.parameters;
                } else {
                    DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
                    paraTypeBuilder.addType(calledMethodNode.owner);
                    calledMethodNode.parameters.forEach(t -> paraTypeBuilder.addType(t));
                    paraTypes = paraTypeBuilder.build();
                }

                newCalledMethodRef = DexConst.ConstMethodRef.make(
                        changedType,
                        calledMethodNode.name,
                        calledMethodNode.returnType,
                        paraTypes);

            } else if (calledMethodDiffMode.isChanged() ||
                    calledMethodDiffMode.isUnChanged()) {
                // 无论调用的方法是否改变，都会继续调用原方法
                // 对于有代码变化的方法，在处理其所在类的时候，会拦截到新实现的方法实现中的
                // 这里需要注意的是区分direct和virtual方法，
                // 1、对于virtual方法，
                //    1) 如果插桩采用的虚方法public化，那么可以直接调用原方法
                //    2) 否则，对于protected和package访问级别的方法采用
                //          a) 反射调用
                //          b) copy方法实现到对应$chg类（抽象方法只能反射调用）
                // 2、对于direct方法
                //    1) 采用反射调用
                //    2) copy方法实现到对应$chg类

                boolean callDirect = false;
                boolean callByReflect = false;
                boolean callChanged = false;
                if (dop.isInvokeSuper()) {
                    if (instrementedCalledMethodNode.accessFlags
                            .containsNoneOf(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_PROTECTED)) {
                        throw new IllegalStateException("can not invoke package private super method "
                                + instrementedCalledMethodNode.toString());
                    }
                    callDirect = true;
                } else if (calledMethodNode.isVirtualMethod() || calledMethodNode.isStatic()) {
                    if (calledMethodNode.accessFlags.containsOneOf(
                            DexAccessFlags.ACC_PUBLIC)) {
                        callDirect = true;
                    } else if (mHost.mInstrumentedVirtualToPublic) {
                        callDirect = true;
                        if (instrementedCalledMethodNode.accessFlags.containsNoneOf(DexAccessFlags.ACC_PUBLIC)) {
                            /*
                            Java Virtual Machine specification中对protected和package private的field和method的访问控制做了规定
                            5.3 Creation and Loading

                            ... At run time, a class or interface is determined not by its name alone, but by a pair: its fully qualified name and its defining class loader. Each such class or interface belongs to a single runtime package. The runtime package of a class or interface is determined by the package name and defining class loader of the class or interface. ...

                            5.4.4 Access Control

                            ... A field or method R is accessible to a class or interface D if and only if any of the following conditions is true: ...

                            R is protected and is declared in a class C, and D is either a subclass of C or C itself.
                            R is either protected or package private (that is, neither public nor protected nor private), and is declared by a class in the same runtime package as D.

                             */
                            // 如果方法是protected 或 package default，在$chg类中调用不到，需要使用反射调用
                            callByReflect = true;
                            callDirect = false;
                        }
                    } else if (mHost.mUseReflection
                            // 抽象方法只能通过反射调用，copy到$chg类中会报错导致生成patch失败
                            || calledMethodNode.accessFlags.containsOneOf(DexAccessFlags.ACC_ABSTRACT)) {
                        callByReflect = true;
                    } else {
                        callChanged = true;
                    }
                } else if (calledMethodNode.isInstanceInitMethod()) {
                    if (calledMethodNode.accessFlags.containsOneOf(
                            DexAccessFlags.ACC_PUBLIC) || mHost.mInstrumentedVirtualToPublic) {
                        callDirect = true;
                    } else {
                        // 对于私有和package构造方法，只能通过反射调用
                        callByReflect = true;
                    }
                } else if (calledMethodNode.accessFlags.containsAllOf(DexAccessFlags.ACC_PRIVATE
                        | DexAccessFlags.ACC_NATIVE)) {
                    callByReflect = true;
                } else { // direct method
                    if (mHost.mUseReflection) {
                        callByReflect = true;
                    } else {
                        callChanged = true;
                    }
                }

                if (callDirect) {
                    // do noting
                }

                if (callByReflect) {
                    mTitanReflectionHelper.reflectMethodInvoke(dop.opcode,
                            calledMethodRef, regs);
                    return;
                }

                if (callChanged) {
                    mHost.addUnProcessedListIfNeed(calledMethodNode);

                    DexType changedType = PatchUtils.getChangeType(
                            calledMethodNode.owner,
                            mDexItemFactory);

                    DexTypeList paraTypes;

                    newOp = dop.isInvokeRange() ?
                            Dops.INVOKE_STATIC_RANGE : Dops.INVOKE_STATIC;

                    if (dop.isInvokeStatic()) {
                        paraTypes = calledMethodNode.parameters;
                    } else {
                        DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
                        paraTypeBuilder.addType(calledMethodNode.owner);
                        calledMethodNode.parameters.forEach(t -> paraTypeBuilder.addType(t));
                        paraTypes = paraTypeBuilder.build();
                    }

                    newCalledMethodRef = DexConst.ConstMethodRef.make(
                            changedType,
                            calledMethodNode.name,
                            calledMethodNode.returnType,
                            paraTypes);
                }
            } else if (calledMethodDiffMode.isRemoved()) {
                // should't happen
            }

        } // end calledMethodNode == null

        super.visitConstInsn(newOp, newRegs, newCalledMethodRef);
    }

    /**
     * 处理字段访问指令
     *
     * @param dop
     * @param regs
     * @param fieldRef
     */
    private void processFieldAccess(
            Dop dop,
            DexRegisterList regs,
            DexConst.ConstFieldRef fieldRef) {

        int newOp = dop.opcode;
        DexRegisterList newRegs = regs;
        DexConst.ConstFieldRef newFieldRef = fieldRef;

        ClassLinker linker = new ClassLinker(mDexItemFactory);
        DexClassLoader loader = mClassLoaderFromNewPool;
        DexClassLoader oldInstrumentLoader = mClassLoaderFromOldInstrumentPool;

        DexFieldNode accessedField = linker.resolveFieldJLS(fieldRef.getOwner(),
                fieldRef.getName(), fieldRef.getType(), loader);

        DexFieldNode accessedFieldInOldInstrument = linker.resolveFieldJLS(fieldRef.getOwner(),
                fieldRef.getName(), fieldRef.getType(), oldInstrumentLoader);

        if (accessedField != null) {
            DiffMode fieldDiff = ChangedClassDiffMarker.getFieldDiffMode(accessedField);
            if (fieldDiff.isAdded() || fieldDiff.isAffectFinalField()) {
                DexType fieldHolderType =
                        PatchUtils.getFieldHolderType(accessedField.owner, mDexItemFactory);

                if (accessedField.isStatic()) {
                    newFieldRef = DexConst.ConstFieldRef.make(
                            fieldHolderType,
                            accessedField.type,
                            accessedField.name);
                } else {

                    DexRegister dstReg;
                    if (dop.isFieldInstancePut()) {
                        dstReg = DexRegister.makeLocalReg(mNewCodeNode.getLocalRegCount());
                    } else {
                        dstReg = regs.get(0);
                    }

                    super.visitConstInsn(Dops.INVOKE_STATIC,
                            DexRegisterList.make(regs.get(1)),
                            mDexItemFactory.changedFieldHolderClass.getOrCreateFieldHolderMethod(
                                    fieldHolderType,
                                    accessedField.owner));
                    super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT, DexRegisterList.make(dstReg));

                    newRegs = DexRegisterList.make(regs.get(0), dstReg);

                    newFieldRef = DexConst.ConstFieldRef.make(
                            fieldHolderType,
                            accessedField.type,
                            accessedField.name);
                }
            } else if (fieldDiff.isChanged() || fieldDiff.isUnChanged()) {
                boolean accessDirect = false;
                boolean accessByReflect = false;
                // 如果field是protected, private或者package-private，在$chg类中调用不到，需要使用反射调用
                if (accessedFieldInOldInstrument.accessFlags.containsOneOf(DexAccessFlags.ACC_PRIVATE
                        | DexAccessFlags.ACC_PROTECTED)
                        || accessedFieldInOldInstrument.accessFlags.containsNoneOf(DexAccessFlags.ACC_PRIVATE
                        | DexAccessFlags.ACC_PROTECTED | DexAccessFlags.ACC_PUBLIC)) {
                    accessByReflect = true;
                }
                // TODO check accessible
                if (accessDirect) {
                    // do noting
                }
                if (accessByReflect) {
                    mTitanReflectionHelper.reflectFieldAccess(dop.opcode, fieldRef, regs);
                    return;
                }
            }
        }

        super.visitConstInsn(newOp, newRegs, newFieldRef);
    }

}
