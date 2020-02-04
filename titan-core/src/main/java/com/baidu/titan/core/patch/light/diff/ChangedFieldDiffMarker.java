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

package com.baidu.titan.core.patch.light.diff;

import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/10/25
 */
public class ChangedFieldDiffMarker {

    private DexClassNode mNewOrgClassNode;

    private DexFieldNode mNewFieldNode;

    private DexClassNode mOldOrgClassNode;

    private DexFieldNode mOldFieldNode;

    private DiffStatus mDiffStatus;

    public ChangedFieldDiffMarker(DexClassNode newOrgClassNode,
                                  DexFieldNode newFieldNode,
                                  DexClassNode oldOrgClassNode,
                                  DexFieldNode oldFieldNode) {
        this.mNewOrgClassNode = newOrgClassNode;
        this.mNewFieldNode = newFieldNode;
        this.mOldOrgClassNode = oldOrgClassNode;
        this.mOldFieldNode = oldFieldNode;
    }

    public DiffStatus diff() {
        return mDiffStatus;
    }

}
