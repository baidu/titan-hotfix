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

package com.baidu.titan.core.patch.light.diff;

import com.baidu.titan.core.instrument.DisableInterceptMarker;
import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexFileVersion;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;
import com.baidu.titan.dex.writer.DexFileWriter;
import com.baidu.titan.sdk.common.TitanConstant;

import java.util.Arrays;

/**
 *
 * method & code differ
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/5/4
 */
public class ChangedMethodDiffMarker extends DexCodeVisitor {

    private static final String TAG = "ChangedMethodDiffMarker";
    /** 记录到dexclassnode中的diff flags key*/
    private static final String CHANGED_METHOD_DIFF_FLAGS = "changed_method_diff_flags";

    private DiffContext mDiffContext;

    private DexMethodNode mNewMethodNode;

    private DexMethodNode mOldMethodNode;

    private DexClassNode mNewOrgClassNode;

    private DexClassNode mOldOrgClassNode;

    private boolean mUseReflection = false;

    private int mDiffStatusFlags;

    private DiffStatus mDiffStatus;

    public static final int DIFF_UNCHANGED = 1;

    public static final int DIFF_CHANGED_COMPATIBLE = 0;

    private static final int DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS = 1;

    private static final int DIFF_CHANGED_INCOMPATIBLE_ABSTRACT = 1 << 1;

    private static final int DIFF_CHANGED_INCOMPATIBLE_VIRTUAL = 1 << 2;

    private static final int DIFF_CHANGED_INCOMPATIBLE_DISABLE_INTERCEPT = 1 << 3;
    /** just-in-time加载模式下修改了clinit，不兼容*/
    private static final int DIFF_CHANGED_INCOMPATIBLE_CLINIT_CHANGED_IN_TIME_POLICY = 1 << 4;

    public ChangedMethodDiffMarker(DiffContext diffContext,
                                   DexClassNode newClassNode,
                                   DexClassNode oldClassNode,
                                   DexMethodNode newMethodNode,
                                   DexMethodNode oldMethodNode) {
        this.mDiffContext = diffContext;
        this.mNewOrgClassNode = newClassNode;
        this.mOldOrgClassNode = oldClassNode;
        this.mNewMethodNode = newMethodNode;
        this.mOldMethodNode = oldMethodNode;
    }

    public DiffStatus diff() {

        if (this.mNewMethodNode.isStatic() != this.mOldMethodNode.isStatic()) {
            mDiffStatusFlags |= DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS;
        }

        if (this.mNewMethodNode.isDirectMethod() != this.mOldMethodNode.isDirectMethod()) {
            mDiffStatusFlags |= DIFF_CHANGED_INCOMPATIBLE_VIRTUAL;
        }

        if (this.mNewMethodNode.accessFlags.containsOneOf(DexAccessFlags.ACC_ABSTRACT) !=
                this.mOldMethodNode.accessFlags.containsOneOf(DexAccessFlags.ACC_ABSTRACT)) {
            mDiffStatusFlags |= DIFF_CHANGED_INCOMPATIBLE_ABSTRACT;
        }

        if (this.mNewMethodNode.accessFlags.containsOneOf(DexAccessFlags.ACC_ABSTRACT)) {
            mDiffStatus = mDiffStatusFlags == 0 ?
                    DiffStatus.UNCHANGED : DiffStatus.CHANGED_COMPATIBLE;
        } else {
            if (diffQuickCompare(this.mNewOrgClassNode, this.mOldOrgClassNode,
                    this.mNewMethodNode, this.mOldMethodNode)) {
                mDiffStatus = DiffStatus.UNCHANGED;
            } else if (mNewMethodNode.isStaticInitMethod()
                    && mDiffContext.loadPolicy == TitanConstant.PATCH_LOAD_POLICY_JUST_IN_TIME) {
                mDiffStatusFlags |= DIFF_CHANGED_INCOMPATIBLE_CLINIT_CHANGED_IN_TIME_POLICY;
                mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
            } else if (DisableInterceptMarker.getInterceptDisable(mNewMethodNode)) {
                mDiffStatusFlags |= DIFF_CHANGED_INCOMPATIBLE_DISABLE_INTERCEPT;
                mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
            } else if ((mDiffStatusFlags & (DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS
                    | DIFF_CHANGED_INCOMPATIBLE_VIRTUAL
                    | DIFF_CHANGED_INCOMPATIBLE_ABSTRACT)) != 0) {
                mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
            }
            else {
                DexCodeNode dexCodeNode = mNewMethodNode.getCode();
                assert dexCodeNode != null;
                dexCodeNode.accept(this);
                mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;
            }
        }
        int savedDiffFlags = mNewOrgClassNode.getExtraInfo(CHANGED_METHOD_DIFF_FLAGS, 0);
        mNewOrgClassNode.setExtraInfo(CHANGED_METHOD_DIFF_FLAGS, mDiffStatusFlags | savedDiffFlags);
        return mDiffStatus;
    }

    /**
     * 是否需要生成class的副本
     *
     * @param dcn dexclassnode
     * @return 是否需要生成class的副本
     */
    static boolean shouldCopyClass(DexClassNode dcn) {
        int changedDiffFlags = dcn.getExtraInfo(CHANGED_METHOD_DIFF_FLAGS, 0);
        return shouldCopyClass(changedDiffFlags);
    }

    /**
     * 清空diff flags
     *
     * @param dcn dexclassnode
     */
    static void clearDiffFlags(DexClassNode dcn) {
        dcn.setExtraInfo(CHANGED_METHOD_DIFF_FLAGS, 0);
    }

    /**
     * 是否需要生成class的副本
     *
     * @param diffStatusFlags diff之后记录的flag
     * @return 是否需要生成class的副本
     */
    private static boolean shouldCopyClass(int diffStatusFlags) {
//        if (diffStatusFlags != 0 &&
//                (diffStatusFlags & ~(DIFF_CHANGED_COMPATIBLE_STATIC_ACCESS
//                        | DIFF_CHANGED_COMPATIBLE_ABSTRACT
//                        | DIFF_CHANGED_COMPATIBLE_VIRTUAL
//                        | DIFF_CHANGED_COMPATIBLE_DISABLE_INTERCEPT)) == 0) {
//            return true;
//        }
        return false;
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format(".method %s status: %s",
                new DexNamedProtoNode(mNewMethodNode).toString(),
                mDiffStatus.toString()));
        logger.incIndent();
        if ((mDiffStatusFlags & DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS) != 0) {
            logger.i(TAG, "method's static access changed");
        }

        if ((mDiffStatusFlags & DIFF_CHANGED_INCOMPATIBLE_ABSTRACT) != 0) {
            logger.i(TAG, "method's abstract access changed");
        }

        if ((mDiffStatusFlags & DIFF_CHANGED_INCOMPATIBLE_VIRTUAL) != 0) {
            logger.i(TAG, "method's access flag changed");
        }

        if ((mDiffStatusFlags & DIFF_CHANGED_INCOMPATIBLE_CLINIT_CHANGED_IN_TIME_POLICY) != 0) {
            logger.i(TAG, "<clinit> changed when load policy is just-in-time");
        }

        if ((mDiffStatusFlags & DIFF_CHANGED_INCOMPATIBLE_DISABLE_INTERCEPT) != 0) {
            logger.i(TAG, "intercept is disabled for this method");
        }
        logger.decIndent();




//        int incompatibleChangedMethodCount = mIncompatibleChangedMethods.size();
//        int compatibleChangedMethodCount = mCompatibleChangedMethods.size();
//        int compatibleAddedMethodCount = mCompatibleAddedMethods.size();
//        int incompatibleAddedMethodCount = mInCompatibleAddedMethods.size();
//
//        if (incompatibleChangedMethodCount != 0) {
//            logger.i(TAG, String.format("%d incompatible changed methods",
//                    incompatibleChangedMethodCount));
//            this.mIncompatibleChangedMethods.forEach((method, marker) -> {
//                logger.i(TAG, String.format("method %s :", method.toString()));
//                marker.printDiffStatus(logger);
//            });
//        }
    }


    private static boolean diffQuickCompare(DexClassNode newClassNode,
                                            DexClassNode oldClassNode,
                                            DexMethodNode newMethodNode,
                                            DexMethodNode oldMethodNode) {
        byte[] oldMethodBytes = generateMethodBytes(oldClassNode, oldMethodNode, true);
        byte[] newMethodBytes = generateMethodBytes(newClassNode, newMethodNode, true);
        return Arrays.equals(oldMethodBytes, newMethodBytes);

    }

    // dex code visit method begin

    @Override
    public void visitBegin() {
        super.visitBegin();
    }

    @Override
    public void visitRegisters(int localRegCount, int parameterRegCount) {
        super.visitRegisters(localRegCount, parameterRegCount);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexTypeList types, DexLabel[] handlers,
                              DexLabel catchAllHandler) {
        super.visitTryCatch(start, end, types, handlers, catchAllHandler);
    }

    @Override
    public void visitLabel(DexLabel label) {
        super.visitLabel(label);
    }

    @Override
    public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
        Dop dop = Dops.dopFor(op);
        if (dop.isInvokeKind()) {
            visitInvokeInsn(op, regs, (DexConst.ConstMethodRef)dexConst);
        }
    }

    private static DexMethodNode getMethodNodeByMethodRef(int dop, ClassLinker linker, DexClassLoader loader,
                                                          DexConst.ConstMethodRef methodRef) {
        Dop dopObj = Dops.dopFor(dop);

        DexMethodNode dmn = null;

        if (dopObj.isInvokeDirect()) {
            dmn = linker.findDirectMethod(
                    methodRef.getOwner(),
                    methodRef.getParameterTypes(),
                    methodRef.getReturnType(),
                    methodRef.getName(),
                    loader);
        } else if (dopObj.isInvokeVirtual() || dopObj.isInvokeSuper()) {
            dmn = linker.findVirtualMethod(
                    methodRef.getOwner(),
                    methodRef.getParameterTypes(),
                    methodRef.getReturnType(),
                    methodRef.getName(),
                    loader);
        } else if (dopObj.isInvokeInterface()) {
            dmn = linker.findInterfaceMethod(
                    methodRef.getOwner(),
                    methodRef.getParameterTypes(),
                    methodRef.getReturnType(),
                    methodRef.getName(),
                    loader);
        } else {
            // impossible
        }
        return dmn;
    }

    private void visitInvokeInsn(int dop,
                                 DexRegisterList regs,
                                 DexConst.ConstMethodRef calledMethodRef) {
        boolean callSelf = calledMethodRef.getOwner().equals(this.mNewOrgClassNode.type);
        if (callSelf) {

            DexMethodNode calledMethodNodeFromOldClass =
                    getMethodNodeByMethodRef(dop, mDiffContext.linker,
                            mDiffContext.classLoaderFromOldPool, calledMethodRef);
            DexMethodNode calledMethodNodeFromNewClass =
                    getMethodNodeByMethodRef(dop, mDiffContext.linker,
                            mDiffContext.classLoaderFromNewPool, calledMethodRef);

            boolean callChangedMethod = calledMethodNodeFromOldClass == null;


        }
    }


    @Override
    public void visitTargetInsn(int op, DexRegisterList regs, DexLabel label) {
        super.visitTargetInsn(op, regs, label);
    }

    @Override
    public void visitSimpleInsn(int op, DexRegisterList regs) {
        super.visitSimpleInsn(op, regs);
    }

    @Override
    public void visitSwitch(int op, DexRegisterList regs, int[] keys, DexLabel[] targets) {
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


    // dex code visit method end

    private static byte[] generateMethodBytes(DexClassNode dcn, DexMethodNode dmn,
                                              boolean ignoreDebugInfo) {
        DexFileWriter dfw = new DexFileWriter();
        dfw.visitBegin();
        dfw.visitDexVersion(DexFileVersion.LATEST_VERSION);

        DexClassVisitor dcv = dfw.visitClass(
                new DexClassVisitorInfo(
                        dcn.type,
                        dcn.superType,
                        dcn.interfaces,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC)));
        dcv.visitBegin();

        DexMethodVisitor dmv = dcv.visitMethod(
                new DexMethodVisitorInfo(
                        dmn.owner,
                        dmn.name,
                        dmn.parameters,
                        dmn.returnType,
                        new DexAccessFlags(dmn.accessFlags)));

        dmn.accept(new DexMethodVisitor(dmv) {

            @Override
            public void visitBegin() {
                super.visitBegin();
            }

            @Override
            public DexAnnotationVisitor visitAnnotationDefault() {
                return super.visitAnnotationDefault();
            }

            @Override
            public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotationInfo) {
                return super.visitAnnotation(annotationInfo);
            }

            @Override
            public DexAnnotationVisitor visitParameterAnnotation(int parameter,
                                                        DexAnnotationVisitorInfo annotationInfo) {
                return super.visitParameterAnnotation(parameter, annotationInfo);
            }

            @Override
            public DexCodeVisitor visitCode() {
                DexCodeVisitor delegateDcv = super.visitCode();
                return new DexCodeVisitor(delegateDcv) {

                    @Override
                    public void visitBegin() {
                        super.visitBegin();
                    }

                    @Override
                    public void visitRegisters(int localRegCount, int parameterRegCount) {
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
                        if (op == Dops.CONST_STRING) {
                            op = Dops.CONST_STRING_JUMBO;
                        }
                        super.visitConstInsn(op, regs, dexConst);
                    }

                    @Override
                    public void visitTargetInsn(int op, DexRegisterList regs, DexLabel label) {
                        super.visitTargetInsn(op, regs, label);
                    }

                    @Override
                    public void visitSimpleInsn(int op, DexRegisterList regs) {
                        super.visitSimpleInsn(op, regs);
                    }

                    @Override
                    public void visitSwitch(int op, DexRegisterList regs, int[] keys,
                                            DexLabel[] targets) {
                        super.visitSwitch(op, regs, keys, targets);
                    }

                    @Override
                    public void visitParameters(DexString[] parameters) {
                        if (!ignoreDebugInfo) {
                            super.visitParameters(parameters);
                        }
                    }

                    @Override
                    public void visitLocal(int reg, DexString name, DexType type,
                                           DexString signature, DexLabel start, DexLabel end) {
                        super.visitLocal(reg, name, type, signature, start, end);
                    }

                    @Override
                    public void visitLineNumber(int line, DexLabel start) {
                        if (!ignoreDebugInfo) {
                            super.visitLineNumber(line, start);
                        }
                    }

                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                    }
                };
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        });


        dcv.visitEnd();

        dfw.visitEnd();

        return dfw.toByteArray();
    }

}
