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

import com.baidu.titan.core.instrument.InstrumentMain;
import com.baidu.titan.dex.MultiDexFileBytes;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * test
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/10
 */

public class InstrumentV2Test {

    private File baseOutDir;

    public InstrumentV2Test() {
    }

    @Test
    public void instrument() throws Exception {
        File baseDir = new File("/Users/zhangdi07/dev/titan/v2/20180426");
        File inputDir = baseDir;
                // new File(baseDir,"instrument");
        File baseOutDir = new File(baseDir, "instrument-out");
        baseOutDir.mkdirs();

        File manifestFile = new File(baseDir, "AndroidManifest.xml");

        File outDir = new File(baseOutDir, new SimpleDateFormat("yyyyMMddHHmmss").format(new
                Date()));
        outDir.mkdirs();

        for (File apkOrDex : inputDir.listFiles()) {
            if (!apkOrDex.isDirectory()) {
                if (apkOrDex.getName().endsWith(".dex")) {
                    System.out.println("[process file] " + apkOrDex.getAbsolutePath());
//                    processBytes(getFileContent(apkOrDex.getAbsolutePath()));
                } else if (apkOrDex.getName().endsWith(".apk")) {
                    System.out.println("[process apk] " + apkOrDex.getAbsolutePath());

                    doInstrument(apkOrDex, outDir, manifestFile);
                }
            }
        }
        System.out.println("done");
    }

    private void doInstrument(File apkFile, File outBaseDir, File manifestFile) throws Exception {
        InstrumentMain.Argument argument = new InstrumentMain.Argument();
//        argument.apkFile = apkFile;

        argument.setInstrumentMateriel(MultiDexFileBytes.createFromZipFile(apkFile), manifestFile);

        argument.setWorkDir(new File(outBaseDir, "build"));

        argument.setBootClassPath(TestUtil.getBootClassPath());

        InstrumentMain instrument = new InstrumentMain(argument);
        MultiDexFileBytes outputDexBytes = instrument.doInstrument();

        File outApk = new File(outBaseDir, "instrumented.apk");

        System.out.println("out apk = " + outApk);

        if (!outputDexBytes.isValid()) {
            throw new IllegalStateException();
        }
        outputDexBytes.writeToZipFile(outApk);
    }



    public void fillComponent(InstrumentMain.Argument argument) throws DocumentException {
        File outDir = new File("D:\\titan-v2\\instrument-v2\\apktool");
//        Apktool.decodeApk(new File("D:\\titan-v2\\instrument-v2\\s.apk"),
//                outDir, Apktool.FLAG_NO_SOURCE);



        File menifestFile = new File(outDir, "AndroidManifest.xml");
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(menifestFile);
        List<Node> activitys = document.selectNodes("/manifest/application/activity");
        List<String> ams = new ArrayList<>();
        for (Node node : activitys) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String am = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + am.replace('.', '/') + ";";
            ams.add(typeDesc);
        }

        argument.setComponent(InstrumentMain.Argument.COMPONENT_ACTIVITY, ams);

        List<Node> services = document.selectNodes("/manifest/application/service");
        List<String> ses = new ArrayList<>();
        for (Node node : services) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String service = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + service.replace('.', '/') + ";";
            ses.add(typeDesc);
        }

        argument.setComponent(InstrumentMain.Argument.COMPONENT_SERVICE, ses);


        List<Node> provides = document.selectNodes("/manifest/application/provider");
        List<String> ps = new ArrayList<>();
        for (Node node : provides) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String receiver = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + receiver.replace('.', '/') + ";";
            ps.add(typeDesc);
        }

        argument.setComponent(InstrumentMain.Argument.COMPONENT_PROVIDER, ps);


        List<Node> receivers = document.selectNodes("/manifest/application/receiver");
        List<String> rs = new ArrayList<>();
        for (Node node : receivers) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String receiver = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + receiver.replace('.', '/') + ";";
            rs.add(typeDesc);
        }

        argument.setComponent(InstrumentMain.Argument.COMPONENT_RECEIVER, rs);


    }



}
