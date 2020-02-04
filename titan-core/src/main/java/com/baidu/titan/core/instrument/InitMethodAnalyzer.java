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

package com.baidu.titan.core.instrument;

import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.analyze.InstructionInfo;
import com.baidu.titan.dex.analyze.MethodAnalyzer;
import com.baidu.titan.dex.analyze.types.RegType;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.insn.DexConstInsnNode;
import com.baidu.titan.dex.node.insn.DexInsnNode;

import java.util.ArrayList;
import java.util.List;


/**
 * a helper method to analyze init method.
 *
 * @author zhangdi07
 * @since 2017/9/12
 */

public class InitMethodAnalyzer {

    private DexCodeNode mInitCodeNode;

    private DexMethodNode mMethodNode;

    private DexConst.ConstMethodRef mInitMethodRef;

    private ApplicationDexPool mAppPool;

    private DexItemFactory mFactory;

    public InitMethodAnalyzer(DexMethodNode methodNode, DexCodeNode initCodeNode,
                              ApplicationDexPool appPool, DexItemFactory factory) {
        super();
        this.mMethodNode = methodNode;
        this.mInitCodeNode = initCodeNode;
        this.mAppPool = appPool;
        this.mFactory = factory;
    }

    public boolean analyze() {
        MethodAnalyzer analyzer = new MethodAnalyzer(this.mMethodNode, new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return InitMethodAnalyzer.this.mAppPool.findClassFromAll(type);
            }
        });

        try {
            boolean success = analyzer.analyze();

            if (success) {
                DexCodeNode analyzedCodeNode = analyzer.getAnalyzedCode();
                List<DexInsnNode> insns = analyzedCodeNode.getInsns();
                List<DexConst.ConstMethodRef> initMethods = new ArrayList<>();
                for (DexInsnNode insNode : insns) {
                    if (insNode instanceof DexConstInsnNode) {
                        DexConstInsnNode constInsnNode = (DexConstInsnNode)insNode;
                        InstructionInfo insInfo = InstructionInfo.infoForIns(constInsnNode);
                        switch (constInsnNode.getOpcode()) {
                            case Dops.INVOKE_DIRECT:
                            case Dops.INVOKE_DIRECT_RANGE: {
                                DexRegisterList regs = constInsnNode.getRegisters();
                                DexConst.ConstMethodRef calledMethod =
                                        (DexConst.ConstMethodRef) constInsnNode.getConst();

                                if (calledMethod.getName().equals(mFactory.methods.initMethodName)) {
                                    DexRegister thisReg = regs.get(0);
                                    RegType thisRegType =
                                            insInfo.registerPc.getRegTypeFromDexRegister(thisReg);

                                    if (thisRegType.isUninitializedThisReference()) {
                                        initMethods.add(calledMethod);
                                    }
                                }
                            }

                        }
                    }
                }
                if (initMethods.size() != 1) {
                    return false;
                }
                this.mInitMethodRef = initMethods.get(0);
                return true;
            } else {
                return false;
            }
        } finally {
            analyzer.clear();
        }
    }


    public DexConst.ConstMethodRef getInitMethod() {
        return mInitMethodRef;
    }

}
