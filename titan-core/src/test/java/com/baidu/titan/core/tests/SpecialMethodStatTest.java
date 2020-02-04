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

package com.baidu.titan.core.tests;

import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.extensions.DexInterfacesHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * 用来统计主流应用方法参数列表
 *
 * @author shanghuibo@baidu.com
 * @since 2018/12/14
 */
public class SpecialMethodStatTest {

    @Test
    public void stat() throws Exception {
        File apkDir = new File("/home/shanghuibo/tmp");
        File outDir = new File("/home/shanghuibo/tmp");
        HashMap<String, Integer> statMap = new HashMap<>();

        Arrays.stream(apkDir.listFiles()).filter(f -> !f.isDirectory() && f.getName().endsWith(".apk"))
                .forEach(f -> {
                    try {
                        statOneApk(f, statMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        FileWriter fw = new FileWriter(new File(outDir, "statAll.txt"));
        List<Map.Entry<String, Integer>> list = statMap.entrySet().stream().sorted((e1, e2) -> {
            if (e1.getValue().intValue() == e2.getValue().intValue()) {
                return e1.getKey().compareTo(e2.getKey());
            }
            return e2.getValue().compareTo(e1.getValue());
        }).collect(Collectors.toList());
        int sum = 0;
        for (Map.Entry<String, Integer> e : list) {
            sum += e.getValue();
        }
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<String, Integer> e = list.get(i);
            count += e.getValue();
            System.out.println(e.getKey() + " " + e.getValue());
            float per = (float) count / sum;
            try {
                fw.write((i + 1) + " " + e.getKey() + " " + e.getValue() + " " + per + "\n");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        fw.close();
        System.out.println("done");
    }

    private void statOneApk(File apkFile, HashMap<String, Integer> statMap) throws Exception {
        DexItemFactory dexItemFactory = new DexItemFactory();
        ApplicationDexPool appDexPool = new ApplicationDexPool(dexItemFactory);
        appDexPool.fillProgramDexs(MultiDexFileBytes.createFromZipFile(apkFile));
        fillLbraryClass(appDexPool);

        System.out.println("start stat " + apkFile.getName());
        doStat(appDexPool, statMap);
    }

    private void fillLbraryClass(ApplicationDexPool appPool) {
        File libraryFile = TestUtil.getBootClassPath().get(0);

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

        } finally {
            try {
                jarInput.close();
            } catch (IOException e) {

            }

        }
    }

    private void doStat(ApplicationDexPool appDexPool, HashMap<String, Integer> statMap) throws Exception {

        appDexPool.acceptAll(new DexSuperClassHierarchyFiller(appDexPool::findClassFromAll));
        appDexPool.acceptAll(new DexSubClassHierarchyFiller(appDexPool::findClassFromAll));
        appDexPool.acceptAll(new DexInterfacesHierarchyFiller(appDexPool::findClassFromAll));

//        MultiDexFileNode multiDexFileNode = new MultiDexFileNode();
//        reader.accept(multiDexFileNode.asVisitor());
//
//        DexClassPoolNode classPoolNode = new DexClassPoolNode();
//        multiDexFileNode.accept(new DexClassPoolFiller(classPoolNode));

        DexClassLoader classLoader = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return appDexPool.findClassFromAll(type);
            }
        };
        int[] classCount = new int[1];
        appDexPool.acceptProgram(new DexClassPoolNodeVisitor() {
            @Override
            public void visitClass(DexClassNode dcn) {
                classCount[0] = classCount[0] + 1;
                dcn.getMethods().forEach(m -> {
                    StringBuilder descBuilder = new StringBuilder();
                    if (m.parameters.count() == 0) {
                        descBuilder.append(DexItemFactory.VoidClass.SHORT_DESCRIPTOR);
                    } else {
                        m.parameters.forEach(type -> {
                            char shortType = type.toShortDescriptor();
                            switch (shortType) {
                                case DexItemFactory.ArrayType.SHORT_DESCRIPTOR:
                                case DexItemFactory.ReferenceType.SHORT_DESCRIPTOR: {
                                    shortType = DexItemFactory.ReferenceType.SHORT_DESCRIPTOR;
                                }
                            }
                            descBuilder.append(shortType);
                        });
                    }

                    String desc = descBuilder.toString();
                    if (desc.length() > 50) {
                        System.out.println("desc = " + desc + " length = " + desc.length());
                        System.out.println("class = " + m.owner);
                        System.out.println("method = " + m.name);
                    }
                    statMap.compute(desc, (k, v) -> {
                        if (v == null) {
                            return 1;
                        } else {
                            return v + 1;
                        }
                    });

                });
            }

            @Override
            public void classPoolVisitEnd() {

            }
        });

        System.out.println("finish process, class count = " + classCount[0]);

    }

}
