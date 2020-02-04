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

import com.baidu.titan.core.instrument.InstrumentMain;
import com.baidu.titan.core.instrument.InstrumentType;
import com.baidu.titan.core.tests.TestUtil;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.reader.MultiDexFileReader;
import com.baidu.titan.dex.smali.SmaliReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插桩测试入口类
 *
 * @author zhangdi07@baidu.com
 * @since 2018/12/09
 */
@RunWith(Parameterized.class)
public class InstrumentTest {

    private File testDir;
    private File outRootDir;
    private String testName;

    public InstrumentTest(File testDir, File outRootDir, String testName) {
        this.testDir = testDir;
        this.outRootDir = outRootDir;
        this.testName = testName;
    }

    @Parameterized.Parameters(name = "{2}")
    public static Collection<Object[]> data() throws IOException {
        File instrumentTestDir = new File("src/test/smali/instrument");
        File outRootDir = new File("build/test/out/instrument");
        File[] testFiles = instrumentTestDir.listFiles((dir, name) -> name.startsWith("t"));

        List<Object[]> data = Arrays.stream(testFiles)
                .sorted()
                .map(testDir -> new Object[] {testDir, outRootDir, testDir.getName()})
                .collect(Collectors.toList());
        return data;
    }

    @Test
    public void testCase() throws IOException {
        System.out.println("begin test for " + testDir);

        File outDir = new File(outRootDir, testDir.getName());
        outDir.mkdirs();

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "titan-test");
        tmpDir.mkdirs();

        File inputTestDir = new File(testDir, "input");
        File expectTestDir = new File(testDir, "expect");

        InstrumentMain.Argument argument = new InstrumentMain.Argument();
        argument.setInstrumentType(InstrumentType.FULL);
        argument.setBootClassPath(TestUtil.getBootClassPath());
        argument.setWorkDir(tmpDir);

        SmaliReader smaliReader = new SmaliReader(
                SmaliReader.SmaliPath.createFromDir(inputTestDir), tmpDir);

        argument.setOldDexs(MultiDexFileBytes.createFromOrderedDexBytes(smaliReader.toDexFileBytes()));

        InstrumentMain instrumentMain = new InstrumentMain(argument);
        MultiDexFileBytes outBytes = instrumentMain.doInstrument();

        MultiDexFileReader mdfr = new MultiDexFileReader(new DexItemFactory());
        outBytes.forEach((dexId, bytes) -> mdfr.addDexContent(dexId, bytes.getDexFileBytes()));

        MultiDexFileNode mdfn = new MultiDexFileNode();
        mdfr.accept(mdfn.asVisitor());

        mdfn.smaliToDir(outDir);

        Assert.assertTrue(TestUtil.noDifferent(outDir, expectTestDir));
    }

}
