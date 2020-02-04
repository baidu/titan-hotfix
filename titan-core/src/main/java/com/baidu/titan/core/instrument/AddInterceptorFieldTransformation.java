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
 * 添加Interceptor字段
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/8/21
 */
public class AddInterceptorFieldTransformation implements DexClassPoolNodeVisitor {

    private TitanDexItemFactory mFactory;

    public AddInterceptorFieldTransformation(TitanDexItemFactory factory) {
        this.mFactory = factory;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        addInterceptorField(dcn);
    }

    private void addInterceptorField(DexClassNode dcn) {
        int accessFlags = DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC
                | DexAccessFlags.ACC_SYNTHETIC;

        DexFieldNode interceptorField = new DexFieldNode(
                mFactory.instrumentedClass.interceptorFieldName,
                mFactory.interceptableClass.type,
                dcn.type,
                new DexAccessFlags(accessFlags));

        dcn.addField(interceptorField);
    }

    @Override
    public void classPoolVisitEnd() {

    }

}
