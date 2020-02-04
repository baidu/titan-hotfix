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
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 * BuddyGenerator子类，其中的方法生成采用的策略是：调用Changed类同名方法
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/21
 */
public class CallChangedBuddyGenerator extends BuddyGenerator {

    public DexType changeType;

    public CallChangedBuddyGenerator(DexType genesisType,
                                     DexType buddyType,
                                     DexType buddySuperType,
                                     DexType changeType,
                                     DexTypeList buddyInterfaces,
                                     TitanDexItemFactory factory) {
        super(genesisType, buddyType, buddySuperType, buddyInterfaces, factory);
        this.changeType = changeType;
    }


    @Override
    protected void generateForOneMethod(DexClassNode classNode,
                                        DexClassVisitor classVisitor,
                                        DexNamedProtoNode method) {
        DexMethodVisitor methodVisitor = classVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        buddyType,
                        method.name,
                        method.parameters,
                        method.returnType,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC)));

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                method.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        int vReturnResReg = 0;
        int pOrgThisReg = 0;
        int pGenesisThisReg = 0;
        codeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeParameterReg(pGenesisThisReg),
                        DexRegister.makeParameterReg(pOrgThisReg)),
                DexConst.ConstFieldRef.make(
                        buddyType,
                        genesisType,
                        factory.buddyClass.genesisObjFieldName));

        DexRegisterList.Builder callChangedRegBuilder = DexRegisterList.newBuilder();

        DexTypeList.Builder callChangedTypeListBuilder = DexTypeList.newBuilder();

        int nextCallChangeReg = pOrgThisReg;
        callChangedRegBuilder.addReg(DexRegister.makeParameterReg(nextCallChangeReg++));
        callChangedTypeListBuilder.addType(genesisType);

        for (int i = 0; i < method.parameters.count(); i++) {
            DexType paraType = method.parameters.getType(i);
            callChangedTypeListBuilder.addType(paraType);
            switch (paraType.toTypeDescriptor().charAt(0)) {
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    callChangedRegBuilder.addReg(
                            DexRegister.makeDoubleParameterReg(nextCallChangeReg));
                    nextCallChangeReg += 2;
                    break;
                }
                default: {
                    callChangedRegBuilder.addReg(DexRegister.makeParameterReg(nextCallChangeReg));
                    nextCallChangeReg += 1;
                    break;
                }
            }
        }

        DexTypeList callChangeParaTypes = callChangedTypeListBuilder.build();

        codeVisitor.visitConstInsn(
                Dops.INVOKE_STATIC_RANGE,
                callChangedRegBuilder.build(),
                DexConst.ConstMethodRef.make(
                        changeType,
                        method.name,
                        method.returnType,
                        callChangeParaTypes));

        switch (method.returnType.toShortDescriptor()) {
            case DexItemFactory.BooleanClass.SHORT_DESCRIPTOR:
            case DexItemFactory.ByteClass.SHORT_DESCRIPTOR:
            case DexItemFactory.ShortClass.SHORT_DESCRIPTOR:
            case DexItemFactory.CharacterClass.SHORT_DESCRIPTOR:
            case DexItemFactory.IntegerClass.SHORT_DESCRIPTOR:
            case DexItemFactory.FloatClass.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));

                codeVisitor.visitSimpleInsn(
                        Dops.RETURN,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));
                break;
            }
            case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
            case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT_WIDE,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));

                codeVisitor.visitSimpleInsn(
                        Dops.RETURN_WIDE,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));
                break;
            }
            case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
            case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));

                codeVisitor.visitSimpleInsn(
                        Dops.RETURN_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vReturnResReg)));
                break;
            }
            case DexItemFactory.VoidClass.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.RETURN_VOID,
                        DexRegisterList.EMPTY);
                break;
            }
            default:
                break;
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }
}
