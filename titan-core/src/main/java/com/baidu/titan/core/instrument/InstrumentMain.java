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

import com.baidu.titan.apktool.Apktool;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.filters.ComponentClassFilter;
import com.baidu.titan.core.filters.DisableInterceptClassNodePoolVisitorFilter;
import com.baidu.titan.core.filters.MeizuPushFilter;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.core.transforms.AccessFlagsTransformation;
import com.baidu.titan.core.transforms.StaticInitMethodFiller;
import com.baidu.titan.core.util.Utils;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.extensions.BestEffortMultiDexSplitter;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.extensions.DexInterfacesHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassPoolNode;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.writer.MultiDexFileWriter;
import com.baidu.titan.sdk.runtime.InteceptParameters;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * 插桩机制入口类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/6
 */
public class InstrumentMain {

    /**
     * 参数
     */
    private Argument mArgument;

    /**
     * 插桩参数信息
     */
    public static class Argument {

        private boolean fromApkFile;

        private MultiDexFileBytes orgDexs;

        private File manifestFile;

        private File apkFile;

        private InstrumentFilter filter;

        private Map<String, List<String>> components = new HashMap<>();

        private List<File> bootClassPath;

        private File workDir;

        private Set<DexType> mainDexList;

        /** 是否过滤meizu push sdk*/
        private boolean filterMeizuPush = true;

        public static final String COMPONENT_APPLICATION = "application";

        public static final String COMPONENT_ACTIVITY = "activity";

        public static final String COMPONENT_SERVICE = "service";

        public static final String COMPONENT_RECEIVER = "receiver";

        public static final String COMPONENT_PROVIDER = "provider";
        /** 拦截器Specail方法的参数里列表，没有没有设定则使用默认值 */
        private String[] mInvokeSpecialParas = InteceptParameters.SPECIAL_PARAMETERS;

        private InstrumentType mInstrumentType = InstrumentType.METHOD;

        private boolean makeVirtualMethodToPublic = true;

        public void setWorkDir(File workDir) {
            this.workDir = workDir;
        }

        public void setInstrumentType(InstrumentType type) {
            this.mInstrumentType = type;
        }

        public InstrumentType getInstrumentType() {
            return mInstrumentType;
        }

        public boolean isMakeVirtualMethodToPublic() {
            return this.makeVirtualMethodToPublic;
        }

        public void setOldDexs(MultiDexFileBytes oldDexs) {
            this.orgDexs = oldDexs;
        }

        public MultiDexFileBytes getOrgDexs() {
            return orgDexs;
        }

        public void setFilter(InstrumentFilter filter) {
            this.filter = filter;
        }

        public void setComponent(String cn, List<String> components) {
            this.components.put(cn, components);
        }

        public void setBootClassPath(List<File> classPath) {
            this.bootClassPath = classPath;
        }

        public void setInstrumentMateriel(MultiDexFileBytes orgDexs, File manifestFile) {
            this.orgDexs = orgDexs;
            this.manifestFile = manifestFile;
            this.fromApkFile = false;
        }

        public void setApkFile(File apkFile) {
            this.apkFile = apkFile;
            this.fromApkFile = true;
        }

        public void setInterceptInvokeSpecialParas(String[] paras) {
            this.mInvokeSpecialParas = paras;
        }

        public String[] getInterceptInvokeSpecialPars() {
            return mInvokeSpecialParas;
        }

        public void setMainDexList(Set<DexType> mainDexList) {
            this.mainDexList = mainDexList;
        }

        /**
         * 设置是否过滤meizu push sdk
         *
         * @param filterMeizuPush 是否过滤meizu push sdk
         */
        public void setFileterMeizuPush(boolean filterMeizuPush) {
            this.filterMeizuPush = filterMeizuPush;
        }

    }

    public InstrumentMain(Argument argument) {
        this.mArgument = argument;
    }

    private boolean fillApplicationPool(ApplicationDexPool appPool, Argument argument) {

        // convert dex files to class pool
        appPool.fillProgramDexs(mArgument.orgDexs);

        for (File libraryFile: mArgument.bootClassPath) {
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
        return true;
    }

    private void setupProject(ApplicationDexPool appPool, Argument argument) {
        // fill library and program class pool
        fillApplicationPool(appPool, argument);

        // setup component info
        if (argument.fromApkFile) {
            File projectApkToolDir = new File(argument.workDir, "instrumentApkToolDir");
            fillComponentInfoFromApk(projectApkToolDir, argument.apkFile, argument.components);
        } else {
            if (argument.manifestFile != null) {
                fillComponentsInfo(argument.manifestFile, argument.components);
            }

        }


        // mark class kind
        appPool.acceptProgram(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM));
        appPool.acceptLibrary(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_LIBRARY));


    }

    private static boolean fillComponentInfoFromApk(File apktoolDir, File apkPath,
                                                    Map<String, List<String>> components) {
        if (!apktoolDir.exists()) {
            if (!Apktool.decodeApk(apkPath, apktoolDir, Apktool.FLAG_NO_SOURCE)) {
                return false;
            }
        }

        File manifestFile = new File(apktoolDir, "apktool-out/AndroidManifest.xml");
        return fillComponentsInfo(manifestFile, components);
    }

    private static boolean fillComponentsInfo(File manifestFile,
                                              Map<String, List<String>> components) {
        try {

            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(manifestFile);

            // read activity class names
            List<Node> activityNodes = document.selectNodes("/manifest/application/activity");
            if (activityNodes != null) {
                List<String> activityTypes = activityNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> Utils.classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_ACTIVITY, activityTypes);
            }

            // read service class names
            List<Node> serviceNodes = document.selectNodes("/manifest/application/service");
            if (serviceNodes != null) {
                List<String> serviceTypes = serviceNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> Utils.classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_SERVICE, serviceTypes);
            }

            // read provider class names
            List<Node> providerNodes = document.selectNodes("/manifest/application/provider");
            if (providerNodes != null) {
                List<String> providerTypes = providerNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> Utils.classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_PROVIDER, providerTypes);
            }

            // read receiver class names
            List<Node> receiverNodes = document.selectNodes("/manifest/application/receiver");
            if (receiverNodes != null) {
                List<String> receiverTypes = receiverNodes.stream()
                        .map(node -> ((Element)node).attribute("name").getValue())
                        .map(className -> Utils.classNameToTypeDesc(className))
                        .collect(Collectors.toList());
                components.put(InstrumentMain.Argument.COMPONENT_RECEIVER, receiverTypes);
            }
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    /**
     * 执行插桩逻辑
     *
     * @return
     */
    public MultiDexFileBytes doInstrument() {

        TitanDexItemFactory factory = new TitanDexItemFactory();

        ApplicationDexPool appPool = new ApplicationDexPool(factory);

        this.mArgument.workDir.mkdirs();

        setupProject(appPool, this.mArgument);

        // setup for class hierarchy
        appPool.acceptAll(new DexSuperClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexSubClassHierarchyFiller(appPool::findClassFromAll));
        appPool.acceptAll(new DexInterfacesHierarchyFiller(appPool::findClassFromAll));

        if (this.mArgument.filterMeizuPush) {
            appPool.acceptProgram(new MeizuPushMarker());
        }

        // collect package-info annotations
        DisableInterceptCollector disableCollector = new DisableInterceptCollector(factory);
        appPool.acceptProgram(disableCollector);
        // mark disable intercept flag
        appPool.acceptProgram(new DisableInterceptMarker(disableCollector, mArgument.filter, factory));

        // 修改类及其成员的访问级别，以便Patch修复的时候，避免反射调用，提高效率
        appPool.acceptProgram(new MeizuPushFilter(new AccessFlagsTransformation(
                true,
                mArgument.isMakeVirtualMethodToPublic(),
                true,
                true)));

        // 收集所有Android组件信息
        appPool.acceptAll(new AndroidComponentMarker(
                mArgument.components.get(Argument.COMPONENT_APPLICATION),
                AndroidComponentFlag.TYPE_APPLICATION));
        appPool.acceptAll(new AndroidComponentMarker(
                mArgument.components.get(Argument.COMPONENT_ACTIVITY),
                AndroidComponentFlag.TYPE_ACTIVITY));
        appPool.acceptAll(new AndroidComponentMarker(
                mArgument.components.get(Argument.COMPONENT_SERVICE),
                AndroidComponentFlag.TYPE_SERVICE));
        appPool.acceptAll(new AndroidComponentMarker(
                mArgument.components.get(Argument.COMPONENT_RECEIVER),
                AndroidComponentFlag.TYPE_BROADCAST_RECEIVER));
        appPool.acceptAll(new AndroidComponentMarker(
                mArgument.components.get(Argument.COMPONENT_PROVIDER),
                AndroidComponentFlag.TYPE_CONTENT_PROVIDER));

        // 针对全量热更新的情况，收集所有的Android组件层级信息。
        if (mArgument.getInstrumentType() == InstrumentType.FULL) {
            InstrumentGenesisClassHierarchy igch = new InstrumentGenesisClassHierarchy();
            igch.buildGenesisClassForComponent(
                    factory.activityClass.type,
                    appPool, AndroidComponentFlag.TYPE_ACTIVITY, factory);
            igch.buildGenesisClassForComponent(
                    factory.serviceClass.type,
                    appPool, AndroidComponentFlag.TYPE_SERVICE, factory);
            igch.buildGenesisClassForComponent(
                    factory.broadcastReceiverClass.type,
                    appPool, AndroidComponentFlag.TYPE_BROADCAST_RECEIVER, factory);
            igch.buildGenesisClassForComponent(
                    factory.contentProviderClass.type,
                    appPool, AndroidComponentFlag.TYPE_CONTENT_PROVIDER, factory);

            // 对于所有Component class,都插入一个<clinit>方法，作用是做到拦截器(interceptor初始化的懒加载)，
            // 如果之前不存在的话
            appPool.acceptProgram(new ComponentClassFilter(new StaticInitMethodFiller(factory)));
        }

        // 对于方法级热修复，是否支持新增字段的修复能力
        boolean supportAddField = true;
        if (supportAddField) {
            appPool.acceptProgram(
                    new DisableInterceptClassNodePoolVisitorFilter(
                            new FieldSupportTransformation(factory), true));
        }

        // 添加拦截器字段
        appPool.acceptProgram(
                new DisableInterceptClassNodePoolVisitorFilter(
                        new AddInterceptorFieldTransformation(factory), true));

        // do real instrument
        appPool.acceptProgram(
                new DisableInterceptClassNodePoolVisitorFilter(
                        new InstrumentTransformation(
                                factory,
                                true,
                                mArgument.getInstrumentType(),
                                appPool,
                                mArgument.getInterceptInvokeSpecialPars()),
                        true));

        // ApplicationDexPool.programPool => DexClassPoolNode
        DexClassPoolNode classPool = new DexClassPoolNode();
        appPool.acceptProgram(classPool.asVisitor());

        Set<DexType> mainDexList = mArgument.mainDexList == null
                ? Collections.emptySet() : mArgument.mainDexList;
        // multidex分包策略
        // TODO 后继根据DexLayout进行关联度分析进行分包
        BestEffortMultiDexSplitter multiDexSplitter =
                new BestEffortMultiDexSplitter(classPool, mainDexList, false);
        multiDexSplitter.split();

        // 写入后端文件
        MultiDexFileNode mdfn = multiDexSplitter.getMultiDexFileNode();
        MultiDexFileWriter writer = new MultiDexFileWriter();
//        ExecutorService executors =
//                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        mdfn.accept(writer, executors);
        mdfn.accept(writer);
//        executors.shutdown();

        return writer.getMultiDexFileBytes();
    }
}
