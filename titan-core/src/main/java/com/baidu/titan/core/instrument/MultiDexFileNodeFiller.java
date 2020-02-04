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


import com.baidu.titan.core.Constant;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFileNode;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加DexFileNode到MultiDexfileNode中
 *
 * @author zhangdi07
 * @since 2017/9/19
 */
public class MultiDexFileNodeFiller implements DexClassPoolNodeVisitor {

    private MultiDexFileNode mMultiDexFileNode;

    private Map<Integer, DexFileNode> mDexFiles = new HashMap<>();

    private List<DexClassNode> mLeftClasses = new ArrayList<>();

    public MultiDexFileNodeFiller(MultiDexFileNode mdfn) {
        this.mMultiDexFileNode = mdfn;
    }

    private DexFileNode getOrCreateDexFileNode(int dexId) {
        DexFileNode dfn = mDexFiles.get(dexId);
        if (dfn == null) {
            dfn = new DexFileNode();
            mDexFiles.put(dexId, dfn);
            this.mMultiDexFileNode.addDexFile(dexId, dfn);
        }
        return dfn;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        int dexId = dcn.getExtraInfo(Constant.EXTRA_KEY_DEX_ID, -1);
        if (dexId < 0) {
            mLeftClasses.add(dcn);
        } else {
            DexFileNode dfn = getOrCreateDexFileNode(dexId);
            dfn.addClass(dcn);
        }


    }

    @Override
    public void classPoolVisitEnd() {
        int nextDexId = 1;
        for (Map.Entry<Integer, DexFileNode> entry: mDexFiles.entrySet()){
            if (nextDexId < entry.getKey()) {
                nextDexId = entry.getKey();
            }
        }
        nextDexId++;
        DexFileNode dfn = getOrCreateDexFileNode(nextDexId);
        mLeftClasses.forEach(dfn::addClass);

    }
}
