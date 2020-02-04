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
import com.baidu.titan.core.patch.full.generator.CallSuperChangedClassGenerator;
import com.baidu.titan.core.patch.full.generator.TopBuddyGenerator;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * 生成Buddy类相关信息
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/27
 */
public class PatchBuddyClassHierarchy {

    public final Map<Integer, ComponentBuddyHolder> componentBuddyMap = new HashMap<>();

    private final PatchGenesisClassHierarchy mPatchGenesisClassHierarchy;

    public final Map<DexType, ComponentClassInfo> allComponents = new HashMap<>();

    private ApplicationDexPool mBuddyClassPool;

    private ApplicationDexPool mChangedClassPool;

    public PatchBuddyClassHierarchy(PatchGenesisClassHierarchy patchGenesisClassHierarchy,
                                    ApplicationDexPool buddyClassPool,
                                    ApplicationDexPool changedClassPool) {
        this.mPatchGenesisClassHierarchy = patchGenesisClassHierarchy;
        this.mBuddyClassPool = buddyClassPool;
        this.mChangedClassPool = changedClassPool;
    }

    /**
     * 标识具体一个组件类的相关信息
     */
    public static class ComponentClassInfo {

        public int componetType;

        public DexType dexType;

        public DexClassNode classNode;

        public AndroidComponentFlag componentFlag;

        public BuddyClassHolder buddyClassHolder;

    }

    /**
     *
     * 标识具体一个组件类型的相关信息，包含该组件类型所对应的所有Buddy类型
     * 一个Buddy类与一个顶层的系统组建类相对应
     *
     * component(one) <-> BuddyClassHolder(more)
     *
     */
    public static class ComponentBuddyHolder {

        public final int componentType;

        /**
         * 包含所有的BuddyClass
         */
        final HashMap<DexType, BuddyClassHolder> buddyClasses = new HashMap<>();

        public ComponentBuddyHolder(int componentType, DexItemFactory factory) {
            this.componentType = componentType;
        }

    }

    /**
     * 标识Buddy类，及其子类相关信息
     */
    public class BuddyClassHolder {

        public DexType bottomLibraryType;

        public DexType buddyType;

        public DexType genesisType;

        public DexType changedType;

        public DexClassNode bottomLibraryClassNode;

        public int componentType;

        public TitanDexItemFactory factory;

        public ComponentBuddyHolder parent;

        public List<DexClassNode> topClasses = new ArrayList<>();

        public Set<DexNamedProtoNode> overrideMethods = new HashSet<>();
        /** 用于生成Change类，默认行为是调用Super类同名方法 */
        public CallSuperChangedClassGenerator changeGenerator;
        /** 用于生成Buddy类，默认行为是调用对应的Change类 */
        public TopBuddyGenerator buddyGenerator;

        public DexClassNode buddyClassNode;

        public DexClassNode changedClassNode;

        public BuddyClassHolder(ComponentBuddyHolder parent,
                                DexType bottomLibraryType,
                                DexClassNode bottomLibraryClassNode,
                                int componentType,
                                TitanDexItemFactory factory) {
            this.componentType = componentType;
            this.bottomLibraryType = bottomLibraryType;
            this.factory = factory;
            this.parent = parent;
            this.bottomLibraryClassNode = bottomLibraryClassNode;

            this.genesisType = PatchUtils.getGenesisType(this.bottomLibraryType, factory);
            this.buddyType = PatchUtils.getBuddyType(this.genesisType, factory);
            this.changedType = PatchUtils.getChangeType(this.bottomLibraryType, factory);

            this.buddyGenerator = new TopBuddyGenerator(
                    this.genesisType,
                    this.buddyType,
                    /** this.bottomLibraryType, */
                    factory.objectClass.type,
                    this.changedType,
                    DexTypeList.empty(),
                    factory);

            this.changeGenerator = new CallSuperChangedClassGenerator(
                    this.genesisType,
                    this.changedType,
                    this.bottomLibraryClassNode.type,
                    factory);

        }

    }

    public void buildBuddyClassForComponent(DexType rootType, ApplicationDexPool appPool,
                                            int componentType, TitanDexItemFactory factory) {
        ComponentBuddyHolder componentBuddyHolder = componentBuddyMap
                .computeIfAbsent(componentType, e ->
                    new ComponentBuddyHolder(componentType, factory));
        preOrderTraversalDexTypes(rootType, appPool, componentType, factory, componentBuddyHolder);

        componentBuddyHolder.buddyClasses.forEach((t, buddyClassHolder) -> {
            buddyClassHolder.overrideMethods.forEach(m -> {
                    buddyClassHolder.buddyGenerator.addBuddyMethod(m);
                    buddyClassHolder.changeGenerator.addChangeMethod(m);
            });

            buddyClassHolder.bottomLibraryClassNode.getMethods().stream()
                    .filter(m -> m.isInstanceInitMethod()
                            && m.accessFlags.containsOneOf(DexAccessFlags.ACC_PUBLIC |
                            DexAccessFlags.ACC_PROTECTED))
                    .forEach(m -> buddyClassHolder.buddyGenerator.addInitMethod(
                            new DexNamedProtoNode(m.name, m.parameters, m.returnType)));

            List<NamedProtoMethodWithFlag> vtable =
                    collectVirtualMethodHierarchy(buddyClassHolder.bottomLibraryClassNode);
            vtable.stream()
                    .forEach(m -> {
                        buddyClassHolder.buddyGenerator.addBuddyMethod(m);
                        buddyClassHolder.changeGenerator.addChangeMethod(m);
                    });

//            buddyClassHolder.buddyGenerator.addInitMethod(
//                    new DexNamedProtoNode(factory.createString("<init>"), DexTypeList.empty(),
//                            factory.voidClass.primitiveType));

            DexClassNode buddyClassNode = buddyClassHolder.buddyGenerator.generate();
            buddyClassHolder.buddyClassNode = buddyClassNode;

            AndroidComponentMarker.setFlag(buddyClassNode, buddyClassHolder.componentType,
                    AndroidComponentFlag.FLAG_BUDDY);
            AndroidComponentMarker.setFlag(buddyClassNode, buddyClassHolder.componentType,
                    AndroidComponentFlag.FLAG_SUPER);

            buddyClassHolder.changedClassNode = buddyClassHolder.changeGenerator.generate();


            // fix it later
            DexType buddyOrgSuperType = buddyClassNode.superType;
            buddyClassNode.superType = buddyClassHolder.bottomLibraryType;

            DexSuperClassHierarchyFiller.setSuperClass(
                    buddyClassHolder.bottomLibraryClassNode, buddyClassNode);

            buddyClassHolder.topClasses.forEach(c -> {
                DexSubClassHierarchyFiller.removeSubClass(
                        buddyClassHolder.bottomLibraryClassNode, c.type);
                DexSuperClassHierarchyFiller.setSuperClass(buddyClassNode, c);
                DexSubClassHierarchyFiller.addSubClass(buddyClassNode, c);
            });

//            DexSubClassHierarchyFiller.forEachSubClass(
//                    buddyClassHolder.bottomLibraryClassNode, subClass -> {
//                        DexSubClassHierarchyFiller.removeSubClass(
//                                buddyClassHolder.bottomLibraryClassNode, subClass.type);
//
//
//
//                        DexSuperClassHierarchyFiller.setSuperClass(buddyClassNode, subClass);
//                        DexSubClassHierarchyFiller.addSubClass(buddyClassNode, subClass);
//            });



//            appPool.addProgramClass(buddyClassNode);
            // TODO appPool是否正确？

            buddyClassHolder.topClasses.forEach(topProgramClass -> {
                DexSubClassHierarchyFiller.removeSubClass(buddyClassHolder.bottomLibraryClassNode,
                        topProgramClass.type);
                // TODO generate buddy class node
                DexSubClassHierarchyFiller.addSubClass(buddyClassNode, topProgramClass);
                topProgramClass.superType = buddyClassHolder.buddyType;
                reWriterTopProgramClass(topProgramClass, buddyClassHolder.bottomLibraryClassNode,
                        buddyClassHolder.buddyType);
            });

            buddyClassNode.superType = buddyOrgSuperType;

            DexSuperClassHierarchyFiller.setSuperClass(null, buddyClassNode);

        });

    }

    public void fillAppPool() {
        this.componentBuddyMap.forEach((componentType, buddySet) ->
                buddySet.buddyClasses.forEach((buddyType, buddyHolder) -> {
            this.mBuddyClassPool.addProgramClass(buddyHolder.buddyClassNode);
            this.mChangedClassPool.addProgramClass(buddyHolder.changedClassNode);
        }));
    }

    private void preOrderTraversalDexTypes(DexType type, ApplicationDexPool appPool,
                                           int componentType, TitanDexItemFactory factory,
                                           ComponentBuddyHolder componentBuddyHolder) {
        DexClassNode dcn = appPool.findClassFromAll(type);
        DexClassNode superDcn = DexSuperClassHierarchyFiller.getSuperClass(dcn);

        AndroidComponentFlag cf = AndroidComponentMarker.getComponentFlag(dcn);

        if (cf == null) {
            return;
        }

        if (type.toTypeDescriptor().startsWith("Lcom/baidu/titan/sdk")) {
            return;
        }

        boolean directComponent = cf.hasFlags(componentType, AndroidComponentFlag.FLAG_DIRECT);
        boolean superComponent = cf.hasFlags(componentType, AndroidComponentFlag.FLAG_SUPER);

        if (!directComponent && !superComponent) {
            return;
        }

        // 找到程序类和系统类的分界处
        if (DexClassKindMarker.isProgramClass(dcn) && DexClassKindMarker.isLibraryClass(superDcn)) {

            buildBuddyClass(superDcn, dcn, factory, componentBuddyHolder, componentType, appPool);
        }

        DexSubClassHierarchyFiller.forEachSubClass(dcn, subType -> preOrderTraversalDexTypes(
                subType.type, appPool, componentType, factory, componentBuddyHolder));

    }

    /**
     *
     * @param bottomLibraryClassNode 最下层的系统类
     * @param topProgramClass 最顶层的程序类
     * @param factory
     * @param componentBuddyHolder
     * @param componentType 组件类型
     * @param appPool 类库池
     */
    private void buildBuddyClass(DexClassNode bottomLibraryClassNode,
                                 DexClassNode topProgramClass,
                                 TitanDexItemFactory factory,
                                 ComponentBuddyHolder componentBuddyHolder,
                                 int componentType,
                                 ApplicationDexPool appPool) {
        DexType buddyType = PatchUtils.getBuddyType(bottomLibraryClassNode.type, factory);
        DexType genesisType = PatchUtils.getGenesisType(bottomLibraryClassNode.type, factory);

        BuddyClassHolder holder = componentBuddyHolder.buddyClasses
                .computeIfAbsent(buddyType, e -> {
                    BuddyClassHolder newHolder = new BuddyClassHolder(
                            componentBuddyHolder,
                            bottomLibraryClassNode.type,
                            bottomLibraryClassNode,
                            componentType,
                            factory);
                    // TODO ? genesis
                    return newHolder;
                });
        //
        holder.topClasses.add(topProgramClass);

        fillComponentClass(topProgramClass, componentType, holder);

        PatchGenesisClassHierarchy.ComponentGenesisHolder genesisHolder =
                this.mPatchGenesisClassHierarchy.componentGenesisMap.get(componentType);

        PatchGenesisClassHierarchy.GenesisClassHolder genesisClassHolder =
                genesisHolder.genesisClasses.get(genesisType);

        topDownTrasversalOverridedMethods(topProgramClass,
                genesisClassHolder.instrumentedVirtualMethods,
                holder.overrideMethods);

    }

    private void fillComponentClass(DexClassNode componentClass,
                                    int componentType,
                                    BuddyClassHolder buddyClassHolder) {
        AndroidComponentFlag componentFlag = AndroidComponentMarker.getComponentFlag(componentClass);
        if (componentFlag == null) {
            return;
        }

        ComponentClassInfo componentClassInfo = new ComponentClassInfo();
        componentClassInfo.buddyClassHolder = buddyClassHolder;
        componentClassInfo.classNode = componentClass;
        componentClassInfo.dexType = componentClass.type;
        componentClassInfo.componetType = componentType;
        componentClassInfo.componentFlag = componentFlag;


        allComponents.put(componentClass.type, componentClassInfo);

        DexSubClassHierarchyFiller.forEachSubClass(componentClass, subClass -> {
            fillComponentClass(subClass, componentType, buddyClassHolder);
        });

    }

    private void topDownTrasversalOverridedMethods(DexClassNode componentClassNode,
                                                   Set<DexNamedProtoNode> fullVirtualMethods,
                                                   Set<DexNamedProtoNode> overrideMethods) {
        // Buddy类中包含所有的虚方法，包含父类中的final方法
        componentClassNode.getMethods().stream()
                .map(m -> new DexNamedProtoNode(m.name, m.parameters, m.returnType))
                .filter(dnp -> fullVirtualMethods.contains(dnp))
                .forEach(dnp -> overrideMethods.add(dnp));

        DexSubClassHierarchyFiller.forEachSubClass(componentClassNode, subType ->
                topDownTrasversalOverridedMethods(subType, fullVirtualMethods, overrideMethods));
    }

    private void reWriterTopProgramClass(DexClassNode topProgramClass,
                                         DexClassNode bottomLibraryClass,
                                         DexType genesisType) {
        DexType bottomLibraryType = bottomLibraryClass.type;
        DexType topProgramType = topProgramClass.type;
        topProgramClass.getMethods().forEach(m -> {
            DexCodeNode oldCode = m.getCode();
            boolean initMethod = m.isInstanceInitMethod();
            if (oldCode != null) {
                DexCodeNode newCode = new DexCodeNode();
                oldCode.accept(new DexCodeFormatVerifier(
                        new DexCodeVisitor(newCode.asVisitor()) {

                    @Override
                    public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
                        if (dexConst instanceof DexConst.ConstMethodRef) {
                            DexConst.ConstMethodRef methodRef = (DexConst.ConstMethodRef)dexConst;
                            switch (op) {
                                case Dops.INVOKE_DIRECT:
                                case Dops.INVOKE_DIRECT_RANGE: {
                                    if (initMethod) {
                                        if (methodRef.getName().equals("<init>") &&
                                                methodRef.getOwner().equals(bottomLibraryType)) {
                                            dexConst = DexConst.ConstMethodRef.make(genesisType,
                                                    methodRef.getName(), methodRef.getReturnType
                                                            (), methodRef.getParameterTypes());
                                        }
                                    }
                                    break;
                                }
                                case Dops.INVOKE_SUPER:
                                case Dops.INVOKE_SUPER_RANGE: {
                                    if (methodRef.getOwner().equals(bottomLibraryType)) {
                                        dexConst = DexConst.ConstMethodRef.make(genesisType,
                                                methodRef.getName(), methodRef.getReturnType
                                                        (), methodRef.getParameterTypes());
                                    }
                                    break;
                                }
                            }
                        }
                        super.visitConstInsn(op, regs, dexConst);
                    }
                }));
                m.setCode(newCode);
            }
        });
    }


    private List<NamedProtoMethodWithFlag> collectVirtualMethodHierarchy(DexClassNode dcn) {
        if (dcn == null) {
            return new ArrayList<>();
        }

        DexClassNode superDcn = DexSuperClassHierarchyFiller.getSuperClass(dcn);

        // first, we collect super(s) class's vtable
        List<NamedProtoMethodWithFlag> superVtable = collectVirtualMethodHierarchy(superDcn);

        // second, we find out this class's virtual method
        List<NamedProtoMethodWithFlag> thisVirtualMethods = dcn.getMethods().stream()
                .filter(dmn -> filterOverrideableVirtualMethod(dmn))
                .map(dmn -> methodNodeToNamedProtoMethod(dmn))
                .collect(Collectors.toList());

        // last, we do distinct merge with this class's method first
        List<NamedProtoMethodWithFlag> vtable = superVtable.stream()
                .filter(npm -> !thisVirtualMethods.contains(npm))
                .collect(Collectors.toList());

        vtable.addAll(thisVirtualMethods);

        // and, we need filter final method
        vtable = vtable.stream()
                .filter(npm -> !npm.accessFlags.containsOneOf(DexAccessFlags.ACC_FINAL))
                .collect(Collectors.toList());

        return vtable;
    }

    private NamedProtoMethodWithFlag methodNodeToNamedProtoMethod(DexMethodNode dmn) {
        int access = dmn.accessFlags.getFlags();
        int newAccess = access;
        //& (DexConstant.ACC_PUBLIC | DexConstant.ACC_PROTECTED);
        NamedProtoMethodWithFlag npm =
                new NamedProtoMethodWithFlag(
                        dmn.name,
                        dmn.returnType,
                        dmn.parameters,
                        new DexAccessFlags(newAccess));
        return npm;
    }

    private boolean filterOverrideableVirtualMethod(DexMethodNode dmn) {
        DexAccessFlags access = dmn.accessFlags;
        // filter non virtual method
        if (access.containsOneOf(DexAccessFlags.ACC_PRIVATE | DexAccessFlags.ACC_STATIC
                | DexAccessFlags.ACC_CONSTRUCTOR)) {
            return false;
        }

        if (dmn.isInstanceInitMethod()) {
            return false;
        }

        // filter package access level
        if (access.containsNoneOf(
                DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_PROTECTED)) {
            return false;
        }
        return true;
    }



    public static class NamedProtoMethodWithFlag extends DexNamedProtoNode {

        public DexAccessFlags accessFlags;

        public NamedProtoMethodWithFlag(DexString name, DexType returnType, DexTypeList parameters,
                                    DexAccessFlags accessFlags) {
            super(name, parameters, returnType);
        this.accessFlags = accessFlags;
        }
    }

}
