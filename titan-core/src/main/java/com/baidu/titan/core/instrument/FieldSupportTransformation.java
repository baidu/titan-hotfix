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
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 *
 * 在插桩阶段，通过此Transformation之后，能够支持新增字段的热修复
 *
 * @author zhangdi07@baidu.com
 * @since 2018/5/21
 */
public class FieldSupportTransformation implements DexClassPoolNodeVisitor {

    private TitanDexItemFactory mFactory;

    public FieldSupportTransformation(TitanDexItemFactory factory) {
        this.mFactory = factory;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        addFieldHolder(dcn);
    }

    private void addFieldHolder(DexClassNode dcn) {
        DexFieldNode fieldHolderField = new DexFieldNode(
                mFactory.instrumentedClass.fieldHolderFieldName,
                mFactory.fieldHolderClass.type,
                dcn.type,
                new DexAccessFlags(
                        DexAccessFlags.ACC_PUBLIC,
                        DexAccessFlags.ACC_SYNTHETIC,
                        DexAccessFlags.ACC_TRANSIENT));

        dcn.addField(fieldHolderField);
    }

    @Override
    public void classPoolVisitEnd() {

    }

}
