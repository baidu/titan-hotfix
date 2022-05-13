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
import com.baidu.titan.core.patch.common.TitanReflectionHelper;
import com.baidu.titan.core.patch.light.LightPatchClassPools;
import com.baidu.titan.core.patch.light.diff.ChangedClassDiffMarker;
import com.baidu.titan.core.patch.light.diff.ClassPoolDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffMode;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 *
 * 新增类
 *
 * @author zhangdi07@baidu.com
 * @since 2018/7/8
 */
public class LightAddedClassGenerator implements DexClassNodeVisitor {

    private DexClassNode mNewOrgClassNode;

    private DexClassNode mAddedClassNode;

    private DexClassVisitor mAddedClassVisitor;

    private TitanDexItemFactory mDexItemFactory;

    private LightPatchClassPools mClassPools;

    private DexClassLoader mDexClassLoaderFromNewPool;
    private DexClassLoader mDexClassLoaderFromOldPool;

    private boolean mInstrumentedVirtualToPublic = true;

    private boolean mUseReflection;


    private LightAddedClassGenerator(DexClassNode newOrgClassNode,
                                    LightPatchClassPools classPools,
                                    TitanDexItemFactory dexFactory) {
        this.mNewOrgClassNode = newOrgClassNode;
        this.mClassPools = classPools;
        this.mDexItemFactory = dexFactory;
    }

    public static void generate(DexClassNode newOrgClassNode,
                                LightPatchClassPools classPools,
                                TitanDexItemFactory dexFactory) {
        LightAddedClassGenerator addedClassGenerator =
                new LightAddedClassGenerator(newOrgClassNode, classPools, dexFactory);
        addedClassGenerator.doGenerate();
    }

    private void doGenerate() {

        mDexClassLoaderFromNewPool = new DexClassLoader() {

            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode rewriteDcn = mClassPools.rewriteClassPool.findClassFromAll(type);
                if (rewriteDcn != null) {
                    return rewriteDcn;
                }
                return mClassPools.newOrgClassPool.findClassFromAll(type);
            }

        };

        mDexClassLoaderFromOldPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return mClassPools.oldOrgClassPool.findClassFromAll(type);
            }
        };

        int oldAccessFlags = mNewOrgClassNode.accessFlags.getFlags();
        int accessFlags =
                (oldAccessFlags & (~(DexAccessFlags.ACC_PROTECTED | DexAccessFlags.ACC_PRIVATE)))
                        | DexAccessFlags.ACC_PUBLIC;

        this.mAddedClassNode = new DexClassNode(
                mNewOrgClassNode.type,
                new DexAccessFlags(accessFlags),
                mNewOrgClassNode.superType,
                mNewOrgClassNode.interfaces);

        this.mClassPools.addedClassPool.addProgramClass(this.mAddedClassNode);

        this.mAddedClassVisitor = this.mAddedClassNode.asVisitor();

        this.mAddedClassVisitor.visitBegin();
        this.mNewOrgClassNode.accept(this);
        this.mAddedClassVisitor.visitEnd();

        mClassPools.addedClassPool.addProgramClass(this.mAddedClassNode);

    }


    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {
        dan.accept(mAddedClassVisitor.visitAnnotation(new DexAnnotationVisitorInfo(
                dan.getType(),
                dan.getVisibility())));
    }

    @Override
    public void visitMethod(DexMethodNode dmn) {
        DexMethodVisitor methodVisitor = mAddedClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        dmn.owner,
                        dmn.name,
                        dmn.parameters,
                        dmn.returnType,
                        new DexAccessFlags(dmn.accessFlags.getFlags())));

        dmn.accept(new DexMethodVisitor(methodVisitor) {

            @Override
            public DexCodeVisitor visitCode() {
                DexCodeVisitor superCodeVisitor = super.visitCode();
                return new DexCodeRewritter(dmn, superCodeVisitor, mDexClassLoaderFromNewPool,
                        mDexClassLoaderFromOldPool);
            }
        });
    }

    private class DexCodeRewritter extends DexCodeVisitor {

        private final DexMethodNode mMethodNode;

        private final DexClassLoader mClassLoaderFromNewPool;
        private final DexClassLoader mClassLoaderFromOldPool;

        private TitanReflectionHelper mTitanReflectionHelper;

        public DexCodeRewritter(DexMethodNode methodNode, DexCodeVisitor delegate,
                                DexClassLoader classLoader, DexClassLoader oldClassLoader) {
            super(delegate);
            this.mMethodNode = methodNode;
            this.mClassLoaderFromNewPool = classLoader;
            this.mClassLoaderFromOldPool = oldClassLoader;
            mTitanReflectionHelper = new TitanReflectionHelper(mDexItemFactory, delegate);
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
            DexClassLoader oldLoader = mClassLoaderFromOldPool;

            DexMethodNode calledMethodNode = null;
            DexMethodNode oldCalledMethodNode = null;

            if (dop.isInvokeDirect() || dop.isInvokeStatic()) {
                calledMethodNode = linker.findDirectMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        loader);
                oldCalledMethodNode = linker.findDirectMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        oldLoader);
            } else if (dop.isInvokeVirtual() || dop.isInvokeSuper()) {
                calledMethodNode = linker.findVirtualMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        loader);
                oldCalledMethodNode = linker.findVirtualMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        oldLoader);
            } else if (dop.isInvokeInterface()) {
                calledMethodNode = linker.findInterfaceMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        loader);
                oldCalledMethodNode = linker.findInterfaceMethod(
                        calledMethodRef.getOwner(),
                        calledMethodRef.getParameterTypes(),
                        calledMethodRef.getReturnType(),
                        calledMethodRef.getName(),
                        oldLoader);
            } else {
                // impossible
            }

            if (calledMethodNode != null) {

                DexClassNode calledClassNode =
                        mClassLoaderFromNewPool.findClass(calledMethodNode.owner);

                DiffMode calledClassDiffMode = ClassPoolDiffMarker.getClassDiffMode(calledClassNode);

                // 如果是新增类的话，不需要做任何改动，因为在同一个classloader下
                if (!calledClassDiffMode.isAdded()) {

                    DiffMode calledMethodDiffMode =
                            ChangedClassDiffMarker.getMethodDiffMode(calledMethodNode);

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
                        //          b) copy方法实现到对应$chg类
                        // 2、对于direct方法
                        //    1) 采用反射调用
                        //    2) copy方法实现到对应$chg类

                        boolean callDirect = false;
                        boolean callByReflect = false;
                        boolean callChanged = false;
                        if (calledMethodNode.isVirtualMethod() || calledMethodNode.isStatic()) {
                            if (calledMethodNode.accessFlags.containsOneOf(
                                    DexAccessFlags.ACC_PUBLIC)) {
                                callDirect = true;
                            }  else if (mInstrumentedVirtualToPublic) {
                                callDirect = true;
                                DexAccessFlags calledMethodAccessFlags = null;
                                if (oldCalledMethodNode != null) {
                                    calledMethodAccessFlags = oldCalledMethodNode
                                            .getExtraInfo(Constant.EXTRA_KEY_INSTRUMENT_ACCESS_FLAGS, null);
                                }
                                if (calledMethodAccessFlags == null) {
                                    calledMethodAccessFlags = calledMethodNode.accessFlags;
                                }
                                if (calledMethodAccessFlags.containsNoneOf(DexAccessFlags.ACC_PUBLIC)) {
                                    // 如果方法是protected 或 package default，在$chg类中调用不到，需要使用反射调用
                                    callByReflect = true;
                                    callDirect = false;
                                }
                            } else if (mUseReflection) {
                                callByReflect = true;
                            } else {
                                callChanged = true;
                            }
                        } else if (calledMethodNode.isInstanceInitMethod()) {
                            if (calledMethodNode.accessFlags.containsOneOf(
                                    DexAccessFlags.ACC_PUBLIC) || mInstrumentedVirtualToPublic) {
                                callDirect = true;
                            } else {
                                // 对于私有和package构造方法，只能通过反射调用
                                callByReflect = true;
                            }
                        } else { // direct method
                            if (mUseReflection) {
                                callByReflect = true;
                            } else {
                                callChanged = true;
                            }
                        }

                        if (callDirect) {
                            // do noting
                        }

                        if (callByReflect) {
                            mTitanReflectionHelper.reflectMethodInvoke(dop.opcode, calledMethodRef, regs);
                        }

                        if (callChanged) {

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

            DexFieldNode accessedField = linker.resolveFieldJLS(fieldRef.getOwner(),
                    fieldRef.getName(), fieldRef.getType(), loader);

            if (accessedField != null) {
                DexClassNode calledClassNode = mClassLoaderFromNewPool.findClass(accessedField.owner);
                DiffMode calledClassDiffMode = ClassPoolDiffMarker.getClassDiffMode(calledClassNode);

                // 如果是新增类，保持不变
                if (!calledClassDiffMode.isAdded()) {
                    DiffMode fieldDiff = ChangedClassDiffMarker.getFieldDiffMode(accessedField);
                    if (fieldDiff.isAdded()) {
                        DexType fieldHolderType =
                                PatchUtils.getFieldHolderType(accessedField.owner, mDexItemFactory);

                        if (accessedField.isStatic()) {
                            newFieldRef = DexConst.ConstFieldRef.make(
                                    fieldHolderType,
                                    accessedField.type,
                                    accessedField.name);
                        } else {
                            DexRegister dstReg = regs.get(0);
                            super.visitConstInsn(Dops.INVOKE_STATIC,
                                    DexRegisterList.make(regs.get(1)),
                                    mDexItemFactory.changedFieldHolderClass.getOrCreateFieldHolderMethod(
                                            fieldHolderType,
                                            accessedField.owner));
                            super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                                    DexRegisterList.make(dstReg));

                            newRegs = DexRegisterList.make(dstReg, dstReg);

                            newFieldRef = DexConst.ConstFieldRef.make(
                                    fieldHolderType,
                                    accessedField.type,
                                    accessedField.name);
                        }
                    }
                }
            }

            super.visitConstInsn(newOp, newRegs, newFieldRef);
        }

    }

    @Override
    public void visitField(DexFieldNode dfn) {
        dfn.accept(mAddedClassVisitor.visitField(
                new DexFieldVisitorInfo(
                        dfn.owner,
                        dfn.name,
                        dfn.type,
                        dfn.accessFlags)));
    }

    @Override
    public void visitClassNodeEnd() {
    }

}
