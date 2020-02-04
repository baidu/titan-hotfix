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
import com.baidu.titan.core.util.TitanHashs;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexLabel;

/**
 * 处理<clinit>函数相关逻辑
 *
 * @author zhangdi07@baidu.com
 * @since 2018/12/06
 */
public class StaticInitMethodTransformation extends DexCodeVisitor {

    private DexMethodNode mDexMethodNode;

    private DexClassNode mDexClassNode;

    private DexLabel mInterceptLabel;

    private DexLabel mOriginLabel;

    private int mVClassClinitInterceptorReg = 0;

    private TitanDexItemFactory mFactory;

    private static final int MIN_LOCAL_REG_COUNT = 4;

    public StaticInitMethodTransformation(DexCodeVisitor delegate,
                                          DexClassNode dexClassNode,
                                          DexMethodNode dexMethodNode,
                                          TitanDexItemFactory factory) {
        super(delegate);
        this.mDexClassNode = dexClassNode;
        this.mDexMethodNode = dexMethodNode;
        this.mFactory = factory;
    }

    @Override
    public void visitBegin() {
        super.visitConstInsn(
                Dops.SGET_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(mVClassClinitInterceptorReg)),
                mFactory.classClinitInterceptorStorageClass.interceptorField);
        mInterceptLabel = new DexLabel();
        super.visitTargetInsn(
                Dops.IF_NEZ,
                DexRegisterList.make(DexRegister.makeLocalReg(mVClassClinitInterceptorReg)),
                mInterceptLabel);
        mOriginLabel = new DexLabel();
        super.visitLabel(mOriginLabel);
    }

    @Override
    public void visitRegisters(int localRegCount, int parameterRegCount) {
        super.visitRegisters(Math.max(localRegCount, MIN_LOCAL_REG_COUNT), parameterRegCount);
    }

    @Override
    public void visitEnd() {
        super.visitLabel(mInterceptLabel);

        String typeDesc = this.mDexClassNode.type.toTypeDescriptor();
        // 使用自定义String的hashCode算法，防止jdk不同版本的String.hashCode()方法实现不同
        int typeDescHashCode = TitanHashs.type2HashCode(this.mDexClassNode.type);

        final int vHashCodeReg = 1;
        final int vTypeDescReg = 2;
        final int vClassInterceptorReg = 3;
        final int vInterceptResultReg = 4;
        final int vFlagReg = 5;

        super.visitConstInsn(
                Dops.CONST_4,
                DexRegisterList.make(DexRegister.makeLocalReg(vHashCodeReg)),
                DexConst.LiteralBits32.make(typeDescHashCode));
        super.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vTypeDescReg)),
                DexConst.ConstString.make(typeDesc));

        super.visitConstInsn(
                Dops.INVOKE_INTERFACE,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mVClassClinitInterceptorReg),
                        DexRegister.makeLocalReg(vHashCodeReg),
                        DexRegister.makeLocalReg(vTypeDescReg)),
                mFactory.classClinitInterceptableClass.invokeClinitMethod);

        super.visitSimpleInsn(
                Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)));
        // 如果invokeClinit返回的InterceptResult为null，则返回label:origin
        super.visitTargetInsn(
                Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResultReg)),
                mOriginLabel);

        DexLabel compareFlagLabel = new DexLabel();
        // 如果InterceptResult.interceptor不为null，则赋值到本类的$ic变量中
        super.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vClassInterceptorReg),
                        DexRegister.makeLocalReg(vInterceptResultReg)),
                mFactory.interceptResultClass.interceptorField);
        super.visitTargetInsn(Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vClassInterceptorReg)),
                compareFlagLabel);

        super.visitConstInsn(
                Dops.SPUT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vClassInterceptorReg)),
                DexConst.ConstFieldRef.make(
                        mDexClassNode.type,
                        mFactory.interceptableClass.type,
                        mFactory.instrumentedClass.interceptorFieldName));

        super.visitLabel(compareFlagLabel);

        super.visitConstInsn(
                Dops.IGET,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFlagReg),
                        DexRegister.makeLocalReg(vInterceptResultReg)),
                mFactory.interceptResultClass.flagsField);

        // if (flag & Constant.INTERCEPT_RESULT_FLAG_INTERCEPTED) == 0) ，goto :origin
        // 这里判断INTERCEPT_RESULT_FLAG_INTERCEPTED如果不等于0，则再次调用postInvokeClinit方法，
        // 进行clinit方法更新
        super.visitConstInsn(
                Dops.AND_INT_LIT8,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFlagReg),
                        DexRegister.makeLocalReg(vFlagReg)),
                DexConst.LiteralBits32.make(Constant.INTERCEPT_RESULT_FLAG_INTERCEPTED));

        super.visitTargetInsn(
                Dops.IF_EQZ,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFlagReg)),
                mOriginLabel);

        super.visitConstInsn(
                Dops.INVOKE_INTERFACE,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(mVClassClinitInterceptorReg),
                        DexRegister.makeLocalReg(vHashCodeReg),
                        DexRegister.makeLocalReg(vTypeDescReg)),
                mFactory.classClinitInterceptableClass.invokePostClinitMethod);

        super.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
        super.visitEnd();
    }

}
