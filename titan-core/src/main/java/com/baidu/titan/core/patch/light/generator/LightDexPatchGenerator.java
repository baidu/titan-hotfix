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

package com.baidu.titan.core.patch.light.generator;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.light.LightPatchClassPools;
import com.baidu.titan.core.patch.light.diff.ClassPoolDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffMode;
import com.baidu.titan.core.patch.light.generator.changed.LightChangedClassGenerator;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 *
 * Dex Patch生成入口类，该类负责所有新增和修改类的Patch生成逻辑。
 *
 * @author zhangdi07@baidu.com
 * @since 2018/5/4
 */
public class LightDexPatchGenerator implements DexClassPoolNodeVisitor {

    private DexClassLoader mNewClassLoader;

    private DexClassLoader mOldClassLoader;

    private LightPatchClassPools mClassPools;

    private static final int VISIT_MODE_NEW_CLASS_POOL = 1;

    private static final int VISIT_MODE_OLD_CLASS_POOL = 2;

    private int mVisitMode;

    private TitanDexItemFactory mDexFactory;

    private PatchArgument mPatchArgument;

    public LightDexPatchGenerator(LightPatchClassPools classPools,
                                  TitanDexItemFactory dexFactory,
                                  PatchArgument patchArgument) {
        this.mClassPools = classPools;
        this.mDexFactory = dexFactory;
        this.mPatchArgument = patchArgument;
    }

    /**
     * 生成Dex Light Patch，生成过程中将相关类放到具体的Class Pool中
     */
    public void generate() {
        this.mVisitMode = VISIT_MODE_NEW_CLASS_POOL;
        this.mClassPools.newOrgClassPool.acceptProgram(this);
        this.mVisitMode = VISIT_MODE_OLD_CLASS_POOL;
        this.mClassPools.oldOrgClassPool.acceptProgram(this);

        // 生成clinit interceptor
        LightClassClinitInterceptorGenerator.generate(mClassPools, mDexFactory);
        // 生成patchloader
        LightPatchLoaderGenerator.generate(mClassPools, mDexFactory);
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        switch (mVisitMode) {
            case VISIT_MODE_NEW_CLASS_POOL: {
                visitClassFromNewPool(dcn);
                break;
            }
            case VISIT_MODE_OLD_CLASS_POOL: {
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void classPoolVisitEnd() {

    }

    private void visitClassFromNewPool(DexClassNode newClassNode) {
        DexClassNode rewriteClassNode = ClassPoolDiffMarker.getRewriteClass(newClassNode);
        if (rewriteClassNode != null) {
            // 替换为rewrite class并生成patch
            newClassNode = rewriteClassNode;
        }
        DiffMode diff = ClassPoolDiffMarker.getClassDiffMode(newClassNode);
        if (diff.isAdded()) {
            // 新增类
            LightAddedClassGenerator.generate(
                    newClassNode,
                    mClassPools,
                    mDexFactory);
        } else if (diff.isChanged()) {
            // 变化类
            LightChangedClassGenerator changedClassGenerator = new LightChangedClassGenerator(
                    mPatchArgument,
                    mClassPools.oldOrgClassPool.getProgramClassPool().getClass(newClassNode.type),
                    newClassNode,
                    this.mClassPools,
                    this.mDexFactory);
            changedClassGenerator.generate();
        } else if (diff.isRemoved()) {
            // 被移除的类
        } else if (diff.isUnChanged()) {
            // 未变化的类
        }
    }

}
