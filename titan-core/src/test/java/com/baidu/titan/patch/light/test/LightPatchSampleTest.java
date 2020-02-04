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
import com.baidu.titan.dex.writer.MultiDexFileWriter;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LightPatchSampleTest {
    @Test
    public void testAll() throws IOException {
        File curDir = Paths.get(".").normalize().toFile();
        File lightPatchTestDir = new File(curDir.getParentFile(), "titan-sample/titan-product");

        testCase(lightPatchTestDir);
    }


    public void testCase(File testDir) throws IOException {
        System.out.println("begin test for " + testDir);

        PatchArgument argument = new PatchArgument();
        argument.setPatchPolicy(PatchPolicy.PATCH_POLICY_LIGHT_ONLY);

        argument.setBootClassPath(TestUtil.getBootClassPath());

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "titan-test");
        tmpDir.mkdirs();

        argument.oldProject.setOldOrgDexs(
                MultiDexFileBytes.createFromDirectory(new File(testDir, "org-dex")));

        argument.oldProject.setOldInstrumentedDexs(
                MultiDexFileBytes.createFromZipFile(new File(testDir, "old.apk")));

        argument.newProject.setNewOrgDexs(
                MultiDexFileBytes.createFromDirectory(new File(testDir, "new-org-dex")));


        LightPatch lp = new LightPatch(argument);
        lp.analyze();
        lp.doPatch();
        MultiDexFileNode dexs = lp.getOutputs();
        System.out.println();
        dexs.smaliToDir(new File(testDir, "patch-smali"));
        MultiDexFileWriter fileWriter = new MultiDexFileWriter();
        dexs.accept(fileWriter);
        fileWriter.getMultiDexFileBytes().writeToZipFile(new File(testDir, "patch.apk"));

    }
}
