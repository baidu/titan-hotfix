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
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.analyze.InstructionInfo;
import com.baidu.titan.dex.analyze.MethodAnalyzer;
import com.baidu.titan.dex.analyze.RegisterPc;
import com.baidu.titan.dex.analyze.types.PreciseConstHiType;
import com.baidu.titan.dex.analyze.types.PreciseConstLoType;
import com.baidu.titan.dex.analyze.types.PreciseConstType;
import com.baidu.titan.dex.analyze.types.RegType;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.insn.DexConstInsnNode;
import com.baidu.titan.dex.node.insn.DexInsnNode;
import com.baidu.titan.dex.node.insn.DexLabelNode;
import com.baidu.titan.dex.node.insn.DexLineNumberNode;
import com.baidu.titan.dex.node.insn.DexOpcodeInsnNode;
import com.baidu.titan.dex.node.insn.DexSimpleInsnNode;
import com.baidu.titan.dex.node.insn.DexTargetInsnNode;
import com.baidu.titan.dex.node.insn.DexTryCatchNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/4/2
 */
public class InitMethodSplitter {

    private DexClassNode dcn;

    private DexMethodNode initMethodNode;

    private DexClassLoader classLoader;

    private TitanDexItemFactory mFactory;

    private SpliteInfo mSpliteInfo;

    private boolean mIsSuperABuddyClass;

    public static final int RES_OK = 0;

    public static final int RES_ERROR_ONE_MORE_OR_LESS_INIT_INVOKE_INS = -1;

    public static final int RES_ERROR_NON_LINEAR_INS = -2;

    public static final int RES_ERROR_UNSUPPORT_PREINIT_INS = -3;

    public static final int RES_ERROR_UNKOWN = -4;



    public InitMethodSplitter(DexClassNode classNode, DexMethodNode methodNode,
                              DexClassLoader loader, TitanDexItemFactory factory,
                              boolean isSuperABuddyClass) {
        this.dcn = classNode;
        this.initMethodNode = methodNode;
        this.classLoader = loader;
        this.mFactory = factory;
        this.mIsSuperABuddyClass = isSuperABuddyClass;
    }

    private static class SpliteInfo {

        public int initInvokeInsIdx = -1;

        public DexMethodNode preInitMethodNode;

        public DexMethodNode initBodyMethodNode;

        public InstructionInfo initInvokeInsInfo;

        public DexConst.ConstMethodRef thisOrSuperMethodRef;

        public int minUndefinedLocalReg;

        public int localRegArrayLength;

    }

    public DexMethodNode getPreInitMethod() {
        return mSpliteInfo.preInitMethodNode;
    }

    public DexMethodNode getInitBodyMethod() {
        return mSpliteInfo.initBodyMethodNode;
    }

    public int doSplit() {
        DexCodeNode codeNode = initMethodNode.getCode();
        MethodAnalyzer analyzer = new MethodAnalyzer(initMethodNode, classLoader);
        boolean sucess = analyzer.analyze();
        DexCodeNode analyzedCode = analyzer.getAnalyzedCode();

        SpliteInfo spliteInfo = new SpliteInfo();
        mSpliteInfo = spliteInfo;
        int res = setup(analyzedCode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }

        res = buildPreInitMethod(analyzedCode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }

        res = buildInitBodyMethod(analyzedCode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }

        return res;

    }

    private DexCodeNode buildFirstHalfPreInitCodeNode(DexCodeNode orgCodeNode, SpliteInfo spliteInfo) {
        DexCodeNode preInitCodeNode = new DexCodeNode();


        List<DexInsnNode> preInitIns = new ArrayList<>();
        preInitCodeNode.setInsns(preInitIns);
        List<DexLineNumberNode> preInitLines = new ArrayList<>();
        preInitCodeNode.setLineNumbers(preInitLines);
        List<DexTryCatchNode> preInitTries = new ArrayList<>();
        preInitCodeNode.setTryCatches(preInitTries);

        int preInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;

        preInitCodeNode.setRegisters(orgCodeNode.getLocalRegCount(), preInitParaRegCount);

        for (int insIdx = 0; insIdx < spliteInfo.initInvokeInsIdx; insIdx++) {
            DexInsnNode insnNode = orgCodeNode.getInsns().get(insIdx);

            if (insnNode instanceof DexLabelNode) {
                DexLabelNode labelNode = (DexLabelNode)insnNode;
                if (orgCodeNode.getLineNumbers() != null) {
                    for (DexLineNumberNode lineNumNode : orgCodeNode.getLineNumbers()) {
                        if (lineNumNode.getStartLabel() == labelNode) {
                            preInitLines.add(lineNumNode);
                        }
                    }
                }

                if (orgCodeNode.getTryCatches() != null) {
                    for (DexTryCatchNode tryCatchNode : orgCodeNode.getTryCatches()) {
                        boolean containTryItem = false;
                        if (tryCatchNode.getStart() == labelNode ||
                                tryCatchNode.getEnd() == labelNode ||
                                tryCatchNode.getCatchAllHandler() == labelNode) {
                            containTryItem = true;
                        }
                        if (tryCatchNode.getHandlers() != null) {
                            for (DexLabelNode handler : tryCatchNode.getHandlers()) {
                                if (handler == labelNode) {
                                    containTryItem = true;
                                }
                            }
                        }
                        if (containTryItem) {
                            preInitTries.add(tryCatchNode);
                        }
                    }
                }

                preInitIns.add(insnNode);
            } else {
                preInitIns.add(insnNode);
            }
        }

        int pInitBuddyContextReg = preInitParaRegCount - 1;

        DexConstInsnNode orgInvokeThisSuperCall =
                (DexConstInsnNode)orgCodeNode.getInsns().get(spliteInfo.initInvokeInsIdx);


        DexConst.ConstMethodRef invokeThisSuperMethodRef =
                (DexConst.ConstMethodRef)orgInvokeThisSuperCall.getConst();


        DexRegisterList.Builder invokeThisSuperRegsBuilder = DexRegisterList.newBuilder();

        orgInvokeThisSuperCall.getRegisters().forEach(r -> invokeThisSuperRegsBuilder.addReg(r));

        invokeThisSuperRegsBuilder.addReg(DexRegister.makeParameterReg(pInitBuddyContextReg));

        // paramTypes
        DexTypeList.Builder invokeThisSuperParaTypesBuilder = DexTypeList.newBuilder();
        invokeThisSuperParaTypesBuilder.addType(invokeThisSuperMethodRef.getOwner());

        for (int i = 0; i < invokeThisSuperMethodRef.getParameterTypes().count(); i++) {
            invokeThisSuperParaTypesBuilder.addType(
                    invokeThisSuperMethodRef.getParameterTypes().getType(i));
        }
        invokeThisSuperParaTypesBuilder.addType(mFactory.buddyInitContextClass.type);

        preInitIns.add(new DexConstInsnNode(
                Dops.INVOKE_STATIC,
                invokeThisSuperRegsBuilder.build(),
                DexConst.ConstMethodRef.make(
                        invokeThisSuperMethodRef.getOwner(), /** this or super */
                        mFactory.titanMethods.preInitMethodName,
                        mFactory.voidClass.primitiveType,
                        mFactory.intern(invokeThisSuperParaTypesBuilder.build()))));
        return preInitCodeNode;

    }

    private int buildPreInitMethod(DexCodeNode orgCodeNode, SpliteInfo spliteInfo) {
        DexTypeList.Builder newParaBuilder = DexTypeList.newBuilder();

        newParaBuilder.addType(initMethodNode.owner);
        for (int i = 0; i < initMethodNode.parameters.count(); i++) {
            newParaBuilder.addType(initMethodNode.parameters.getType(i));
        }
        newParaBuilder.addType(mFactory.buddyInitContextClass.type);

        DexMethodNode preInitMethodNode = new DexMethodNode(
                mFactory.titanMethods.preInitMethodName,
                initMethodNode.owner,
                mFactory.intern(newParaBuilder.build()),
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_STATIC));

        DexCodeNode preInitFirstHalfCodeNode =
                buildFirstHalfPreInitCodeNode(orgCodeNode, spliteInfo);

        DexCodeNode preInitCodeNode = new DexCodeNode();

        preInitMethodNode.setCode(preInitCodeNode);

        DexCodeVisitor preInitCodeVisitor = preInitCodeNode.asVisitor();

        preInitFirstHalfCodeNode.accept(new DexCodeFormatVerifier(
                new DexCodeVisitor(preInitCodeVisitor) {

            private int mContextHolderReg;

            @Override
            public void visitRegisters(int localRegCount, int parameterRegCount) {
                super.visitRegisters(localRegCount, parameterRegCount);
            }

            private void visitInvokeThisOrSuperIns() {

                int preInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                int pInitBuddyContextReg = preInitParaRegCount - 1;

                // contextHolder = buddyInitContext.makeNext(local, para);
                int vLocalCountReg = spliteInfo.minUndefinedLocalReg;
                int vParaCountReg = spliteInfo.minUndefinedLocalReg + 1;
                this.mContextHolderReg = spliteInfo.minUndefinedLocalReg;
                // local reg count
                super.visitConstInsn(Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vLocalCountReg)),
                        DexConst.LiteralBits32.make(spliteInfo.localRegArrayLength));
                // para reg count (not include this reg -> p0)
                super.visitConstInsn(Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vParaCountReg)),
                        DexConst.LiteralBits32.make(orgCodeNode.getParameterRegCount() -1));
                super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                        DexRegisterList.make(
                                DexRegister.makeParameterReg(pInitBuddyContextReg),
                                DexRegister.makeLocalReg(vLocalCountReg),
                                DexRegister.makeLocalReg(vParaCountReg)),
                        mFactory.buddyInitContextClass.makeNextMethod);

                super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(this.mContextHolderReg)));

                // call this or super $preInit

                DexConstInsnNode orgInvokeThisSuperCall =
                        (DexConstInsnNode)orgCodeNode.getInsns().get(spliteInfo.initInvokeInsIdx);


                DexConst.ConstMethodRef invokeThisSuperMethodRef =
                        (DexConst.ConstMethodRef)orgInvokeThisSuperCall.getConst();


                DexRegisterList.Builder invokeThisSuperRegsBuilder = DexRegisterList.newBuilder();

                orgInvokeThisSuperCall.getRegisters().forEach(r ->
                        invokeThisSuperRegsBuilder.addReg(r));

                invokeThisSuperRegsBuilder.addReg(DexRegister.makeParameterReg(pInitBuddyContextReg));

                // paramTypes
                DexTypeList.Builder invokeThisSuperParaTypesBuilder = DexTypeList.newBuilder();
                invokeThisSuperParaTypesBuilder.addType(invokeThisSuperMethodRef.getOwner());

                invokeThisSuperMethodRef.getParameterTypes().forEach(type ->
                        invokeThisSuperParaTypesBuilder.addType(type));

                invokeThisSuperParaTypesBuilder.addType(mFactory.buddyInitContextClass.type);

                super.visitConstInsn(Dops.INVOKE_STATIC,
                        invokeThisSuperRegsBuilder.build(),
                        DexConst.ConstMethodRef.make(
                                invokeThisSuperMethodRef.getOwner(), /** this or super */
                                mFactory.titanMethods.preInitMethodName,
                                mFactory.voidClass.primitiveType,
                                mFactory.intern(invokeThisSuperParaTypesBuilder.build())));
            }

            private void visitSaveLocalAndParameterRegs() {
                int vLocalArrayReg = mContextHolderReg + 1;
                int vArrayIdxReg = mContextHolderReg + 2;

                super.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vLocalArrayReg),
                                DexRegister.makeLocalReg(mContextHolderReg)),
                        mFactory.buddyInitHolderClass.localsField);

                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;

                int nextLocalArrayIdx = 0;

                for (int localReg = 0; localReg < registerPc.mLocalRegs.length; localReg++) {
                    RegType regType = registerPc
                            .getRegTypeFromDexRegister(DexRegister.makeLocalReg(localReg));

                    DexConst.ConstMethodRef valueOfMethod = null;
                    int localArrayIdxWidth = 1;
                    boolean storeRegValue = false;

                    if (regType.isUndefined()) {
                        storeRegValue = false;
                    } else if (regType.isConstantTypes()) {
                        if (!regType.isPrecise()) {
                            throw new IllegalStateException();
                        }
                        storeRegValue = false;
                    } else if (regType.isLowHalf() || regType.isHighHalf()) {
                        if (regType.isLowHalf()) {
                            storeRegValue = true;
                            localArrayIdxWidth = 2;

                            if (regType.isLongLo()) {
                                valueOfMethod = mFactory.longClass.valueOfMethod;
                            } else if (regType.isDoubleLo()) {
                                valueOfMethod = mFactory.doubleClass.valueOfMethod;
                            }
                        }
                    } else if (regType.isCategory1Types()) {
                        storeRegValue = true;
                        if (regType.isBooleanTypes()) {
                            valueOfMethod = mFactory.booleanClass.valueOfMethod;
                        } else if (regType.isByteTypes()) {
                            valueOfMethod = mFactory.byteClass.valueOfMethod;
                        } else if (regType.isCharTypes()) {
                            valueOfMethod = mFactory.characterClass.valueOfMethod;
                        } else if (regType.isShortTypes()) {
                            valueOfMethod = mFactory.shortClass.valueOfMethod;
                        } else if (regType.isFloatTypes()) {
                            valueOfMethod = mFactory.floatClass.valueOfMethod;
                        } else if (regType.isIntegralTypes()) {
                            valueOfMethod = mFactory.integerClass.valueOfMethod;
                        } else {

                        }
                    } else if (regType.isReferenceTypes()) {
                        if (!regType.isUninitializedTypes()) {
                            storeRegValue = true;
                        }
                    }

                    if (storeRegValue) {
                        if (valueOfMethod != null) {
                            super.visitConstInsn(Dops.INVOKE_STATIC,
                                    DexRegisterList.make(
                                            localArrayIdxWidth == 1 ?
                                                    DexRegister.makeLocalReg(localReg) :
                                                    DexRegister.makeDoubleLocalReg(localReg)),
                                    valueOfMethod);
                            super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                                    DexRegisterList.make(DexRegister.makeLocalReg(localReg)));
                        }

                        super.visitConstInsn(Dops.CONST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                DexConst.LiteralBits32.make(nextLocalArrayIdx));

                        super.visitSimpleInsn(Dops.APUT_OBJECT,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(localReg),
                                        DexRegister.makeLocalReg(vLocalArrayReg),
                                        DexRegister.makeLocalReg(vArrayIdxReg)));

                        nextLocalArrayIdx += localArrayIdxWidth;
                    }
                }



                // store parameter regs

                int vParamArrayReg = mContextHolderReg + 1;
                vArrayIdxReg = mContextHolderReg + 2;

                super.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vLocalArrayReg),
                                DexRegister.makeLocalReg(mContextHolderReg)),
                        mFactory.buddyInitHolderClass.parasField);

                int nextParamArrayIdx = 0;

                for (int paraReg = 0; paraReg < registerPc.mParaRegs.length; paraReg++) {
                    RegType regType = registerPc
                            .getRegTypeFromDexRegister(DexRegister.makeParameterReg(paraReg));

                    DexConst.ConstMethodRef valueOfMethod = null;
                    int moveResultOp = -1;
                    int paramArrayIdxWidth = 1;
                    boolean storeRegValue = false;

                    if (regType.isUndefined()) {
                        storeRegValue = false;
                    } else if (regType.isLowHalf() || regType.isHighHalf()) {
                        if (regType.isLowHalf()) {
                            storeRegValue = true;
                            paramArrayIdxWidth = 2;
                            if (regType.isLongLo()) {
                                valueOfMethod = mFactory.longClass.valueOfMethod;
                                moveResultOp = Dops.MOVE_RESULT_WIDE;
                            } else if (regType.isDoubleLo()) {
                                valueOfMethod = mFactory.doubleClass.valueOfMethod;
                                moveResultOp = Dops.MOVE_RESULT_WIDE;
                            }
                        }
                    } else if (regType.isCategory1Types()) {
                        storeRegValue = true;
                        moveResultOp = Dops.MOVE_RESULT;
                        if (regType.isBooleanTypes()) {
                            valueOfMethod = mFactory.booleanClass.valueOfMethod;
                        } else if (regType.isByteTypes()) {
                            valueOfMethod = mFactory.byteClass.valueOfMethod;
                        } else if (regType.isCharTypes()) {
                            valueOfMethod = mFactory.characterClass.valueOfMethod;
                        } else if (regType.isShortTypes()) {
                            valueOfMethod = mFactory.shortClass.valueOfMethod;
                        } else if (regType.isFloatTypes()) {
                            valueOfMethod = mFactory.floatClass.valueOfMethod;
                        } else if (regType.isIntegralTypes()) {
                            valueOfMethod = mFactory.integerClass.valueOfMethod;
                        } else {

                        }
                    } else if (regType.isReferenceTypes()) {
                        if (!regType.isUninitializedTypes()) {
                            storeRegValue = true;
                        }

                    }

                    if (storeRegValue) {
                        if (valueOfMethod != null) {
                            super.visitConstInsn(Dops.INVOKE_STATIC,
                                    DexRegisterList.make(DexRegister.makeLocalReg(paraReg)),
                                    valueOfMethod);
                        }
                        if (moveResultOp != -1) {
                            super.visitSimpleInsn(moveResultOp,
                                    DexRegisterList.make(DexRegister.makeLocalReg(paraReg)));
                        }

                        super.visitConstInsn(Dops.CONST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                DexConst.LiteralBits32.make(nextLocalArrayIdx));

                        super.visitSimpleInsn(Dops.APUT_OBJECT,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(paraReg),
                                        DexRegister.makeLocalReg(vLocalArrayReg),
                                        DexRegister.makeLocalReg(vArrayIdxReg)));

                        nextParamArrayIdx += paramArrayIdxWidth;
                    }
                }
            }

            @Override
            public void visitEnd() {

                visitInvokeThisOrSuperIns();

                visitSaveLocalAndParameterRegs();

                super.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());
                super.visitEnd();
            }

        }));

        DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(preInitMethodNode);

        spliteInfo.preInitMethodNode = preInitMethodNode;

        return RES_OK;
    }

    private DexCodeNode buildSecondHalfInitBodyCodeNode(DexCodeNode orgCodeNode,
                                                        SpliteInfo spliteInfo) {
        List<DexInsnNode> orgInsList = orgCodeNode.getInsns();
        DexCodeNode initBodyCodeNode = new DexCodeNode();


        List<DexInsnNode> initBodyIns = new ArrayList<>();
        initBodyCodeNode.setInsns(initBodyIns);
        List<DexLineNumberNode> preInitLines = new ArrayList<>();
        initBodyCodeNode.setLineNumbers(preInitLines);
        List<DexTryCatchNode> preInitTries = new ArrayList<>();
        initBodyCodeNode.setTryCatches(preInitTries);


        for (int insIdx = spliteInfo.initInvokeInsIdx + 1; insIdx < orgInsList.size(); insIdx++) {
            DexInsnNode insnNode = orgInsList.get(insIdx);

            if (insnNode instanceof DexLabelNode) {
                DexLabelNode labelNode = (DexLabelNode)insnNode;
                if (orgCodeNode.getLineNumbers() != null) {
                    for (DexLineNumberNode lineNumNode : orgCodeNode.getLineNumbers()) {
                        if (lineNumNode.getStartLabel() == labelNode) {
                            preInitLines.add(lineNumNode);
                        }
                    }
                }

                if (orgCodeNode.getTryCatches() != null) {
                    for (DexTryCatchNode tryCatchNode : orgCodeNode.getTryCatches()) {
                        boolean containTryItem = false;
                        if (tryCatchNode.getStart() == labelNode ||
                                tryCatchNode.getEnd() == labelNode ||
                                tryCatchNode.getCatchAllHandler() == labelNode) {
                            containTryItem = true;
                        }
                        if (tryCatchNode.getHandlers() != null) {
                            for (DexLabelNode handler : tryCatchNode.getHandlers()) {
                                if (handler == labelNode) {
                                    containTryItem = true;
                                }
                            }
                        }
                        if (containTryItem) {
                            preInitTries.add(tryCatchNode);
                        }
                    }
                }

                initBodyIns.add(insnNode);
            } else {
                initBodyIns.add(insnNode);
            }
        }

        return initBodyCodeNode;

    }

    /**
     *
     * @param orgCodeNode
     * @param spliteInfo
     * @return
     */
    private int buildInitBodyMethod(DexCodeNode orgCodeNode, SpliteInfo spliteInfo) {
        DexCodeNode secondHalfInitBodyCodeNode =
                buildSecondHalfInitBodyCodeNode(orgCodeNode, spliteInfo);

        DexTypeList.Builder newParaBuilder = DexTypeList.newBuilder();

        initMethodNode.parameters.forEach(t -> newParaBuilder.addType(t));

        newParaBuilder.addType(mFactory.buddyInitContextClass.type);

        DexMethodNode initBodyMethodNode = new DexMethodNode(
                mFactory.methods.initMethodName,
                initMethodNode.owner,
                mFactory.intern(newParaBuilder.build()),
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_CONSTRUCTOR));

        DexCodeNode initBodyCodeNode = new DexCodeNode();

        initBodyMethodNode.setCode(initBodyCodeNode);

        DexCodeVisitor initBodyCodeVisitor = initBodyCodeNode.asVisitor();

        secondHalfInitBodyCodeNode.accept(new DexCodeFormatVerifier(
                new DexCodeVisitor(initBodyCodeVisitor) {

            private int mVCurrentInitContextHolderReg;

            private void visitCallThisOrSuperIns() {

                int initBodyParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                int pInitBuddyContextReg = initBodyParaRegCount - 1;

                int vNextContextHolderReg = mVCurrentInitContextHolderReg + 1;

                int vCalledParaArrayReg = mVCurrentInitContextHolderReg + 2;

                int vArrayIdxReg = mVCurrentInitContextHolderReg + 3;

                int vNextCalledParamReg = mVCurrentInitContextHolderReg + 4;

                final int pThisReg = 0;

                DexRegisterList.Builder invokeThisSuperRegsBuilder = DexRegisterList.newBuilder();

                super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                        DexRegisterList.make(DexRegister.makeParameterReg(pInitBuddyContextReg)),
                        mFactory.buddyInitContextClass.currentMethod);

                super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(mVCurrentInitContextHolderReg)));

                super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                        DexRegisterList.make(DexRegister.makeParameterReg(pInitBuddyContextReg)),
                        mFactory.buddyInitContextClass.nextMethod);

                super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vNextContextHolderReg)));

                super.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vCalledParaArrayReg),
                                DexRegister.makeLocalReg(vNextContextHolderReg)),
                        mFactory.buddyInitHolderClass.parasField);


                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;

                // move this reg
                RegType thisRegType = registerPc
                        .getRegTypeFromDexRegister(DexRegister.makeParameterReg(pThisReg));
                if (!thisRegType.isUninitializedThisReference()) {
                    throw new IllegalStateException();
                }

                super.visitSimpleInsn(Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCalledParamReg),
                                DexRegister.makeParameterReg(pThisReg)));

                invokeThisSuperRegsBuilder.addReg(DexRegister.makeLocalReg(vNextCalledParamReg));

                vNextCalledParamReg++;


                int nextParamArrayIdx = 0;

                for (DexType paraType : spliteInfo.thisOrSuperMethodRef.getParameterTypes()) {
                    DexConst.ConstMethodRef primitiveValueMethod = null;
                    DexType checkCastType = null;

                    int moveResultOp = -1;
                    int paramRegWidth = 1;

                    switch (paraType.toShortDescriptor()) {
                        case DexItemFactory.BooleanClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.booleanClass.primitiveValueMethod;
                            checkCastType = mFactory.booleanClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.ByteClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.byteClass.primitiveValueMethod;
                            checkCastType = mFactory.byteClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.ShortClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.shortClass.primitiveValueMethod;
                            checkCastType = mFactory.shortClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.CharacterClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.characterClass.primitiveValueMethod;
                            checkCastType = mFactory.characterClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.IntegerClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.integerClass.primitiveValueMethod;
                            checkCastType = mFactory.integerClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.LongClass.SHORT_DESCRIPTOR: {
                            paramRegWidth = 2;
                            primitiveValueMethod = mFactory.longClass.primitiveValueMethod;
                            checkCastType = mFactory.longClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT_WIDE;
                            break;
                        }
                        case DexItemFactory.FloatClass.SHORT_DESCRIPTOR: {
                            primitiveValueMethod = mFactory.floatClass.primitiveValueMethod;
                            checkCastType = mFactory.floatClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                            break;
                        }
                        case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                            paramRegWidth = 2;
                            primitiveValueMethod = mFactory.doubleClass.primitiveValueMethod;
                            checkCastType = mFactory.longClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT_WIDE;
                            break;
                        }
                        case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                        case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                            checkCastType = paraType;
                            break;
                        }
                        default: {
                            break;
                        }
                    }


                    super.visitConstInsn(Dops.CONST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                DexConst.LiteralBits32.make(nextParamArrayIdx));

                    super.visitSimpleInsn(Dops.AGET_OBJECT,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(vNextCalledParamReg),
                                        DexRegister.makeLocalReg(vCalledParaArrayReg),
                                        DexRegister.makeLocalReg(vArrayIdxReg)));

                    if (checkCastType != null) {
                        super.visitConstInsn(Dops.CHECK_CAST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vNextCalledParamReg)),
                                    DexConst.ConstType.make(checkCastType));
                    }

                    if (primitiveValueMethod != null) {
                        super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(vNextCalledParamReg)),
                                    primitiveValueMethod);
                        super.visitSimpleInsn(moveResultOp, DexRegisterList.make(
                                    DexRegister.makeLocalReg(vNextCalledParamReg)));
                    }

                    invokeThisSuperRegsBuilder.addReg(
                            paramRegWidth == 1 ?
                                    DexRegister.makeLocalReg(vNextCalledParamReg) :
                                    DexRegister.makeDoubleLocalReg(vNextCalledParamReg));

                    vNextCalledParamReg += paramRegWidth;

                    nextParamArrayIdx++;
                }

                super.visitSimpleInsn(Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCalledParamReg),
                                DexRegister.makeParameterReg(pInitBuddyContextReg)));

                invokeThisSuperRegsBuilder.addReg(DexRegister.makeLocalReg(vNextCalledParamReg));


                // paramTypes
                DexTypeList.Builder invokeThisSuperParaTypesBuilder = DexTypeList.newBuilder();

                spliteInfo.thisOrSuperMethodRef.getParameterTypes().forEach(t ->
                        invokeThisSuperParaTypesBuilder.addType(t));

                invokeThisSuperParaTypesBuilder.addType(mFactory.buddyInitContextClass.type);

                super.visitConstInsn(Dops.INVOKE_DIRECT,
                        invokeThisSuperRegsBuilder.build(),
                        DexConst.ConstMethodRef.make(
                                spliteInfo.thisOrSuperMethodRef.getOwner(), /** this or super */
                                mFactory.methods.initMethodName,
                                mFactory.voidClass.primitiveType,
                                mFactory.intern(invokeThisSuperParaTypesBuilder.build())));
            }

            private void visitRestoreLocals() {
//                if (spliteInfo.localRegArrayLength == 0) {
//                    return;
//                }

                int vLocalArrayReg = mVCurrentInitContextHolderReg + 1;
                int vArrayIdxReg = mVCurrentInitContextHolderReg + 2;

                super.visitConstInsn(Dops.IGET_OBJECT,
                        DexRegisterList.make(DexRegister.makeLocalReg(vLocalArrayReg),
                                DexRegister.makeLocalReg(mVCurrentInitContextHolderReg)),
                        mFactory.buddyInitHolderClass.localsField);

                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;

                int nextLocalArrayIdx = 0;

                for (int localReg = 0; localReg < registerPc.mLocalRegs.length; localReg++) {
                    RegType regType = registerPc
                            .getRegTypeFromDexRegister(DexRegister.makeLocalReg(localReg));

                    DexConst.ConstMethodRef primitiveValueMethod = null;
                    DexType checkCastType = null;

                    int moveResultOp = -1;
                    int localArrayIdxWidth = 1;
                    boolean restoreRegValue = false;

                    if (regType.isUndefined()) {
                        restoreRegValue = false;
                    } else if (regType.isConstantTypes()) {
                        if (!regType.isPrecise()) {
                            throw new IllegalStateException();
                        }

                        if (regType.isPreciseConstant()) {
                            PreciseConstType constType = (PreciseConstType)regType;
                            super.visitConstInsn(Dops.CONST,
                                    DexRegisterList.make(DexRegister.makeLocalReg(localReg)),
                                    DexConst.LiteralBits32.make(constType.constantValue()));
                        } else if (regType.isPreciseConstantLo()) {
                            PreciseConstLoType constLoType = (PreciseConstLoType)regType;
                            PreciseConstHiType constHiType =
                                    (PreciseConstHiType)registerPc.getRegTypeFromDexRegister(
                                            DexRegister.makeLocalReg(localReg + 1));
                            long loValue = constLoType.constantValueLo();
                            long hiValue = constHiType.constantValueHi();

                            super.visitConstInsn(Dops.CONST_WIDE,
                                    DexRegisterList.make(DexRegister.makeDoubleLocalReg(localReg)),
                                    DexConst.LiteralBits64.make(loValue | hiValue << 32));
                        } else if (regType.isPreciseConstantHi()) {
                            // skip
                        }
                    } else if (regType.isLowHalf() || regType.isHighHalf()) {
                        if (regType.isLowHalf()) {
                            restoreRegValue = true;
                            localArrayIdxWidth = 2;
                            if (regType.isLongLo()) {
                                primitiveValueMethod = mFactory.longClass.primitiveValueMethod;
                                checkCastType = mFactory.longClass.boxedType;
                                moveResultOp = Dops.MOVE_RESULT_WIDE;
                            } else if (regType.isDoubleLo()) {
                                primitiveValueMethod = mFactory.doubleClass.primitiveValueMethod;
                                checkCastType = mFactory.longClass.boxedType;
                                moveResultOp = Dops.MOVE_RESULT_WIDE;
                            }
                        }
                    } else if (regType.isCategory1Types()) {
                        restoreRegValue = true;
                        moveResultOp = Dops.MOVE_RESULT;
                        if (regType.isBooleanTypes()) {
                            primitiveValueMethod = mFactory.booleanClass.primitiveValueMethod;
                            checkCastType = mFactory.booleanClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                        } else if (regType.isByteTypes()) {
                            primitiveValueMethod = mFactory.byteClass.primitiveValueMethod;
                            checkCastType = mFactory.byteClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                        } else if (regType.isCharTypes()) {
                            primitiveValueMethod = mFactory.characterClass.primitiveValueMethod;
                            checkCastType = mFactory.characterClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                        } else if (regType.isShortTypes()) {
                            primitiveValueMethod = mFactory.shortClass.primitiveValueMethod;
                            checkCastType = mFactory.shortClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                        } else if (regType.isIntegralTypes()) {
                            primitiveValueMethod = mFactory.integerClass.primitiveValueMethod;
                            checkCastType = mFactory.integerClass.boxedType;
                            moveResultOp = Dops.MOVE_RESULT;
                        } else {

                        }
                    } else if (regType.isReferenceTypes()) {
                        restoreRegValue = true;
                        checkCastType = regType.getDexType();
                    }

                    if (restoreRegValue) {

                        super.visitConstInsn(Dops.CONST,
                                DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                DexConst.LiteralBits32.make(nextLocalArrayIdx));

                        super.visitSimpleInsn(Dops.AGET_OBJECT,
                                DexRegisterList.make(
                                        DexRegister.makeLocalReg(localReg),
                                        DexRegister.makeLocalReg(vLocalArrayReg),
                                        DexRegister.makeLocalReg(vArrayIdxReg)));

                        if (checkCastType != null) {
                            super.visitConstInsn(Dops.CHECK_CAST,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(localReg)),
                                    DexConst.ConstType.make(checkCastType));
                        }

                        if (primitiveValueMethod != null) {
                            super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                                    DexRegisterList.make(
                                            DexRegister.makeLocalReg(localReg)),
                                    primitiveValueMethod);
                            super.visitSimpleInsn(moveResultOp, DexRegisterList.make(
                                    localArrayIdxWidth == 1 ?
                                            DexRegister.makeLocalReg(localReg) :
                                            DexRegister.makeDoubleLocalReg(localReg)));
                        }

                        nextLocalArrayIdx++;
                    }
                }

            }

            @Override
            public void visitBegin() {

                this.mVCurrentInitContextHolderReg = spliteInfo.minUndefinedLocalReg;

                visitCallThisOrSuperIns();

                // restore locals
                visitRestoreLocals();

                super.visitBegin();
            }

            @Override
            public void visitRegisters(int localRegCount, int parameterRegCount) {

            }
        }));

        DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(initBodyMethodNode);
        spliteInfo.initBodyMethodNode = initBodyMethodNode;

        return RES_OK;
    }

    private int setup(DexCodeNode codeNode, SpliteInfo spliteInfo) {
        int res = findThisInitInvokeIns(codeNode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }

        return traceRegisterUsage(codeNode, spliteInfo);
    }

    private int traceRegisterUsage(DexCodeNode codeNode, SpliteInfo spliteInfo) {
        List<DexInsnNode> insns = codeNode.getInsns();
        DexInsnNode invokeThisOrSuperIns = insns.get(spliteInfo.initInvokeInsIdx);
        InstructionInfo insInfo = InstructionInfo.infoForIns(invokeThisOrSuperIns);


        int localRegCount = insInfo.registerPc.mLocalRegs.length;
        int minUndefinedLocalReg = localRegCount;


        for (int i = 0; i < localRegCount; i++) {
            RegType regType =
                    insInfo.registerPc.getRegTypeFromDexRegister(DexRegister.makeLocalReg(i));
            if (regType.isUndefined()) {
                minUndefinedLocalReg = i;
            } else if (regType.isConstantTypes()) {
                if (!regType.isPrecise()) {
                    throw new IllegalStateException();
                }
                minUndefinedLocalReg = localRegCount;

                if (regType.isPreciseConstant()) {


                } else if (regType.isPreciseConstantLo()) {

                } else if (regType.isPreciseConstantHi()) {

                }
            } else if (regType.isCategory1Types()) {
                spliteInfo.localRegArrayLength++;
                minUndefinedLocalReg = localRegCount;
            } else if (regType.isLowHalf() || regType.isHighHalf()) {
                minUndefinedLocalReg = localRegCount;
                if (regType.isLowHalf()) {
                    spliteInfo.localRegArrayLength++;
                }
            } else if (regType.isReferenceTypes()) {
                minUndefinedLocalReg = localRegCount;
                if (regType.isUninitializedTypes()) {
                }

                spliteInfo.localRegArrayLength++;
            } else {
                return RES_ERROR_UNKOWN;
            }
        }


        spliteInfo.minUndefinedLocalReg = minUndefinedLocalReg;
        spliteInfo.initInvokeInsInfo = insInfo;

        return RES_OK;

    }


    private int findThisInitInvokeIns(DexCodeNode codeNode, SpliteInfo spliteInfo) {
        List<DexInsnNode> insns = codeNode.getInsns();

        List<Integer> initInvokeIdxList = new ArrayList<>();
        for (int insIdx = 0; insIdx < insns.size(); insIdx++) {
            DexInsnNode insNode = insns.get(insIdx);
            InstructionInfo insnsInfo = InstructionInfo.infoForIns(insNode);

            if (insNode instanceof DexOpcodeInsnNode) {
                DexRegisterList regs = ((DexOpcodeInsnNode)insNode).getRegisters();
                if (insNode instanceof DexConstInsnNode) {
                    DexConstInsnNode constantOp = (DexConstInsnNode)insNode;

                    Dop dop = Dops.dopFor(constantOp.getOpcode());
                    switch (dop.opcode) {
                        case Dops.INVOKE_DIRECT:
                        case Dops.INVOKE_DIRECT_RANGE: {
                            DexConst.ConstMethodRef calledMethod =
                                    (DexConst.ConstMethodRef) constantOp.getConst();

                            if (calledMethod.getName().equals("<init>")) {
                                RegType thisRegType =
                                        insnsInfo.registerPc.getRegTypeFromDexRegister(regs.get(0));

                                if (thisRegType.isUninitializedThisReference()) {
                                    initInvokeIdxList.add(insIdx);
                                }
                            }
                            break;
                        }
                        case Dops.IPUT:
                        case Dops.IPUT_BOOLEAN:
                        case Dops.IPUT_BYTE:
                        case Dops.IPUT_CHAR:
                        case Dops.IPUT_OBJECT:
                        case Dops.IPUT_SHORT:
                        case Dops.IPUT_WIDE: {
                            RegType thisRegType =
                                    insnsInfo.registerPc.getRegTypeFromDexRegister(regs.get(1));
                            if (thisRegType.isUninitializedThisReference()) {
                                return RES_ERROR_UNSUPPORT_PREINIT_INS;
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }

        if (initInvokeIdxList.size() != 1) {
            return RES_ERROR_ONE_MORE_OR_LESS_INIT_INVOKE_INS;
        }

        int thisInitInvokeIdx = initInvokeIdxList.get(0);

        spliteInfo.thisOrSuperMethodRef =
                (DexConst.ConstMethodRef)((DexConstInsnNode)insns.get(thisInitInvokeIdx)).getConst();

        for (int insIdx = 0; insIdx < thisInitInvokeIdx; insIdx++) {
            DexInsnNode insNode = insns.get(insIdx);
            InstructionInfo insnsInfo = InstructionInfo.infoForIns(insNode);

            if (insNode instanceof DexLabelNode) {
                DexLabelNode testLabelNode = (DexLabelNode)insNode;
                for (int i = thisInitInvokeIdx + 1; i < insns.size(); i++) {
                    DexInsnNode testInsNode = insns.get(i);
                    if (testInsNode instanceof DexTargetInsnNode) {
                        DexTargetInsnNode testTargetIns = (DexTargetInsnNode)testInsNode;
                        if (testTargetIns.getTarget() == testLabelNode) {
                            return RES_ERROR_NON_LINEAR_INS;
                        }
                    }
                }
                List<DexTryCatchNode> tries = codeNode.getTryCatches();
                if (tries != null) {
                    for (int tryIdx = 0; tryIdx < tries.size(); tryIdx++) {
                        DexTryCatchNode tryCatchNode = tries.get(tryIdx);
                        if (tryCatchNode.getStart() == testLabelNode ||
                                tryCatchNode.getEnd() == testLabelNode) {
                            return RES_ERROR_NON_LINEAR_INS;
                        }
                        if (tryCatchNode.getCatchAllHandler() == testLabelNode) {
                            return RES_ERROR_NON_LINEAR_INS;
                        }
                        DexLabelNode[] handlers = tryCatchNode.getHandlers();
                        if (handlers != null) {
                            for (DexLabelNode handler : handlers) {
                                if (handler == testLabelNode) {
                                    return RES_ERROR_NON_LINEAR_INS;
                                }
                            }
                        }
                    }
                }

            } else if (insNode instanceof DexOpcodeInsnNode) {
                DexOpcodeInsnNode opcodeInsn = (DexOpcodeInsnNode)insNode;
                Dop dop = Dops.dopFor(opcodeInsn.getOpcode());
                if (dop.opcode == Dops.MONITOR_ENTER || dop.opcode == Dops.MONITOR_EXIT) {
                    return RES_ERROR_NON_LINEAR_INS;
                }

                if (dop.canBranch() || dop.canReturn()) {
                    return RES_ERROR_NON_LINEAR_INS;
                }
            }
        }
        spliteInfo.initInvokeInsIdx = thisInitInvokeIdx;
        return RES_OK;
    }

    private void checkConstOp(DexCodeNode codeNode, int invokeInitIdx) {
        int constOpCount = 0;
        int simpleOpCount = 0;
        int targetOpCount = 0;
        for (int i = 0; i < invokeInitIdx; i++) {
            DexInsnNode previousIns = codeNode.getInsns().get(i);
            if (previousIns instanceof DexOpcodeInsnNode) {
                if (previousIns instanceof DexConstInsnNode) {
                    constOpCount++;
                } else if (previousIns instanceof DexSimpleInsnNode) {
                    simpleOpCount++;
                } else if (previousIns instanceof DexTargetInsnNode) {
                    targetOpCount++;
                }
            }
        }

        if (targetOpCount > 0) {
            System.out.println("target count = " + targetOpCount + " " + initMethodNode);
        }

        if (simpleOpCount > 0) {
            System.out.println("simple count = " + simpleOpCount + " " + initMethodNode);
        }


//        if (constOpCount > 0) {
//            System.out.println("const count = " + constOpCount + " c =" + (targetOpCount + simpleOpCount) +
//                    initMethodNode);
//        }

        if (constOpCount > 0 && (simpleOpCount + targetOpCount) >0) {
            System.out.println("const count = " + constOpCount + " c =" + (targetOpCount + simpleOpCount) +
                    initMethodNode);
        }



    }

}
