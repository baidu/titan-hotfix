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

package com.baidu.titan.core.patch.full;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.full.generator.DirectComponentInterceptorGenerator;
import com.baidu.titan.core.patch.full.generator.GeneratorHolder;
import com.baidu.titan.core.patch.full.generator.InterceptorGenerator;
import com.baidu.titan.core.patch.full.generator.RedirectToBuddyInterceptorGenerator;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用于从插桩后的Dex中读取Genesis类的继承链，并生成相应的Interceptor类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/14
 */
public class PatchGenesisClassHierarchy {

    public final HashMap<Integer, ComponentGenesisHolder> componentGenesisMap = new HashMap<>();

    private TitanDexItemFactory factory;

    public final ComponentMapper directComponentMapper;

    private ApplicationDexPool mInterceptorPool;

    public PatchGenesisClassHierarchy(TitanDexItemFactory factory,
                                      ApplicationDexPool interceptorPool,
                                      DirectComponentMapper componentMapper) {
        this.factory = factory;
        this.mInterceptorPool = interceptorPool;
        this.directComponentMapper = componentMapper;
    }

    /**
     * 存储特定Component类型的列表,包含多个Genesis层级
     */
    public static class ComponentGenesisHolder {

        public final int componentType;

        /**
         * key:genesis type
         * value:genesis class holder
         */
        final HashMap<DexType, GenesisClassHolder> genesisClasses = new HashMap<>();

        public ComponentGenesisHolder(int componentType, DexItemFactory factory) {
            this.componentType = componentType;

        }

    }

    /**
     * 包含单个Genesis Top Type相关信息和生成器，包含所有的子Component
     */
    public class GenesisClassHolder {

        public DexClassNode genesisClassNode;

        public DexType genesisType;

        public final GeneratorHolder generatorHolder;

        public final int componentType;

        public TitanDexItemFactory factory;
        /** 用于生成Interceptor */
        public RedirectToBuddyInterceptorGenerator interceptorGenerator;

        public ComponentGenesisHolder parent;

        public Map<DexType, ComponentClassHolder> components = new HashMap<>();

        public final Set<DexNamedProtoNode> classBuddyMethods = new HashSet<>();

        public final Set<DexNamedProtoNode> instrumentedVirtualMethods = new HashSet<>();

        public GenesisClassHolder(ComponentGenesisHolder parent,
                                  DexClassNode genesisClassNode,
                                  int componentType,
                                  TitanDexItemFactory factory) {
            this.genesisClassNode = genesisClassNode;
            this.componentType = componentType;
            this.genesisType = genesisClassNode.type;
            this.factory = factory;
            this.generatorHolder = new GeneratorHolder(this.genesisType, factory);
            this.parent = parent;

            this.interceptorGenerator = new RedirectToBuddyInterceptorGenerator(
                    genesisType,
                    PatchUtils.getInterceptorType(genesisType, factory),
                    PatchUtils.getBuddyType(genesisType, factory),
                    genesisType,
                    factory,
                    genesisClassNode);
            this.generatorHolder.interceptorGenerator = this.interceptorGenerator;
        }

    }

    /**
     * 单个具体的Component Class
     */
    public class ComponentClassHolder {

        public final DexClassNode classNode;

        public final Set<DexNamedProtoNode> thisVirtualMethods;

        public final AndroidComponentFlag componentFlag;

        public final int componentType;

        public final GeneratorHolder generatorHolder;

        public DexItemFactory factory;

        public InterceptorGenerator interceptorGenerator;

        public GenesisClassHolder parent;

        public ComponentClassHolder(GenesisClassHolder parent,
                                    DexClassNode classNode,
                                    Set<DexNamedProtoNode> thisVirtualMethods,
                                    AndroidComponentFlag flag,
                                    int componentType,
                                    TitanDexItemFactory factory) {
            this.factory = factory;
            this.classNode = classNode;
            this.thisVirtualMethods = thisVirtualMethods;
            this.componentFlag = flag;
            this.componentType = componentType;
            this.parent = parent;
            this.generatorHolder = new GeneratorHolder(this.classNode.type, factory);
            if (isDirectComponent()) {
                this.interceptorGenerator = new DirectComponentInterceptorGenerator(
                        classNode.type,
                        PatchUtils.getInterceptorType(classNode.type, factory),
                        PatchUtils.getBuddyType(parent.genesisType, factory),
                        directComponentMapper.map(classNode.type),
                        parent.genesisType,
                        factory,
                        classNode);
                // 对于组件类，加入默认构造函数
                this.classNode.getMethods().stream()
                        .filter(m -> m.isInstanceInitMethod() && m.parameters.count() == 0)
                        .map(m -> new DexNamedProtoNode(m.name, m.parameters, m.returnType))
                        .forEach(dnp -> {
                            this.interceptorGenerator.addInterceptMethod(dnp);
                        });
            } else {
                this.interceptorGenerator = new RedirectToBuddyInterceptorGenerator(
                        classNode.type,
                        PatchUtils.getInterceptorType(classNode.type, factory),
                        PatchUtils.getBuddyType(parent.genesisType, factory),
                        parent.genesisType,
                        factory,
                        classNode);
            }

            // 加入
            this.thisVirtualMethods.forEach(dnp -> this.interceptorGenerator.addInterceptMethod(dnp));

            this.generatorHolder.interceptorGenerator = this.interceptorGenerator;
        }

        public boolean isSuperComponent() {
            return this.componentFlag.hasFlags(this.componentType, AndroidComponentFlag.FLAG_SUPER);
        }


        public boolean isDirectComponent() {
            return this.componentFlag.hasFlags(this.componentType, AndroidComponentFlag.FLAG_DIRECT);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComponentClassHolder that = (ComponentClassHolder) o;

            return classNode != null ? classNode.type.equals(that.classNode.type) :
                    that.classNode == null;
        }

        @Override
        public int hashCode() {
            return classNode != null ? classNode.hashCode() : 0;
        }
    }

    /**
     * 收集指定Component类型的层次类型信息
     *
     * @param componentRoot
     * @param componentType
     */
    public void collectGenesisClassHierarchyForComponent(DexClassNode componentRoot,
                                                         int componentType) {
        ComponentGenesisHolder cgh = new ComponentGenesisHolder(componentType, factory);
        this.componentGenesisMap.put(componentType, cgh);
        traversalGenesis(componentRoot, componentType, cgh);
    }

    public void fillAppPool() {
        this.componentGenesisMap.forEach((componentTypeValue, componentGenesisHolder) -> {
            componentGenesisHolder.genesisClasses.forEach((genesisType, genesisHolder) -> {
                DexClassNode genesisInterceptor = genesisHolder.interceptorGenerator.generate();
                mInterceptorPool.addProgramClass(genesisInterceptor);
                genesisHolder.components.forEach((componentType, componentClassHolder) -> {
                    DexClassNode componentIntercetor =
                            componentClassHolder.interceptorGenerator.generate();
                    mInterceptorPool.addProgramClass(componentIntercetor);
                });
            });

        });
    }

    private static boolean filterOverrideableVirtualMethod(DexMethodNode dmn) {
        DexAccessFlags access = dmn.accessFlags;
        // filter non virtual method
        if (access.containsOneOf(DexAccessFlags.ACC_PRIVATE | DexAccessFlags.ACC_STATIC
                | DexAccessFlags.ACC_CONSTRUCTOR)) {
            return false;
        }

        if (dmn.name.toString().equals("<init>")) {
            return false;
        }

        // filter package access level
        if (access.containsNoneOf(DexAccessFlags.ACC_PUBLIC
                | DexAccessFlags.ACC_PROTECTED)) {
            return false;
        }
        return true;
    }

    private void traversalGenesis(DexClassNode dcn, int componentType,
                                         ComponentGenesisHolder cgh) {
        if (dcn.type.toTypeDescriptor().endsWith("$genesis;")) {

            GenesisClassHolder genesisHolder = new GenesisClassHolder(cgh, dcn, componentType,
                    this.factory);

            cgh.genesisClasses.put(dcn.type, genesisHolder);

            Set<DexNamedProtoNode> fullVirtualMethods = dcn.getMethods().stream()
                    .filter(k -> filterOverrideableVirtualMethod(k))
                    .map(k -> new DexNamedProtoNode(k.name, k.parameters, k.returnType))
                    .collect(Collectors.toSet());

            genesisHolder.instrumentedVirtualMethods.addAll(fullVirtualMethods);

            DexSubClassHierarchyFiller.forEachSubClass(dcn, sub ->
                    postOrderBuildClassBuddy(genesisHolder, sub, fullVirtualMethods,
                            componentType));

            return;
        }

        DexSubClassHierarchyFiller.forEachSubClass(dcn, sub -> traversalGenesis(sub,
                componentType, cgh));
    }

    private Set<DexNamedProtoNode> postOrderBuildClassBuddy(
            GenesisClassHolder genesisClassHolder, DexClassNode dcn,
            Set<DexNamedProtoNode> fullVirtualMethods, int componentType) {
        AndroidComponentFlag acf = AndroidComponentMarker.getComponentFlag(dcn);
        if (acf == null) {
            return null;
        }

        // TODO add a flag helper
        if (!acf.hasFlags(componentType, AndroidComponentFlag.FLAG_SUPER) && !acf.hasFlags
                (componentType, AndroidComponentFlag.FLAG_DIRECT)) {
            return null;
        }

        boolean directComponent = acf.hasFlags(componentType, AndroidComponentFlag.FLAG_DIRECT);


        List<Set<DexNamedProtoNode>> subVirtualMethodList = new ArrayList<>();
        DexSubClassHierarchyFiller.forEachSubClass(dcn, sub -> {

            Set<DexNamedProtoNode> subVirtualMethods = postOrderBuildClassBuddy(genesisClassHolder,
                    sub, fullVirtualMethods, componentType);
            // if subVirtualMethods ==  null implies this sub class not belong android component
            // hierarchy tree
            if (subVirtualMethods != null) {
                subVirtualMethodList.add(subVirtualMethods);
            }
        });

        Set<DexNamedProtoNode> thisVirtualMethod = dcn.getMethods().stream()
                .map(k -> new DexNamedProtoNode(k.name, k.parameters, k.returnType))
                .filter(k -> fullVirtualMethods.contains(k))
                .filter(k -> {
                    // if this class is a direct component class
                    if (directComponent) {
                        return true;
                    }

                    boolean containsAll = true;
                    boolean containsOne = false;
                    for (Set<DexNamedProtoNode> oneSubVirtualMethods : subVirtualMethodList) {
                        if (oneSubVirtualMethods.contains(k)) {
                            containsOne = true;
                        } else {
                            containsAll = false;
                        }
                    }

                    // a opt way
                    if (containsOne && containsAll) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toSet());

        ComponentClassHolder componentClassHolder = new ComponentClassHolder(
                genesisClassHolder, dcn, thisVirtualMethod, acf, componentType, this.factory);

        genesisClassHolder.components.put(dcn.type, componentClassHolder);

        genesisClassHolder.classBuddyMethods.addAll(thisVirtualMethod);

        return thisVirtualMethod;
    }

}
