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
public class AddedFieldDiffMarker {

    private DexClassNode mDexClassNode;

    private DexFieldNode mNewFieldNode;

    private DiffStatus mDiffStatus;

    public AddedFieldDiffMarker(DexClassNode dcn, DexFieldNode newField) {
        this.mDexClassNode = dcn;
        this.mNewFieldNode = newField;
    }


    public DiffStatus diff() {
        mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;
        return mDiffStatus;
    }



}
