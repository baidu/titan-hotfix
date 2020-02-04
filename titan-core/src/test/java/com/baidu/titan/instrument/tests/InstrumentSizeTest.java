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

package com.baidu.titan.instrument.tests;

import com.baidu.titan.core.instrument.InstrumentFilter;
import com.baidu.titan.core.instrument.InstrumentMain;
import com.baidu.titan.core.instrument.InstrumentType;
import com.baidu.titan.core.tests.TestUtil;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.reader.MultiDexFileReader;
import com.baidu.titan.dex.smali.SmaliReader;
import com.baidu.titan.dex.writer.MultiDexFileWriter;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 插桩测试，同时配置titan_paras中参数列表
 *
 * @author shanghuibo@baidu.com
 * @since 2018/12/14
 */
public class InstrumentSizeTest {

    @Test
    public void testAll() throws IOException {
        File rootDir = new File(".").getAbsoluteFile().getParentFile();
        File testOrgDexDir = new File(rootDir, "titan-sample/titan-product/org-dex");
        File outApk = new File(rootDir, "titan-sample/titan-product/instumentSize.apk");
        testCase(testOrgDexDir, outApk);
    }

    public void testCase(File testOrgDexDir, File outApk) throws IOException {
        System.out.println("begin test for " + testOrgDexDir);
        InstrumentMain.Argument argument = new InstrumentMain.Argument();
        argument.setInstrumentType(InstrumentType.METHOD);
        argument.setBootClassPath(TestUtil.getBootClassPath());

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "titan-test");
        tmpDir.mkdirs();

        argument.setWorkDir(tmpDir);

        MultiDexFileBytes orgDexBytes = MultiDexFileBytes.createFromDirectory(testOrgDexDir);
        argument.setOldDexs(orgDexBytes);

        argument.setFilter(new InstrumentFilter() {
            @Override
            public boolean acceptClass(String typeDesc) {
                return true;
            }

            @Override
            public boolean acceptMethod(String methodDesc) {
                return true;
            }

        });


        InstrumentMain instrumentMain = new InstrumentMain(argument);
        MultiDexFileBytes outBytes = instrumentMain.doInstrument();

        MultiDexFileReader mdfr = new MultiDexFileReader(new DexItemFactory());
        outBytes.forEach((dexId, bytes) -> mdfr.addDexContent(dexId, bytes.getDexFileBytes()));

        MultiDexFileNode mdfn = new MultiDexFileNode();
        mdfr.accept(mdfn.asVisitor());

        System.out.println("out apk = " + outApk);

        if (!outBytes.isValid()) {
            throw new IllegalStateException();
        }
        outBytes.writeToZipFile(outApk);
        System.out.println("out apk size = " + outApk.length());
    }

}
