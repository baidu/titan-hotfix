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

import org.junit.Test;

import java.io.File;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/6/3
 */
public class TitanInstrumentFromProject {

    @Test
    public void test() throws Exception {
        File projectBaseDir = new File("/Users/zhangdi07/dev/repo/baidu/searchbox-android/titan-sample/");
        File baseDir = new File("/Users/zhangdi07/dev/titan/v2/20180602");
        File baseOutDir = new File(baseDir, "instrument-out");
        baseOutDir.mkdirs();

        MultiDexFileBytes multiDexFileBytes = MultiDexFileBytes.createFromDirectory(
                new File(projectBaseDir, "app/build/outputs/titan/debug/org-dex"));

        File manifestFile = new File(projectBaseDir,
                "app/build/intermediates/manifests/full/debug/AndroidManifest.xml");

        doInstrument(multiDexFileBytes, baseOutDir, manifestFile);
    }

    private void doInstrument(MultiDexFileBytes multiDexFileBytes, File outBaseDir, File manifestFile) throws
            Exception {
        InstrumentMain.Argument argument = new InstrumentMain.Argument();
//        argument.apkFile = apkFile;

        argument.setInstrumentMateriel(multiDexFileBytes, manifestFile);

        argument.setWorkDir(new File(outBaseDir, "build"));

        argument.setBootClassPath(TestUtil.getBootClassPath());

        InstrumentMain instrument = new InstrumentMain(argument);
        MultiDexFileBytes outDexBytes = instrument.doInstrument();

        File outApk = new File(outBaseDir, "instrumented.apk");

        outDexBytes.writeToZipFile(outApk);

        System.out.println("out apk = " + outApk);

//        if (outputBytes == null) {
//            throw new IllegalStateException();
//        }
//        ZipUtil.writeDexBytesToApk(outApk , outputBytes);
    }

}
