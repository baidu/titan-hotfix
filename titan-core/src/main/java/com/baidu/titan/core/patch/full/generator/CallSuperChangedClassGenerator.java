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

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 *
 * 用于生成Change类，重写行为，用于调用Super类同名方法
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/21
 */
public class CallSuperChangedClassGenerator extends ChangedClassGenerator {


    public CallSuperChangedClassGenerator(DexType orgType,
                                          DexType changeType,
                                          DexType changeSuperType,
                                          DexItemFactory factory) {
        super(orgType, changeType, changeSuperType, factory);
    }

    @Override
    protected void generateForOneMethod(DexClassNode classNode,
                                        DexClassVisitor classVisitor,
                                        DexNamedProtoNode method) {

        DexTypeList.Builder staticParameterTypesBuilder = DexTypeList.newBuilder();

        DexRegisterList.Builder callSuperRegListBuilder = DexRegisterList.newBuilder();

        int nextRegIdx = 0;
        int nextCallSuperReg = 0;

        staticParameterTypesBuilder.addType(orgType);
        callSuperRegListBuilder.addReg(DexRegister.makeParameterReg(nextCallSuperReg++));
        nextRegIdx++;

        for (int i = 0; i < method.parameters.count(); i++) {
            DexType paraType = method.parameters.getType(i);

            staticParameterTypesBuilder.addType(paraType);

            switch (paraType.toTypeDescriptor().charAt(0)) {
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    callSuperRegListBuilder.addReg(
                            DexRegister.makeDoubleParameterReg(nextCallSuperReg));
                    nextCallSuperReg += 2;
                    break;
                }
                default: {
                    callSuperRegListBuilder.addReg(
                            DexRegister.makeParameterReg(nextCallSuperReg));
                    nextCallSuperReg++;
                    break;
                }
            }
            nextRegIdx++;
        }

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        changeType,
                        method.name,
                        staticParameterTypesBuilder.build(),
                        method.returnType,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC)));

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                true,
                staticParameterTypesBuilder.build(),
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        codeVisitor.visitConstInsn(
                Dops.INVOKE_SUPER_RANGE,
                callSuperRegListBuilder.build(),
                DexConst.ConstMethodRef.make(
                        changeSuperType,
                        method.name,
                        method.returnType,
                        method.parameters));

        int vResultReg = 0;

        switch (method.returnType.toShortDescriptor()) {
            case DexItemFactory.VoidClass.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());
                break;
            }
            case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
            case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                codeVisitor.visitSimpleInsn(
                        Dops.RETURN_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                break;
            }
            case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR:
            case DexItemFactory.LongClass.SHORT_DESCRIPTOR: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT_WIDE,
                        DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                codeVisitor.visitSimpleInsn(
                        Dops.RETURN_WIDE,
                        DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                break;
            }
            default: {
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_RESULT,
                        DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                codeVisitor.visitSimpleInsn(
                        Dops.RETURN,
                        DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                break;
            }
        }


        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }
}
