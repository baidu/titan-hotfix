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

package com.baidu.titan.apktool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


/**
 * @author zhangdi
 * @since 2017/8/22
 */

public class Apktool {

    public static final int FLAG_NO_RESOURCE = 1 << 0;

    public static final int FLAG_NO_SOURCE = 1 << 1;

    public static final int FLAG_FORCE = 1 << 2;

    /**
     * decompile apk at outDir/apktool-out dir
     *
     * @param apkFile
     * @param outDir
     * @param flags
     * @return
     */
    public static boolean decodeApk(File apkFile, File outDir, int flags) {

        ArrayList<String> argList = new ArrayList<>();
        if ((flags & FLAG_NO_RESOURCE) != 0) {
            argList.add("-r");
        }

        if ((flags & FLAG_NO_SOURCE) != 0) {
            argList.add("-s");
        }

        if ((flags & FLAG_FORCE) != 0) {
            argList.add("-f");
        }

        argList.add("d");

        argList.add("-o");
        argList.add(new File(outDir, "apktool-out").getAbsolutePath());

        argList.add(apkFile.getAbsolutePath());

        String[] args = new String[argList.size()];
        argList.toArray(args);

        outDir.mkdirs();

        File tmpApkJarFile = null;
        InputStream apktoolJarInput = null;
        FileOutputStream apkToolJarOutput = null;
        URLClassLoader apkToolClassLoader = null;

        try {
            tmpApkJarFile = File.createTempFile(".apkJar", ".jar", outDir);
            apktoolJarInput = Apktool.class.getResourceAsStream("apktool_2.2.4.jar");

            apkToolJarOutput = new FileOutputStream(tmpApkJarFile);

            byte[] buf = new byte[512 * 1024];

            int len;

            while ((len = apktoolJarInput.read(buf)) > 0) {
                apkToolJarOutput.write(buf, 0, len);
            }

            apkToolClassLoader = new URLClassLoader(
                    new URL[]{new URL("file", null, tmpApkJarFile.getAbsolutePath())},
                    Object.class.getClassLoader());

            Class<?> apktoolMainClass = apkToolClassLoader.loadClass("brut.apktool.Main");

            Method apktoolMainMethod = apktoolMainClass.getDeclaredMethod("main", String[].class);

            apktoolMainMethod.invoke(null, (Object) args);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (apkToolClassLoader != null) {
                try {
                    apkToolClassLoader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (apktoolJarInput != null) {
                try {
                    apktoolJarInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (apkToolJarOutput != null) {
                try {
                    apkToolJarOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tmpApkJarFile != null) {
                tmpApkJarFile.delete();
            }
        }
        return false;
    }
}
