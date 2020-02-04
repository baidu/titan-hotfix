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

package com.baidu.titan.core.patch.light.generator.changed;

import com.baidu.titan.core.Constant;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
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
import java.util.Arrays;
import java.util.List;

/**
 * 对<init>方法进行分割，分割的原因是，根据jvm规范，this.<init>和super.<init>方法只能在<init>方法中执行，如果与普通方法一样，
 * 将方法实现全部复制到$chg类中，会导致调用出现错误，所以需要将<init>方法分为三段：
 * 1. uninit方法，包括super.<init>或者this.<init>指令之前的所有指令
 * 2. super.<init>或者this.<init>的调用指令，在插桩时由插桩逻辑调用
 * 3. initBody方法，包括super.<init>或者this.<init>指令之后的所有指令
 *
 * 分为三段后，需要对寄存器的值进行保存和恢复，在uninit中指令执行完后，将所有用到的local寄存器和parameter寄存器保存到initContext.locals数组，
 * 在initBody中根据initContext中保存的数据对寄存器进行恢复。
 * 同时，super.<init>需要的寄存器会单独存放到initContext.callArgs数组，便于插桩逻辑恢复执行uninit方法后的super.<init>需要的寄存器
 *
 * 由于super.<init>在插桩逻辑中调用，为便于处理，单独提供了callArgs数组，与locals数组可能存在重复，但这种重复避免了插桩逻辑与patch生成逻辑的过度耦合
 * 降低了代码的复杂度
 *
 * @author shanghuibo@baidu.com
 * @since 2018/4/2
 */
public class LightInitMethodSplitter {

    private final LightChangedClassGenerator host;

    private DexClassNode changedClassNode;

    private DexMethodNode initMethodNode;

    private DexClassLoader classLoader;

    private DexClassLoader oldInstrumentClassLoader;

    private TitanDexItemFactory mFactory;

    private SpliteInfo mSpliteInfo;

    private static final int RES_OK = 0;

    private static final int RES_ERROR_ONE_MORE_OR_LESS_INIT_INVOKE_INS = -1;

    private static final int RES_ERROR_NON_LINEAR_INS = -2;

    private static final int RES_ERROR_UNSUPPORT_PREINIT_INS = -3;

    private static final int RES_ERROR_UNKNOWN = -4;



    LightInitMethodSplitter(LightChangedClassGenerator host,
                            DexClassNode changedClassNode, DexMethodNode methodNode,
                            DexClassLoader loader,
                            DexClassLoader oldInstrumentLoader,
                            TitanDexItemFactory factory) {
        this.changedClassNode = changedClassNode;
        this.initMethodNode = methodNode;
        this.classLoader = loader;
        this.oldInstrumentClassLoader = oldInstrumentLoader;
        this.mFactory = factory;
        this.host = host;
    }

    private static class SpliteInfo {

        int initInvokeInsIdx = -1;

        DexMethodNode unInitMethodNode;

        DexMethodNode initBodyMethodNode;

        public InstructionInfo initInvokeInsInfo;

        DexConst.ConstMethodRef thisOrSuperMethodRef;

        public DexRegisterList savedRegs;

        public int minUndefinedLocalReg;

        int localRegArrayLength;

    }

    public DexMethodNode getUnInitMethod() {
        return mSpliteInfo.unInitMethodNode;
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

        res = buildUnInitMethod(analyzedCode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }
        res = buildInitBodyMethod(analyzedCode, spliteInfo);
        if (res != RES_OK) {
            return res;
        }

        return res;

    }

    private DexCodeNode buildFirstHalfUnInitCodeNode(DexCodeNode orgCodeNode, SpliteInfo spliteInfo) {
        DexCodeNode unInitCodeNode = new DexCodeNode();


        List<DexInsnNode> unInitIns = new ArrayList<>();
        unInitCodeNode.setInsns(unInitIns);
        List<DexLineNumberNode> unInitLines = new ArrayList<>();
        unInitCodeNode.setLineNumbers(unInitLines);
        List<DexTryCatchNode> unInitTries = new ArrayList<>();
        unInitCodeNode.setTryCatches(unInitTries);

        int unInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;

        unInitCodeNode.setRegisters(orgCodeNode.getLocalRegCount(), unInitParaRegCount);

        for (int insIdx = 0; insIdx < spliteInfo.initInvokeInsIdx; insIdx++) {
            DexInsnNode insnNode = orgCodeNode.getInsns().get(insIdx);

            if (insnNode instanceof DexLabelNode) {
                DexLabelNode labelNode = (DexLabelNode)insnNode;
                if (orgCodeNode.getLineNumbers() != null) {
                    for (DexLineNumberNode lineNumNode : orgCodeNode.getLineNumbers()) {
                        if (lineNumNode.getStartLabel() == labelNode) {
                            unInitLines.add(lineNumNode);
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
                            unInitTries.add(tryCatchNode);
                        }
                    }
                }

                unInitIns.add(insnNode);
            } else {
                unInitIns.add(insnNode);
            }
        }

        return unInitCodeNode;

    }

    private int buildUnInitMethod(DexCodeNode orgCodeNode, SpliteInfo spliteInfo) {
        DexTypeList.Builder newParaBuilder = DexTypeList.newBuilder();

        newParaBuilder.addType(initMethodNode.owner);
        initMethodNode.parameters.forEach(newParaBuilder::addType);
        newParaBuilder.addType(mFactory.initContextClass.type);

        DexMethodNode unInitMethodNode = new DexMethodNode(
                mFactory.changedClass.instanceUnInitMethodName,
                changedClassNode.type,
                mFactory.intern(newParaBuilder.build()),
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_STATIC));

        DexCodeNode preInitFirstHalfCodeNode =
                buildFirstHalfUnInitCodeNode(orgCodeNode, spliteInfo);

        DexCodeNode unInitCodeNode = new DexCodeNode();

        unInitMethodNode.setCode(unInitCodeNode);

        DexCodeVisitor unInitCodeVisitor = unInitCodeNode.asVisitor();

        preInitFirstHalfCodeNode.accept(new DexCodeFormatVerifier(
                new InstanceInitChangedCodeRewriter(
                        new DexCodeVisitor(unInitCodeVisitor) {

                            @Override
                            public void visitRegisters(int localRegCount, int parameterRegCount) {
                                super.visitRegisters(localRegCount, parameterRegCount);
                            }

                            private void newArray(final int vParaArrayReg, int arraySize, int pDstObjectReg, DexConst.ConstFieldRef fieldRef) {
                                if (arraySize > 0) {
                                    final int vParaSizeReg = vParaArrayReg;

                                    super.visitConstInsn(
                                            Dops.CONST,
                                            DexRegisterList.make(DexRegister.makeLocalReg(vParaSizeReg)),
                                            DexConst.LiteralBits32.make(arraySize));

                                    super.visitConstInsn(
                                            Dops.NEW_ARRAY,
                                            DexRegisterList.make(
                                                    DexRegister.makeLocalReg(vParaArrayReg),
                                                    DexRegister.makeLocalReg(vParaSizeReg)),
                                            DexConst.ConstType.make(
                                                    mFactory.createArrayType(mFactory.objectClass.type)));

                                    super.visitConstInsn(
                                            Dops.IPUT_OBJECT,
                                            DexRegisterList.make(
                                                    DexRegister.makeLocalReg(vParaArrayReg),
                                                    DexRegister.makeParameterReg(pDstObjectReg)),
                                            fieldRef);
                                }
                            }

                            private void visitSaveLocalAndParameterRegs() {

                                // unInit方法参数数量，包括最后的initContext
                                int unInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                                // initContext参数在最后
                                int pInitContextReg = unInitParaRegCount - 1;

                                // 使用未定义的local寄存器来存取数组
                                int vLocalsArrayReg = spliteInfo.minUndefinedLocalReg;
                                int vArrayIdxReg = spliteInfo.minUndefinedLocalReg + 1;
                                int vLocalsTmpResultReg = spliteInfo.minUndefinedLocalReg + 2;

                                DexRegisterList.Builder regsBuilder = DexRegisterList.newBuilder();

                                // 计算当前用到的local与param reg数量
                                // 创建initContext.locals 数组,将最后一条指令的registerPc中的寄存器值存到locals数组中
                                // 这个应该用initInvokeInsInfo的registerPc就可以，initInvoke方法不会改变寄存器状态
                                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;
//                        newArray(vLocalsArrayReg, registerPc.localRegs.length + registerPc.paraRegs.length,
//                                pInitContextReg, mFactory.initContextClass.localsField);

                                // 将需要存储的local寄存器保存到registerlist中
                                for (int localReg = 0; localReg < registerPc.mLocalRegs.length; localReg ++) {
                                    DexRegister dexRegister = getSaveToLocalReg(registerPc, localReg, DexRegister.REG_REF_LOCAL);
                                    if (dexRegister != null) {
                                        regsBuilder.addReg(dexRegister);
                                    }
                                }

                                // 将需要存储的parameter寄存器保存到registerlist中
                                for (int paraReg = 0; paraReg < registerPc.mParaRegs.length; paraReg ++) {
                                    DexRegister dexRegister = getSaveToLocalReg(registerPc, paraReg, DexRegister.REG_REF_PARAMETER);
                                    if (dexRegister != null) {
                                        regsBuilder.addReg(dexRegister);
                                    }
                                }

                                spliteInfo.savedRegs = regsBuilder.build();

                                newArray(vLocalsArrayReg, spliteInfo.savedRegs.count(), pInitContextReg,
                                        mFactory.initContextClass.localsField);

                                // 将需要保存的寄存器值存储到initContext.locals数组中
                                for (int reg = 0; reg < spliteInfo.savedRegs.count(); reg ++) {
                                    DexRegister dexRegister = spliteInfo.savedRegs.get(reg);
                                    RegType regType = registerPc.getRegTypeFromDexRegister(dexRegister);
                                    DexType type = regType.getDexType();

                                    DexConst.ConstMethodRef valueOfMethod = null;

                                    if (regType.isConstantTypes()) {
                                        if (!regType.isPrecise()) {
                                            throw new IllegalStateException();
                                        }

                                       continue;
                                    } else if (regType.isLowHalf() || regType.isHighHalf()) {
                                        if (regType.isLowHalf()) {
                                            if (regType.isLongLo()) {
                                                valueOfMethod = mFactory.longClass.valueOfMethod;
                                            } else if (regType.isDoubleLo()) {
                                                valueOfMethod = mFactory.doubleClass.valueOfMethod;
                                            }
                                        }
                                    } else if (regType.isCategory1Types()) {
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
                                    }
                                        DexRegister localsReg;
                                        if (valueOfMethod != null) {
                                            localsReg = DexRegister.makeLocalReg(vLocalsTmpResultReg);
                                            // 对值类型进行box操作并存到原结果寄存器中
                                            super.visitConstInsn(Dops.INVOKE_STATIC,
                                                    DexRegisterList.make(dexRegister),
                                                    mFactory.methods.valueOfMethodForType(type));
                                            super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                                                    DexRegisterList.make(localsReg));
                                        } else {
                                            localsReg = dexRegister;
                                        }

                                    super.visitConstInsn(Dops.CONST,
                                            DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                            DexConst.LiteralBits32.make(reg));

                                    super.visitSimpleInsn(Dops.APUT_OBJECT,
                                            DexRegisterList.make(
                                                    localsReg,
                                                    DexRegister.makeLocalReg(vLocalsArrayReg),
                                                    DexRegister.makeLocalReg(vArrayIdxReg)));
                                }
                            }

                            private DexRegister getSaveToLocalReg(RegisterPc registerPc, int reg, int ref) {
                                RegType regType = registerPc
                                        .getRegTypeFromDexRegister(DexRegister.make(reg, DexRegister.REG_WIDTH_ONE_WORD, ref));
                                int regWidth = 0;

                                // 判断是否要保存寄存器值，并获取boxed函数
                                if (regType.isUndefined()) {
                                    regWidth = 0;
                                } else if (regType.isConstantTypes()) {
                                    if (!regType.isPrecise()) {
                                        throw new IllegalStateException();
                                    }

                                    if (regType.isConstantLo()) {
                                        regWidth = 2;
                                    } else if (regType.isPreciseConstantHi()) {
                                        regWidth = 0;
                                    } else if (regType.isConstant()) {
                                        regWidth = 1;
                                    }
                                } else if (regType.isLowHalf() || regType.isHighHalf()) {
                                    if (regType.isLowHalf()) {
                                        regWidth = 2;
                                    }
                                } else if (regType.isCategory1Types()) {
                                    regWidth = 1;
                                } else if (regType.isReferenceTypes()) {
                                    if (!regType.isUninitializedTypes()) {
                                        regWidth = 1;
                                    }
                                }
                                if (regWidth > 0) {
                                    return DexRegister.make(reg, regWidth, ref);
                                }
                                return null;
                            }

                            private void visitSaveCallArgsRegs() {
                                final int preInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                                final int pInitContextReg = preInitParaRegCount - 1;

                                final int vCallArgsArrayReg = spliteInfo.minUndefinedLocalReg;
                                final int vArrayIdxReg = spliteInfo.minUndefinedLocalReg + 1;

                                final int vTmpValueOfResultReg = spliteInfo.minUndefinedLocalReg + 2;

                                // 创建initContext.callArgs数组，callArgs数组中保存调用superOrThis.<init>时需要的寄存器
                                // 需要知道superOrThis.<init>用了哪些寄存器，将这些寄存器值保存下来

                                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;
                                DexConstInsnNode invokeInitNode = (DexConstInsnNode) spliteInfo.initInvokeInsInfo.attachedInsNode;
                                DexRegisterList regs = invokeInitNode.getRegisters();
                                DexConst.ConstMethodRef methodRef = (DexConst.ConstMethodRef) invokeInitNode.getConst();

                                DexTypeList types = methodRef.getParameterTypes();
                                newArray(vCallArgsArrayReg,
                                        regs.count() - 1, //第一个参数为this，要干掉
                                        pInitContextReg, mFactory.initContextClass.callArgsField);


                                for (int paraIdx = 0; paraIdx < types.count(); paraIdx++) {
                                    final DexType type = types.getType(paraIdx);
                                    final DexRegister reg = regs.get(paraIdx + 1);

                                    DexRegister paraReg;

                                    if (type.isPrimitiveType()) {
                                        paraReg = DexRegister.makeLocalReg(vTmpValueOfResultReg);
                                        // 对值进行box操作并存到原寄存器中
                                        super.visitConstInsn(Dops.INVOKE_STATIC,
                                                DexRegisterList.make(reg),
                                                mFactory.methods.valueOfMethodForType(type));
                                        super.visitSimpleInsn(Dops.MOVE_RESULT_OBJECT,
                                                DexRegisterList.make(paraReg));
                                    } else {
                                        paraReg = reg;
                                    }

                                    super.visitConstInsn(Dops.CONST,
                                            DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                            DexConst.LiteralBits32.make(paraIdx));

                                    super.visitSimpleInsn(Dops.APUT_OBJECT,
                                            DexRegisterList.make(
                                                    paraReg,
                                                    DexRegister.makeLocalReg(vCallArgsArrayReg),
                                                    DexRegister.makeLocalReg(vArrayIdxReg)));

                                }
                            }

                            @Override
                            public void visitEnd() {

                                visitSaveLocalAndParameterRegs();
                                visitSaveCallArgsRegs();
                                visitSetInterceptFlag();

                                super.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());
                                super.visitEnd();
                            }

                            private void visitSetInterceptFlag() {
                                final int preInitParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                                final int pInitContextReg = preInitParaRegCount - 1;

                                final int vOldFlagReg = spliteInfo.minUndefinedLocalReg;

                                super.visitConstInsn(
                                        Dops.IGET,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(vOldFlagReg),
                                                DexRegister.makeParameterReg(pInitContextReg)),
                                        mFactory.initContextClass.flagField);

                                super.visitConstInsn(Dops.OR_INT_LIT8,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(vOldFlagReg),
                                                DexRegister.makeLocalReg(vOldFlagReg)),
                                        DexConst.LiteralBits32.make(Constant.INIT_CONTEXT_FLAG_INTERCEPTED));

                                super.visitConstInsn(
                                        Dops.IPUT,
                                        DexRegisterList.make(
                                                DexRegister.makeLocalReg(vOldFlagReg),
                                                DexRegister.makeParameterReg(pInitContextReg)),
                                        mFactory.initContextClass.flagField);
                            }

                        }, classLoader, oldInstrumentClassLoader, mFactory, host, orgCodeNode)));

        DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(unInitMethodNode);

        spliteInfo.unInitMethodNode = unInitMethodNode;

        return RES_OK;
    }

    private DexCodeNode buildSecondHalfInitBodyCodeNode(DexCodeNode orgCodeNode,
                                                        SpliteInfo spliteInfo) {
        List<DexInsnNode> orgInsList = orgCodeNode.getInsns();
        DexCodeNode initBodyCodeNode = new DexCodeNode();


        List<DexInsnNode> initBodyIns = new ArrayList<>();
        initBodyCodeNode.setInsns(initBodyIns);
        List<DexLineNumberNode> initBodyLines = new ArrayList<>();
        initBodyCodeNode.setLineNumbers(initBodyLines);
        List<DexTryCatchNode> initBodyTries = new ArrayList<>();
        initBodyCodeNode.setTryCatches(initBodyTries);


        for (int insIdx = spliteInfo.initInvokeInsIdx + 1; insIdx < orgInsList.size(); insIdx++) {
            DexInsnNode insnNode = orgInsList.get(insIdx);

            if (insnNode instanceof DexLabelNode) {
                DexLabelNode labelNode = (DexLabelNode)insnNode;
                if (orgCodeNode.getLineNumbers() != null) {
                    for (DexLineNumberNode lineNumNode : orgCodeNode.getLineNumbers()) {
                        if (lineNumNode.getStartLabel() == labelNode) {
                            initBodyLines.add(lineNumNode);
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
                            initBodyTries.add(tryCatchNode);
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

        newParaBuilder.addType(initMethodNode.owner);
        initMethodNode.parameters.forEach(newParaBuilder::addType);

        newParaBuilder.addType(mFactory.initContextClass.type);

        DexMethodNode initBodyMethodNode = new DexMethodNode(
                mFactory.changedClass.instanceInitBodyMethodName,
                changedClassNode.type,
                mFactory.intern(newParaBuilder.build()),
                mFactory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_STATIC));

        DexCodeNode initBodyCodeNode = new DexCodeNode();

        initBodyMethodNode.setCode(initBodyCodeNode);

        DexCodeVisitor initBodyCodeVisitor = initBodyCodeNode.asVisitor();

        secondHalfInitBodyCodeNode.accept(
                new InstanceInitChangedCodeRewriter(
                        new DexCodeVisitor(initBodyCodeVisitor) {
                            private void visitRestoreLocals() {
                                int initBodyParaRegCount = orgCodeNode.getParameterRegCount() + 1;
                                int pInitContextReg = initBodyParaRegCount - 1;

                                int vLocalsArrayReg = spliteInfo.minUndefinedLocalReg;
                                int vArrayIdxReg = spliteInfo.minUndefinedLocalReg + 1;
                                int vTmpObjReg = spliteInfo.minUndefinedLocalReg + 2;

                                // 读initContext.locals
                                super.visitConstInsn(Dops.IGET_OBJECT,
                                        DexRegisterList.make(DexRegister.makeLocalReg(vLocalsArrayReg),
                                                DexRegister.makeParameterReg(pInitContextReg)),
                                        mFactory.initContextClass.localsField);

                                RegisterPc registerPc = spliteInfo.initInvokeInsInfo.registerPc;

                                DexRegisterList registerList = spliteInfo.savedRegs;

                                for (int i = 0; i < registerList.count(); i ++) {
                                    DexRegister dexRegister = registerList.get(i);
                                    RegType regType = registerPc.getRegTypeFromDexRegister(dexRegister);
                                    DexType type = regType.getDexType();

                                    DexConst.ConstMethodRef primitiveValueMethod = null;
                                    DexType checkCastType = null;


                                    int moveResultOp = -1;

                                    if (regType.isConstantTypes()) {
                                        if (!regType.isPrecise()) {
                                            throw new IllegalStateException();
                                        }

                                        if (regType.isPreciseConstantLo()) {
                                            PreciseConstLoType constLoType = (PreciseConstLoType)regType;
                                            PreciseConstHiType constHiType =
                                                    (PreciseConstHiType)registerPc.getRegTypeFromDexRegister(
                                                            DexRegister.make(dexRegister.getReg(),
                                                                    DexRegister.REG_WIDTH_ONE_WORD,
                                                                    dexRegister.getRef()));
                                            long loValue = constLoType.constantValueLo();
                                            long hiValue = constHiType.constantValueHi();

                                            super.visitConstInsn(Dops.CONST_WIDE,
                                                    DexRegisterList.make(DexRegister.make(dexRegister.getReg(),
                                                            DexRegister.REG_WIDTH_DOUBLE_WORD,
                                                            dexRegister.getRef())),
                                                    DexConst.LiteralBits64.make(loValue | hiValue << 32));
                                        } else if (regType.isPreciseConstantHi()) {
                                            // skip
                                        } else if (regType.isPreciseConstant()) {
                                            PreciseConstType constType = (PreciseConstType)regType;
                                            super.visitConstInsn(Dops.CONST,
                                                    DexRegisterList.make(DexRegister.make(dexRegister.getReg(),
                                                            DexRegister.REG_WIDTH_ONE_WORD,
                                                            dexRegister.getRef())),
                                                    DexConst.LiteralBits32.make(constType.constantValue()));
                                        }
                                        continue;
                                    } else if (regType.isLowHalf() || regType.isHighHalf()) {
                                        if (regType.isLowHalf()) {
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
                                        checkCastType = regType.getDexType();
                                    }


                                    DexRegister restoreRegister;
                                    if (primitiveValueMethod != null) {
                                        restoreRegister = DexRegister.makeLocalReg(vTmpObjReg);
                                    } else {
                                        restoreRegister = dexRegister;
                                    }

                                    if (checkCastType != null) {
                                        super.visitConstInsn(Dops.CONST,
                                                DexRegisterList.make(DexRegister.makeLocalReg(vArrayIdxReg)),
                                                DexConst.LiteralBits32.make(i));

                                        super.visitSimpleInsn(Dops.AGET_OBJECT,
                                                DexRegisterList.make(
                                                        restoreRegister,
                                                        DexRegister.makeLocalReg(vLocalsArrayReg),
                                                        DexRegister.makeLocalReg(vArrayIdxReg)));

                                        super.visitConstInsn(Dops.CHECK_CAST,
                                                DexRegisterList.make(restoreRegister),
                                                DexConst.ConstType.make(checkCastType));
                                    }

                                    if (primitiveValueMethod != null) {
                                        super.visitConstInsn(Dops.INVOKE_VIRTUAL,
                                                DexRegisterList.make(restoreRegister),
                                                primitiveValueMethod);
                                        super.visitSimpleInsn(moveResultOp, DexRegisterList.make(dexRegister));
                                    }
                                }
                            }

                            @Override
                            public void visitBegin() {
                                // restore locals
                                visitRestoreLocals();

                                super.visitBegin();
                            }

                            @Override
                            public void visitRegisters(int localRegCount, int parameterRegCount) {

                            }

                            @Override
                            public void visitEnd() {
                                super.visitEnd();
                            }
                        }, classLoader, oldInstrumentClassLoader, mFactory, host, orgCodeNode));
        DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(initBodyMethodNode);

        initBodyCodeNode.accept(new DexCodeFormatVerifier());

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
                return RES_ERROR_UNKNOWN;
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
