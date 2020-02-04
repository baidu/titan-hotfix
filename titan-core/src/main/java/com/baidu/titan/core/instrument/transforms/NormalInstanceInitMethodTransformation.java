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

package com.baidu.titan.core.instrument.transforms;

import com.baidu.titan.core.Constant;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * deal with normal component's <init> method
 *
 * @author zhangdi07@baidu.com
 * @since 2018/12/10
 */
public class NormalInstanceInitMethodTransformation extends DexCodeVisitor {

    private DexMethodNode mDexMethodNode;

    private DexClassNode mDexClassNode;

    private DexLabel mInterceptLabel;

    private DexLabel mOriginLabel;

    private int mInterceptorReg = 0;

    private DexConst.ConstMethodRef mInitMethodRef;

    private TitanDexItemFactory mFactory;

    public NormalInstanceInitMethodTransformation(DexCodeVisitor delegate,
                                                  DexClassNode classNode,
                                                  DexMethodNode methodNode,
                                                  DexConst.ConstMethodRef initMethod,
                                                  TitanDexItemFactory factory) {
        super(delegate);
        this.mDexClassNode = classNode;
        this.mDexMethodNode = methodNode;
        this.mInitMethodRef = initMethod;
        this.mFactory = factory;
    }

    @Override
    public void visitBegin() {
        super.visitConstInsn(
                Dops.SGET_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(mInterceptorReg)),
                DexConst.ConstFieldRef.make(
                        mDexClassNode.type,
                        mFactory.interceptableClass.type,
                        mFactory.instrumentedClass.interceptorFieldName));
        mInterceptLabel = new DexLabel();
        super.visitTargetInsn(
                Dops.IF_NEZ,
                DexRegisterList.make(DexRegister.makeLocalReg(mInterceptorReg)),
                mInterceptLabel);
        mOriginLabel = new DexLabel();
        super.visitLabel(mOriginLabel);
    }

    @Override
    public void visitEnd() {
        super.visitLabel(mInterceptLabel);
        AtomicInteger minLocalRegCount = new AtomicInteger(0);
        super.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.EMPTY,
                mFactory.titanRuntimeClass.newInitContextMethod);

        int vInitContextReg = 1;

        super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInitContextReg)));

        // step 1 : 将<init>方法中的参数赋值给InitContext.initArgs
        DexTypeList paraTypes = mDexMethodNode.parameters;
        if (paraTypes.count() > 0) {
            final int vParaSizeReg = 2;
            final int vParaArrayReg = 2;

            super.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParaSizeReg)),
                    DexConst.LiteralBits32.make(paraTypes.count()));

            super.visitConstInsn(
                    Dops.NEW_ARRAY,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParaArrayReg),
                            DexRegister.makeLocalReg(vParaSizeReg)),
                    DexConst.ConstType.make(
                            mFactory.createArrayType(mFactory.objectClass.type)));

            super.visitConstInsn(
                    Dops.IPUT_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParaArrayReg),
                            DexRegister.makeLocalReg(vInitContextReg)),
                    mFactory.initContextClass.initArgsField);

            int nextParaReg = 1;
            for (int paraIdx = 0; paraIdx < paraTypes.count(); paraIdx++) {
                DexType type = paraTypes.getType(paraIdx);

                DexConst.ConstMethodRef valueOfMethodRef = type.isPrimitiveType()
                        ? mFactory.methods.valueOfMethodForType(type) : null;

                final int vArrayIdxReg = 3;
                // idx
                super.visitConstInsn(
                        Dops.CONST_16,
                        DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                        DexConst.LiteralBits32.make(paraIdx));
                if (valueOfMethodRef == null) {
                    super.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeParameterReg(nextParaReg),
                                    DexRegister.makeLocalReg(vParaArrayReg),
                                    DexRegister.makeLocalReg(vArrayIdxReg)));

                } else {
                    DexRegisterList valueOfRegs = DexRegisterList.make(type.isWideType()
                            ? DexRegister.makeDoubleParameterReg(nextParaReg) :
                            DexRegister.makeParameterReg(nextParaReg));

                    super.visitConstInsn(Dops.INVOKE_STATIC, valueOfRegs, valueOfMethodRef);
                    final int vTmpValueOfReg = 4;
                    super.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpValueOfReg)));

                    super.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpValueOfReg),
                                    DexRegister.makeLocalReg(vParaArrayReg),
                                    DexRegister.makeLocalReg(vArrayIdxReg)));
                }
                nextParaReg += (type.isWideType() ? 2 : 1);
            }
        }


        // step 2 : call $ic.invokeUninit(methodid, initContext)
        // method id
        int methodId = MethodIdAssigner.getMethodId(mDexMethodNode);

        DexConst.LiteralBits32 methodConstant = DexConst.LiteralBits32.make(methodId);

        int vMethodIdReg = 2;
        super.visitConstInsn(
                Dops.CONST_16,
                DexRegisterList.make(DexRegister.makeLocalReg(vMethodIdReg)),
                methodConstant);

        DexConst.ConstMethodRef unInitMethod = mFactory.interceptableClass.invokeUnInitMethod;

        super.visitConstInsn(Dops.INVOKE_INTERFACE,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mInterceptorReg),
                        DexRegister.makeLocalReg(vMethodIdReg),
                        DexRegister.makeLocalReg(vInitContextReg)),
                unInitMethod);

        int vTmpFlagReg = 3;
        int vTmpFlagCompareReg = 4;
        super.visitConstInsn(Dops.IGET,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vTmpFlagReg),
                        DexRegister.makeLocalReg(vInitContextReg)),
                mFactory.initContextClass.flagField);

        // if (flag & Constant.INIT_CONTEXT_FLAG_INTERCEPTED) == 0) ，goto :origin
        super.visitConstInsn(Dops.AND_INT_LIT8,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vTmpFlagCompareReg),
                        DexRegister.makeLocalReg(vTmpFlagReg)),
                DexConst.LiteralBits32.make(Constant.INIT_CONTEXT_FLAG_INTERCEPTED));

        super.visitTargetInsn(Dops.IF_EQZ,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vTmpFlagCompareReg)),
                mOriginLabel);

        // if (flag & Constant.INIT_CONTEXT_FLAG_BUDDY) != 0) ，goto : callBuddyInit
        super.visitConstInsn(Dops.AND_INT_LIT8,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vTmpFlagCompareReg),
                        DexRegister.makeLocalReg(vTmpFlagReg)),
                DexConst.LiteralBits32.make(Constant.INIT_CONTEXT_FLAG_BUDDY));

        int pThisReg = 0;

        // call default <init> method
        // TODO 后继考虑支持通过InitContext中的参数选择其他this或super的构造函数
        visitCallInit(pThisReg, vInitContextReg, mInitMethodRef);

        // InitContext.thisArg = this
        super.visitConstInsn(Dops.IPUT_OBJECT, DexRegisterList.make(
                DexRegister.makeParameterReg(pThisReg),
                DexRegister.makeLocalReg(vInitContextReg)),
                mFactory.initContextClass.thisArgField);

        // call $ic.invokeInitBody(methodId, initContext)
        super.visitConstInsn(Dops.INVOKE_INTERFACE,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mInterceptorReg),
                        DexRegister.makeLocalReg(vMethodIdReg),
                        DexRegister.makeLocalReg(vInitContextReg)),
                mFactory.interceptableClass.invokeInitBodyMethod);

        super.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        super.visitEnd();
    }

    /**
     * 调用this.<init>或者super.<init>
     *
     * @param pThisReg
     * @param vInitContextReg
     * @param initMethodRef
     */
    private void visitCallInit(int pThisReg,
                               int vInitContextReg,
                               DexConst.ConstMethodRef initMethodRef) {

        DexTypeList initMethodParaTypes = initMethodRef.getParameterTypes();
        final int callInitRegCount = 1 + initMethodParaTypes.count();

        DexRegisterList.Builder callInitRegListBuilder = DexRegisterList.newBuilder();

        int nextCallInitParaReg = 5;

        boolean callInitRange = false;

        if (callInitRegCount <= 5) {
            callInitRegListBuilder.addReg(DexRegister.makeParameterReg(pThisReg));
        } else { // invoke-range, regs require continuous
            callInitRange = true;
            // this obj
            super.visitSimpleInsn(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(nextCallInitParaReg),
                            DexRegister.makeParameterReg(pThisReg)));
            callInitRegListBuilder.addReg(DexRegister.makeLocalReg(nextCallInitParaReg));
            nextCallInitParaReg++;
        }

        // 从InitContext.callArgs中取出参数，最好必要的拆箱(unbox)工作
        if (initMethodParaTypes.count() > 0) {
            final int vTmpCallArgReg = 3;
            super.visitConstInsn(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpCallArgReg),
                            DexRegister.makeLocalReg(vInitContextReg)),
                    mFactory.initContextClass.callArgsField);

            final int vCallArgIdxReg = 4;
            for (int initParaIdx = 0; initParaIdx < initMethodParaTypes.count(); initParaIdx++) {
                DexType type = initMethodParaTypes.getType(initParaIdx);
                super.visitConstInsn(Dops.CONST_16,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vCallArgIdxReg)),
                        DexConst.LiteralBits32.make(initParaIdx));

                super.visitSimpleInsn(Dops.AGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(nextCallInitParaReg),
                                DexRegister.makeLocalReg(vTmpCallArgReg),
                                DexRegister.makeLocalReg(vCallArgIdxReg)));

                // callArgs的类型是Object[]，通过aget-object之后的类型是Object，需要做一下check-cast
                DexType checkCastType = type.isPrimitiveType()
                        ? mFactory.boxTypes.getBoxedTypeForPrimitiveType(type) : type;
                super.visitConstInsn(Dops.CHECK_CAST,
                        DexRegisterList.make(DexRegister.makeLocalReg(nextCallInitParaReg)),
                        DexConst.ConstType.make(checkCastType));

                if (type.isPrimitiveType()) {
                    super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(nextCallInitParaReg)),
                            mFactory.methods.primitiveValueMethodForType(type));

                    super.visitSimpleInsn(mFactory.dops.getMoveResultOpForType(type).opcode,
                            DexRegisterList.make(
                                    DexRegister.makeLocalRegWithWide(
                                            nextCallInitParaReg, type.isWideType())));
                }

                callInitRegListBuilder.addReg(
                        DexRegister.makeLocalRegWithWide(nextCallInitParaReg, type.isWideType()));

                /** reg num is 0-based */
                nextCallInitParaReg += (type.isWideType() ? 2 : 1);
            }
        }

        // call this or super <init> methods
        super.visitConstInsn(callInitRange ? Dops.INVOKE_DIRECT_RANGE : Dops.INVOKE_DIRECT,
                callInitRegListBuilder.build(), initMethodRef);
    }

    @Override
    public void visitRegisters(int localRegCount, int parameterRegCount) {
    }
}
