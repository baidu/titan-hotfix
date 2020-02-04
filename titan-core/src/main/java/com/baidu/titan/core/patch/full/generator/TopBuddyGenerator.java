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

package com.baidu.titan.core.patch.full.generator;

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
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;


/**
 *
 * 用于生成Buddy类，在GenesisBuddy类的<init>方法中，
 * 完成genesis$buddy.$genesisObj 和 genesis.$buddyObj的互相赋值
 *
 * @author zhangdi07@baidu.com
 * @since 2017/12/6
 */
public class TopBuddyGenerator extends CallChangedBuddyGenerator {

    public TopBuddyGenerator(DexType genesisType, DexType buddyType, DexType buddySuperType,
                             DexType changeType, DexTypeList buddyInterfaces,
                             TitanDexItemFactory factory) {
        super(genesisType, buddyType, buddySuperType, changeType, buddyInterfaces, factory);
    }

    @Override
    protected void generateForInitMethod(DexClassNode classNode, DexClassVisitor classVisitor,
                                         DexNamedProtoNode method) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                buddyType,
                factory.methods.initMethodName,
                method.parameters,
                factory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_CONSTRUCTOR));

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        codeVisitor.visitRegisters(0,
                Utils.calParameterRegCount(method.parameters) + 1);

        DexRegisterList.Builder regsBuilder = DexRegisterList.newBuilder();

        int nextParaReg = 0;
        regsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg++));
        for (DexType paraType : method.parameters) {
            switch (paraType.toShortDescriptor()) {
                case 'J':
                case 'D': {
                    regsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg));
                    nextParaReg += 2;
                    break;
                }
                default: {
                    regsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg));
                    nextParaReg++;
                    break;
                }
            }
        }

        codeVisitor.visitConstInsn(Dops.INVOKE_DIRECT_RANGE, regsBuilder.build(),
                DexConst.ConstMethodRef.make(
                        buddySuperType,
                        factory.methods.initMethodName,
                        factory.voidClass.primitiveType,
                        method.parameters));

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

    }

    private void generateForPreInitMethod(DexClassNode classNode, DexClassVisitor classVisitor,
                                          DexNamedProtoNode method) {
        DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
        paraTypeBuilder.addType(this.buddyType);
        for (DexType dexType : method.parameters.types()) {
            paraTypeBuilder.addType(dexType);
        }

        // 在参数最后，追加BuddyInitContext参数
        paraTypeBuilder.addType(factory.buddyInitContextClass.type);

        DexTypeList initParaTypes = paraTypeBuilder.build();

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(
                new DexMethodVisitorInfo(buddyType,
                        factory.titanMethods.preInitMethodName,
                        initParaTypes,
                        factory.voidClass.primitiveType,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC)));

        methodVisitor.visitBegin();

        DexCodeVisitor codeVisitor = methodVisitor.visitCode();

        codeVisitor.visitBegin();

        codeVisitor.visitRegisters(0, Utils.calParameterRegCount(initParaTypes));


        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

    }

//    private void generateForInitBodyMethod(DexClassNode classNode, DexClassVisitor classVisitor,
//                                           DexNamedProtoNode method) {
//        DexTypeList.Builder paraTypeBuilder = DexTypeList.newBuilder();
//        for (DexType dexType : method.parameters.types()) {
//            paraTypeBuilder.addType(dexType);
//        }
//
//        // 在参数最后，追加BuddyInitContext参数
//        paraTypeBuilder.addType(
//                factory.createType(TitanConstant.BuddyInitContext.TYPE_DESC));
//
//        DexTypeList initParaTypes = paraTypeBuilder.build();
//
//        DexMethodVisitor methodVisitor = classVisitor.visitMethod(
//                new DexMethodVisitorInfo(buddyType,
//                        factory.createString("<init>"),
//                        initParaTypes,
//                        factory.voidClass.primitiveType,
//                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC
//                                | DexAccessFlags.ACC_CONSTRUCTOR)));
//
//        methodVisitor.visitBegin();
//
//        DexCodeVisitor codeVisitor = methodVisitor.visitCode();
//
//        codeVisitor.visitBegin();
//
//        codeVisitor.visitRegisters(1, 2);
//
//        int pThisBuddyObjReg = 0;
//
//        int vGenesisObjReg = 0;
//
//        int pBuddyInitContextReg = Utils.calParameterRegCount(initParaTypes) - 1;
//
//        // 父类是java.lang.Object, 调用父类的<init>方法
//        codeVisitor.visitConstInsn(DexConstant.Opcodes.INVOKE_DIRECT,
//                DexRegisterList.make(DexRegister.makeParameterReg(pThisBuddyObjReg)),
//                DexConst.ConstMethodRef.make(
//                        buddySuperType,
//                        factory.createString("<init>"),
//                        factory.voidClass.primitiveType,
//                        DexTypeList.empty()));
//
//
//        // vGenesisObjReg = pBuddyInitContextReg.genesisObj
//        codeVisitor.visitConstInsn(DexConstant.Opcodes.IGET_OBJECT,
//                DexRegisterList.make(
//                        DexRegister.makeLocalReg(vGenesisObjReg),
//                        DexRegister.makeParameterReg(pBuddyInitContextReg)),
//                factory.buddyInitContextClass.genesisObjField);
//
//        codeVisitor.visitConstInsn(DexConstant.Opcodes.CHECK_CAST,
//                DexRegisterList.make(DexRegister.makeLocalReg(vGenesisObjReg)),
//                DexConst.ConstType.make(genesisType));
//
//        // genesisComponent.$buddyObj = this;
//        codeVisitor.visitConstInsn(DexConstant.Opcodes.IPUT_OBJECT,
//                DexRegisterList.make(
//                        DexRegister.makeParameterReg(pThisBuddyObjReg),
//                        DexRegister.makeLocalReg(vGenesisObjReg)),
//                DexConst.ConstFieldRef.make(
//                        genesisType,
//                        factory.objectClass.type,
//                        factory.createString(TitanConstant.GenesisClass.FIELD_NAME_BUDDY_OBJ)));
//
//        // this.$genesisObj = genesisComponent
//        codeVisitor.visitConstInsn(DexConstant.Opcodes.IPUT_OBJECT,
//                DexRegisterList.make(
//                        DexRegister.makeLocalReg(vGenesisObjReg),
//                        DexRegister.makeParameterReg(pThisBuddyObjReg)),
//                DexConst.ConstFieldRef.make(
//                        buddyType,
//                        genesisType,
//                        factory.createString(TitanConstant.BuddyClass.FIELD_NAME_GENESIS_OBJ)));
//
//        codeVisitor.visitSimpleInsn(DexConstant.Opcodes.RETURN_VOID, DexRegisterList.EMPTY);
//
//        codeVisitor.visitEnd();
//
//        methodVisitor.visitEnd();
//    }

}
