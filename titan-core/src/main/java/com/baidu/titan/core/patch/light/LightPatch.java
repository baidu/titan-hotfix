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

package com.baidu.titan.core.patch.light;

import com.baidu.titan.core.Constant;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.PatchException;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.patch.light.diff.ClassPoolDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffContext;
import com.baidu.titan.core.patch.light.diff.DiffStatus;
import com.baidu.titan.core.patch.light.generator.LightClassClinitInterceptorGenerator;
import com.baidu.titan.core.patch.light.generator.LightDexPatchGenerator;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.extensions.BestEffortMultiDexSplitter;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.extensions.DexInterfacesHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.extensions.MultiDexSplitter;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexClassPoolNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;
import com.baidu.titan.sdk.common.TitanConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 生成LightPatch（轻量Patch）入口类。
 * <p>
 * 生成LightPatch会包含两个过程：
 *
 * <li>analyze阶段，分析每一个类，类成员的变化，确定可否用轻量Patch生成，并完成标记</li>
 * <li>generate阶段，在上一阶段结果为轻量Patch可兼容的情况下，根据标记结果生成Patch</li>
 *
 * </p>
 *
 * <p>
 *
 * 明确一下几个概念：
 *
 * <li>old-org-dexs           -> 需要热修复的未插桩Dex(s) </li>
 * <li>old-instrumented-dexs  -> 需要热修复的已插桩Dex(s)  </li>
 * <li>new-org-dexs           -> 修复后的未插桩Dex(s) </li>
 *
 * </p>
 *
 * @author zhangdi07@baidu.com
 * @since 2018/4/30
 */
public class LightPatch {

    private PatchArgument mArgument;

    private TitanDexItemFactory mDexFactory;

    private LightPatchClassPools mClassPools;

    private MultiDexFileNode mMultiDexFileNode;

    private boolean mPatchFinished = false;

    public LightPatch(PatchArgument argument) {
        this.mArgument = argument;
        setup();
    }

    private void setup() {
        TitanDexItemFactory factory = new TitanDexItemFactory();
        mDexFactory = factory;

        this.mClassPools = new LightPatchClassPools();

        // old org class pool
        ApplicationDexPool oldOrgDexPool = new ApplicationDexPool(factory);
        setupForOldOrgProject(oldOrgDexPool, mArgument);
        mClassPools.oldOrgClassPool = oldOrgDexPool;

        // old instrumented class pool

        setupForOldInstrumentedProject(mClassPools.oldOrgClassPool, mArgument, factory);
//        mClassPools.oldInstrumentedClassPool = oldInstrumentedDexPool;
        // assign method id for old instrumented class


        // new org class pool
        ApplicationDexPool newOrgDexPool = new ApplicationDexPool(factory);
        setupForNewProject(newOrgDexPool, mArgument);
        mClassPools.newOrgClassPool = newOrgDexPool;

        mClassPools.interceptorClassPool = new ApplicationDexPool(factory);
        mClassPools.changedClassPool = new ApplicationDexPool(factory);
        mClassPools.fieldHolderClassPool = new ApplicationDexPool(factory);
        mClassPools.clinitIntercepotroClassPool = new ApplicationDexPool(factory);
        mClassPools.patchLoaderClassPool = new ApplicationDexPool(factory);
        mClassPools.addedClassPool = new ApplicationDexPool(factory);
        mClassPools.rewriteClassPool = new ApplicationDexPool(factory);
        mClassPools.lazyInitClassPool = new ApplicationDexPool(factory);

    }

    private static String getMethodSignature(DexMethodNode methodNode) {
        StringBuilder sb = new StringBuilder();

        sb.append(methodNode.owner.toTypeDescriptor());
        sb.append("->");
        sb.append(methodNode.name.toString());
        sb.append("(");
        if (methodNode.parameters != null) {
            for (int i = 0; i < methodNode.parameters.count(); i++) {
                sb.append(methodNode.parameters.getType(i).toTypeDescriptor());
            }
        }
        sb.append(")");
        sb.append(methodNode.returnType.toTypeDescriptor());
        return sb.toString();
    }

    public boolean analyze() {
        DiffContext diffContext = new DiffContext(mClassPools.newOrgClassPool,
                mClassPools.oldOrgClassPool,
                mClassPools.rewriteClassPool,
                mDexFactory, mArgument.isSupportFinalFieldChange(),
                mArgument.getClassPatchFilter(),
                mArgument.getLoadPolicy());
        ClassPoolDiffMarker classPoolDiffMarker = new ClassPoolDiffMarker(diffContext);


        DiffStatus diff = classPoolDiffMarker.diff();

        System.out.println("class pool diff status " + diff);

        PrintWriter writer = new PrintWriter(System.out);
        classPoolDiffMarker.printDiffStatus(new TitanLogger(writer, true));
        writer.flush();

        TitanLogger extraLogger = mArgument.getExtraLogger();
        if (extraLogger != null) {
            classPoolDiffMarker.printDiffStatus(extraLogger);
        }

        File smaliOutDir = mArgument.getSmaliOutDir();
        if (smaliOutDir != null) {
            try {
                classPoolDiffMarker.toSmaliFile(mArgument.getSmaliOutDir());
            } catch (IOException e) {
                throw new PatchException("output changed file to smali file failed!", e);
            }
        }

        return diff != DiffStatus.CHANGED_INCOMPATIBLE;
    }

    public boolean doPatch() {
        LightDexPatchGenerator lightDexPatchGenerator =
                new LightDexPatchGenerator(mClassPools, mDexFactory, mArgument);
        lightDexPatchGenerator.generate();

        // 将各种类型Class放到同一个DexClassPool中
        DexClassPoolNode patchPool = new DexClassPoolNode();
        DexClassPoolNodeVisitor addToPatchPoolVisitor = new DexClassPoolNodeVisitor() {

            @Override
            public void visitClass(DexClassNode dcn) {
                patchPool.addClass(dcn);
            }

            @Override
            public void classPoolVisitEnd() {

            }

        };

        this.mClassPools.addedClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.interceptorClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.changedClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.fieldHolderClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.patchLoaderClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.clinitIntercepotroClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.patchLoaderClassPool.acceptProgram(addToPatchPoolVisitor);
        this.mClassPools.clinitIntercepotroClassPool.acceptProgram(addToPatchPoolVisitor);

        // 将patch loader和interceptor相关类放到patch的第一个dex中，以便提升加载速度
        Set<DexType> mainDexList = new HashSet<>();
        DexClassPoolNodeVisitor addToMainListVisitor = new DexClassPoolNodeVisitor() {

            @Override
            public void visitClass(DexClassNode dcn) {
                mainDexList.add(dcn.type);
            }

            @Override
            public void classPoolVisitEnd() {

            }

        };

        this.mClassPools.patchLoaderClassPool.acceptProgram(addToMainListVisitor);
        this.mClassPools.clinitIntercepotroClassPool.acceptProgram(addToMainListVisitor);

        // 做multidex分包
        MultiDexSplitter multiDexSplitter = new BestEffortMultiDexSplitter(patchPool,
                mainDexList, false);

        if (multiDexSplitter.split() != BestEffortMultiDexSplitter.SPLIT_SUCCESS)  {
            return false;
        }

        mMultiDexFileNode = multiDexSplitter.getMultiDexFileNode();
        mPatchFinished = true;
        return true;
    }

    public MultiDexFileNode getOutputs() {
        if (!mPatchFinished) {
            throw new IllegalStateException("call doPatch first!");
        }
        return this.mMultiDexFileNode;
    }

    private static boolean setupForOldOrgProject(ApplicationDexPool appPool, PatchArgument argument) {
        PatchArgument.OldProjectInfo oldProjectInfo = argument.oldProject;

        appPool.fillProgramDexs(oldProjectInfo.getOldOrgDexs());
        oldProjectInfo.setOldOrgDexs(null);

        for (File libraryFile: argument.getBootClassPath()) {
            JarInputStream jarInput = null;
            try {
                jarInput = new JarInputStream(new FileInputStream(libraryFile));
                JarEntry jarEntry = null;
                while ((jarEntry = jarInput.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16 * 1024];
                    int len;

                    while ((len = jarInput.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    appPool.fillLibraryClass(baos.toByteArray());
                    baos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (jarInput != null) {
                        jarInput.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        // setup for class hierarchy
        appPool.acceptAll(new DexSuperClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexSubClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexInterfacesHierarchyFiller(appPool::findClassFromAll));

        // mark class kind
        appPool.acceptProgram(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM));
        appPool.acceptLibrary(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_LIBRARY));
        return true;
    }

    private static boolean setupForOldInstrumentedProject(ApplicationDexPool oldOrgPool,
                                                          PatchArgument argument, DexItemFactory factory) {
        ApplicationDexPool appPool = new ApplicationDexPool(factory);
        PatchArgument.OldProjectInfo oldProjectInfo = argument.oldProject;

        appPool.fillProgramDexs(oldProjectInfo.getOldInstrumentedDexs());
        oldProjectInfo.setOldInstrumentedDexs(null);

        for (File libraryFile: argument.getBootClassPath()) {
            JarInputStream jarInput = null;
            try {
                jarInput = new JarInputStream(new FileInputStream(libraryFile));
                JarEntry jarEntry = null;
                while ((jarEntry = jarInput.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16 * 1024];
                    int len;

                    while ((len = jarInput.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    appPool.fillLibraryClass(baos.toByteArray());
                    baos.close();
                }

            } catch (IOException e) {
                return false;
            } finally {
                try {
                    if (jarInput != null) {
                        jarInput.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        // setup for class hierarchy
//        appPool.acceptAll(new DexSuperClassHierarchyFiller(appPool::findClassFromAll));
//        appPool.acceptAll(new DexSubClassHierarchyFiller(appPool::findClassFromAll));
//        appPool.acceptAll(new DexInterfacesHierarchyFiller(appPool::findClassFromAll));
//
//        // mark class kind
//        appPool.acceptProgram(
//                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM));
//        appPool.acceptLibrary(
//                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_LIBRARY));

        appPool.getProgramClassPool().forEach((t, classNode) -> {
            MethodIdAssigner.assignMethodId(classNode);
            final HashMap<DexString, DexAccessFlags> flagsMap = new HashMap<>();
            classNode.getFields().forEach(fieldNode -> {
                flagsMap.put(fieldNode.name, fieldNode.accessFlags);
            });

            final HashMap<String, DexAccessFlags> methodFlagsMap = new HashMap<>();
            final HashMap<String, Integer> methodIdMap = new HashMap<>();

            classNode.getMethods().forEach(methodNode -> {
                methodFlagsMap.put(getMethodSignature(methodNode), methodNode.accessFlags);
                methodIdMap.put(getMethodSignature(methodNode), methodNode.getExtraInfo("_extra_method_id"));
            });

            DexClassNode oldOrgClassNode = oldOrgPool.findClassFromAll(classNode.type);
            if (oldOrgClassNode != null) {
                oldOrgClassNode.setExtraInfo(Constant.EXTRA_KEY_INSTRUMENT_ACCESS_FLAGS, classNode.accessFlags);
                oldOrgClassNode.getFields().forEach(fieldNode -> {
                    DexAccessFlags flags = flagsMap.get(fieldNode.name);
                    if (flags == null) {
                        System.out.println("method " + fieldNode + " access flags is null");
                    }
                    fieldNode.setExtraInfo(Constant.EXTRA_KEY_INSTRUMENT_ACCESS_FLAGS, flags);
                    if (classNode.type.toTypeDescriptor().contains("DebugViewModel")) {
                        if (fieldNode.name.toString().contains("isShow")) {
                            System.out.println(fieldNode + " accessflags = " +  flags.getFlags());
                        }
                    }
                });

                oldOrgClassNode.getMethods().forEach(methodNode -> {
                    String methodSignature = getMethodSignature(methodNode);
                    DexAccessFlags flags = methodFlagsMap.get(methodSignature);
                    if (flags == null) {
                        System.out.println("method " + methodNode + " access flags is null");
                    }

                    methodNode.setExtraInfo(Constant.EXTRA_KEY_INSTRUMENT_ACCESS_FLAGS, flags);
                    if (methodIdMap.containsKey(methodSignature)) {
                        int methodId = methodIdMap.get(methodSignature);
                        methodNode.setExtraInfo("_extra_method_id", methodId);
                    }
                });
            }
        });
        return true;
    }

    private static boolean setupForNewProject(ApplicationDexPool appPool, PatchArgument argument) {
        PatchArgument.NewProjectInfo newProjectInfo = argument.newProject;

        appPool.fillProgramDexs(newProjectInfo.getNewOrgDexs());
        newProjectInfo.setNewOrgDexs(null);

        for (File libraryFile: argument.getBootClassPath()) {
            JarInputStream jarInput = null;
            try {
                jarInput = new JarInputStream(new FileInputStream(libraryFile));
                JarEntry jarEntry = null;
                while ((jarEntry = jarInput.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16 * 1024];
                    int len;

                    while ((len = jarInput.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    appPool.fillLibraryClass(baos.toByteArray());
                    baos.close();
                }

            } catch (IOException e) {
                return false;
            } finally {
                try {
                    if (jarInput != null) {
                        jarInput.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        // setup for class hierarchy
        appPool.acceptAll(new DexSuperClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexSubClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexInterfacesHierarchyFiller(appPool::findClassFromAll));

        // mark class kind
        appPool.acceptProgram(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM));
        appPool.acceptLibrary(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_LIBRARY));

        File newProjectApktoolDir = new File(argument.getWorkDir(), "newApkToolDir");

        return true;
    }

    /**
     * 获取patch中被修复的类信息
     * 目前分成两部分，一部分是被懒加载的类的typeDesc，一部分是需要立即加载的类的className
     *
     * @return patch中被修复的类信息
     */
    public JSONObject getClassInfo() {
        JSONObject classInfo = new JSONObject();
        JSONArray lazyHashCode = new JSONArray();
        JSONArray instantInitClass = new JSONArray();
        mClassPools.interceptorClassPool.getProgramClassPool().stream()
                .forEach(dcn -> {
                    DexType orgType = PatchUtils.getOrgTypeFromInterceptorType(dcn.type, mDexFactory);
                    if (LightClassClinitInterceptorGenerator.isInterceptorLazyInitAble(dcn)) {
                        lazyHashCode.put(orgType.toTypeDescriptor());
                    } else {
                        String className = PatchUtils.descToType(orgType.toTypeDescriptor());
                        instantInitClass.put(className);
                    }
                });
        classInfo.put(TitanConstant.KEY_LAZY_INIT_CLASS, lazyHashCode);
        classInfo.put(TitanConstant.KEY_INSTANT_INIT_CLASS, instantInitClass);
        return classInfo;
    }
}
