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
import com.baidu.titan.core.patch.PatchArgument;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * patch test
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/19
 */
public class PatchTest {

    @Test
    public void readSmaps() throws IOException, NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("md5");

        File smaps = new File("D:\\tmp\\smaps");
        BufferedReader reader = new BufferedReader(new FileReader(smaps));
        String line;
        // bc580000-bc600000 rw-p 00000000 00:00 0                                  [anon:libc_malloc]
        Pattern vmaPattern = Pattern.compile(
                "([0-9a-fA-F]+?)-([0-9a-fA-F]+?) \\S* \\S* \\d\\d:\\d\\d \\d* \\s*(.*)");
        while ((line = reader.readLine()) != null) {
            Matcher matcher = vmaPattern.matcher(line);
            if (matcher.matches()) {
                System.out.println(matcher.group(1) + " - " + matcher.group(2) + " : " + matcher
                        .group(3).trim());

            }


        }

    }



    public void fillComponent(PatchArgument argument) throws DocumentException {
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

        argument.oldProject.setComponent(InstrumentMain.Argument.COMPONENT_ACTIVITY, ams);

        List<Node> services = document.selectNodes("/manifest/application/service");
        List<String> ses = new ArrayList<>();
        for (Node node : services) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String service = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + service.replace('.', '/') + ";";
            ses.add(typeDesc);
        }

        argument.oldProject.setComponent(InstrumentMain.Argument.COMPONENT_SERVICE, ses);


        List<Node> provides = document.selectNodes("/manifest/application/provider");
        List<String> ps = new ArrayList<>();
        for (Node node : provides) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String receiver = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + receiver.replace('.', '/') + ";";
            ps.add(typeDesc);
        }

        argument.oldProject.setComponent(InstrumentMain.Argument.COMPONENT_PROVIDER, ps);


        List<Node> receivers = document.selectNodes("/manifest/application/receiver");
        List<String> rs = new ArrayList<>();
        for (Node node : receivers) {
//            System.out.println(((Element) node).attribute("name").getValue());
            String receiver = ((Element) node).attribute("name").getValue();
            String typeDesc = "L" + receiver.replace('.', '/') + ";";
            rs.add(typeDesc);
        }

        argument.oldProject.setComponent(InstrumentMain.Argument.COMPONENT_RECEIVER, rs);


    }

    @Test
    public void patch() throws Exception {
        File inputDir = new File("D:\\titan-v2\\patch");
        File baseOutDir = new File("D:\\titan-v2\\instrument\\out\\"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        baseOutDir.mkdirs();

        for (File apkOrDex : inputDir.listFiles()) {
            if (!apkOrDex.isDirectory()) {
                if (apkOrDex.getName().endsWith(".dex")) {
                    System.out.println("[process file] " + apkOrDex.getAbsolutePath());
//                    processBytes(getFileContent(apkOrDex.getAbsolutePath()));
                } else if (apkOrDex.getName().endsWith(".apk")) {
                    System.out.println("[process apk] " + apkOrDex.getAbsolutePath());

                }
            }
        }
        System.out.println("done");
    }

}
