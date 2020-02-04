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
import com.baidu.titan.dex.analyze.MethodAnalyzer;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * test
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/16
 */
public class CodeAnalyzerTest {

    @Test
    public void analyze() throws Exception {
        File inputDir = new File("D:\\titan-v2\\20180117\\org-dex");
        File baseOutDir = new File("D:\\titan-v2\\instrument\\out\\" +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        baseOutDir.mkdirs();

        for (File apkOrDex: inputDir.listFiles()) {
            if (!apkOrDex.isDirectory()) {
                if (apkOrDex.getName().endsWith(".dex")) {
                    System.out.println("[process file] " + apkOrDex.getAbsolutePath());
//                    processBytes(getFileContent(apkOrDex.getAbsolutePath()));
                } else if (apkOrDex.getName().endsWith(".apk")){
                    System.out.println("[process apk] " + apkOrDex.getAbsolutePath());
//                    Map<Integer, byte[]> dexBytes = ZipUtil.getDexContentsFromApk(apkOrDex);

                    DexItemFactory dexItemFactory = new DexItemFactory();
                    ApplicationDexPool appDexPool = new ApplicationDexPool(dexItemFactory);
                    appDexPool.fillProgramDexs(MultiDexFileBytes.createFromZipFile(apkOrDex));
                    fillLbraryClass(appDexPool);

                    doAnalyze(appDexPool);
                }
            }
        }
        System.out.println("done");
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

    private void doAnalyze(ApplicationDexPool appDexPool) throws Exception {

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
                    if (m.getCode() != null) {
                        System.out.println("processing " + m.owner + " " + m.name);
                        MethodAnalyzer analyzer = new MethodAnalyzer(m, classLoader);
                        analyzer.analyze();
                    }


                });
            }

            @Override
            public void classPoolVisitEnd() {

            }
        });

        System.out.println("finish process, class count = " + classCount[0]);

    }

}
