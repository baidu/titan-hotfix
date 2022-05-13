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

package com.baidu.titan.core.patch.light.generator.changed;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.light.LightPatchClassPools;
import com.baidu.titan.core.patch.light.diff.ChangedClassDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffMode;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedFieldProtoNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 用于生成变化类对应的多种类型Class。
 * <p></p>
 * 如果发生改变的类名为A
 * 有新增、修改的方法，就会生成A$chg类，同时会有相对应的拦截器类A$itor
 * 如果有新增的字段，则会生成A$fh类
 * 这些新生成的类也会放到对应的class pool中
 *
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/5/4
 */
public class LightChangedClassGenerator implements DexClassNodeVisitor {

    DexClassNode mNewOrgClassNode;

    DexClassNode mOldOrgClassNode;

    LightPatchClassPools mClassPools;

    boolean mUseReflection;

    boolean mInstrumentedVirtualToPublic = true;
    /** $chg */
    DexClassNode mChangedClassNode;

    private TitanDexItemFactory mDexItemFactory;

    private boolean mCallDirectMethodUseReflection = false;

    private HashMap<DexNamedProtoNode, DexMethodNode> mUnProcessedMethods = new HashMap<>();

    private HashMap<DexNamedProtoNode, DexMethodNode> mProcessedMethods = new HashMap<>();

    private HashMap<DexNamedFieldProtoNode, DexFieldNode> mUnProcessedFields = new HashMap<>();

    private HashMap<DexNamedFieldProtoNode, DexFieldNode> mProcessedFields = new HashMap<>();

    private InterceptorProcessor mInterceptorProcessor;

    private FieldHolderProcessor mFieldHolderProcessor;

    PatchArgument patchArgument;

    public static final String EXTRA_CLINIT_ADDED = "clinit_added";

    public LightChangedClassGenerator(PatchArgument patchArgument,
                                      DexClassNode oldOrgClassNode,
                                      DexClassNode newOrgClassNode,
                                      LightPatchClassPools classPools,
                                      TitanDexItemFactory dexFactory) {
        this.patchArgument = patchArgument;
        this.mOldOrgClassNode = oldOrgClassNode;
        this.mNewOrgClassNode = newOrgClassNode;
        this.mDexItemFactory = dexFactory;
        this.mClassPools = classPools;
        mInterceptorProcessor = new InterceptorProcessor(mDexItemFactory, this);
        mFieldHolderProcessor = new FieldHolderProcessor(mDexItemFactory, this);
    }

    public void generate() {
        this.mChangedClassNode = new DexClassNode(
                PatchUtils.getChangeType(mNewOrgClassNode.type, mDexItemFactory),
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                mNewOrgClassNode.superType,
                DexTypeList.empty());

        mClassPools.changedClassPool.addProgramClass(this.mChangedClassNode);

        DexClassVisitor changedClassVisitor = this.mChangedClassNode.asVisitor();

        changedClassVisitor.visitBegin();

        this.mNewOrgClassNode.accept(this);

        // 处理方法新增、变化
        processMethods(changedClassVisitor);

        this.mInterceptorProcessor.finish();
        // 处理新增字段
        processFields(changedClassVisitor);

        changedClassVisitor.visitEnd();
    }

    /**
     * 处理新增、变化的方法的入口方法
     *
     * @param changedClassVisitor
     */
    private void processMethods(DexClassVisitor changedClassVisitor) {
        while(!mUnProcessedMethods.isEmpty()) {

            // 取出第一个未处理的方法，并从未处理列表中移除
            Iterator<Map.Entry<DexNamedProtoNode, DexMethodNode>> it =
                    mUnProcessedMethods.entrySet().iterator();
            it.hasNext();
            Map.Entry<DexNamedProtoNode, DexMethodNode> entry = it.next();
            it.remove();

            // 提前放到已处理列表中，防止当前方法有递归调用从而多次处理
            mProcessedMethods.putIfAbsent(entry.getKey(), entry.getValue());

            DexMethodNode currentMethod = entry.getValue();

            if (currentMethod.isStaticInitMethod()) {
                processStaticInitMethod(currentMethod, changedClassVisitor);
            } else if (currentMethod.isInstanceInitMethod()) {
                processInstanceInitMethod(currentMethod, changedClassVisitor);
            } else {
                processNormalMethod(currentMethod, changedClassVisitor);
            }
        }
    }

    private void processFields(DexClassVisitor changedClassVisitor) {
        if (mUnProcessedFields.isEmpty()) {
            return;
        }
        while (!mUnProcessedFields.isEmpty()) {
            Iterator<Map.Entry<DexNamedFieldProtoNode, DexFieldNode>> it =
                    mUnProcessedFields.entrySet().iterator();
            it.hasNext();
            Map.Entry<DexNamedFieldProtoNode, DexFieldNode> entry = it.next();
            it.remove();

            mProcessedFields.putIfAbsent(entry.getKey(), entry.getValue());

            DexFieldNode currentField = entry.getValue();
            mFieldHolderProcessor.addField(currentField);
        }
        mFieldHolderProcessor.finish();
    }

    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {
        DiffMode diffMode = ChangedClassDiffMarker.getMethodDiffMode(dmn);
        if (diffMode.isAdded()) {
            addToProcessListIfNeed(dmn, false);
            if (dmn.isStaticInitMethod()) {
                setStaticInitMethodAdded(dmn);
            }

        } else if (diffMode.isChanged()) {
            addToProcessListIfNeed(dmn, true);
        }
        else if (diffMode.isUnChanged()) {
            // do noting
        }

    }

    /**
     * 将static init method标记为added，在patch loader里面需要根据此标记生成调用指令
     *
     * @param dmn
     */
    private void setStaticInitMethodAdded(DexMethodNode dmn) {
        mOldOrgClassNode.setExtraInfo(EXTRA_CLINIT_ADDED, true);
    }

    @Override
    public void visitField(DexFieldNode dfn) {
        DiffMode diffMode = ChangedClassDiffMarker.getFieldDiffMode(dfn);
        if (diffMode.isAdded()) {
            mUnProcessedFields.putIfAbsent(new DexNamedFieldProtoNode(dfn), dfn);
        } else if (diffMode.isAffectFinalField()) {
            mUnProcessedFields.putIfAbsent(new DexNamedFieldProtoNode(dfn), dfn);
        } else if (diffMode.isUnChanged()) {

        } else {

        }
    }

    @Override
    public void visitClassNodeEnd() {

    }

    private DexMethodNode getMethodNode(DexNamedProtoNode methodKey, DexClassNode classNode) {
        List<DexMethodNode> oldMethodNodes = classNode.getMethods().stream()
                .filter(m -> new DexNamedProtoNode(m).equals(methodKey))
                .collect(Collectors.toList());
        return oldMethodNodes.size() > 0 ? oldMethodNodes.get(0) : null;
    }

    private DexMethodNode getMethodNode(DexMethodNode methodNode, DexClassNode classNode) {
        return getMethodNode(new DexNamedProtoNode(methodNode), classNode);
    }

    /**
     * 添加到已处理方法列表中
     *
     * @param newMethodNode
     * @param addToInterceptList 是否放到需要拦截方法列表中，改变的方法需要拦截，结构上新增的方法则不需要，
     *                           因为方法访问权限问题需要copy方法的，也需要拦截
     * @return
     */
    private boolean addToProcessListIfNeed(DexMethodNode newMethodNode, boolean addToInterceptList) {
        DexNamedProtoNode methodKey = new DexNamedProtoNode(newMethodNode);
        if (!mProcessedMethods.containsKey(methodKey)) {
            mUnProcessedMethods.put(methodKey, newMethodNode);

            if (addToInterceptList) {
                DexMethodNode oldMethodNode = getMethodNode(methodKey, mOldOrgClassNode);

                if (oldMethodNode.isStaticInitMethod()) {
                    mInterceptorProcessor.addStaticInitMethod(oldMethodNode);
                } else if (oldMethodNode.isInstanceInitMethod()) {
                    mInterceptorProcessor.addInstanceInitMethod(oldMethodNode);
                } else {
                    mInterceptorProcessor.addNormalMethod(oldMethodNode);
                }
            }

        }
        return false;
    }

    /**
     * 
     *
     * @param newMethod
     * @param changedClassVisitor
     */
    private void processStaticInitMethod(DexMethodNode newMethod,
                                         DexClassVisitor changedClassVisitor) {

        DexConst.ConstMethodRef staticInitMethodRef = mDexItemFactory.changedClass
                .staticInitMethodForType(newMethod.owner);

        DexMethodVisitor staticInitMethodVisitor = changedClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        staticInitMethodRef.getOwner(),
                        staticInitMethodRef.getName(),
                        staticInitMethodRef.getParameterTypes(),
                        staticInitMethodRef.getReturnType(),
                        new DexAccessFlags(DexAccessFlags.ACC_STATIC, DexAccessFlags.ACC_PUBLIC)));

        staticInitMethodVisitor.visitBegin();

        staticInitMethodVisitor.visitEnd();

        DexCodeNode newCodeNode = newMethod.getCode();

        DexCodeRegisterCalculator changedCodeVisitor = new DexCodeRegisterCalculator(
                true,
                staticInitMethodRef.getParameterTypes(),
                new DexCodeFormatVerifier(staticInitMethodVisitor.visitCode()));


        DexClassLoader classLoaderFromNewPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode rewriteDcn = mClassPools.rewriteClassPool.findClassFromAll(type);
                if (rewriteDcn != null) {
                    return rewriteDcn;
                }
                return mClassPools.newOrgClassPool.findClassFromAll(type);
            }
        };

        DexClassLoader classLoaderFromOldPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return mClassPools.oldOrgClassPool.findClassFromAll(type);
            }
        };

        changedCodeVisitor.visitBegin();

        newCodeNode.accept(new DexCodeVisitor(new StaticInitChangedCodeRewriter(
                changedCodeVisitor, classLoaderFromNewPool, classLoaderFromOldPool,
                mDexItemFactory, this, newCodeNode)) {
            @Override
            public void visitBegin() {
                // 不在此处调用super
//                super.visitBegin();
            }

            @Override
            public void visitEnd() {
                // 不在此处调用super
//                super.visitBegin();
            }
        });

        changedCodeVisitor.fillRegisterCount();

        changedCodeVisitor.visitEnd();


    }

    private void processInstanceInitMethod(DexMethodNode newMethod,
                                           DexClassVisitor changedClassVisitor) {
        DexClassLoader classLoaderFromNewPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode rewriteDcn = mClassPools.rewriteClassPool.findClassFromAll(type);
                if (rewriteDcn != null) {
                    return rewriteDcn;
                }
                return mClassPools.newOrgClassPool.findClassFromAll(type);
            }
        };

        DexClassLoader classLoaderFromOldPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return mClassPools.oldOrgClassPool.findClassFromAll(type);
            }
        };
        LightInitMethodSplitter splitter = new LightInitMethodSplitter(this, mChangedClassNode,
                newMethod, classLoaderFromNewPool, classLoaderFromOldPool, mDexItemFactory);
        splitter.doSplit();
        mChangedClassNode.addMethod(splitter.getUnInitMethod());
        mChangedClassNode.addMethod(splitter.getInitBodyMethod());
    }

    private void processNormalMethod(DexMethodNode newMethod,
                                     DexClassVisitor changedClassVisitor) {

        DexCodeNode newCodeNode = newMethod.getCode();

        DexTypeList.Builder changedParaTypes = DexTypeList.newBuilder();
        if (!newMethod.isStatic()) {
            changedParaTypes.addType(newMethod.owner);
        }
        newMethod.parameters.forEach(t -> changedParaTypes.addType(t));

        DexMethodVisitor changedMethodVisitor = changedClassVisitor.visitMethod(
                new DexMethodVisitorInfo(newMethod.owner,
                        newMethod.name,
                        changedParaTypes.build(),
                        newMethod.returnType,
                        new DexAccessFlags(DexAccessFlags.ACC_STATIC, DexAccessFlags.ACC_PUBLIC)));

        changedMethodVisitor.visitBegin();

        DexCodeRegisterCalculator changedCodeVisitor = new DexCodeRegisterCalculator(
                true,
                changedParaTypes.build(),
                new DexCodeFormatVerifier(changedMethodVisitor.visitCode()));

        changedCodeVisitor.visitBegin();

        DexClassLoader classLoaderFromNewPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode rewriteDcn = mClassPools.rewriteClassPool.findClassFromAll(type);
                if (rewriteDcn != null) {
                    return rewriteDcn;
                }
                return mClassPools.newOrgClassPool.findClassFromAll(type);
            }
        };

        DexClassLoader classLoaderFromOldPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return mClassPools.oldOrgClassPool.findClassFromAll(type);
            }
        };

        newCodeNode.accept(
                new DexCodeVisitor(
                new NormalChangedCodeRewriter(
                        changedCodeVisitor, classLoaderFromNewPool, classLoaderFromOldPool,
                        mDexItemFactory, this, newCodeNode)) {
                    @Override
                    public void visitBegin() {
                        // 不在此处调用super
//                        super.visitBegin();
                    }

                    @Override
                    public void visitEnd() {
                        // 不在此处调用super
//                        super.visitEnd();
                    }
                });

        changedCodeVisitor.fillRegisterCount();

        changedCodeVisitor.visitEnd();

        changedMethodVisitor.visitEnd();
    }


    boolean addUnProcessedListIfNeed(DexMethodNode dmn) {
        DexNamedProtoNode key = new DexNamedProtoNode(dmn);
        if (!mProcessedMethods.containsKey(key)) {
            return mUnProcessedMethods.putIfAbsent(key, dmn) == null;
        }
        return false;
    }

    private static DexType getChangedType(DexType type, DexItemFactory factory) {
        return PatchUtils.getChangeType(type, factory);
    }

    private static DexConst.ConstMethodRef getChangedMethodRef(DexConst.ConstMethodRef methodRef,
                                                               boolean staticMethod,
                                                               DexItemFactory factory) {

        DexType changedType = getChangedType(methodRef.getOwner(), factory);

        DexTypeList paraTypes;
        if (staticMethod) {
            paraTypes = methodRef.getParameterTypes();
        } else {
            DexTypeList.Builder paraTypesBuilder = DexTypeList.newBuilder();
            paraTypesBuilder.addType(changedType);
            methodRef.getParameterTypes().forEach(t -> paraTypesBuilder.addType(t));
            paraTypes = factory.intern(paraTypesBuilder.build());
        }

        return DexConst.ConstMethodRef.make(changedType,
                methodRef.getName(),
                methodRef.getReturnType(),
                paraTypes);
    }

}
