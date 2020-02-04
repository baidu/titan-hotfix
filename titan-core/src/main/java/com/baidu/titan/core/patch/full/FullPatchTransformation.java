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
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.analyze.InstructionInfo;
import com.baidu.titan.dex.analyze.MethodAnalyzer;
import com.baidu.titan.dex.analyze.RegisterPc;
import com.baidu.titan.dex.analyze.types.RegType;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.insn.DexConstInsnNode;
import com.baidu.titan.dex.node.insn.DexInsnNode;
import com.baidu.titan.dex.node.insn.DexLabelNode;
import com.baidu.titan.dex.node.insn.DexOpcodeInsnNode;
import com.baidu.titan.dex.node.insn.DexSimpleInsnNode;
import com.baidu.titan.dex.node.insn.DexTargetInsnNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.baidu.titan.core.patch.full.PatchBuddyClassHierarchy.ComponentClassInfo;

/**
 *
 * 对所有的类进行遍历Transform，完成以下转换重写过程：
 * <p>
 * 1) 因为组件的继承关系改变，需要通过代码流分析工具完成instanceof，checkcast等指令的类型转换
 * 2) const-class、getClass的转换
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/28
 */
public class FullPatchTransformation implements DexClassPoolNodeVisitor {

    private final ApplicationDexPool mNewAppPool;

    private final ApplicationDexPool mFullPatchPool;

    private final TitanDexItemFactory mFactory;

    private DexClassNode mCurrentClassNode;

    private PatchBuddyClassHierarchy mBuddyClassHierarchy;

    private ApplicationDexPool mInterceptorPool;

    private ApplicationDexPool mBuddyClassPool;

    public FullPatchTransformation(ApplicationDexPool newAppPool,
                                   ApplicationDexPool interceptorPool,
                                   ApplicationDexPool buddyClassPool,
                                   ApplicationDexPool fullPatchPool,
                                   TitanDexItemFactory factory,
                                   PatchBuddyClassHierarchy buddyClassHierarchy) {
        this.mInterceptorPool = interceptorPool;
        this.mBuddyClassPool = buddyClassPool;
        this.mNewAppPool = newAppPool;
        this.mFullPatchPool = fullPatchPool;
        this.mFactory = factory;
        this.mBuddyClassHierarchy = buddyClassHierarchy;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        this.mCurrentClassNode = dcn;
        List<DexMethodNode> newMethods = new ArrayList<>();
        DexClassLoader classLoader = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode classNode = mNewAppPool.findClassFromAll(type);
                if (classNode == null) {
                    classNode = mBuddyClassPool.findClassFromAll(type);
                }

                if (classNode == null) {
                    classNode = mInterceptorPool.findClassFromAll(type);
                }

                return classNode;
            }
        };

        ComponentClassInfo componentClassInfo = mBuddyClassHierarchy.allComponents.get(dcn.type);

        if (componentClassInfo != null) {
            AndroidComponentFlag componentFlag = componentClassInfo.componentFlag;

        }


        dcn.getMethods().forEach(m -> {
            boolean isInitMethod = m.isInstanceInitMethod();

//            if (componentClassInfo != null && isInitMethod) {
//                InitMethodSplitter splitter = new InitMethodSplitter(dcn, m, classLoader, mFactory);
//                int res = splitter.doSplit();
//
//                DexMethodNode initBodyMethod = splitter.getInitBodyMethod();
//
//                DexMethodNode preInitMethod = splitter.getPreInitMethod();
//
//                newMethods.add(initBodyMethod);
//
//                newMethods.add(preInitMethod);
//                if (initBodyMethod.getCode() != null) {
//                    transformMethod(dcn, initBodyMethod, classLoader);
//                }
//
//                if (preInitMethod.getCode() != null) {
//                    transformMethod(dcn, preInitMethod, classLoader);
//                }
//
//            } else {
//                newMethods.add(m);
//
//            }
            if (m.getCode() != null) {
                transformMethod(dcn, m, classLoader);
            }


        });



        mFullPatchPool.addProgramClass(dcn);
    }


    public static int count = 0;

    private static boolean isInvokeKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_DIRECT:
            case Dops.INVOKE_DIRECT_RANGE:
            case Dops.INVOKE_STATIC:
            case Dops.INVOKE_STATIC_RANGE:
            case Dops.INVOKE_INTERFACE:
            case Dops.INVOKE_INTERFACE_RANGE:
            case Dops.INVOKE_VIRTUAL:
            case Dops.INVOKE_VIRTUAL_RANGE:
            case Dops.INVOKE_SUPER:
            case Dops.INVOKE_SUPER_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isCheckCastKind(int opcode) {
        switch (opcode) {
            case Dops.CHECK_CAST: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInstanceOfKind(int opcode) {
        switch (opcode) {
            case Dops.INSTANCE_OF: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeSuperKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_SUPER:
            case Dops.INVOKE_SUPER_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeStaticKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_STATIC:
            case Dops.INVOKE_STATIC_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeVirtualKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_VIRTUAL:
            case Dops.INVOKE_VIRTUAL_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeRangeKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_DIRECT_RANGE:
            case Dops.INVOKE_STATIC_RANGE:
            case Dops.INVOKE_INTERFACE_RANGE:
            case Dops.INVOKE_SUPER_RANGE:
            case Dops.INVOKE_VIRTUAL_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeInterfaceKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_INTERFACE:
            case Dops.INVOKE_INTERFACE_RANGE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private static boolean isInvokeDirectKind(int opcode) {
        switch (opcode) {
            case Dops.INVOKE_DIRECT:
            case Dops.INVOKE_DIRECT_RANGE: {
                return true;
            }
        }
        return false;
    }

    private static class MethodTransformerContext {

        public DexInsnNode insnNode;

        public DexClassNode dcn;

        public DexMethodNode dmn;

        public MethodAnalyzer methodAnalyzer;

        public DexClassLoader classLoader;

        public boolean rewrittenForMethod = false;

        public boolean rewrittenForIns = false;

        public int maxIncreaseLocalReg = 0;

        public List<DexInsnNode> newInsns = new ArrayList<>();

        public DexCodeNode analyzedCode;

    }

    private void transformMethod(DexClassNode dcn, DexMethodNode methodNode,
                                 DexClassLoader classLoader) {
        AndroidComponentMarker.getComponentFlag(dcn);

        MethodAnalyzer ca = new MethodAnalyzer(methodNode, classLoader);
        boolean analyzeSucess = ca.analyze();

        DexCodeNode analyzedCode = ca.getAnalyzedCode();

        Map<DexType, PatchBuddyClassHierarchy.ComponentClassInfo> allComponents =
                mBuddyClassHierarchy.allComponents;

        MethodTransformerContext context = new MethodTransformerContext();
        context.rewrittenForIns = false;
        context.rewrittenForMethod = false;
        context.analyzedCode = analyzedCode;
        context.dcn = dcn;
        context.newInsns = new ArrayList<>();
        context.maxIncreaseLocalReg = 0;
        context.dmn = methodNode;
        context.methodAnalyzer = ca;
        context.classLoader = classLoader;

        for (DexInsnNode insn : analyzedCode.getInsns()) {
            context.insnNode = insn;
            context.rewrittenForIns = false;


            InstructionInfo insnsInfo = InstructionInfo.infoForIns(insn);
            RegisterPc regPc = insnsInfo.registerPc;

            int nextLocalReg = analyzedCode.getLocalRegCount();

            if (insn instanceof DexOpcodeInsnNode) {
                DexOpcodeInsnNode opInsNode = (DexOpcodeInsnNode) insn;

                DexRegisterList regs = opInsNode.getRegisters();
                Dop dop = Dops.dopFor(opInsNode.getOpcode());

                if (isInvokeKind(dop.opcode)) {
                    transformInvokeKind(context);
                } else if (isCheckCastKind(dop.opcode)) { // check-cast
                    transformCheckCastKind(context);
                } else if (isInstanceOfKind(dop.opcode)){ // instance-of
                    transformInstanceOfKind(context);
                } else {
                    context.newInsns.add(insn);
                }

                if (context.rewrittenForIns) {
                    context.rewrittenForMethod = true;
                    int expandLocalRegForIns = nextLocalReg - analyzedCode.getLocalRegCount() + 1;
                    if (context.maxIncreaseLocalReg < expandLocalRegForIns) {
                        context.maxIncreaseLocalReg = expandLocalRegForIns;
                    }

                }
            } else { // if not opcode ins node
                context.newInsns.add(insn);
            }
        }

        if (context.rewrittenForMethod) {
            DexCodeNode newCode = new DexCodeNode();
            newCode.setTryCatches(analyzedCode.getTryCatches());
            newCode.setLineNumbers(analyzedCode.getLineNumbers());
            newCode.setInsns(context.newInsns);

//            newCode.setRegisters(analyzedCode.getLocalRegCount() + context.maxIncreaseLocalReg,
//                    analyzedCode.getParameterRegCount());



            methodNode.setCode(newCode);
            DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(methodNode);
        }
        ca.clear();
    }

    private void transformInvokeKind(MethodTransformerContext context) {

        Map<DexType, PatchBuddyClassHierarchy.ComponentClassInfo> allComponents =
                mBuddyClassHierarchy.allComponents;

        DexConstInsnNode opcodeIns = (DexConstInsnNode)context.insnNode;
        InstructionInfo insnsInfo = InstructionInfo.infoForIns(opcodeIns);
        RegisterPc regPc = insnsInfo.registerPc;

        int nextLocalReg = context.analyzedCode.getLocalRegCount();

        Dop dop = Dops.dopFor(opcodeIns.getOpcode());
        DexRegisterList regs = opcodeIns.getRegisters();

        boolean rangeCall = isInvokeRangeKind(dop.opcode);

        DexConst.ConstMethodRef calledMethod = (DexConst.ConstMethodRef) opcodeIns.getConst();

        boolean staticMethod = (dop.opcode == Dops.INVOKE_STATIC) ||
                (dop.opcode == Dops.INVOKE_STATIC_RANGE);
        boolean fixParameter = false;

        boolean callInitMethod = isInvokeDirectKind(dop.opcode) &&
                "<init>".equals(calledMethod.getName().toString());
        boolean callSuperMethod = isInvokeSuperKind(dop.opcode);

        List<DexRegister> newRegList = new ArrayList<>();

        // 对于实例方法，
        if (!staticMethod) {
            DexRegister thisReg = regs.get(0);
            RegType thisRegType = regPc.getRegTypeFromDexRegister(thisReg);
            DexType ownerType = calledMethod.getOwner();
            DexType thisActualType = thisRegType.getDexType();

            ClassLinker classLinker = context.methodAnalyzer.getClassLinker();
            DexClassLoader dcl = context.methodAnalyzer.getClassLoader();




            if (!callInitMethod && !callSuperMethod) {

                if (!ownerType.equals(thisActualType)) { // fast check
                    if (!classLinker.isAssignableFrom(ownerType, thisActualType, dcl)) {

                    }
                }


                if (!ownerType.equals(thisActualType) &&
                        allComponents.containsKey(thisActualType)) {
                    fixParameter = true;
                    context.rewrittenForIns = true;
                    ComponentClassInfo componentClassInfo = allComponents.get(thisActualType);
                    context.newInsns.add(
                            new DexConstInsnNode(
                                    Dops.IGET_OBJECT,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(nextLocalReg),
                                            regs.get(0)),
                                    DexConst.ConstFieldRef.make(
                                            componentClassInfo.buddyClassHolder.buddyType,
                                            componentClassInfo.buddyClassHolder.genesisType,
                                            mFactory.buddyClass.genesisObjFieldName)));
                    newRegList.add(DexRegister.makeLocalReg(nextLocalReg));

                    nextLocalReg++;
                }
            }

            // 如果this寄存器没有被类型改写
            if (!fixParameter) {
                newRegList.add(thisReg);
            }

        }

        if (!fixParameter) {
            for (int i = 0; i < calledMethod.getParameterTypes().count(); i++) {
                DexType paraType = calledMethod.getParameterTypes().getType(i);
                DexRegister paraReg = regs.get(staticMethod ? i : i + 1);
                RegType actualParaRegType =
                        insnsInfo.registerPc.getRegTypeFromDexRegister(paraReg);
                DexType actualParaType = actualParaRegType.getDexType();


                if (paraType.isReferenceType() && actualParaRegType.isNonZeroReferenceTypes()) {
                    DexClassNode paraClassNode = context.classLoader.findClass(paraType);
                    DexClassNode actualClassNode = context.classLoader.findClass(actualParaType);
                    if (paraClassNode != null && actualClassNode != null) {
                        if (!context.methodAnalyzer.getClassLinker()
                                .isSubClass(paraClassNode, actualClassNode)) {
                            if (allComponents.containsKey(actualParaType)) {
                                fixParameter = true;
                                context.rewrittenForIns = true;
                            }
                        }
                    } else {
                        if (paraClassNode == null) {
                            System.out.println("cannot find paraType " + paraType + " in" +
                                            context.dmn);
                        }
                        if (actualClassNode != null) {
                            System.out.println("cannot find actual type " + actualParaType +
                            " in " + context.dmn);
                        }

                    }

                }
            }
        }

        if (fixParameter) {
            for (int i = 0; i < calledMethod.getParameterTypes().count(); i++) {
                DexType paraType = calledMethod.getParameterTypes().getType(i);
                DexRegister paraReg = regs.get(staticMethod ? i : i + 1);
                RegType actualParaRegType =
                        insnsInfo.registerPc.getRegTypeFromDexRegister(paraReg);
                DexType actualParaType = actualParaRegType.getDexType();
                boolean changeType = false;

                if (paraType.isReferenceType() && actualParaRegType.isNonZeroReferenceTypes()) {
                    DexClassNode paraClassNode = context.classLoader.findClass(paraType);
                    DexClassNode actualClassNode = context.classLoader.findClass(actualParaType);

                    if (!context.methodAnalyzer.getClassLinker()
                            .isSubClass(paraClassNode, actualClassNode)) {
                        if (allComponents.containsKey(actualParaType)) {
                            if (allComponents.containsKey(actualParaType)) {
                                changeType = true;
                                ComponentClassInfo paraCompClassInfo = allComponents.
                                        get(actualParaType);
                                context.newInsns.add(
                                        new DexConstInsnNode(
                                                Dops.IGET_OBJECT,
                                                DexRegisterList.make(
                                                        DexRegister.makeLocalReg(nextLocalReg),
                                                        paraReg),
                                                DexConst.ConstFieldRef.make(
                                                        paraCompClassInfo.buddyClassHolder.buddyType,
                                                        paraCompClassInfo.buddyClassHolder.genesisType,
                                                        mFactory.buddyClass.genesisObjFieldName)));
                                newRegList.add(DexRegister.makeLocalReg(nextLocalReg));

                                nextLocalReg++;
                            }
                        }
                    }

                }


                if (!changeType) {
                    if (rangeCall) {
                        switch (paraType.toShortDescriptor()) {
                            case 'Z':
                            case 'B':
                            case 'S':
                            case 'C':
                            case 'I':
                            case 'F': {
                                context.newInsns.add(new DexSimpleInsnNode(
                                        Dops.MOVE,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(nextLocalReg),
                                                paraReg)));
                                newRegList.add(DexRegister.makeLocalReg(nextLocalReg));
                                nextLocalReg++;
                                break;
                            }
                            case 'J':
                            case 'D': {
                                context.newInsns.add(new DexSimpleInsnNode(
                                        Dops.MOVE_WIDE,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(nextLocalReg),
                                                paraReg)));
                                newRegList.add(DexRegister.makeDoubleLocalReg(nextLocalReg));
                                nextLocalReg += 2;
                                break;
                            }
                            case 'L':
                            case '[': {
                                context.newInsns.add(new DexSimpleInsnNode(
                                        Dops.MOVE_OBJECT,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(nextLocalReg),
                                                paraReg)));
                                newRegList.add(DexRegister.makeLocalReg(nextLocalReg));
                                nextLocalReg++;
                                break;
                            }
                            default: {
                            }
                        }
                    } else {
                        newRegList.add(paraReg);
                    }
                }
            }
        }

        if (fixParameter) {
            DexRegisterList newRegs = new DexRegisterList(newRegList.size());
            for (int i = 0; i < newRegList.size(); i++) {
                newRegs.setReg(i, newRegList.get(i));
            }

            context.newInsns.add(new DexConstInsnNode(dop.opcode, newRegs, calledMethod));
        } else {
            context.newInsns.add(opcodeIns);
        }
    }

    private void transformCheckCastKind(MethodTransformerContext context) {
        Map<DexType, PatchBuddyClassHierarchy.ComponentClassInfo> allComponents =
                mBuddyClassHierarchy.allComponents;

        DexConstInsnNode opcodeIns = (DexConstInsnNode)context.insnNode;
        InstructionInfo insnsInfo = InstructionInfo.infoForIns(opcodeIns);
        RegisterPc regPc = insnsInfo.registerPc;

        final int firstAvailableLocalReg = context.analyzedCode.getLocalRegCount();

        Dop dop = Dops.dopFor(opcodeIns.getOpcode());
        DexRegisterList regs = opcodeIns.getRegisters();

        // check-cast 的实例寄存器，及其对应的寄存器类型信息
        DexRegister testInstanceReg = regs.get(0);
        RegType testInstanceRegType = insnsInfo.registerPc.getRegTypeFromDexRegister(testInstanceReg);
        DexType testInstanceType = testInstanceRegType.getDexType();

        // 测试类型常量
        DexType testConstType = ((DexConst.ConstType)opcodeIns.getConst()).value();

        if (testConstType.equals(testInstanceType)) {
            context.newInsns.add(opcodeIns);
            return;
        }

        // 要测试的常量组件信息
        ComponentClassInfo testConstTypeComponent = allComponents.get(testConstType);
        // 要测试的实例组件信息
        ComponentClassInfo testInstanceTypeComponent = allComponents.get(testInstanceType);

        if (testConstTypeComponent != null & testInstanceTypeComponent != null) {
            // 如果常量和实例都在组件列表中，可以直接比对
            context.newInsns.add(opcodeIns);
        } else if (testConstTypeComponent != null && testInstanceTypeComponent == null){
            // 如果常量在组件列表中，实例不在组件列表中，
            // 测试(instance-of)一下实例是否是genesisType,
            // 如果是则获取genesis.buddyObj字段的值，与常量类型做instance-of测试
            //

//            final int vInstancOfResReg = firstAvailableLocalReg;

            final int vTmpInstanceResReg = firstAvailableLocalReg;

            final int vGenesisReg = firstAvailableLocalReg + 1;

            final int vBuddyReg = firstAvailableLocalReg + 1;



            DexLabelNode orgCheckCastLabel = new DexLabelNode();

            DexLabelNode afterCheckCastOrgLabel = new DexLabelNode();

            context.newInsns.add(new DexConstInsnNode(Dops.INSTANCE_OF,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpInstanceResReg),
                            testInstanceReg),
                    DexConst.ConstType.make(testConstTypeComponent.buddyClassHolder.genesisType)));

            context.newInsns.add(new DexTargetInsnNode(Dops.IF_EQZ,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpInstanceResReg)),
                    orgCheckCastLabel));

            context.newInsns.add(new DexSimpleInsnNode(Dops.MOVE_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vGenesisReg), testInstanceReg)));


            context.newInsns.add(new DexConstInsnNode(Dops.CHECK_CAST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vGenesisReg)),
                    DexConst.ConstType.make(testConstTypeComponent.buddyClassHolder.genesisType)));

            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vBuddyReg),
                            DexRegister.makeLocalReg(vGenesisReg)),
                    DexConst.ConstFieldRef.make(
                            testConstTypeComponent.buddyClassHolder.genesisType,
                            mFactory.objectClass.type,
                            mFactory.genesisClass.buddyObjFiledName)));

            context.newInsns.add(new DexConstInsnNode(Dops.INSTANCE_OF,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpInstanceResReg),
                            DexRegister.makeLocalReg(vBuddyReg)),
                    opcodeIns.getConst()));


            context.newInsns.add(new DexTargetInsnNode(Dops.IF_EQZ,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpInstanceResReg)),
                    orgCheckCastLabel));

            context.newInsns.add(new DexConstInsnNode(Dops.CHECK_CAST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vBuddyReg)),
                    opcodeIns.getConst()));

            context.newInsns.add(new DexSimpleInsnNode(Dops.MOVE_OBJECT,
                    DexRegisterList.make(
                            testInstanceReg,
                            DexRegister.makeLocalReg(vBuddyReg))));

            context.newInsns.add(
                    new DexTargetInsnNode(Dops.GOTO,
                            DexRegisterList.empty(),
                            afterCheckCastOrgLabel));

            context.newInsns.add(orgCheckCastLabel);
            context.newInsns.add(opcodeIns);
            // # label : after instance of
            context.newInsns.add(afterCheckCastOrgLabel);



//            // ****
//
//            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
//                    DexRegisterList.make(testInstanceReg, testInstanceReg),
//                    DexConst.ConstFieldRef.make(
//                            testConstTypeComponent.buddyClassHolder.genesisType,
//                            mFactory.objectClass.type,
//                            mFactory.createString(TitanConstant.GenesisClass.FIELD_NAME_BUDDY_OBJ))));
//            context.newInsns.add(label);
//            context.newInsns.add(opcodeIns);
//
//            // ***
            context.rewrittenForIns = true;

            log("#const-comp const type = " + testConstType + " instance type = " +
                    testInstanceType);
            log("method = " + context.dmn);
//            log(context.analyzedCode.toSmaliString());
            log("##########################");


        } else if (testConstTypeComponent == null && testInstanceTypeComponent != null) {
//            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
//                    DexRegisterList.make(testReg, testReg),
//                    DexConst.ConstFieldRef.make(
//                            testInstanceTypeComponent.buddyClassHolder.genesisType,
//                            mFactory.objectClass.type,
//                            mFactory.createString(TitanConstant.GenesisClass.FIELD_NAME_BUDDY_OBJ))));
            // TODO check-cast ?
            context.newInsns.add(opcodeIns);
            context.rewrittenForIns = true;

            log("#instance-comp const type = " + testConstType + " instance type =" +
                    " " +
                    testInstanceType);
            log("method = " + context.dmn);
//            log(context.analyzedCode.toSmaliString());
            log("##########################");

        } else { // !testTypeBuddy && !instanceTypeBuddy
            context.newInsns.add(opcodeIns);
        }
    }

    private void transformInstanceOfKind(MethodTransformerContext context) {
        Map<DexType, PatchBuddyClassHierarchy.ComponentClassInfo> allComponents =
                mBuddyClassHierarchy.allComponents;

        DexConstInsnNode opcodeIns = (DexConstInsnNode)context.insnNode;
        InstructionInfo insnsInfo = InstructionInfo.infoForIns(opcodeIns);
        RegisterPc regPc = insnsInfo.registerPc;

        final int firstAvailableLocalReg = context.analyzedCode.getLocalRegCount();


        Dop dop = Dops.dopFor(opcodeIns.getOpcode());
        DexRegisterList regs = opcodeIns.getRegisters();

        // instance-of 的实例寄存器，及其对应的寄存器类型信息
        DexRegister testInstanceReg = regs.get(1);
        RegType testInstanceRegType = insnsInfo.registerPc.getRegTypeFromDexRegister(testInstanceReg);
        DexType testInstanceType = testInstanceRegType.getDexType();

        // instance-of 的结果寄存器
        DexRegister instanceResultReg = regs.get(0);

        // 测试类型常量
        DexType testConstType = ((DexConst.ConstType)opcodeIns.getConst()).value();

        if (testConstType.equals(testInstanceType)) {
            context.newInsns.add(opcodeIns);
            return;
        }

        // 要测试的常量组件信息
        ComponentClassInfo testConstTypeComponent = allComponents.get(testConstType);
        // 要测试的实例组件信息
        ComponentClassInfo testInstanceTypeComponent = allComponents.get(testInstanceType);


        if (testConstTypeComponent != null & testInstanceTypeComponent != null) {
            // 如果常量和实例都在组件列表中，可以直接比对
            context.newInsns.add(opcodeIns);
        } else if (testConstTypeComponent != null && testInstanceTypeComponent == null) {
            // 如果常量在组件列表中，实例不在组件列表中，
            // 测试(instance-of)一下实例是否是genesisType,
            // 如果是则获取genesis.buddyObj字段的值，与常量类型做instance-of测试
            //

//            final int vInstanceOfResReg = firstAvailableLocalReg;

            final int vTmpInstanceResReg = firstAvailableLocalReg;

            final int vGenesisReg = firstAvailableLocalReg;

            final int vBuddyReg = firstAvailableLocalReg;

            DexLabelNode orgInstanceOflabel = new DexLabelNode();
            DexLabelNode afterInstanceOrgLabel = new DexLabelNode();

            context.newInsns.add(new DexConstInsnNode(Dops.INSTANCE_OF,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpInstanceResReg),
                            testInstanceReg),
                    DexConst.ConstType.make(testConstTypeComponent.buddyClassHolder.genesisType)));

            context.newInsns.add(new DexTargetInsnNode(Dops.IF_EQZ,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpInstanceResReg)),
                    orgInstanceOflabel));

            context.newInsns.add(new DexSimpleInsnNode(Dops.MOVE_OBJECT,
                    DexRegisterList.make(DexRegister.makeLocalReg(vGenesisReg), testInstanceReg)));

            context.newInsns.add(new DexConstInsnNode(Dops.CHECK_CAST,
                    DexRegisterList.make(DexRegister.makeLocalReg(vGenesisReg)),
                    DexConst.ConstType.make(testConstTypeComponent.buddyClassHolder.genesisType)));

            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vBuddyReg),
                            DexRegister.makeLocalReg(vGenesisReg)),
                    DexConst.ConstFieldRef.make(
                            testConstTypeComponent.buddyClassHolder.genesisType,
                            mFactory.objectClass.type,
                            mFactory.genesisClass.buddyObjFiledName)));

            context.newInsns.add(new DexConstInsnNode(Dops.INSTANCE_OF,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vTmpInstanceResReg),
                            DexRegister.makeLocalReg(vBuddyReg)),
                    opcodeIns.getConst()));

//            context.newInsns.add(new DexTargetInsnNode(Dops.GOTO,
//                    DexRegisterList.empty(),
//                    afterInstanceOrgLabel));

            context.newInsns.add(new DexTargetInsnNode(Dops.IF_EQZ,
                    DexRegisterList.make(DexRegister.makeLocalReg(vTmpInstanceResReg)),
                    orgInstanceOflabel));

            context.newInsns.add(new DexSimpleInsnNode(Dops.MOVE,
                    DexRegisterList.make(
                            instanceResultReg,
                            DexRegister.makeLocalReg(vTmpInstanceResReg))));
            context.newInsns.add(
                    new DexTargetInsnNode(Dops.GOTO, DexRegisterList.empty(), afterInstanceOrgLabel));

            context.newInsns.add(orgInstanceOflabel);
            context.newInsns.add(opcodeIns);
            // # label : after instance of
            context.newInsns.add(afterInstanceOrgLabel);
            context.rewrittenForIns = true;

            log("#const-comp const type = " + testConstType + " instance type = " +
                    testInstanceType);
            log("method = " + context.dmn);
//            log(context.analyzedCode.toSmaliString());
            log("##########################");

        } else if (testConstTypeComponent == null && testInstanceTypeComponent != null) {
            // 如果常量不在组件列表中，实例在组件列表中，那么，
            // 那么先进行默认比较

            final int vGenesisType = firstAvailableLocalReg;
            final int tmpTestInstanceObjReg = firstAvailableLocalReg;

            DexLabelNode afterInstanceOrgLabel = new DexLabelNode();

            context.newInsns.add(
                    new DexSimpleInsnNode(Dops.MOVE_OBJECT,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(tmpTestInstanceObjReg),
                                    testInstanceReg)));

            context.newInsns.add(opcodeIns);

            context.newInsns.add(new DexTargetInsnNode(Dops.IF_NEZ,
                    DexRegisterList.make(instanceResultReg),
                    afterInstanceOrgLabel));

            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
                    DexRegisterList.make(
                            DexRegister.makeLocalReg(vGenesisType),
                            DexRegister.makeLocalReg(tmpTestInstanceObjReg)),
                    DexConst.ConstFieldRef.make(
                            testInstanceTypeComponent.buddyClassHolder.buddyType,
                            testInstanceTypeComponent.buddyClassHolder.genesisType,
                            mFactory.buddyClass.genesisObjFieldName)));

            context.newInsns.add(new DexConstInsnNode(Dops.INSTANCE_OF,
                    DexRegisterList.make(
                            instanceResultReg,
                            DexRegister.makeLocalReg(vGenesisType)),
                    opcodeIns.getConst()));

            context.newInsns.add(afterInstanceOrgLabel);

//            // TODO
//            context.newInsns.add(new DexConstInsnNode(Dops.IGET_OBJECT,
//                    DexRegisterList.make(testInstanceReg, testInstanceReg),
//                    DexConst.ConstFieldRef.make(
//                            testInstanceTypeComponent.buddyClassHolder.genesisType,
//                            mFactory.objectClass.type,
//                            mFactory.createString(TitanConstant.GenesisClass.FIELD_NAME_BUDDY_OBJ))));

            context.rewrittenForIns = true;

            log("#instance-comp const type = " + testConstType + " instance type =" +
                    " " +
                    testInstanceType);
            log("method = " + context.dmn);
//            log(context.analyzedCode.toSmaliString());
            log("##########################");

        } else { // !testTypeBuddy && !instanceTypeBuddy
            context.newInsns.add(opcodeIns);
        }
    }

    private static void log(String content) {
        System.out.println(content);
    }

    @Override
    public void classPoolVisitEnd() {
//        System.out.println("total count = " + count);
//        System.out.println("total non range count = " + nonRangeCount);
//        System.out.println("total range count " + rangeCount);
    }
}
