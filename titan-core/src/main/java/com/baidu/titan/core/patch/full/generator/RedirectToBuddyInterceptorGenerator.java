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
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.Comparator;

/**
 *
 * 将组件中已经重写的方法定向到Buddy类中
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/21
 */
public class RedirectToBuddyInterceptorGenerator extends InterceptorGenerator {

    public DexType buddyType;

    public DexType genesisType;

    public RedirectToBuddyInterceptorGenerator(DexType orgType, DexType interceptorType,
                                               DexType buddyType, DexType genesisType,
                                               TitanDexItemFactory factory,
                                               DexClassNode instrumentedClassNode) {
        super(orgType, interceptorType, factory, instrumentedClassNode);
        this.buddyType = buddyType;
        this.genesisType = genesisType;
    }

    @Override
    protected void generateForInvokeInit(DexClassNode interceptorClassNode,
                                         DexClassVisitor classVisitor, InvokeMethod invokeMethod) {
        // do noting
        //
    }

    @Override
    protected void generateForInvokeSpecial(DexClassNode interceptorClassNode,
                           DexClassVisitor classVisitor, InvokeMethod invokeMethod) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorType,
                factory.createString(invokeMethod.getInvokeMethodName()),
                invokeMethod.getInvokeParameters(),
                factory.interceptResultClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(methodVisitorInfo);

        invokeMethod.interceptedMethods.sort(Comparator.comparingInt(InterceptedMethod::getMethodId));

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        int paraRegCount = 1 + 2;
        for (int i = 0; i < invokeMethod.invokeKindName.length(); i++) {
            switch (invokeMethod.invokeKindName.charAt(i)) {
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    paraRegCount += 2;
                    break;
                }
                default: {
                    paraRegCount++;
                }
            }
        }

        codeVisitor.visitRegisters(4, paraRegCount);


        int vInterceptResReg = 0;
        int vBuddyReg = 1;

        int pMethodReg = 1;
        int pThisReg = 2;

        int vTmpResRegMayPair = 2;

        DexLabel returnNullLabel = new DexLabel();

        // get intercept result
        codeVisitor.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.EMPTY,
                factory.titanRuntimeClass.getThreadInterceptResultMethod);
        codeVisitor.visitSimpleInsn(
                Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        // check cast thisObj type
        codeVisitor.visitConstInsn(
                Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisReg)),
                DexConst.ConstType.make(genesisType));

        // call $getClassBuddy
//        codeVisitor.visitConstInsn(
//                DexConstant.Opcodes.INVOKE_VIRTUAL,
//                DexRegisterList.make(DexRegister.makeParameterReg(pThisReg)),
//                DexConst.ConstMethodRef.make(
//                        orgType,
//                        factory.createString(TitanConstant.METHOD_NAME_GET_CLASS_BUDDY),
//                        factory.createType(TitanConstant.TYPE_CLASS_BUDDY),
//                        DexTypeList.empty()));
//        codeVisitor.visitSimpleInsn(
//                DexConstant.Opcodes.MOVE_RESULT_OBJECT,
//                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)));

        // getClassBuddy for genesisType's $cb
        codeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vBuddyReg),
                        DexRegister.makeParameterReg(pThisReg)),
                DexConst.ConstFieldRef.make(
                        genesisType,
                        factory.objectClass.type,
                        factory.genesisClass.buddyObjFiledName));
        codeVisitor.visitTargetInsn(
                Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                returnNullLabel);


        codeVisitor.visitConstInsn(
                Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                DexConst.ConstType.make(buddyType));

        // do dispatch
        int[] switchKeys = new int[invokeMethod.interceptedMethods.size()];
        DexLabel[] switchLabels = new DexLabel[invokeMethod.interceptedMethods.size()];
        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            switchKeys[i] = invokeMethod.interceptedMethods.get(i).getMethodId();
            switchLabels[i] = new DexLabel();
        }

        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodReg)),
                switchKeys,
                switchLabels);

        DexLabel returnLabel = new DexLabel();

        codeVisitor.visitLabel(returnLabel);

        codeVisitor.visitSimpleInsn(
                Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        // switchLabels

        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            InterceptedMethod interceptedMethod = invokeMethod.interceptedMethods.get(i);
            codeVisitor.visitLabel(switchLabels[i]);

            DexRegisterList.Builder callBuddyRegsBuilder = DexRegisterList.newBuilder();

            boolean callBuddyRange =
                    1 + Utils.calParameterRegCount(interceptedMethod.method.parameters) > 4;
            if (callBuddyRange) {
                int pBuddyThisReg = pThisReg;
                codeVisitor.visitSimpleInsn(
                        Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeParameterReg(pBuddyThisReg),
                                DexRegister.makeLocalReg(vBuddyReg)));
                callBuddyRegsBuilder.addReg(DexRegister.makeParameterReg(pBuddyThisReg));

            } else {
                callBuddyRegsBuilder.addReg(DexRegister.makeLocalReg(vBuddyReg));
            }


            final int pFirstParaReg = 3;

            if (callBuddyRange) {
                int nextParaReg = pFirstParaReg;
                int nextClassBuddyReg = 3;

                for (DexType type: interceptedMethod.method.parameters.types()) {
                    switch(type.toTypeDescriptor().charAt(0)) {
                        case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                        case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                            codeVisitor.visitSimpleInsn(
                                    Dops.MOVE_WIDE,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(nextClassBuddyReg),
                                            DexRegister.makeParameterReg(nextParaReg)));

                            callBuddyRegsBuilder.addReg(
                                    DexRegister.makeDoubleLocalReg(nextClassBuddyReg));
                            nextClassBuddyReg += 2;
                            nextParaReg += 2;
                            break;
                        }
                        case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                        case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                            codeVisitor.visitSimpleInsn(
                                    Dops.MOVE_WIDE,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(nextClassBuddyReg),
                                            DexRegister.makeParameterReg(nextParaReg)));

                            codeVisitor.visitConstInsn(
                                    Dops.CHECK_CAST,
                                    DexRegisterList.make(DexRegister.makeLocalReg(nextClassBuddyReg)),
                                    DexConst.ConstType.make(type));

                            callBuddyRegsBuilder.addReg(
                                    DexRegister.makeDoubleLocalReg(nextClassBuddyReg));

                            nextClassBuddyReg ++;
                            nextParaReg ++;
                            break;
                        }
                        default: {
                            codeVisitor.visitSimpleInsn(
                                    Dops.MOVE,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(nextClassBuddyReg),
                                            DexRegister.makeParameterReg(nextParaReg)));

                            callBuddyRegsBuilder.addReg(DexRegister.makeLocalReg(nextClassBuddyReg));
                            nextClassBuddyReg++;
                            nextParaReg++;
                            break;

                        }
                    }
                }
            } else {
                int nextParaReg = pFirstParaReg;

                for (DexType type: interceptedMethod.method.parameters.types()) {
                    switch(type.toTypeDescriptor().charAt(0)) {
                        case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                        case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {

                            callBuddyRegsBuilder.addReg(
                                    DexRegister.makeDoubleParameterReg(nextParaReg));
                            nextParaReg += 2;
                            break;
                        }
                        case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                        case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {

                            codeVisitor.visitConstInsn(
                                    Dops.CHECK_CAST,
                                    DexRegisterList.make(DexRegister.makeParameterReg(nextParaReg)),
                                    DexConst.ConstType.make(type));

                            callBuddyRegsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg));

                            nextParaReg ++;
                            break;
                        }
                        default: {

                            callBuddyRegsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg));

                            nextParaReg++;
                            break;
                        }
                    }
                }
            }


            // call buddy's method

            codeVisitor.visitConstInsn(
                    callBuddyRange ? Dops.INVOKE_VIRTUAL_RANGE : Dops.INVOKE_VIRTUAL,
                    callBuddyRegsBuilder.build(),
                    DexConst.ConstMethodRef.make(buddyType,
                            interceptedMethod.method.name,
                            interceptedMethod.method.returnType,
                            interceptedMethod.method.parameters));


            DexType returnType = interceptedMethod.method.returnType;

            // vTmpResRegMayPair
            switch (returnType.toTypeDescriptor().charAt(0)) {
                case DexItemFactory.BooleanClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));

                    codeVisitor.visitConstInsn(
                            Dops.IPUT_BOOLEAN,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.booleanValueField);
                    break;
                }
                case DexItemFactory.ByteClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_BYTE,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.byteValueField);
                    break;
                }
                case DexItemFactory.ShortClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_SHORT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.shortValueField);
                    break;
                }
                case DexItemFactory.CharacterClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_CHAR,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.charValueField);
                    break;
                }
                case DexItemFactory.IntegerClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.intValueField);
                    break;
                }
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_WIDE,
                            DexRegisterList.make(DexRegister.makeDoubleLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_WIDE,
                            DexRegisterList.make(
                                    DexRegister.makeDoubleLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.longValueField);
                    break;
                }
                case DexItemFactory.FloatClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.floatValueField);
                    break;
                }
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_WIDE,
                            DexRegisterList.make(DexRegister.makeDoubleLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeDoubleLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.doubleValueField);
                    break;
                }
                case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.objValueField);
                    break;
                }

            }
            codeVisitor.visitTargetInsn(Dops.GOTO, DexRegisterList.EMPTY, returnLabel);

            // if buddy is null, then return null
            codeVisitor.visitLabel(returnNullLabel);
            codeVisitor.visitConstInsn(
                    Dops.CONST_4,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)),
                    DexConst.LiteralBits32.make(0));
            codeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }

    @Override
    protected void generateForInvokeCommon(DexClassNode interceptorClassNode,
                           DexClassVisitor classVisitor, InvokeMethod invokeMethod) {

        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorType,
                factory.createString(invokeMethod.getInvokeMethodName()),
                invokeMethod.getInvokeParameters(),
                factory.interceptResultClass.type,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(methodVisitorInfo);

        invokeMethod.interceptedMethods.sort(Comparator.comparingInt(InterceptedMethod::getMethodId));

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();

        int paraRegCount = 1 + 2;
        for (int i = 0; i < invokeMethod.invokeKindName.length(); i++) {
            switch (invokeMethod.invokeKindName.charAt(i)) {
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    paraRegCount += 2;
                    break;
                }
                default: {
                    paraRegCount++;
                }
            }
        }

        codeVisitor.visitRegisters(4, paraRegCount);


        int vInterceptResReg = 0;
        int vTmpArgIdxReg = 1;
        int vBuddyReg = 2;
        int vTmpResRegMayPair = 1;

        int pMethodReg = 1;
        int pThisReg = 2;
        int pArgsArrayReg = 3;

        DexLabel returnNullLabel = new DexLabel();

        // get intercept result
        codeVisitor.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.EMPTY,
                factory.titanRuntimeClass.getThreadInterceptResultMethod);
        codeVisitor.visitSimpleInsn(
                Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        // check cast thisObj type
        codeVisitor.visitConstInsn(
                Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisReg)),
                DexConst.ConstType.make(genesisType));

        // call $getClassBuddy
//        codeVisitor.visitConstInsn(
//                DexConstant.Opcodes.INVOKE_VIRTUAL,
//                DexRegisterList.make(DexRegister.makeParameterReg(pThisReg)),
//                DexConst.ConstMethodRef.make(
//                        orgType,
//                        factory.createString(TitanConstant.METHOD_NAME_GET_CLASS_BUDDY),
//                        factory.createType(TitanConstant.TYPE_CLASS_BUDDY),
//                        DexTypeList.empty()));
//        codeVisitor.visitSimpleInsn(
//                DexConstant.Opcodes.MOVE_RESULT_OBJECT,
//                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)));

        // getClassBuddy for genesisType's $cb
        codeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vBuddyReg),
                        DexRegister.makeParameterReg(pThisReg)),
                DexConst.ConstFieldRef.make(
                        genesisType,
                        factory.objectClass.type,
                        factory.genesisClass.buddyObjFiledName));

        codeVisitor.visitTargetInsn(
                Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                returnNullLabel);


        codeVisitor.visitConstInsn(
                Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                DexConst.ConstType.make(buddyType));

        // do dispatch
        int[] switchKeys = new int[invokeMethod.interceptedMethods.size()];
        DexLabel[] switchLabels = new DexLabel[invokeMethod.interceptedMethods.size()];
        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            switchKeys[i] = invokeMethod.interceptedMethods.get(i).getMethodId();
            switchLabels[i] = new DexLabel();
        }

        codeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeParameterReg(pMethodReg)),
                switchKeys,
                switchLabels);

        DexLabel returnLabel = new DexLabel();

        codeVisitor.visitLabel(returnLabel);

        codeVisitor.visitSimpleInsn(
                Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));

        // switchLabels

        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            InterceptedMethod interceptedMethod = invokeMethod.interceptedMethods.get(i);
            codeVisitor.visitLabel(switchLabels[i]);

            int vNextCallBuddyReg = vBuddyReg;

            DexRegisterList.Builder callBuddyRegsBuilder = DexRegisterList.newBuilder();

            callBuddyRegsBuilder.addReg(DexRegister.makeLocalReg(vNextCallBuddyReg++));

            int nextArgsIdx = 0;
            for (DexType type : interceptedMethod.method.parameters.types()) {
                codeVisitor.visitConstInsn(
                        Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vTmpArgIdxReg)),
                        DexConst.LiteralBits32.make(nextArgsIdx));

                codeVisitor.visitSimpleInsn(
                        Dops.AGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallBuddyReg),
                                DexRegister.makeParameterReg(pArgsArrayReg),
                                DexRegister.makeLocalReg(vTmpArgIdxReg)));
                int width = 1;
                switch (type.toTypeDescriptor().charAt(0)) {
                    case DexItemFactory.BooleanClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.booleanClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.booleanClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.ByteClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.byteClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.byteClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.ShortClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.shortClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.shortClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.CharacterClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.characterClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.characterClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.IntegerClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.integerClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.integerClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.LongClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.longClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.longClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        width++;
                        break;
                    }
                    case DexItemFactory.FloatClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.floatClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.floatClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        break;
                    }
                    case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(factory.doubleClass.boxedType));
                        codeVisitor.visitConstInsn(
                                Dops.INVOKE_VIRTUAL,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                factory.doubleClass.primitiveValueMethod);
                        codeVisitor.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)));
                        width++;
                        break;
                    }
                    case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                    case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                        codeVisitor.visitConstInsn(
                                Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCallBuddyReg)),
                                DexConst.ConstType.make(type));

                        break;
                    }
                    default: {
                        break;
                    }
                }

                callBuddyRegsBuilder.addReg(width > 1 ?
                        DexRegister.makeDoubleLocalReg(vNextCallBuddyReg) :
                        DexRegister.makeLocalReg(vNextCallBuddyReg));

//                nextCallBuddyRegIdx++;
                nextArgsIdx++;
                vNextCallBuddyReg += width;
            }


            // call buddy's method

            codeVisitor.visitConstInsn(
                    Dops.INVOKE_VIRTUAL_RANGE,
                    callBuddyRegsBuilder.build(),
                    DexConst.ConstMethodRef.make(buddyType,
                            interceptedMethod.method.name,
                            interceptedMethod.method.returnType,
                            interceptedMethod.method.parameters));

            DexType returnType = interceptedMethod.method.returnType;

            // vTmpResRegMayPair
            switch (returnType.toShortDescriptor()) {
                case DexItemFactory.BooleanClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_BOOLEAN,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.booleanValueField);
                    break;
                }
                case DexItemFactory.ByteClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_BYTE,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.byteValueField);
                    break;
                }
                case DexItemFactory.ShortClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_SHORT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.shortValueField);
                    break;
                }
                case DexItemFactory.CharacterClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_CHAR,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.charValueField);
                    break;
                }
                case DexItemFactory.IntegerClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.intValueField);
                    break;
                }
                case DexItemFactory.LongClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_WIDE,
                            DexRegisterList.make(DexRegister.makeDoubleLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_WIDE,
                            DexRegisterList.make(
                                    DexRegister.makeDoubleLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.longValueField);
                    break;
                }
                case DexItemFactory.FloatClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.floatValueField);
                    break;
                }
                case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_WIDE,
                            DexRegisterList.make(DexRegister.makeDoubleLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT,
                            DexRegisterList.make(
                                    DexRegister.makeDoubleLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.doubleValueField);
                    break;
                }
                case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                    codeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vTmpResRegMayPair)));
                    codeVisitor.visitConstInsn(
                            Dops.IPUT_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vTmpResRegMayPair),
                                    DexRegister.makeLocalReg(vInterceptResReg)),
                            factory.interceptResultClass.objValueField);
                    break;
                }

            }
            codeVisitor.visitTargetInsn(
                    Dops.GOTO, DexRegisterList.EMPTY, returnLabel);

            // if buddy is null, then return null
            codeVisitor.visitLabel(returnNullLabel);
            codeVisitor.visitConstInsn(
                    Dops.CONST_4,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)),
                    DexConst.LiteralBits32.make(0));
            codeVisitor.visitSimpleInsn(
                    Dops.RETURN_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vInterceptResReg)));
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();
    }


}
