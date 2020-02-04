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

package com.baidu.titan.patch.light.test;

import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.PatchPolicy;
import com.baidu.titan.core.patch.light.LightPatch;
import com.baidu.titan.core.tests.TestUtil;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.node.MultiDexFileNode;
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
 * @author zhangdi07@baidu.com
 * @since 2018/9/19
 */
@RunWith(Parameterized.class)
public class LightPatchTest {

    private File testDir;
    private File outRootDir;
    private String testName;

    public LightPatchTest(File testDir, File outRootDir, String testName) {
        this.testDir = testDir;
        this.outRootDir = outRootDir;
        this.testName = testName;
    }


//    @Test
//    public void testAll() throws IOException {
//        File lightPatchTestDir = new File("src/test/smali/lightpatch");
//        File outRootDir = new File("build/test/out/lightpatch");
//        File[] testFiles = lightPatchTestDir.listFiles((dir, name) -> name.startsWith("t"));
//
//        for (File testCaseDir : testFiles) {
//            testCase(testCaseDir, outRootDir);
//        }
//    }


    @Parameterized.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        File lightPatchTestDir = new File("src/test/smali/lightpatch");
        File outRootDir = new File("build/test/out/lightpatch");
        File[] testFiles = lightPatchTestDir.listFiles((dir, name) -> name.startsWith("t"));
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

        PatchArgument argument = new PatchArgument();
        argument.setPatchPolicy(PatchPolicy.PATCH_POLICY_LIGHT_ONLY);
        argument.setBootClassPath(TestUtil.getBootClassPath());

        SmaliReader oldSmaliReader = new SmaliReader(
                SmaliReader.SmaliPath.createFromDir(new File(inputTestDir, "old")), tmpDir);

        SmaliReader oldInstrumentedSmaliReader = new SmaliReader(
                SmaliReader.SmaliPath.createFromDir(
                        new File(inputTestDir, "old-instrumented")), tmpDir);

        SmaliReader newSmaliReader = new SmaliReader(
                SmaliReader.SmaliPath.createFromDir(new File(inputTestDir, "new")), tmpDir);

        argument.oldProject.setOldOrgDexs(
                MultiDexFileBytes.createFromOrderedDexBytes(oldSmaliReader.toDexFileBytes()));

        argument.oldProject.setOldInstrumentedDexs(
                MultiDexFileBytes.createFromOrderedDexBytes(oldInstrumentedSmaliReader.toDexFileBytes()));

        argument.newProject.setNewOrgDexs(
                MultiDexFileBytes.createFromOrderedDexBytes(newSmaliReader.toDexFileBytes()));

        LightPatch lp = new LightPatch(argument);
        lp.analyze();
        lp.doPatch();
        MultiDexFileNode dexs = lp.getOutputs();
        System.out.println();
        dexs.smaliToDir(outDir);

        Assert.assertTrue(TestUtil.noDifferent(outDir, expectTestDir));
    }


}
