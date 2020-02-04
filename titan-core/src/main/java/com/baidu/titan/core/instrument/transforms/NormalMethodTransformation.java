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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;

import java.util.Set;

/**
 * deal with normal method (except <init> and <clinit> method
 *
 *
 *  @author zhangdi07@baidu.com
 *  @since 2018/12/10
 *
 */
public class NormalMethodTransformation extends DexCodeVisitor {

    private DexMethodNode mDexMethodNode;

    private DexClassNode mDexClassNode;

    private DexLabel mInterceptLabel;

    private DexLabel mOriginLabel;

    private TitanDexItemFactory mFactory;

    private int mParamRegCount;

    private int mLocalRegCount;

    private static final int MIN_LOCAL_REG_COUNT = 4;

    private int mInterceptorReg = 0;

    private final Set<String> mSpecialParameterSet;

    public NormalMethodTransformation(DexCodeVisitor delegate,
                                      DexClassNode dexClassNode,
                                      DexMethodNode dexMethodNode,
                                      TitanDexItemFactory factory,
                                      Set<String> specialParameterSet) {
        super(delegate);
        this.mDexClassNode = dexClassNode;
        this.mDexMethodNode = dexMethodNode;
        this.mFactory = factory;
        this.mSpecialParameterSet = specialParameterSet;
    }

    @Override
    public void visitBegin() {
        super.visitConstInsn(Dops.SGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mInterceptorReg)),
                DexConst.ConstFieldRef.make(
                        mDexClassNode.type,
                        mFactory.interceptableClass.type,
                        mFactory.instrumentedClass.interceptorFieldName));
        mInterceptLabel = new DexLabel();
        super.visitTargetInsn(Dops.IF_NEZ,
                DexRegisterList.make(DexRegister.makeLocalReg(mInterceptorReg)),
                mInterceptLabel);
        mOriginLabel = new DexLabel();
        super.visitLabel(mOriginLabel);
    }

    @Override
    public void visitRegisters(int localRegCount, int parameterRegCount) {

        this.mParamRegCount = parameterRegCount;
        this.mLocalRegCount = Math.max(localRegCount, MIN_LOCAL_REG_COUNT);

        super.visitRegisters(this.mLocalRegCount, this.mParamRegCount);
    }

    /**
     * invoke prototype:
     * <pre>
     * InterceptResult invokeCommon(int methodId, Object thisObj, Object[] args);
     * </pre>
     *
     * <pre>
     * v0             v1           v2          v3     ...  vn  p0  ... pm
     * [invoke this] [method id] [this]  [para array]
     * </pre>
     *
     */
    private void writeCommonInterceptor() {
        boolean staticMethod = mDexMethodNode.accessFlags.containsOneOf(
                DexAccessFlags.ACC_STATIC);
        DexTypeList paraTypes = mDexMethodNode.parameters;
        int paraArraySize = paraTypes.count();

        // 设置参数数组Size
        int vArraySizeReg = 3;
        super.visitConstInsn(Dops.CONST_16,
                DexRegisterList.make(DexRegister.makeLocalReg(vArraySizeReg)),
                DexConst.LiteralBits32.make(paraArraySize));

        int vParaArrayReg = 3;
        super.visitConstInsn(Dops.NEW_ARRAY, DexRegisterList.make(
                DexRegister.makeLocalReg(vParaArrayReg),
                DexRegister.makeLocalReg(vArraySizeReg)),
                DexConst.ConstType.make(mFactory.createArrayType(mFactory.objectClass.type)));

        if (paraTypes.count() > 0) {
            int nextParaReg = staticMethod ? 0 : 1;
            for (int paraIdx = 0; paraIdx < paraTypes.count(); paraIdx++) {
                DexType type = paraTypes.getType(paraIdx);
                DexConst.ConstMethodRef valueOfMethodRef =
                        type.isPrimitiveType() ?
                                mFactory.methods.valueOfMethodForType(type) : null;
                int vArrayIdxReg = 1;
                // idx
                super.visitConstInsn(Dops.CONST_16,
                        DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                        DexConst.LiteralBits32.make(paraIdx));
                if (valueOfMethodRef == null) {
                    // 对基本类型，要通过其包装类的valueOf方法进行装箱(box)
                    super.visitSimpleInsn(Dops.APUT_OBJECT, DexRegisterList.make(
                            DexRegister.makeParameterReg(nextParaReg),
                            DexRegister.makeLocalReg(vParaArrayReg),
                            DexRegister.makeLocalReg(vArrayIdxReg)));
                } else {
                    //
                    DexRegisterList valueOfRegs = DexRegisterList.make(type.isWideType() ?
                            DexRegister.makeDoubleParameterReg(nextParaReg) :
                            DexRegister.makeParameterReg(nextParaReg));

                    super.visitConstInsn(Dops.INVOKE_STATIC, valueOfRegs, valueOfMethodRef);
                    int vTmpValueOfReg = 2;
                    super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpValueOfReg)));

                    super.visitSimpleInsn(Dops.APUT_OBJECT, DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpValueOfReg),
                            DexRegister.makeLocalReg(vParaArrayReg),
                            DexRegister.makeLocalReg(vArrayIdxReg)));
                }
                nextParaReg += (type.isWideType() ? 2 : 1);
            }
        }

        // method id
        int methodId = MethodIdAssigner.getMethodId(mDexMethodNode);

        DexConst.LiteralBits32 methodConstant = DexConst.LiteralBits32.make(methodId);

        int vMethodIdReg = 1;
        super.visitConstInsn(Dops.CONST_16,
                DexRegisterList.make(DexRegister.makeLocalReg(vMethodIdReg)),
                methodConstant);

        // cur this obj
        final int vParaThisReg = 2;
        if (staticMethod) {
            // static方法，this obj reg设置为null
            super.visitConstInsn(Dops.CONST_4,
                    DexRegisterList.make(DexRegister.makeLocalReg(vParaThisReg)),
                    DexConst.LiteralBits32.make(0));
        } else {
            int pParaThisReg = 0;
            super.visitSimpleInsn(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vParaThisReg),
                            DexRegister.makeParameterReg(pParaThisReg)));
        }

        DexConst.ConstMethodRef interceptMethod = mFactory.interceptableClass
                .invokeCommonMethod;

        super.visitConstInsn(Dops.INVOKE_INTERFACE_RANGE,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mInterceptorReg),
                        DexRegister.makeLocalReg(vMethodIdReg),
                        DexRegister.makeLocalReg(vParaThisReg),
                        DexRegister.makeLocalReg(vParaArrayReg)),
                interceptMethod);
    }

    /**
     * invoke prototype:
     * <pre>
     * InterceptResult invokeShortType(int methodId, Object thisObj, ...);
     * </pre>
     * example: a method
     *
     * <pre>
     * int bar(int , String value)
     * </pre>
     *
     * it's corresponding interceptor
     * <pre>
     * InterceptResult invokeIL..(int methodId, Object thisObj, int arg0, Object arg1);
     * </pre>
     *
     * for instance method:
     *
     * <pre>
     * v0 v1 v2   ...    vn-1           vn         p0     p1  ... pm
     *              [invoke this]  [method id]  [this]   [para regs]
     * </pre>
     *
     * for static method:
     * <pre>
     * v0 v1 v2   ...    vn-2           vn-1       vn         p0  ... pm
     *             [invoke this]  [method id]  [this=null]  [para regs]
     * </pre>
     *
     * @param shortParamDesc
     */
    private void writeSpecialInterceptor(String shortParamDesc) {
        // 最后一个local reg索引
        int vLastLocalReg = mLocalRegCount - 1;

        boolean staticMethod = mDexMethodNode.accessFlags.containsOneOf(
                DexAccessFlags.ACC_STATIC);

        DexTypeList parameterTypes = mDexMethodNode.parameters;

        // invoke方法原型是：
        // InterceptResult invokeLL..(int methodId, Object thisObj, Object arg0);
        // 这里static方法和instance方法的对应的invoke原型是一致的，只是thisObj参数为null
        // 如果当前方法是static方法，该方法参数寄存器个数中不包含this寄存器，
        // 所以静态方法相对于源方法寄存器个数需要多一个的寄存器
        // instance方法对应的invoke方法寄存器个数 = invokeThisObjReg + methodIdReg
        // + thisObjReg + methodReg(s) - thisObjReg = mParamRegSize + 2
        // static方法对应的invoke方法寄存器个数 = invokeThisObjReg + methodIdReg
        // + thisObjReg(null) + methodReg(s) = mParamRegSize + 3

        DexRegisterList.Builder invokeRegListBuilder = DexRegisterList.newBuilder();
        int pParaThisReg = 0;

        // step 1 : invoke method's this reg
        int vInvokeThisReg = staticMethod ? vLastLocalReg - 2 : vLastLocalReg - 1;
        invokeRegListBuilder.addReg(DexRegister.makeLocalReg(vInvokeThisReg));

        // 因为使用invoke-range opcode,要求参数寄存器是连续的,所以需要将mInterceptorReg的引用
        // 赋值到invokeThisReg中
        // 如果invokeThisReg == mInterceptorReg的话，则省去了move操作
        if (vInvokeThisReg != mInterceptorReg) {
            super.visitSimpleInsn(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vInvokeThisReg),
                            DexRegister.makeLocalReg(mInterceptorReg)));
        }

        // step 2 : invoke method's methodid para && reg
        int vMethodIdReg = vInvokeThisReg + 1;
        invokeRegListBuilder.addReg(DexRegister.makeLocalReg(vMethodIdReg));

        // prepare method id reg constant
        // 这里要使用当前方法的最终methodid，在该阶段还不能确定出来，需要最终生成dex的时候确定

        int methodId = MethodIdAssigner.getMethodId(mDexMethodNode);

        DexConst.LiteralBits32 methodConstant = DexConst.LiteralBits32.make(methodId);
        super.visitConstInsn(Dops.CONST_16,
                DexRegisterList.make(DexRegister.makeLocalReg(vMethodIdReg)), methodConstant);

        // step 3 : current method's this obj para && reg
        if (staticMethod) {
            DexRegister staticThisRegister = DexRegister.makeLocalReg(vMethodIdReg + 1);
            invokeRegListBuilder.addReg(staticThisRegister);

            // 如果是static方法的话，thisObj参数为null，按照虚拟机规范，也需要主动赋值为0
            super.visitConstInsn(Dops.CONST_16,
                    DexRegisterList.make(staticThisRegister), DexConst.LiteralBits32.make(0));
        } else {
            invokeRegListBuilder.addReg(DexRegister.makeParameterReg(pParaThisReg));
        }

        // step 4 : cur method paras & reg
        if (parameterTypes.count() > 0) {
            int pNextParaReg = staticMethod ? 0 : 1;
            for (int i = 0; i < parameterTypes.count(); i++) {
                DexType type = parameterTypes.getType(i);
                if (type.isArrayType() || type.isReferenceType()) {
                    type = mFactory.objectClass.type;
                }

                if (type.isWideType()) {
                    invokeRegListBuilder.addReg(DexRegister.makeDoubleParameterReg(pNextParaReg));
                    pNextParaReg += 2;
                } else {
                    invokeRegListBuilder.addReg(DexRegister.makeParameterReg(pNextParaReg));
                    pNextParaReg += 1;
                }
            }
        }

        DexConst.ConstMethodRef invokeSpecialMethod =
                mFactory.interceptableClass.getInvokeSpecialMethod(shortParamDesc);

        super.visitConstInsn(Dops.INVOKE_INTERFACE_RANGE,
                invokeRegListBuilder.build(), invokeSpecialMethod);
    }

    @Override
    public void visitEnd() {
        super.visitLabel(mInterceptLabel);

        StringBuilder shortDescBuilder = new StringBuilder();
        DexTypeList paraTypes = mDexMethodNode.parameters;
        if (paraTypes.count() == 0) {
            shortDescBuilder.append(DexItemFactory.VoidClass.SHORT_DESCRIPTOR);
        } else {
            for (DexType paraType : paraTypes.types()) {
                char shortType = paraType.toShortDescriptor();
                // 对于数组来说，这里统一归类为Object引用类型
                if (shortType == DexItemFactory.ArrayType.SHORT_DESCRIPTOR) {
                    shortType = DexItemFactory.ReferenceType.SHORT_DESCRIPTOR;
                }
                shortDescBuilder.append(shortType);
            }
        }

        String shortParaDesc = shortDescBuilder.toString();

        if (mSpecialParameterSet.contains(shortParaDesc)) {
            writeSpecialInterceptor(shortParaDesc);
        } else {
            writeCommonInterceptor();
        }

        final int vInterceptResReg = 0;
        // InterceptRes存储到reg 0上
        super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        // 如果InterceptRes为null， 则跳转到原始逻辑
        super.visitTargetInsn(Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)), mOriginLabel);

        // 否则，执行Intercept逻辑
        final int vRetValueReg = 1;

        DexType returnType = mDexMethodNode.returnType;

        DexRegister returnValueReg = null;
        if (!returnType.isVoidType()) {
            Dop fieldGetOp = mFactory.dops.getFieldGetOpForType(returnType, false);
            DexConst.ConstFieldRef valueField =
                    mFactory.interceptResultClass.getValueFieldForType(returnType);
            returnValueReg = returnType.isWideType() ?
                    DexRegister.makeDoubleLocalReg(vRetValueReg) :
                    DexRegister.makeLocalReg(vRetValueReg);

            super.visitConstInsn(fieldGetOp.opcode,
                    DexRegisterList.make(
                            returnValueReg,
                            DexRegister.makeLocalReg(vInterceptResReg)),
                    valueField);

            if (returnType.isArrayType() || returnType.isReferenceType()) {
                // 对于引用类型，如果返回值不是Object的话，需要做一次check-cast转换
                if (!mFactory.objectClass.type.equals(returnType)) {
                    super.visitConstInsn(Dops.CHECK_CAST,
                            DexRegisterList.make(DexRegister.makeLocalReg(vRetValueReg)),
                            DexConst.ConstType.make(returnType));
                }
            }
        }
        Dop returnOp = mFactory.dops.getReturnOpForType(returnType);
        DexRegisterList returnRegs = returnType.isVoidType() ?
                DexRegisterList.empty() :
                DexRegisterList.newBuilder().addReg(returnValueReg).build();
        super.visitSimpleInsn(returnOp.opcode, returnRegs);

        super.visitEnd();
    }
}
