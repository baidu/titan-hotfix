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
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.core.util.Utils;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexAnnotationVisibilitys;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 在插桩阶段，分析Android Component层级关系，完成以下事情：
 * <li>找到top program component type和bottom library component type分界线，插入$genesis类型</li>
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/12
 */
public class InstrumentGenesisClassHierarchy {

    public final HashMap<Integer, ComponentGenesisHolder> componentGenesisMap = new HashMap<>();

    private static final String EXTRA_COMPONENT_WORM_HOLE_INIT_METHOD =
            "_extra_component_worm_hole_init_method";

    private static final String EXTRA_COMPONENT_DIRECT_INIT = "_extra_component_direct_init";

    public InstrumentGenesisClassHierarchy() {

    }

    /**
     * 存储特定Component类型的列表,包含多个Genesis层级
     */
    public static class ComponentGenesisHolder {

        public final int componentType;

        /**
         * key: genesisType
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

        public DexType genesisType;

        /** genesisClass所对应的父类 */
        public DexClassNode bottomLibraryClassNode;
        /** Android Component 类型 */
        public final int componentType;

        public TitanDexItemFactory factory;
        /** $genesis类节点 */
        public DexClassNode genesisClass;

        public ComponentGenesisHolder parent;
        /** genesisType的所有直接子类 */
        public List<DexClassNode> topClasses = new ArrayList<>();

        public Map<DexType, ComponentClassHolder> components = new HashMap<>();

        public GenesisClassHolder(ComponentGenesisHolder parent,
                                  DexType genesisType,
                                  DexClassNode bottomLibraryClassNode,
                                  int componentType, TitanDexItemFactory factory) {
            this.componentType = componentType;
            this.genesisType = genesisType;
            this.factory = factory;
            this.parent = parent;
            this.bottomLibraryClassNode = bottomLibraryClassNode;
        }

        /**
         * 添加从Object到bottomLibrary的所有虚方法，以便子类重写的时候，可以实现拦截机制
         *
         * @param genesisClassVisitor
         * @param gensisClass
         */
        private void addVirtualMethods(DexClassVisitor genesisClassVisitor,
                                       DexClassNode gensisClass) {
            List<NamedProtoMethodWithFlag> vtable =
                    collectVirtualMethodHierarchy(this.bottomLibraryClassNode);

            vtable.forEach(npm -> {

                DexMethodVisitor dmv = genesisClassVisitor.visitMethod(
                        new DexMethodVisitorInfo(
                                genesisClass.type,
                                npm.name,
                                npm.parameters,
                                npm.returnType,
                                new DexAccessFlags(
                                        npm.accessFlags.getFlags() & (~DexAccessFlags.ACC_ABSTRACT))));

                dmv.visitBegin();

                DexCodeRegisterCalculator dcv = new DexCodeRegisterCalculator(
                        false,
                        npm.parameters,
                        new DexCodeFormatVerifier(dmv.visitCode()));

                dcv.visitBegin();

                DexRegisterList.Builder invokeSuperRegsBuilder = DexRegisterList.newBuilder();

                int nextParaReg = 0;
                invokeSuperRegsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg++));

                for (DexType type : npm.parameters) {
                    switch (type.toShortDescriptor()) {
                        case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                        case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                            invokeSuperRegsBuilder.addReg(
                                    DexRegister.makeDoubleParameterReg(nextParaReg));
                            nextParaReg += 2;
                            break;
                        }
                        default: {
                            invokeSuperRegsBuilder.addReg(DexRegister.makeParameterReg(nextParaReg));
                            nextParaReg += 1;
                        }
                    }
                }

                dcv.visitConstInsn(
                        Dops.INVOKE_SUPER,
                        invokeSuperRegsBuilder.build(),
                        DexConst.ConstMethodRef.make(bottomLibraryClassNode.type,
                                npm.name, npm.returnType, npm.parameters));

                int vResultReg = 0;

                switch (npm.returnType.toShortDescriptor()) {
                    case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                    case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                        dcv.visitSimpleInsn(
                                Dops.MOVE_RESULT_WIDE,
                                DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                        dcv.visitSimpleInsn(
                                Dops.RETURN_WIDE,
                                DexRegisterList.make(DexRegister.makeDoubleLocalReg(vResultReg)));
                        break;
                    }
                    case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR:
                    case DexItemFactory.ArrayType.SHORT_DESCRIPTOR: {
                        dcv.visitSimpleInsn(
                                Dops.MOVE_RESULT_OBJECT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                        dcv.visitSimpleInsn(
                                Dops.RETURN_OBJECT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                        break;
                    }

                    case DexItemFactory.VoidClass.SHORT_DESCRIPTOR: {
                        dcv.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);
                        break;
                    }
                    default: {
                        dcv.visitSimpleInsn(
                                Dops.MOVE_RESULT,
                                DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                        dcv.visitSimpleInsn(
                                Dops.RETURN,
                                DexRegisterList.make(DexRegister.makeLocalReg(vResultReg)));
                        break;
                    }
                }

                dcv.fillRegisterCount();

                dcv.visitEnd();

                dmv.visitEnd();

            });
        }

        /**
         * 添加与bottomLibraryClass相同的<init>方法，以便Patch中可以根据MethodId选择性的调用任何<init>方法。
         *
         * @param genesisClassVisitor
         * @param gensisClass
         */
        private void addInitMethods(DexClassVisitor genesisClassVisitor, DexClassNode gensisClass) {
            // 查找所有非private（public | protected）的<init>方法
            List<DexMethodNode> initMethods = bottomLibraryClassNode.getMethods().stream()
                    .filter(dmn -> dmn.isInstanceInitMethod())
                    .filter(dmn -> dmn.accessFlags.containsOneOf(
                            DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_PROTECTED))
                    .sorted()
                    .collect(Collectors.toList());

            int methodId = -1;
            for (DexMethodNode superInitMethod : initMethods) {
                methodId++;
                DexMethodVisitor methodVisitor = genesisClassVisitor.visitMethod(
                        new DexMethodVisitorInfo(
                                genesisClass.type,
                                superInitMethod.name,
                                superInitMethod.parameters,
                                superInitMethod.returnType,
                                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC
                                        | DexAccessFlags.ACC_CONSTRUCTOR)));

                methodVisitor.visitBegin();

                // 添加GenesisInitMethod注解，并且赋值methodId
                DexAnnotationVisitor genesisInitMethodAnnotation = methodVisitor.visitAnnotation(
                        new DexAnnotationVisitorInfo(
                                factory.annotationClasses.genesisInitMethod.type,
                                DexAnnotationVisibilitys.get(
                                        DexAnnotationVisibilitys.ANNOTATION_VISIBILITY_RUNTIME)));

                genesisInitMethodAnnotation.visitBegin();

                genesisInitMethodAnnotation.visitPrimitive(
                        factory.annotationClasses.genesisInitMethod.methodIdElementName,
                        methodId);

                genesisInitMethodAnnotation.visitEnd();

                // code begin
                DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                        false,
                        superInitMethod.parameters,
                        new DexCodeFormatVerifier(methodVisitor.visitCode()));

                codeVisitor.visitBegin();

                DexRegisterList.Builder invokeSuperRegsBuilder = DexRegisterList.newBuilder();

                int pNextParaReg = 0;
                invokeSuperRegsBuilder.addReg(DexRegister.makeParameterReg(pNextParaReg++));
                for (DexType type : superInitMethod.parameters.types()) {
                    switch (type.toShortDescriptor()) {
                        case DexItemFactory.LongClass.SHORT_DESCRIPTOR:
                        case DexItemFactory.DoubleClass.SHORT_DESCRIPTOR: {
                            invokeSuperRegsBuilder.addReg(
                                    DexRegister.makeDoubleParameterReg(pNextParaReg));
                            pNextParaReg += 2;
                            break;
                        }
                        default: {
                            invokeSuperRegsBuilder.addReg(DexRegister.makeParameterReg(pNextParaReg));
                            pNextParaReg += 1;
                            break;
                        }
                    }
                }

                codeVisitor.visitConstInsn(
                        Dops.INVOKE_DIRECT,
                        invokeSuperRegsBuilder.build(),
                        DexConst.ConstMethodRef.make(
                                superInitMethod.owner,
                                superInitMethod.name,
                                superInitMethod.returnType,
                                superInitMethod.parameters));

                codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

                codeVisitor.visitEnd();

                codeVisitor.fillRegisterCount();

                methodVisitor.visitEnd();
            }
        }

        /**
         * 构建Genesis Class Node
         */
        public void buildGenesisClassIfNeed() {
            if (genesisClass != null) {
                return;
            }

            DexType genesisType = Utils.appendTypeSuffix(bottomLibraryClassNode.type,
                    Constant.SUFFIX_GENESIS_TYPE, factory);


            genesisClass = new DexClassNode(
                    genesisType,
                    new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                    bottomLibraryClassNode.type,
                    DexTypeList.empty());

            DexClassVisitor genesisClassVisitor = genesisClass.asVisitor();

            genesisClassVisitor.visitBegin();

            markGenesisType(genesisClassVisitor);

            addBuddyObjField(genesisClassVisitor, genesisClass);

            addVirtualMethods(genesisClassVisitor, genesisClass);

            addInitMethods(genesisClassVisitor, genesisClass);

            genesisClassVisitor.visitEnd();
        }

        /**
         *
         * 添加GenesisType注解，标识此类为Genesis Type
         *
         * @param classVisitor
         */
        private void markGenesisType(DexClassVisitor classVisitor) {
            DexAnnotationVisitor dav = classVisitor.visitAnnotation(
                    new DexAnnotationVisitorInfo(
                            factory.genesisTypeAnnotation.type,
                            DexAnnotationVisibilitys.get(
                                    DexAnnotationVisibilitys.ANNOTATION_VISIBILITY_RUNTIME)));
            dav.visitBegin();
            dav.visitEnd();
        }

        /**
         * 添加BuddyObj字段，用于全量热更新时保存Buddy实例
         *
         * @param classVisitor
         * @param classNode
         */
        private void addBuddyObjField(DexClassVisitor classVisitor, DexClassNode classNode) {
            DexFieldVisitor fieldVisitor = classVisitor.visitField(
                    new DexFieldVisitorInfo(
                            classNode.type,
                            factory.genesisClass.buddyObjFiledName,
                            factory.objectClass.type,
                            new DexAccessFlags(DexAccessFlags.ACC_PUBLIC,
                                    DexAccessFlags.ACC_SYNTHETIC)));
            fieldVisitor.visitBegin();
            fieldVisitor.visitEnd();
        }

        /**
         * 自顶而下的收集所有可重写的虚方法(!package && !final)
         * @param dcn
         * @return
         */
        private List<NamedProtoMethodWithFlag> collectVirtualMethodHierarchy(DexClassNode dcn) {
            if (dcn == null) {
                return new ArrayList<>();
            }

            DexClassNode superDcn = DexSuperClassHierarchyFiller.getSuperClass(dcn);

            // first, we collect super(s) class's vtable
            List<NamedProtoMethodWithFlag> superVtable = collectVirtualMethodHierarchy(superDcn);

            // second, we find out this class's virtual method
            List<NamedProtoMethodWithFlag> thisVirtualMethods = dcn.getMethods().stream()
                    .filter(dmn -> filterOverridableVirtualMethod(dmn))
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

        private boolean filterOverridableVirtualMethod(DexMethodNode dmn) {
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

    }

    public static class NamedProtoMethodWithFlag extends DexNamedProtoNode {

        public DexAccessFlags accessFlags;

        public NamedProtoMethodWithFlag(DexString name, DexType returnType, DexTypeList parameters,
                                        DexAccessFlags accessFlags) {
            super(name, parameters, returnType);
            this.accessFlags = accessFlags;
        }
    }

    /**
     * 单个具体的Component Class
     */
    public class ComponentClassHolder {

        public final DexClassNode classNode;

        public final AndroidComponentFlag componentFlag;

        public final int componentType;

        public DexItemFactory factory;

        public GenesisClassHolder parent;

        public ComponentClassHolder(GenesisClassHolder parent, DexClassNode classNode,
                                    AndroidComponentFlag flag, int componentType,
                                    DexItemFactory factory) {
            this.factory = factory;
            this.classNode = classNode;
            this.componentFlag = flag;
            this.componentType = componentType;
            this.parent = parent;
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

    public void buildGenesisClassForComponent(DexType rootType, ApplicationDexPool appPool,
                                              int componentType, TitanDexItemFactory factory) {
        ComponentGenesisHolder componentGenesisHolder = componentGenesisMap
                .computeIfAbsent(componentType, e ->
                        new  ComponentGenesisHolder(componentType, factory));

        preOrderTraversalDexTypes(rootType, appPool, componentType, factory, componentGenesisHolder);

        componentGenesisHolder.genesisClasses.forEach((t, holder) -> {
            appPool.getProgramClassPool().addClass(holder.genesisClass);

            AndroidComponentMarker.setFlag(holder.genesisClass, componentType,
                    AndroidComponentFlag.FLAG_SUPER);

            holder.topClasses.forEach(k -> {
                DexSubClassHierarchyFiller.removeSubClass(holder.bottomLibraryClassNode, k.type);
                DexSubClassHierarchyFiller.addSubClass(holder.genesisClass, k);
                k.superType = holder.genesisClass.type;
                reWriterTopProgramClass(k, holder.bottomLibraryClassNode, holder.genesisType);
            });
        });
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
                oldCode.accept(new DexCodeFormatVerifier(new DexCodeVisitor(newCode.asVisitor()) {

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
                                            dexConst = DexConst.ConstMethodRef.make(
                                                    genesisType,
                                                    methodRef.getName(),
                                                    methodRef.getReturnType(),
                                                    methodRef.getParameterTypes());
                                        }
                                    }
                                    break;
                                }
                                case Dops.INVOKE_SUPER:
                                case Dops.INVOKE_SUPER_RANGE: {
                                    if (methodRef.getOwner().equals(bottomLibraryType)) {
                                        dexConst = DexConst.ConstMethodRef.make(
                                                genesisType,
                                                methodRef.getName(),
                                                methodRef.getReturnType(),
                                                methodRef.getParameterTypes());
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


    private void preOrderTraversalDexTypes(DexType type, ApplicationDexPool appPool,
                                           int componentType, TitanDexItemFactory factory,
                                           ComponentGenesisHolder componentGenesisHolder) {
        DexClassNode dcn = appPool.findClassFromAll(type);
        DexClassNode superDcn = DexSuperClassHierarchyFiller.getSuperClass(dcn);

        AndroidComponentFlag cf = AndroidComponentMarker.getComponentFlag(dcn);

        if (cf == null) {
            return;
        }

        // 这里过滤掉Titan 相关的所有组件
        if (dcn.type.toTypeDescriptor().startsWith("Lcom/baidu/titan/sdk/")) {
            return;
        }

        boolean isProgramKind = DexClassKindMarker.isProgramClass(dcn);
        // 是否是直接组件实现类，换句话说，就是在AndroidManifest中声明的
        boolean directComponent = cf.hasFlags(componentType, AndroidComponentFlag.FLAG_DIRECT);
        // 是否是直接组件类的*直接或者间接*父类。一个类可以同时是direct和super组件。
        boolean superComponent = cf.hasFlags(componentType, AndroidComponentFlag.FLAG_SUPER);

        if (!directComponent && !superComponent) {
            return;
        }

        // TODO ：后继需要处理在AndroidManifest中声明的组件类型本身就是Library Class的情况

        if (isProgramKind && directComponent) {
            // 对于组件类，目前的都是无参的默认构造函数, 如果后继Android系统支持有参的构造函数，再将此处通用化一下
            DexMethodNode initMethod = dcn.getMethods().stream()
                    .filter(m -> m.returnType.equals(factory.voidClass.primitiveType) &&
                                m.name.equals(factory.methods.initMethodName) &&
                                m.parameters.equals(DexTypeList.empty()))
                    .findFirst().get();
            // 这里标记为直接组件的默认构造函数，后继插桩阶段会有单独处理
            setComponentDirectInitMethod(initMethod, true);
        }

        // 对于super类型，需要插入*虫洞* <init>方法
        if (isProgramKind && superComponent) {
            buildWormHoleMethod(dcn, factory);
        }

        // 找到了topProgramClass和bottomLibraryClass分界线，插入并构建$genesis类型
        if (isProgramKind && DexClassKindMarker.isLibraryClass(superDcn)) {
            buildGenesisClass(superDcn, dcn, factory, componentGenesisHolder, componentType, appPool);
        }

        DexSubClassHierarchyFiller.forEachSubClass(dcn, subType -> preOrderTraversalDexTypes(
                        subType.type, appPool, componentType, factory, componentGenesisHolder));
    }

    private static DexAnnotationNode findGenesisInitMethodAnnotation(DexMethodNode m,
                                                                     TitanDexItemFactory factory) {
        List<DexAnnotationNode> mas = m.getMethodAnnotations();
        if (mas != null) {
            for (DexAnnotationNode annotation : mas) {
                if (annotation.getType().equals(factory.annotationClasses.genesisInitMethod.type)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    private static int getGenesisInitMethodId(DexMethodNode m, TitanDexItemFactory factory) {
        DexAnnotationNode annotation = findGenesisInitMethodAnnotation(m, factory);
        DexConst.EncodedAnnotation encodedAnnotation = annotation.getEncodedAnnotation();
        for (DexConst.AnnotationElement annotationElement : encodedAnnotation.getAnnotationItems()) {
            if (annotationElement.getName().equals(
                    factory.annotationClasses.genesisInitMethod.methodIdElementName)) {
                return annotationElement.getValue().asInt();
            }
        }
        throw new IllegalStateException(
                "GenesisInitMethod annotation without methodId value");
    }

    private void buildGenesisCallSuperSelectorMethod(DexClassNode genesisClassNode,
                                                     DexClassNode superDcn,
                                                     TitanDexItemFactory factory) {

        DexMethodNode dmn = new DexMethodNode(new DexMethodVisitorInfo(
                genesisClassNode.type,
                factory.methods.initMethodName,
                factory.createTypesVariable(factory.buddyInitContextClass.type),
                factory.voidClass.primitiveType,
                new DexAccessFlags(
                        DexAccessFlags.ACC_PUBLIC,
                        DexAccessFlags.ACC_CONSTRUCTOR,
                        DexAccessFlags.ACC_SYNTHETIC)));

        DexMethodVisitor initMethodVisitor = dmn.asVisitor();

        DexCodeVisitor initCodeVisitor = new DexCodeFormatVerifier(initMethodVisitor.visitCode());
        initCodeVisitor.visitBegin();

        final int pThisReg = 0;
        final int pBuddyInitContextReg = 1;
        final int vMethodIdReg = 0;
        final int vBuddyInitHolder = 1;
        final int vCallSuperParsReg = 2;
        final int vCallSuperParsRegIdxReg = 3;
        final int vCallSuperFirstReg = 4;

        int vExceptionReg = 0;
        int vExceptionDetailReg = 1;

        List<DexMethodNode> methods = genesisClassNode.getMethods().stream()
                .filter(m -> findGenesisInitMethodAnnotation(m, factory) != null)
                .sorted(Comparator.comparingInt(m -> getGenesisInitMethodId(m, factory)))
                .collect(Collectors.toList());

        int[] keys = methods.stream()
                .mapToInt(m -> getGenesisInitMethodId(m, factory))
                .toArray();

        DexLabel returnLabel = new DexLabel();

        DexLabel[] labels = methods.stream()
                .map(m -> new DexLabel())
                .toArray(DexLabel[]::new);


        initCodeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vMethodIdReg),
                        DexRegister.makeParameterReg(pBuddyInitContextReg)),
                factory.buddyInitContextClass.initMethodIdField);

        initCodeVisitor.visitConstInsn(
                Dops.INVOKE_VIRTUAL,
                DexRegisterList.make(DexRegister.makeParameterReg(pBuddyInitContextReg)),
                factory.buddyInitContextClass.currentMethod);

        initCodeVisitor.visitSimpleInsn(
                Dops.MOVE_RESULT_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vBuddyInitHolder)));

        initCodeVisitor.visitSwitch(
                Dops.SPARSE_SWITCH,
                DexRegisterList.make(DexRegister.makeLocalReg(vMethodIdReg)),
                keys,
                labels);

        // throws
        initCodeVisitor.visitConstInsn(
                Dops.NEW_INSTANCE,
                DexRegisterList.make(DexRegister.makeLocalReg(vExceptionReg)),
                DexConst.ConstType.make(factory.throwClasses.illegalStateExceptionClass.type));

        initCodeVisitor.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(DexRegister.makeLocalReg(vExceptionDetailReg)),
                DexConst.ConstString.make("unknown method id"));

        initCodeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vExceptionReg),
                        DexRegister.makeLocalReg(vExceptionDetailReg)),
                factory.throwClasses.illegalStateExceptionClass.detailInitMethod);

        initCodeVisitor.visitSimpleInsn(
                Dops.THROW,
                DexRegisterList.make(DexRegister.makeLocalReg(vExceptionReg)));

        initCodeVisitor.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vCallSuperParsReg),
                        DexRegister.makeLocalReg(vBuddyInitHolder)),
                factory.buddyInitHolderClass.parasField);

        for (int mi = 0; mi < methods.size(); mi++) {
            DexMethodNode m = methods.get(mi);

            initCodeVisitor.visitLabel(labels[mi]);

            int vNextCallSuperParaReg = vCallSuperFirstReg;
            DexRegisterList.Builder callSuperRegBuilder = DexRegisterList.newBuilder();

            int callSuperRegCount = Utils.calParameterRegCount(m.parameters) + 1;
            boolean rangeCall = true;
            if (callSuperRegCount <= 4) {
                rangeCall = false;
                callSuperRegBuilder.addReg(DexRegister.makeParameterReg(pThisReg));

            } else {
                initCodeVisitor.visitSimpleInsn(
                        Dops.MOVE_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallSuperParaReg),
                                DexRegister.makeParameterReg(pThisReg)));
                callSuperRegBuilder.addReg(DexRegister.makeLocalReg(vNextCallSuperParaReg));
                vNextCallSuperParaReg++;
            }

            for (int i = 0; i < m.parameters.count(); i++) {
                DexType paraType = m.parameters.getType(i);

                initCodeVisitor.visitConstInsn(Dops.CONST,
                        DexRegisterList.make(DexRegister.makeLocalReg(vCallSuperParsRegIdxReg)),
                        factory.dexConsts.createLiteralBits32(i));

                initCodeVisitor.visitSimpleInsn(Dops.AGET_OBJECT,
                        DexRegisterList.make(
                                DexRegister.makeLocalReg(vNextCallSuperParaReg),
                                DexRegister.makeLocalReg(vCallSuperParsReg),
                                DexRegister.makeLocalReg(vCallSuperParsRegIdxReg)));

                if (paraType.isPrimitiveType()) {
                    initCodeVisitor.visitConstInsn(
                            Dops.INVOKE_VIRTUAL,
                            DexRegisterList.make(
                                    DexRegister.makeLocalReg(vNextCallSuperParaReg)),
                            factory.methods.primitiveValueMethodForType(paraType));

                    initCodeVisitor.visitSimpleInsn(
                            Dops.MOVE_RESULT_OBJECT,
                            DexRegisterList.make(DexRegister.makeLocalReg(vNextCallSuperParaReg)));

                    if (paraType.isWideType()) {
                        callSuperRegBuilder.addReg(
                                DexRegister.makeDoubleLocalReg(vNextCallSuperParaReg));
                        vNextCallSuperParaReg += 2;
                    } else {
                        callSuperRegBuilder.addReg(DexRegister.makeLocalReg(vNextCallSuperParaReg));
                        vNextCallSuperParaReg++;
                    }
                } else if (paraType.isReferenceType() || paraType.isArrayType()) {
                    initCodeVisitor.visitConstInsn(
                            Dops.CHECK_CAST,
                            DexRegisterList.make(DexRegister.makeLocalReg(vNextCallSuperParaReg)),
                            DexConst.ConstType.make(paraType));
                    callSuperRegBuilder.addReg(DexRegister.makeLocalReg(vNextCallSuperParaReg));
                    vNextCallSuperParaReg++;
                }
            }

            initCodeVisitor.visitConstInsn(
                    rangeCall ? Dops.INVOKE_DIRECT_RANGE : Dops.INVOKE_DIRECT,
                    callSuperRegBuilder.build(),
                    DexConst.ConstMethodRef.make(
                            genesisClassNode.type,
                            factory.methods.initMethodName,
                            factory.voidClass.primitiveType,
                            m.parameters));

            initCodeVisitor.visitTargetInsn(Dops.GOTO, DexRegisterList.empty(), returnLabel);
        }


        initCodeVisitor.visitLabel(returnLabel);
        initCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        initCodeVisitor.visitEnd();
        initMethodVisitor.visitEnd();

        DexCodeRegisterCalculator.autoSetRegisterCountForMethodNode(dmn);

        genesisClassNode.addMethod(dmn);

        setWormHoleInitMethod(dmn, true);
    }

    /**
     * 按照JVM Spec，子类的构造函数必须调用直接父类的构造函数。意味着所有的<init>方法初始化必须是按照继承关系逐级的，
     * 不能出现越级初始化。
     *
     * *虫洞*<init>方法存在的目的就是为了遵守JVM Spec，对于继承链上的中间类走虫洞快速方法。
     *
     * @param dcn
     * @param factory
     */
    private void buildWormHoleMethod(DexClassNode dcn, TitanDexItemFactory factory) {

        DexMethodNode dmn = new DexMethodNode(new DexMethodVisitorInfo(
                dcn.type,
                factory.methods.initMethodName,
                factory.createTypesVariable(factory.buddyInitContextClass.type),
                factory.voidClass.primitiveType,
                new DexAccessFlags(
                        DexAccessFlags.ACC_PUBLIC,
                        DexAccessFlags.ACC_CONSTRUCTOR,
                        DexAccessFlags.ACC_SYNTHETIC)));

        DexMethodVisitor initMethodVisitor = dmn.asVisitor();

        DexCodeRegisterCalculator initCodeVisitor = new DexCodeRegisterCalculator(
                false,
                dmn.parameters,
                new DexCodeFormatVerifier(initMethodVisitor.visitCode()));

        initCodeVisitor.visitBegin();

        int pThisReg = 0;
        int pBuddyInitContextReg = 1;

        initCodeVisitor.visitConstInsn(Dops.INVOKE_DIRECT,
                DexRegisterList.make(
                        DexRegister.makeParameterReg(pThisReg),
                        DexRegister.makeParameterReg(pBuddyInitContextReg)),
                DexConst.ConstMethodRef.make(
                        dcn.superType,
                        factory.methods.initMethodName,
                        factory.voidClass.primitiveType,
                        factory.createTypesVariable(factory.buddyInitContextClass.type)));

        initCodeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        initCodeVisitor.fillRegisterCount();

        initCodeVisitor.visitEnd();
        initMethodVisitor.visitEnd();

        dcn.addMethod(dmn);

        // 标记为虫洞方法，插桩阶段不做处理
        setWormHoleInitMethod(dmn, true);
    }

    private void buildGenesisClass(DexClassNode bottomLibraryClassNode,
                                   DexClassNode topProgramClass,
                                   TitanDexItemFactory factory,
                                   ComponentGenesisHolder componentGenesisHolder,
                                   int componentType,
                                   ApplicationDexPool appPool) {
        DexType genesisType = Utils.appendTypeSuffix(bottomLibraryClassNode.type,
                Constant.SUFFIX_GENESIS_TYPE, factory);

        GenesisClassHolder holder = componentGenesisHolder.genesisClasses.
                computeIfAbsent(genesisType, e -> {
                    GenesisClassHolder newHolder = new GenesisClassHolder(componentGenesisHolder,
                            genesisType, bottomLibraryClassNode, componentType, factory);
                    //构建Genesis Class
                    newHolder.buildGenesisClassIfNeed();

                    appPool.getProgramClassPool().addClass(newHolder.genesisClass);

                    // 构建虫洞调用末端方法，定义在genesis Class中，根据BuddyInitContext.methodId进行分发，
                    // 来决定调用bottomLibrary的具体<init>方法。
                    buildGenesisCallSuperSelectorMethod(
                            newHolder.genesisClass,
                            bottomLibraryClassNode,
                            factory);

                    AndroidComponentMarker.setFlag(newHolder.genesisClass, componentType,
                            AndroidComponentFlag.FLAG_SUPER);
                    return newHolder;
                });

        holder.topClasses.add(topProgramClass);
    }

    public static void setWormHoleInitMethod(DexMethodNode dmn, boolean set) {
        dmn.setExtraInfo(EXTRA_COMPONENT_WORM_HOLE_INIT_METHOD, set);
    }

    /**
     * 是否是虫洞方法
     * @param dmn
     * @return
     */
    public static boolean isWormHoleInitMethod(DexMethodNode dmn) {
        return dmn.getExtraInfo(EXTRA_COMPONENT_WORM_HOLE_INIT_METHOD, false);
    }

    public static void setComponentDirectInitMethod(DexMethodNode dmn, boolean set) {
        dmn.setExtraInfo(EXTRA_COMPONENT_DIRECT_INIT, set);
    }

    /**
     * 是否是直接组件的构造函数，这里目前都是默认的无参构造函数，和目前Android组件初始化标准一致。
     * @param dmn
     * @return
     */
    public static boolean isComponentDirectInitMethod(DexMethodNode dmn) {
        return dmn.getExtraInfo(EXTRA_COMPONENT_DIRECT_INIT, false);
    }


}
