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

package com.baidu.titan.core.patch.full.generator;

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * 用于生成Changed类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/18
 */
public class ChangedClassGenerator {

    public DexType orgType;

    public DexType changeType;

    public DexType changeSuperType;

    public DexItemFactory factory;

    public Set<DexNamedProtoNode> methods = new HashSet<>();


    public ChangedClassGenerator(DexType orgType, DexType changeType, DexType changeSuperType,
                                 DexItemFactory factory) {
        this.orgType = orgType;
        this.changeType = changeType;
        this.changeSuperType = changeSuperType;
        this.factory = factory;
    }

    public void addChangeMethod(DexNamedProtoNode method) {
        this.methods.add(method);
    }

    public DexClassNode generate() {
        DexClassNode classNode = new DexClassNode(
                changeType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                changeSuperType,
                DexTypeList.empty());

        DexClassVisitor classVisitor = classNode.asVisitor();

        classVisitor.visitBegin();

        methods.forEach(m -> generateForOneMethod(classNode, classVisitor, m));

        classVisitor.visitEnd();
        return classNode;
    }

    protected void generateForOneMethod(DexClassNode classNode, DexClassVisitor classVisitor,
                                        DexNamedProtoNode method) {

    }


}
