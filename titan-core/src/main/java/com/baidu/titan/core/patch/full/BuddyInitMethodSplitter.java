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

package com.baidu.titan.core.patch.full;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.util.Utils;
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
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexMethodVisitor;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/4/16
 */
public class BuddyInitMethodSplitter {

    private DexClassNode mClassNode;

    private DexMethodNode initMethodNode;

    private TitanDexItemFactory mFactory;

    private SpliteInfo mSpliteInfo;

    private PatchBuddyClassHierarchy.BuddyClassHolder mBuddyClassHolder;

    public static final int RES_OK = 0;



    public BuddyInitMethodSplitter(DexClassNode classNode, DexMethodNode methodNode,
                                   TitanDexItemFactory factory,
                                   PatchBuddyClassHierarchy.BuddyClassHolder buddyClassHolder) {
        this.mClassNode = classNode;
        this.initMethodNode = methodNode;
        this.mFactory = factory;
        this.mBuddyClassHolder = buddyClassHolder;
    }

    private static class SpliteInfo {

        public DexMethodNode preInitMethodNode;

        public DexMethodNode initBodyMethodNode;

    }

    public DexMethodNode getPreInitMethod() {
        return mSpliteInfo.preInitMethodNode;
    }

    public DexMethodNode getInitBodyMethod() {
        return mSpliteInfo.initBodyMethodNode;
    }

    public int doSplit() {
        this.mSpliteInfo = new SpliteInfo();

        generateForPreInitMethod();
        generateForInitBodyMethod();

        return RES_OK;
    }


    private void generateForPreInitMethod() {

        DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
        paraTypeBuilder.addType(this.mClassNode.type);
        for (DexType dexType : this.initMethodNode.parameters.types()) {
            paraTypeBuilder.addType(dexType);
        }

        // 在参数最后，追加BuddyInitContext参数
        paraTypeBuilder.addType(mFactory.buddyInitContextClass.type);

        DexTypeList initParaTypes = paraTypeBuilder.build();

        DexMethodNode preInitMethodNode = new DexMethodNode(
                mFactory.titanMethods.preInitMethodName,
                this.mClassNode.type,
                initParaTypes,
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC));

        DexMethodVisitor methodVisitor = preInitMethodNode.asVisitor();

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                true,
                preInitMethodNode.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        final int paraRegCount = Utils.calParameterRegCount(initParaTypes);
        final int pBuddyInitContextReg = paraRegCount - 1;
        final int vLocalCountReg = 0;
        final int vParaCountReg = 1;

        codeVisitor.visitConstInsn(Dops.CONST,
                DexRegisterList.make(DexRegister.makeLocalReg(vLocalCountReg)),
                DexConst.LiteralBits32.make(0));

        codeVisitor.visitConstInsn(Dops.CONST,
                DexRegisterList.make(DexRegister.makeLocalReg(vParaCountReg)),
                DexConst.LiteralBits32.make(0));

        codeVisitor.visitConstInsn(
                Dops.INVOKE_VIRTUAL,
                DexRegisterList.make(
                        DexRegister.makeParameterReg(pBuddyInitContextReg),
                        DexRegister.makeLocalReg(vLocalCountReg),
                        DexRegister.makeLocalReg(vParaCountReg)),
                mFactory.buddyInitContextClass.makeNextMethod);

        // TODO 存储local和parameter 寄存器信息

        // TODO choose method id

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

        mSpliteInfo.preInitMethodNode = preInitMethodNode;

    }

    private void generateForInitBodyMethod() {
        DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
        for (DexType dexType : this.initMethodNode.parameters.types()) {
            paraTypeBuilder.addType(dexType);
        }

        // 在参数最后，追加BuddyInitContext参数
        paraTypeBuilder.addType(mFactory.buddyInitContextClass.type);

        DexTypeList initParaTypes = paraTypeBuilder.build();

        DexMethodNode initBodyMethodNode = new DexMethodNode(
                mFactory.methods.initMethodName,
                this.mClassNode.type,
                initParaTypes,
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_CONSTRUCTOR));

        DexMethodVisitor methodVisitor = initBodyMethodNode.asVisitor();

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                initBodyMethodNode.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        int pThisBuddyObjReg = 0;

        int vGenesisObjReg = 0;

        int pBuddyInitContextReg = Utils.calParameterRegCount(initParaTypes);

        // 父类是java.lang.Object, 调用父类的<init>方法
        codeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisBuddyObjReg)),
                DexConst.ConstMethodRef.make(
                        // TODO ?
                        this.mClassNode.superType,
                        mFactory.methods.initMethodName,
                        mFactory.voidClass.primitiveType,
                        DexTypeList.empty()));


        // vGenesisObjReg = pBuddyInitContextReg.genesisObj
        codeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vGenesisObjReg),
                        DexRegister.makeParameterReg(pBuddyInitContextReg)),
                mFactory.buddyInitContextClass.genesisObjField);

        codeVisitor.visitConstInsn(
                Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeLocalReg(vGenesisObjReg)),
                DexConst.ConstType.make(mBuddyClassHolder.genesisType));

        // genesisComponent.$buddyObj = this;
        codeVisitor.visitConstInsn(
                Dops.IPUT_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeParameterReg(pThisBuddyObjReg),
                        DexRegister.makeLocalReg(vGenesisObjReg)),
                DexConst.ConstFieldRef.make(
                        this.mBuddyClassHolder.genesisType,
                        mFactory.objectClass.type,
                        mFactory.genesisClass.buddyObjFiledName));

        // this.$genesisObj = genesisComponent
        codeVisitor.visitConstInsn(
                Dops.IPUT_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vGenesisObjReg),
                        DexRegister.makeParameterReg(pThisBuddyObjReg)),
                DexConst.ConstFieldRef.make(
                        this.mBuddyClassHolder.buddyType,
                        this.mBuddyClassHolder.genesisType,
                        mFactory.buddyClass.genesisObjFieldName));

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

        this.mSpliteInfo.initBodyMethodNode = initBodyMethodNode;
    }

}
