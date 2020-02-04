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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 测试工具类
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/28
 */
public class TestUtil {

    public static List<File> getBootClassPath() {
        String androidSdkPath = System.getenv("ANDROID_HOME");
        if (androidSdkPath == null || androidSdkPath.isEmpty()) {
            throw new RuntimeException("未设置 ANDROID_HOME 环境变量，请设置后重启执行");
        }
        File androidJar = new File(String.format("%s/platforms/android-26/android.jar",
                androidSdkPath));
        File apacheHttpJar = new File(
                String.format("%s/platforms/android-26/optional/org.apache.http.legacy.jar",
                        androidSdkPath));
        List<File> bootClassPath = new ArrayList<>();
        bootClassPath.add(androidJar);
        bootClassPath.add(apacheHttpJar);

        return bootClassPath;
    }

    public static boolean noDifferent(File src, File expect) throws IOException {
        if (src == null || !src.exists() || expect == null || !expect.exists()) {
            throw new IllegalArgumentException();
        }
        Queue<File> srcFileQueue = new ArrayDeque<>();
        Queue<File> expectFileQueue = new ArrayDeque<>();

        srcFileQueue.offer(src);
        expectFileQueue.offer(expect);

        File srcElement;
        File expectElement;
        while (!srcFileQueue.isEmpty() && !expectFileQueue.isEmpty()) {
            srcElement = srcFileQueue.poll();
            expectElement = expectFileQueue.poll();
            if (srcElement.isDirectory() && expectElement.isDirectory()) {
                File[] srcFiles = srcElement.listFiles();
                if (srcFiles != null) {
                    srcFileQueue.addAll(Arrays.asList(srcFiles));
                }

                File[] expectFiles = expectElement.listFiles();
                if (expectFiles != null) {
                    expectFileQueue.addAll(Arrays.asList(expectFiles));
                }
            } else if (srcElement.isFile() && expectElement.isFile()) {
                String result = compareFile(srcElement, expectElement);
                if (!result.isEmpty()) {
                    throw new AssertionError("\n diff content: " + result
                            + "\n between output smali: \n "
                            + srcElement.getAbsolutePath()
                            + "\n and expect smali: \n "
                            + expectElement.getAbsolutePath());
                }
            }
        }

        if (srcFileQueue.isEmpty() && expectFileQueue.isEmpty()) {
            return true;
        } else {
            throw new AssertionError("have redundant files:  src: " + srcFileQueue + "  expect: " + expectFileQueue);
        }
    }

    /**
     * 比较两个文本内容是否一致
     */
    private static String compareFile(File src, File expect) throws IOException {
        String text1 = readFile(src.getAbsolutePath());
        String text2 = readFile(expect.getAbsolutePath());

        DiffMatchPatch dmp = new DiffMatchPatch();
        dmp.diffTimeout = 0;

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(text1, text2, false);
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation != DiffMatchPatch.Operation.EQUAL) {
                return diff.text;
            }
        }
        return "";
    }

    /**
     * Read a file from disk and return the text contents.
     */
    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileReader input = new FileReader(filename);
        BufferedReader bufRead = new BufferedReader(input);
        try {
            String line = bufRead.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = bufRead.readLine();
            }
        } finally {
            bufRead.close();
            input.close();
        }
        return sb.toString();
    }
}
