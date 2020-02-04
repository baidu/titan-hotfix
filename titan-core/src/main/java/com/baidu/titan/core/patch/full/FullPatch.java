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

import com.baidu.titan.apktool.Apktool;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.instrument.InstrumentMain;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.PatchException;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.analyze.MethodAnalyzer;
import com.baidu.titan.dex.extensions.BestEffortMultiDexSplitter;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.extensions.DexInterfacesHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.extensions.MultiDexSplitter;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.MultiDexFileNode;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/5/15
 */
public class FullPatch {


    private PatchArgument mArgument;

    private TitanDexItemFactory mFactory;

    private MultiDexFileNode mMultiDexFileNode;

    public FullPatch(PatchArgument argument) {
        this.mArgument = argument;
        this.mFactory = new TitanDexItemFactory();
    }


    public void doPatch() throws PatchException {
        TitanDexItemFactory factory = mFactory;
        DirectComponentMapper componentMapper = new DirectComponentMapper(factory);

        ApplicationDexPool oldAppPool = new ApplicationDexPool(factory);
        ApplicationDexPool newAppPool = new ApplicationDexPool(factory);
        ApplicationDexPool buddyClassPool = new ApplicationDexPool(factory);
        ApplicationDexPool changedClassPool = new ApplicationDexPool(factory);
        ApplicationDexPool loaderClassPool = new ApplicationDexPool(factory);
        ApplicationDexPool clinitInterceptorPool = new ApplicationDexPool(factory);
        ApplicationDexPool fullPatchPool = new ApplicationDexPool(factory);
        ApplicationDexPool interceptorPool = new ApplicationDexPool(factory);

        ApplicationDexPool finalFullPatchPool = new ApplicationDexPool(factory);

        // old class pool
        setupForOldProject(oldAppPool, mArgument);

        PatchGenesisClassHierarchy patchGenesisClassHierarchy =
                new PatchGenesisClassHierarchy(factory, interceptorPool, componentMapper);

        patchGenesisClassHierarchy.collectGenesisClassHierarchyForComponent(
                oldAppPool.findClassFromAll(factory.activityClass.type),
                AndroidComponentFlag.TYPE_ACTIVITY);

        patchGenesisClassHierarchy.collectGenesisClassHierarchyForComponent(
                oldAppPool.findClassFromAll(factory.serviceClass.type),
                AndroidComponentFlag.TYPE_SERVICE);

        patchGenesisClassHierarchy.collectGenesisClassHierarchyForComponent(
                oldAppPool.findClassFromAll(factory.broadcastReceiverClass.type),
                AndroidComponentFlag.TYPE_BROADCAST_RECEIVER);

        patchGenesisClassHierarchy.collectGenesisClassHierarchyForComponent(
                oldAppPool.findClassFromAll(factory.contentProviderClass.type),
                AndroidComponentFlag.TYPE_CONTENT_PROVIDER);

        patchGenesisClassHierarchy.fillAppPool();


        // new class pool
        setupForNewProject(newAppPool, mArgument);

        PatchBuddyClassHierarchy patchBuddyClassHierarchy = new PatchBuddyClassHierarchy(
                patchGenesisClassHierarchy, buddyClassPool, changedClassPool);

        patchBuddyClassHierarchy.buildBuddyClassForComponent(factory.activityClass.type,
                newAppPool, AndroidComponentFlag.TYPE_ACTIVITY, factory);

        patchBuddyClassHierarchy.buildBuddyClassForComponent(factory.serviceClass.type,
                newAppPool, AndroidComponentFlag.TYPE_SERVICE, factory);

        patchBuddyClassHierarchy.buildBuddyClassForComponent(factory.broadcastReceiverClass.type,
                newAppPool, AndroidComponentFlag.TYPE_BROADCAST_RECEIVER, factory);

        patchBuddyClassHierarchy.buildBuddyClassForComponent(factory.contentProviderClass.type,
                newAppPool, AndroidComponentFlag.TYPE_CONTENT_PROVIDER, factory);

        patchBuddyClassHierarchy.fillAppPool();


        newAppPool.acceptProgram(new ComponentInitMethodSplitTransformation(newAppPool,
                fullPatchPool, factory, patchBuddyClassHierarchy));

        new BuddyClassInitMethodSplitterTransformation(newAppPool, fullPatchPool, factory,
                patchBuddyClassHierarchy).spliteAll();

        newAppPool.acceptProgram(new FullPatchTransformation(newAppPool, interceptorPool,
                buddyClassPool, fullPatchPool, factory, patchBuddyClassHierarchy));


        PatchLoaderGenerator.generateLoader(interceptorPool, clinitInterceptorPool,
                loaderClassPool, factory);


        fullPatchPool.getProgramClassPool().forEach(dcn -> {
            if (!dcn.type.toTypeDescriptor().startsWith("Lcom/baidu/titan/sdk/")) {
                finalFullPatchPool.addProgramClass(dcn);
            }
        });

        interceptorPool.getProgramClassPool().forEach(dcn -> {
            finalFullPatchPool.addProgramClass(dcn);
        });

        changedClassPool.getProgramClassPool().forEach(dcn -> {
            finalFullPatchPool.addProgramClass(dcn);
        });

        buddyClassPool.getProgramClassPool().forEach(dcn -> {
            finalFullPatchPool.addProgramClass(dcn);
        });

        loaderClassPool.getProgramClassPool().forEach(dcn -> {
            finalFullPatchPool.addProgramClass(dcn);
        });

        clinitInterceptorPool.getProgramClassPool().forEach(dcn -> {
            finalFullPatchPool.addProgramClass(dcn);
        });


        // test
        DexClassLoader dcl = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode dcn = newAppPool.getLibraryClassPool().getClass(type);
                if (dcn == null) {
                    dcn = finalFullPatchPool.findClassFromAll(type);
                }
                if (dcn == null) {
                    dcn = oldAppPool.getProgramClassPool().getClass(type);
                }
                return dcn;
            }
        };

        finalFullPatchPool.getProgramClassPool().forEach(dcn -> {
            dcn.getMethods().forEach(dmn -> {
                if (dmn.getCode() != null) {
                    MethodAnalyzer ma = new MethodAnalyzer(dmn, dcl);
                    if (!ma.analyze()) {
                        System.err.println("fail for " + dmn);
                    }
                    ma.clear();
                }

            });
        });


        MultiDexSplitter multiDexSplitter =
                new BestEffortMultiDexSplitter(finalFullPatchPool.getProgramClassPool(), null, false);

        multiDexSplitter.split();

//        MultiDexFileWriter mdfw = new MultiDexFileWriter();

//        multiDexSplitter.getMultiDexFileNode().accept(mdfw);

//        MultiDexFileBytes patchMultiDexBytes = mdfw.getMultiDexFileBytes();

//        mArgument.getDexOutDir().mkdirs();

//        try {
//            patchMultiDexBytes.writeToDir(mArgument.getDexOutDir());
//        } catch (IOException e) {
//            throw new PatchException(e);
//        }
        mMultiDexFileNode = multiDexSplitter.getMultiDexFileNode();

    }

    public MultiDexFileNode getOutputs() {
        return mMultiDexFileNode;
    }


    private static String classNameToTypeDesc(String className) {
        return "L" + className.replace('.', '/') + ";";
    }

    private static boolean fillComponentsInfoFromApkFile(File apktoolDir, File apkPath,
                                                         Map<String, List<String>> components) {

        if (!apktoolDir.exists()) {
            if (!Apktool.decodeApk(apkPath, apktoolDir,
                    Apktool.FLAG_NO_SOURCE | Apktool.FLAG_FORCE)) {
                return false;
            }
         }
        File manifestFile = new File(apktoolDir, "apktool-out/AndroidManifest.xml");
        return fillComponentsInfoFromManifestFile(manifestFile, components);
    }


    private static boolean fillComponentsInfoFromManifestFile(File manifestFile,
                                                         Map<String, List<String>> components) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(manifestFile);

            // read activity class names
            List<Node> activityNodes = document.selectNodes("/manifest/application/activity");
            if (activityNodes != null) {
                List<String> activityTypes = activityNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_ACTIVITY, activityTypes);
            }

            // read service class names
            List<Node> serviceNodes = document.selectNodes("/manifest/application/service");
            if (serviceNodes != null) {
                List<String> serviceTypes = serviceNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_SERVICE, serviceTypes);
            }

            // read provider class names
            List<Node> providerNodes = document.selectNodes("/manifest/application/provider");
            if (providerNodes != null) {
                List<String> providerTypes = providerNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_PROVIDER, providerTypes);
            }

            // read receiver class names
            List<Node> receiverNodes = document.selectNodes("/manifest/application/receiver");
            if (receiverNodes != null) {
                List<String> receiverTypes = receiverNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_RECEIVER, receiverTypes);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setupForOldProject(ApplicationDexPool appPool, PatchArgument argument) {
        PatchArgument.OldProjectInfo oldProjectInfo = argument.oldProject;

//        oldProjectInfo.setOldInstrumentedDexs(
//                MultiDexFileBytes.createFromZipFile(oldProjectInfo.getOldApkFile()));

        appPool.fillProgramDexs(oldProjectInfo.getOldInstrumentedDexs());

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
                }

            } catch (IOException e) {
                return false;
            } finally {
                try {
                    jarInput.close();
                } catch (IOException e) {
                    return false;
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

        if (argument.oldProject.isManifestFromOldApk()) {
            File oldProjectApktoolDir = new File(argument.getWorkDir(), "oldApkToolDir");
            fillComponentsInfoFromApkFile(oldProjectApktoolDir, argument.oldProject.getOldApkFile(),
                    argument.oldProject.getComponents());
        } else {
            fillComponentsInfoFromManifestFile(argument.oldProject.getManifestFile(),
                    argument.oldProject.getComponents());
        }

        Map<String, List<String>> components = oldProjectInfo.getComponents();
        // setup component info
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_APPLICATION),
                AndroidComponentFlag.TYPE_APPLICATION));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_ACTIVITY),
                AndroidComponentFlag.TYPE_ACTIVITY));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_SERVICE),
                AndroidComponentFlag.TYPE_SERVICE));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_RECEIVER),
                AndroidComponentFlag.TYPE_BROADCAST_RECEIVER));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_PROVIDER),
                AndroidComponentFlag.TYPE_CONTENT_PROVIDER));


        return true;
    }


    private static boolean setupForNewProject(ApplicationDexPool appPool, PatchArgument argument) {
        PatchArgument.NewProjectInfo newProjectInfo = argument.newProject;

//        newProjectInfo.setNewOrgDexs(
//                MultiDexFileBytes.createFromZipFile(newProjectInfo.getNewApkFile()));


        appPool.fillProgramDexs(newProjectInfo.getNewOrgDexs());

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
                }

            } catch (IOException e) {
                return false;
            } finally {
                try {
                    jarInput.close();
                } catch (IOException e) {
                    return false;
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

        if (argument.newProject.isManifestFromNewApkFile()) {
            File newProjectApktoolDir = new File(argument.getWorkDir(), "newApkToolDir");

            fillComponentsInfoFromApkFile(newProjectApktoolDir, argument.newProject.getNewApkFile(),
                    argument.newProject.getComponents());
        } else {
            fillComponentsInfoFromManifestFile(argument.newProject.getManifestFile(),
                    argument.newProject.getComponents());
        }


        Map<String, List<String>> components = newProjectInfo.getComponents();
        // setup component info
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_APPLICATION),
                AndroidComponentFlag.TYPE_APPLICATION));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_ACTIVITY),
                AndroidComponentFlag.TYPE_ACTIVITY));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_SERVICE),
                AndroidComponentFlag.TYPE_SERVICE));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_RECEIVER),
                AndroidComponentFlag.TYPE_BROADCAST_RECEIVER));
        appPool.acceptAll(new AndroidComponentMarker(
                components.get(PatchArgument.COMPONENT_PROVIDER),
                AndroidComponentFlag.TYPE_CONTENT_PROVIDER));

        return true;
    }

}
