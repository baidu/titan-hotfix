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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * 用于生成Buddy类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/18
 */
public class BuddyGenerator {

    public DexType genesisType;

    public DexType buddyType;

    public TitanDexItemFactory factory;

    public DexType buddySuperType;

    public DexTypeList buddyInterfaces;

    protected Set<DexNamedProtoNode> methods = new HashSet<>();

    protected Set<DexNamedProtoNode> initMethods = new HashSet<>();


    public BuddyGenerator(DexType genesisType, DexType buddyType, DexType buddySuperType,
                          DexTypeList buddyInterfaces, TitanDexItemFactory factory) {
        this.genesisType = genesisType;
        this.buddyType = buddyType;
        this.buddySuperType = buddySuperType;
        this.factory = factory;
        this.buddyInterfaces = buddyInterfaces;
    }

    public void addBuddyMethod(DexNamedProtoNode method) {
        this.methods.add(method);
    }

    public void addInitMethod(DexNamedProtoNode initMethod) {
        this.initMethods.add(initMethod);
    }


    public DexClassNode generate() {

        DexClassNode classNode = new DexClassNode(
                buddyType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                buddySuperType,
                buddyInterfaces);

        DexClassVisitor classVisitor = classNode.asVisitor();

        classVisitor.visitBegin();

        generateGenesisField(classNode, classVisitor);

        methods.stream()
                .forEach(m -> {
                    generateForOneMethod(classNode, classVisitor, m);
                });

        initMethods.stream()
                .forEach(m -> generateForInitMethod(classNode, classVisitor, m));

        classVisitor.visitEnd();
        return classNode;

    }

    protected void generateGenesisField(DexClassNode classNode, DexClassVisitor classVisitor) {
        DexFieldVisitor fieldVisitor = classVisitor.visitField(new DexFieldVisitorInfo(
                classNode.type,
                factory.buddyClass.genesisObjFieldName,
                genesisType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_SYNTHETIC)));
        fieldVisitor.visitBegin();
        fieldVisitor.visitEnd();
    }

    protected void generateForOneMethod(DexClassNode classNode, DexClassVisitor classVisitor,
                                        DexNamedProtoNode method) {

    }

    protected void generateForInitMethod(DexClassNode classNode, DexClassVisitor classVisitor,
                                        DexNamedProtoNode method) {

    }






}
