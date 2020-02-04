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

package com.baidu.titan.core.patch.common;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;

/**
 *
 * 反射
 *
 * @author zhangdi07@baidu.com
 * @since 2018/9/9
 */
public class TitanReflectionHelper {

    private boolean mStaticMethod;

    private DexConst.ConstMethodRef mCurrentCalledMethodRef;

    private DexRegisterList mCalledRegs;

    private DexCodeVisitor mDelegate;

    private TitanDexItemFactory mDexItemFactory;

    private int mLocalRegCount;

    private int STATE_REFLECT_INVOKE_NONE = 0;

    private int STATE_REFLECT_INVOKE_WAIT_FOR_MOVE_RESULT = 1;

    private int mReflectInvokeStatus = STATE_REFLECT_INVOKE_NONE;

    public TitanReflectionHelper(TitanDexItemFactory dexItemFactory, DexCodeVisitor delegate) {
        this.mDexItemFactory = dexItemFactory;
        mDelegate = delegate;
    }

    public void reflectMethodInvoke(int op,
                                    DexConst.ConstMethodRef calledMethodRef,
                                    DexRegisterList calledRegs) {
        Dop dop = Dops.dopFor(op);
        mStaticMethod = dop.isInvokeStatic();
        mCurrentCalledMethodRef = calledMethodRef;
        mCalledRegs = calledRegs;

        if (mStaticMethod) {
            reflectInvokeStaticMethod(calledMethodRef, calledRegs);
        } else {
            reflectInvokeInstanceMethod(calledMethodRef, calledRegs);
        }
        mReflectInvokeStatus = STATE_REFLECT_INVOKE_WAIT_FOR_MOVE_RESULT;
    }

    public void reflectFieldAccess(int op, DexConst.ConstFieldRef fieldRef, DexRegisterList regs) {
        Dop dop = Dops.dopFor(op);
        assert dop.isFieldAccessKind();
        if (dop.isFieldStaticGet() || dop.isFieldInstanceGet()) {
            reflectFieldGet(op, fieldRef, regs);
        } else {
            reflectFieldPut(op, fieldRef, regs);
        }
    }

    private void reflectFieldGet(int op, DexConst.ConstFieldRef fieldRef, DexRegisterList regs) {
        Dop dop = Dops.dopFor(op);
        boolean staticGet = dop.isFieldStaticGet();
        // sstaticop vAA, field@BBBB
        // A: value register or pair; may be source or dest (8 bits)
        // B: static field reference index (16 bits)
        // ===========
        // iinstanceop vA, vB, field@CCCC
        // A: value register or pair; may be source or dest (4 bits)
        // B: object register (4 bits)
        // C: instance field reference index (16 bits)

        DexRegister resultReg = regs.get(0);
        DexType fieldType = fieldRef.getType();
        DexRegister resultObjectReg = resultReg;
        if (fieldType.isWideType()) {
            resultObjectReg = resultReg.isLocalReg() ?
                    DexRegister.makeLocalReg(resultReg.getReg()) :
                    DexRegister.makeParameterReg(resultReg.getReg());
        }

        final int vTargetClassReg = mLocalRegCount;
        final int vFieldNameReg = mLocalRegCount + 1;
        final int vNullTargetObjectReg = mLocalRegCount + 2;

        DexRegisterList.Builder callGetFieldRegsBuilder = DexRegisterList.newBuilder();
        if (staticGet) {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vNullTargetObjectReg)),
                    DexConst.LiteralBits32.make(0));
            callGetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vNullTargetObjectReg));
        } else {
            callGetFieldRegsBuilder.addReg(regs.get(1));
        }
        // const-class vTargetClass type
        mDelegate.visitConstInsn(
                Dops.CONST_CLASS,
                DexRegisterList.make(DexRegister.makeLocalReg(vTargetClassReg)),
                DexConst.ConstType.make(fieldRef.getOwner()));
        callGetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vTargetClassReg));

        // const-string vFieldName fieldName
        mDelegate.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldNameReg)),
                DexConst.ConstString.make(fieldRef.getName().toString()));
        callGetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vFieldNameReg));

        // invoke-static vObject vTargetClass vFieldName TitanRuntime.getField
        mDelegate.visitConstInsn(
                Dops.INVOKE_STATIC,
                callGetFieldRegsBuilder.build(),
                mDexItemFactory.titanRuntimeClass.getFieldMethod);

        // move-result-object vResult
        mDelegate.visitSimpleInsn(
                Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(resultObjectReg));

        if (fieldType.isArrayType() || fieldType.isReferenceType()) {
            mDelegate.visitConstInsn(
                    Dops.CHECK_CAST,
                    DexRegisterList.make(resultObjectReg),
                    DexConst.ConstType.make(fieldType));
        } else if (fieldType.isWideType()) {
            mDelegate.visitConstInsn(
                    Dops.CHECK_CAST,
                    DexRegisterList.make(resultObjectReg),
                    DexConst.ConstType.make(
                            mDexItemFactory.boxTypes.getBoxedTypeForPrimitiveType(fieldType)));
            mDelegate.visitConstInsn(
                    Dops.INVOKE_VIRTUAL,
                    DexRegisterList.make(resultObjectReg),
                    mDexItemFactory.methods.primitiveValueMethodForType(fieldType));
            mDelegate.visitSimpleInsn(
                    Dops.MOVE_RESULT_WIDE,
                    DexRegisterList.make(resultReg));
        } else {
            mDelegate.visitConstInsn(
                    Dops.CHECK_CAST,
                    DexRegisterList.make(resultObjectReg),
                    DexConst.ConstType.make(
                            mDexItemFactory.boxTypes.getBoxedTypeForPrimitiveType(fieldType)));
            mDelegate.visitConstInsn(
                    Dops.INVOKE_VIRTUAL,
                    DexRegisterList.make(resultObjectReg),
                    mDexItemFactory.methods.primitiveValueMethodForType(fieldType));
            mDelegate.visitSimpleInsn(
                    Dops.MOVE_RESULT,
                    DexRegisterList.make(resultReg));
        }
    }

    private void reflectFieldPut(int op, DexConst.ConstFieldRef fieldRef, DexRegisterList regs) {
        Dop dop = Dops.dopFor(op);
        boolean staticPut = dop.isFieldStaticPut();
        // sstaticop vAA, field@BBBB
        // A: value register or pair; may be source or dest (8 bits)
        // B: static field reference index (16 bits)
        // ===========
        // iinstanceop vA, vB, field@CCCC
        // A: value register or pair; may be source or dest (4 bits)
        // B: object register (4 bits)
        // C: instance field reference index (16 bits)

        DexRegister valueReg = regs.get(0);
        DexType fieldType = fieldRef.getType();

        final int vTargetClassReg = mLocalRegCount;
        final int vFieldNameReg = mLocalRegCount + 1;
        final int vNullTargetObjectReg = mLocalRegCount + 2;
        final int vValueObjectReg = mLocalRegCount + 3;

        DexRegisterList.Builder callSetFieldRegsBuilder = DexRegisterList.newBuilder();
        if (staticPut) {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vNullTargetObjectReg)),
                    DexConst.LiteralBits32.make(0));
            callSetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vNullTargetObjectReg));
        } else {
            callSetFieldRegsBuilder.addReg(regs.get(1));
        }

        // 基本类型转成boxed类型
        DexRegister valueObjectReg = valueReg;
        if (fieldType.isPrimitiveType()) {
            valueObjectReg = DexRegister.makeLocalReg(vValueObjectReg);
            mDelegate.visitConstInsn(
                    Dops.INVOKE_STATIC,
                    DexRegisterList.make(valueReg),
                    mDexItemFactory.methods.valueOfMethodForType(fieldType));
            mDelegate.visitSimpleInsn(
                    Dops.MOVE_RESULT_OBJECT,
                    DexRegisterList.make(valueObjectReg));
        }

        callSetFieldRegsBuilder.addReg(valueObjectReg);

        mDelegate.visitConstInsn(
                Dops.CONST_CLASS,
                DexRegisterList.make(DexRegister.makeLocalReg(vTargetClassReg)),
                DexConst.ConstType.make(fieldRef.getOwner()));
        callSetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vTargetClassReg));

        mDelegate.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldNameReg)),
                DexConst.ConstString.make(fieldRef.getName().toString()));
        callSetFieldRegsBuilder.addReg(DexRegister.makeLocalReg(vFieldNameReg));

        mDelegate.visitConstInsn(
                Dops.INVOKE_STATIC,
                callSetFieldRegsBuilder.build(),
                mDexItemFactory.titanRuntimeClass.setFieldMethod);
    }

    private void reflectInvokeInstanceMethod(DexConst.ConstMethodRef calledMethodRef,
                                           DexRegisterList calledRegs) {
        DexRegister thisOrgReg = calledRegs.get(0);

        final int vIndexReg = mLocalRegCount;
        final int vParamArraySizeReg = mLocalRegCount;

        final int vTmpStoreMayPairReg = mLocalRegCount + 1;
        // reverse
        final int vParamsReg =  mLocalRegCount + 3;
        final int vParameterTypesReg = mLocalRegCount + 4;
        final int vMethodNameReg = mLocalRegCount + 5;

        int paraSize = calledMethodRef.getParameterTypes().count();
        if (paraSize == 0) {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParamsReg)),
                    DexConst.LiteralBits32.make(0));

            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParameterTypesReg)),
                    DexConst.LiteralBits32.make(0));
        } else {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.LiteralBits32.make(paraSize));

            // new Class[paramArraySize]
            mDelegate.visitConstInsn(
                    Dops.NEW_ARRAY,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParamsReg),
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.ConstType.make(
                            mDexItemFactory.createArrayType(mDexItemFactory.objectClass.type)));

            // new Class[paramArraySize]
            mDelegate.visitConstInsn(
                    Dops.NEW_ARRAY,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParameterTypesReg),
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.ConstType.make(
                            mDexItemFactory.createArrayType(mDexItemFactory.classClass.type)));

            for (int i = 0; i < paraSize; i++) {
                DexType paraType = calledMethodRef.getParameterTypes().getType(i);

                mDelegate.visitConstInsn(
                        Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vIndexReg)),
                        DexConst.LiteralBits32.make(i));

                DexRegister orgParaReg = calledRegs.get(i + 1);

                if (paraType.isPrimitiveType()) {
                    DexConst.ConstMethodRef valueOfMethod =
                            mDexItemFactory.methods.valueOfMethodForType(paraType);
                    mDelegate.visitConstInsn(
                            Dops.INVOKE_STATIC,
                            DexRegisterList.make(orgParaReg),
                            valueOfMethod);
                    mDelegate.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpStoreMayPairReg)));
                    mDelegate.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpStoreMayPairReg),
                                    DexRegister.makeLocalReg(vParamsReg),
                                    DexRegister.makeLocalReg(vIndexReg)));

                } else {
                    mDelegate.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    orgParaReg,
                                    DexRegister.makeLocalReg(vParamsReg),
                                    DexRegister.makeLocalReg(vIndexReg)));
                }


                mDelegate.visitConstInsn(
                        Dops.CONST_CLASS,
                        DexRegisterList.make(DexRegister.makeLocalReg(vTmpStoreMayPairReg)),
                        DexConst.ConstType.make(paraType));

                // arrayop vAA, vBB, vCC
                // A: value register or pair; may be source or dest (8 bits)
                // B: array register (8 bits)
                // C: index register (8 bits)

                mDelegate.visitSimpleInsn(
                        Dops.APUT_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vTmpStoreMayPairReg),
                                DexRegister.makeLocalReg(vParameterTypesReg),
                                DexRegister.makeLocalReg(vIndexReg)));
            }

        }
        mDelegate.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(DexRegister.makeLocalReg(vMethodNameReg)),
                DexConst.ConstString.make(calledMethodRef.getName().toString()));

        mDelegate.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.make(
                        thisOrgReg,
                        DexRegister.makeLocalReg(vParamsReg),
                        DexRegister.makeLocalReg(vParameterTypesReg),
                        DexRegister.makeLocalReg(vMethodNameReg)),
                mDexItemFactory.titanRuntimeClass.invokeInstanceMethod);
    }

    private void reflectInvokeStaticMethod(DexConst.ConstMethodRef calledMethodRef,
                                         DexRegisterList calledRegs) {
        final int vIndexReg = mLocalRegCount;
        final int vParamArraySizeReg = mLocalRegCount;

        final int vTmpStoreMayPairReg = mLocalRegCount + 1;
        // reverse
        final int vReceiverClassReg = mLocalRegCount + 3;
        final int vParamsReg =  mLocalRegCount + 4;
        final int vParameterTypesReg = mLocalRegCount + 5;
        final int vMethodNameReg = mLocalRegCount + 6;

        mDelegate.visitConstInsn(
                Dops.CONST_CLASS,
                DexRegisterList.make(DexRegister.makeLocalReg(vReceiverClassReg)),
                DexConst.ConstType.make(calledMethodRef.getOwner()));

        int paraSize = calledMethodRef.getParameterTypes().count();
        if (paraSize == 0) {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParamsReg)),
                    DexConst.LiteralBits32.make(0));

            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParameterTypesReg)),
                    DexConst.LiteralBits32.make(0));
        } else {
            mDelegate.visitConstInsn(
                    Dops.CONST,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.LiteralBits32.make(paraSize));

            mDelegate.visitConstInsn(
                    Dops.NEW_ARRAY,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParamsReg),
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.ConstType.make(
                            mDexItemFactory.createArrayType(mDexItemFactory.objectClass.type)));

            mDelegate.visitConstInsn(
                    Dops.NEW_ARRAY,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParameterTypesReg),
                            DexRegister.makeLocalReg(vParamArraySizeReg)),
                    DexConst.ConstType.make(
                            mDexItemFactory.createArrayType(mDexItemFactory.classClass.type)));

            for (int i = 0; i < paraSize; i++) {
                DexType paraType = calledMethodRef.getParameterTypes().getType(i);

                mDelegate.visitConstInsn(
                        Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vIndexReg)),
                        DexConst.LiteralBits32.make(i));

                DexRegister orgParaReg = calledRegs.get(i);

                if (paraType.isPrimitiveType()) {
                    DexConst.ConstMethodRef valueOfMethod =
                            mDexItemFactory.methods.valueOfMethodForType(paraType);
                    mDelegate.visitConstInsn(
                            Dops.INVOKE_STATIC,
                            DexRegisterList.make(orgParaReg),
                            valueOfMethod);
                    mDelegate.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpStoreMayPairReg)));
                    mDelegate.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpStoreMayPairReg),
                                    DexRegister.makeLocalReg(vParamsReg),
                                    DexRegister.makeLocalReg(vIndexReg)));

                } else {
                    mDelegate.visitSimpleInsn(
                            Dops.APUT_OBJECT,
                            DexRegisterList.make(
                                    orgParaReg,
                                    DexRegister.makeLocalReg(vParamsReg),
                                    DexRegister.makeLocalReg(vIndexReg)));
                }


                mDelegate.visitConstInsn(
                        Dops.CONST_CLASS,
                        DexRegisterList.make(DexRegister.makeLocalReg(vTmpStoreMayPairReg)),
                        DexConst.ConstType.make(paraType));

                // arrayop vAA, vBB, vCC
                // A: value register or pair; may be source or dest (8 bits)
                // B: array register (8 bits)
                // C: index register (8 bits)

                mDelegate.visitSimpleInsn(
                        Dops.APUT_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vTmpStoreMayPairReg),
                                DexRegister.makeLocalReg(vParameterTypesReg),
                                DexRegister.makeLocalReg(vIndexReg)));

            }

            mDelegate.visitConstInsn(
                    Dops.CONST_STRING,
                    DexRegisterList.make(DexRegister.makeLocalReg(vMethodNameReg)),
                    DexConst.ConstString.make(calledMethodRef.getName().toString()));

            mDelegate.visitConstInsn(
                    Dops.INVOKE_STATIC,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vReceiverClassReg),
                            DexRegister.makeLocalReg(vParamsReg),
                            DexRegister.makeLocalReg(vParameterTypesReg),
                            DexRegister.makeLocalReg(vMethodNameReg)),
                    mDexItemFactory.titanRuntimeClass.invokeInstanceMethod);
        }

    }

    private boolean visitInsn(int op, DexRegisterList regs) {
        if (mReflectInvokeStatus == STATE_REFLECT_INVOKE_WAIT_FOR_MOVE_RESULT) {
            try {
                switch (op) {
                    case Dops.MOVE_RESULT:
                    case Dops.MOVE_RESULT_OBJECT:
                    case Dops.MOVE_RESULT_WIDE: {
                        DexRegister resultReg = regs.get(0);
                        DexType returnType = mCurrentCalledMethodRef.getReturnType();
                        if (returnType.isVoidType()) {
                            throw new IllegalStateException("void return type, but move-result found");
                        } else if (returnType.isReferenceType() || returnType.isArrayType()) {
                            mDelegate.visitSimpleInsn(
                                    Dops.MOVE_RESULT_OBJECT,
                                    DexRegisterList.make(resultReg));
                            mDelegate.visitConstInsn(
                                    Dops.CHECK_CAST,
                                    DexRegisterList.make(resultReg),
                                    DexConst.ConstType.make(returnType));
                        } else if (returnType.isWideType()) {
                            DexRegister singleResultReg =
                                    resultReg.isLocalReg() ?
                                            DexRegister.makeLocalReg(resultReg.getReg()):
                                            DexRegister.makeParameterReg(resultReg.getReg());

                            mDelegate.visitSimpleInsn(
                                    Dops.MOVE_RESULT_OBJECT,
                                    DexRegisterList.make(singleResultReg));
                            mDelegate.visitConstInsn(
                                    Dops.CHECK_CAST,
                                    DexRegisterList.make(singleResultReg),
                                    DexConst.ConstType.make(
                                            mDexItemFactory.boxTypes
                                                    .getBoxedTypeForPrimitiveType(returnType)));
                            mDelegate.visitConstInsn(
                                    Dops.INVOKE_VIRTUAL,
                                    DexRegisterList.make(singleResultReg),
                                    mDexItemFactory.methods.primitiveValueMethodForType(returnType));
                            mDelegate.visitSimpleInsn(op, regs);
                        } else {
                            mDelegate.visitSimpleInsn(
                                    Dops.MOVE_RESULT_OBJECT,
                                    DexRegisterList.make(resultReg));
                            mDelegate.visitConstInsn(
                                    Dops.CHECK_CAST,
                                    DexRegisterList.make(resultReg),
                                    DexConst.ConstType.make(
                                            mDexItemFactory.boxTypes
                                                    .getBoxedTypeForPrimitiveType(returnType)));
                            mDelegate.visitConstInsn(
                                    Dops.INVOKE_VIRTUAL,
                                    DexRegisterList.make(resultReg),
                                    mDexItemFactory.methods.primitiveValueMethodForType(returnType));
                            mDelegate.visitSimpleInsn(op, regs);
                        }
                        return true;
                    }
                    default:
                        break;
                }
            } finally {
                mReflectInvokeStatus = STATE_REFLECT_INVOKE_NONE;
                mCurrentCalledMethodRef = null;
                mCalledRegs = null;
            }
        }

        return false;
    }

    public boolean visitRegisters(int localRegCount, int parameterRegCount) {
        mLocalRegCount = localRegCount;
        return false;
    }

    /**
     * 访问try catch结构体
     *
     * @param start
     * @param end
     * @param types
     * @param handlers
     */
    public boolean visitTryCatch(DexLabel start, DexLabel end, DexTypeList types,
                              DexLabel[] handlers, DexLabel catchAllHandler) {
        return false;
    }

    /**
     * 访问代码标签信息
     *
     * @param label
     */
    public boolean visitLabel(DexLabel label) {
        return false;
    }

    /**
     * 访问常量信息，包括字符串，基本类型等常量
     *
     * @param op
     * @param regs
     * @param dexConst
     */
    public boolean visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
        return visitInsn(op, regs);
    }

    /**
     * 访问跳转指令信息
     *
     * @param op
     * @param regs
     * @param label
     */
    public boolean visitTargetInsn(int op, DexRegisterList regs, DexLabel label) {
        return visitInsn(op, regs);
    }

    /**
     * 访问简单指令信息
     *
     * @param op
     * @param regs
     */
    public boolean visitSimpleInsn(int op, DexRegisterList regs) {
        return visitInsn(op, regs);
    }

    public boolean visitSwitch(int op, DexRegisterList regs, int[] keys, DexLabel[] targets) {
        return visitInsn(op, regs);
    }

}

