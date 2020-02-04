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
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.HashSet;
import java.util.List;

/**
 * 收集所有带有DisableIntercept注解的包
 *
 * @author zhangdi07
 * @since 2017/9/10
 */

public class DisableInterceptCollector implements DexClassPoolNodeVisitor {

    private HashSet<String> mPackages = new HashSet<>();

    private TitanDexItemFactory mFactory;

    public DisableInterceptCollector(TitanDexItemFactory factory) {
        this.mFactory = factory;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        String typeDesc = dcn.type.toTypeDescriptor();
        if (typeDesc.endsWith("package-info;")) {
            List<DexAnnotationNode> dans = dcn.getClassAnnotations();
            if (dans != null && dans.size() > 0) {
                for (DexAnnotationNode dan : dans) {
                    if (mFactory.annotationClasses.disableInterceptAnnotation.type.equals(dan.getType())) {
                        mPackages.add(typeDesc);
                    }
                }
            }
        }
    }

    @Override
    public void classPoolVisitEnd() {

    }

    public boolean contains(String packageInfoDesc) {
        return mPackages.contains(packageInfoDesc);
    }


}
