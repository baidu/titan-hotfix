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

package com.baidu.titan.core.pool;


import com.baidu.titan.core.Constant;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexClassPoolNode;
import com.baidu.titan.dex.node.DexFileNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;
import com.baidu.titan.dex.visitor.MultiDexFileNodeVisitor;

/**
 * 用于标记类属于哪一个dex
 *
 * @author zhangdi07
 * @since 2017/9/13
 */
public class DexClassPoolFiller implements DexClassPoolNodeVisitor, MultiDexFileNodeVisitor {

    private final DexClassPoolNode mClassPool;

    private int mDexId;

    public DexClassPoolFiller(DexClassPoolNode dcp) {
        this.mClassPool = dcp;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        dcn.setExtraInfo(Constant.EXTRA_KEY_DEX_ID, mDexId);
        this.mClassPool.addClass(dcn);
    }

    @Override
    public void classPoolVisitEnd() {

    }

    @Override
    public void visitDexFile(int dexId, DexFileNode dfn) {
        mDexId = dexId;
        dfn.accept(this);
    }

}
