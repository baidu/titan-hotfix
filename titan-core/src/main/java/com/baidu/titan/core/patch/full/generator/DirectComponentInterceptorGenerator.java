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
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 *
 * 除了具有RedirectToBuddyInterceptorGenerator的功能之外，额外需要处理构造函数的逻辑
 *
 * @author zhangdi07@baidu.com
 * @since 2017/12/5
 */
public class DirectComponentInterceptorGenerator extends RedirectToBuddyInterceptorGenerator {

    public DexType newDirectComponentType;

    public DirectComponentInterceptorGenerator(DexType orgType,
                                               DexType interceptorType,
                                               DexType buddyType,
                                               DexType newDirectComponentType,
                                               DexType genesisType,
                                               TitanDexItemFactory factory,
                                               DexClassNode instrumentedClassNode) {
        super(orgType, interceptorType, buddyType, genesisType, factory, instrumentedClassNode);
        this.newDirectComponentType = newDirectComponentType;
    }

    @Override
    protected void generateForInvokeInit(DexClassNode interceptorClassNode,
                                         DexClassVisitor classVisitor, InvokeMethod invokeMethod) {

        if (invokeMethod.interceptedMethods.size() != 1) {
            throw new IllegalStateException();
        }

        // invokeUnInit
        generateForUnInit(interceptorClassNode, classVisitor, invokeMethod);

        // invokeInitBody
        generateForInitBody(interceptorClassNode, classVisitor, invokeMethod);

    }



    private void generateForUnInit(DexClassNode interceptorClassNode,
                                   DexClassVisitor classVisitor, InvokeMethod invokeMethod) {
        DexMethodVisitorInfo invokeUninitMethodInfo = new DexMethodVisitorInfo(
                this.interceptorType,
                factory.interceptableClass.invokeUnInitMethod.getName(),
                factory.createTypesVariable(
                        factory.integerClass.primitiveType,
                        factory.initContextClass.type),
                factory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor invokeUnInitMethod = classVisitor.visitMethod(invokeUninitMethodInfo);

        int pMethodReg = 1;

        int pInitContextReg = 2;

        int vBuddyInitContextReg = 0;

        int vTmpReg = 1;

        int vCallArgArrayReg = 2;

        int vCallPreInitNullThisReg = 3;

        invokeUnInitMethod.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                invokeUninitMethodInfo.parameters,
                new DexCodeFormatVerifier(invokeUnInitMethod.visitCode()));

        codeVisitor.visitBegin();

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
                switchKeys, switchLabels);

        DexLabel returnLabel = new DexLabel();

        codeVisitor.visitLabel(returnLabel);

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            InterceptedMethod interceptedMethod = invokeMethod.interceptedMethods.get(i);
            codeVisitor.visitLabel(switchLabels[i]);

            // BuddyInitContext buddyInitContext = new BuddyInitContext();
            codeVisitor.visitConstInsn(
                    Dops.NEW_INSTANCE,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstType.make(factory.buddyInitContextClass.type));

            codeVisitor.visitConstInsn(
                    Dops.INVOKE_DIRECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstMethodRef.make(
                            factory.buddyInitContextClass.type,
                            factory.methods.initMethodName,
                            factory.voidClass.primitiveType,
                            DexTypeList.empty()));

//            codeVisitor.visitConstInsn(
//                    Dops.IGET_OBJECT,
//                    DexRegisterList.make(
//                            DexRegister.makeLocalReg(vInitHolderReg),
//                            DexRegister.makeLocalReg(vBuddyInitContextReg)),
//                    DexConst.ConstFieldRef.make(
//                            factory.createType("Lcom/baidu/titan/runtime/BuddyInitContext;"),
//                            factory.createType("Lcom/baidu/titan/runtime/BuddyInitHolder;"),
//                            factory.createString("head")));

            // call directComponent's default preInit

            codeVisitor.visitConstInsn(Dops.CONST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vCallPreInitNullThisReg)),
                    DexConst.LiteralBits32.make(0));

            codeVisitor.visitConstInsn(
                    Dops.INVOKE_STATIC,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vCallPreInitNullThisReg),
                            DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstMethodRef.make(
                            newDirectComponentType,
                            factory.titanMethods.preInitMethodName,
                            factory.voidClass.primitiveType,
                            factory.createTypesVariable(
                                    newDirectComponentType,
                                    factory.buddyInitContextClass.type)));

            // TODO InitContext.flag |= 2;
            // 设置 initContext.flag = 2，使得call-super阶段可以直接调用父类的<init>(..., BuddyInitContext)方法
            codeVisitor.visitConstInsn(
                    Dops.CONST_4,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpReg)),
                    DexConst.LiteralBits32.make(2));

            codeVisitor.visitConstInsn(
                    Dops.IPUT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpReg),
                            DexRegister.makeParameterReg(pInitContextReg)),
                    factory.initContextClass.flagField);

            // setup for invoke-direct args
            // initContext.callArgs = new Object[] { buddyInitContext };
            codeVisitor.visitConstInsn(Dops.FILLED_NEW_ARRAY,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstType.make(factory.createArrayType(factory.objectClass.type)));
            codeVisitor.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vCallArgArrayReg)));

            codeVisitor.visitConstInsn(Dops.IPUT_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vCallArgArrayReg),
                            DexRegister.makeParameterReg(pInitContextReg)),
                    factory.initContextClass.callArgsField);
            // return
            codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        invokeUnInitMethod.visitEnd();
    }


    private void generateForInitBody(DexClassNode interceptorClassNode,
                                   DexClassVisitor classVisitor, InvokeMethod invokeMethod) {
        DexMethodVisitorInfo initBodyVisitorInfo = new DexMethodVisitorInfo(
                this.interceptorType,
                factory.interceptableClass.invokeInitBodyMethod.getName(),
                factory.createTypesVariable(
                        factory.integerClass.primitiveType,
                        factory.initContextClass.type),
                factory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));

        DexMethodVisitor invokeUnInitMethod = classVisitor.visitMethod(initBodyVisitorInfo);

        int pMethodReg = 1;

        int pInitContextReg = 2;

        int vBuddyInitContextReg = 0;

        int vTmpReg = 1;

        int vTmpCallArgsReg = 2;

        int vTmpThisObj = 2;

        int vBuddyReg = 3;

        invokeUnInitMethod.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                initBodyVisitorInfo.parameters,
                new DexCodeFormatVerifier(invokeUnInitMethod.visitCode()));

        codeVisitor.visitBegin();

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
                switchKeys, switchLabels);

        DexLabel returnLabel = new DexLabel();

        codeVisitor.visitLabel(returnLabel);

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        for (int i = 0; i < invokeMethod.interceptedMethods.size(); i++) {
            InterceptedMethod interceptedMethod = invokeMethod.interceptedMethods.get(i);
            codeVisitor.visitLabel(switchLabels[i]);

            // vBuddyInitContextReg = initContext(p0).callArgs[0]
            codeVisitor.visitConstInsn(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpCallArgsReg),
                            DexRegister.makeParameterReg(pInitContextReg)),
                    factory.initContextClass.callArgsField);

            codeVisitor.visitConstInsn(Dops.CONST_4,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpReg)),
                    DexConst.LiteralBits32.make(0));

            codeVisitor.visitSimpleInsn(Dops.AGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vBuddyInitContextReg),
                            DexRegister.makeLocalReg(vTmpCallArgsReg),
                            DexRegister.makeLocalReg(vTmpReg)));
            codeVisitor.visitConstInsn(Dops.CHECK_CAST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstType.make(factory.buddyInitContextClass.type));
            // vBuddyInitContextReg end

            // call BuddyInitContext.moveToFirst
            codeVisitor.visitConstInsn(Dops.INVOKE_VIRTUAL,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    factory.buddyInitContextClass.moveToFirstMethod);

            // BuddyInitContextReg.genesisObj = initContext.thisObj
            codeVisitor.visitConstInsn(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpThisObj),
                            DexRegister.makeParameterReg(pInitContextReg)),
                    factory.initContextClass.thisArgField);
            codeVisitor.visitConstInsn(Dops.IPUT_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpThisObj),
                            DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    factory.buddyInitContextClass.genesisObjField);
            // end

            // new NewDirectComponent(buddyInitContext)
            codeVisitor.visitConstInsn(Dops.NEW_INSTANCE,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                    DexConst.ConstType.make(newDirectComponentType));

            codeVisitor.visitConstInsn(Dops.INVOKE_DIRECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vBuddyReg),
                            DexRegister.makeLocalReg(vBuddyInitContextReg)),
                    DexConst.ConstMethodRef.make(
                            newDirectComponentType,
                            factory.methods.initMethodName,
                            factory.voidClass.primitiveType,
                            factory.createTypesVariable(factory.buddyInitContextClass.type)));
            // end

            codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
        }

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        invokeUnInitMethod.visitEnd();

    }

}
