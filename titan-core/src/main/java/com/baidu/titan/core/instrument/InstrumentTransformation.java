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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.instrument.transforms.ComponentDirectInstanceInitMethodTransformation;
import com.baidu.titan.core.instrument.transforms.NormalInstanceInitMethodTransformation;
import com.baidu.titan.core.instrument.transforms.NormalMethodTransformation;
import com.baidu.titan.core.instrument.transforms.StaticInitMethodTransformation;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.HashSet;

/**
 * 代码插装相关逻辑。根据前面流程
 *
 * 在此时，所有方法、字段等成员都已经确定，不能再进行增减删工作，此处进行类内方法id的分配工作。
 *
 * @author zhangdi07
 * @since 2017/9/14
 */

public class InstrumentTransformation implements DexClassPoolNodeVisitor, DexClassNodeVisitor {

    private DexClassNode mCurrentClassNode;

    private TitanDexItemFactory mFactory;

    private final HashSet<String> mSpecialParameterSet = new HashSet<>();

    private boolean mInstrumentInitMethod;

    private InstrumentType mInstrumentType;

    private ApplicationDexPool mAppPool;


    public InstrumentTransformation(TitanDexItemFactory factory,
                                    boolean instrumentInitMethod,
                                    InstrumentType instrumentType,
                                    ApplicationDexPool appPool,
                                    String[] invokeSpecialParas) {
        this.mFactory = factory;
        this.mInstrumentInitMethod = instrumentInitMethod;
        this.mInstrumentType = instrumentType;
        this.mAppPool = appPool;
        if (invokeSpecialParas != null) {
            for (String para : invokeSpecialParas) {
                mSpecialParameterSet.add(para);
            }
        }
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        this.mCurrentClassNode = dcn;
        // 给当前类的所有方法分配methodId
        // attention : 此处之后，不允许在修改类成员了
        MethodIdAssigner.assignMethodId(dcn);

        dcn.accept(this);
    }



    @Override
    public void classPoolVisitEnd() {

    }

    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {
        // 忽略标记为无需插桩的方法
        boolean disableInterceptMethod = DisableInterceptMarker.getInterceptDisable(dmn);
        if (disableInterceptMethod) {
            return;
        }


        AndroidComponentFlag componentFlag =
                AndroidComponentMarker.getComponentFlag(mCurrentClassNode);

        DexCodeNode dcn = dmn.getCode();

        DexCodeNode newDcn = new DexCodeNode();

        boolean instanceInit = dmn.isInstanceInitMethod();
        boolean staticInit = dmn.isStaticInitMethod();

        if (instanceInit) {
            // 处理<init>方法逻辑，这里分为多种情况：

            boolean doNormalInitMethodInstrument = false;
            boolean doDirectComponentInitMethodInstrument = false;

            if (componentFlag != null) {
                // case 1：如果是组件类的构造函数：
                boolean wormHoleMethod = InstrumentGenesisClassHierarchy.isWormHoleInitMethod(dmn);

                boolean directInitMethod = InstrumentGenesisClassHierarchy
                        .isComponentDirectInitMethod(dmn);

                if (wormHoleMethod) {
                    // case 1.1 如果是*虫洞*方法，则不进行插装
                    // skip, do noting
                    doNormalInitMethodInstrument = false;
                    doDirectComponentInitMethodInstrument = false;
                } else if (directInitMethod) {
                    // case 1.2 如果是*直接*组件的构造函数
                    if (mInstrumentType == InstrumentType.FULL) {
                        // case 1.2.1 如果插桩类型是是全量修复，
                        // 具体插桩逻辑参考ComponentDirectInstanceInitMethodTransformation的实现。
                        doNormalInitMethodInstrument = false;
                        doDirectComponentInitMethodInstrument = true;
                    } else if (mInstrumentInitMethod) {
                        // case 1.2.2 轻量修复插桩，逻辑同case 2
                        doNormalInitMethodInstrument = true;
                        doDirectComponentInitMethodInstrument = false;
                    }
                } else {
                    // case 1.3 如果是普通组件类的<init>，逻辑同case 2
                    doNormalInitMethodInstrument = mInstrumentInitMethod;
                    doDirectComponentInitMethodInstrument = false;
                }
            } else {
                // case 2: 普通<init>插桩逻辑，受mInstrumentInitMethod变量控制
                doNormalInitMethodInstrument = mInstrumentInitMethod;
                doDirectComponentInitMethodInstrument = false;
            }

            if (doNormalInitMethodInstrument || doDirectComponentInitMethodInstrument) {
                // 通过InitMethodAnalyzer找到super.<init>或this.<init>方法
                InitMethodAnalyzer ima = new InitMethodAnalyzer(dmn, dcn, mAppPool, mFactory);
                if (!ima.analyze()) {
                    throw new IllegalStateException("method analyze failed for " + dmn.toString()
                            + "smali content : " + dmn.toSmaliString());

                }

                if (doNormalInitMethodInstrument) {
                    dcn.accept(new DexCodeFormatVerifier(
                            new NormalInstanceInitMethodTransformation(
                                    newDcn.asVisitor(),
                                    mCurrentClassNode,
                                    dmn,
                                    ima.getInitMethod(),
                                    mFactory)));
                    dmn.setCode(newDcn);
                    // 自动计算寄存器数量
                    DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(dmn);
                } else if (doDirectComponentInitMethodInstrument) {
                    dcn.accept(new DexCodeFormatVerifier(
                            new ComponentDirectInstanceInitMethodTransformation(
                                    newDcn.asVisitor(),
                                    mCurrentClassNode,
                                    dmn,
                                    ima.getInitMethod(),
                                    mFactory)));

                    dmn.setCode(newDcn);
                    DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(dmn);
                }

            }

        } else if (staticInit) {
            // 处理<clinit>相关插桩逻辑
            dcn.accept(new DexCodeFormatVerifier(
                    new StaticInitMethodTransformation(
                            newDcn.asVisitor(),
                            mCurrentClassNode,
                            dmn,
                            mFactory)));

            dmn.setCode(newDcn);
            DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(dmn);
        } else {
            // 处理非<init>和<clinit>之外的插桩逻辑
            dcn.accept(new DexCodeFormatVerifier(
                    new NormalMethodTransformation(
                            newDcn.asVisitor(),
                            mCurrentClassNode,
                            dmn,
                            mFactory,
                            mSpecialParameterSet)));
            dmn.setCode(newDcn);
            DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(dmn);
        }
    }

    @Override
    public void visitField(DexFieldNode dfn) {

    }

    @Override
    public void visitClassNodeEnd() {

    }

}
