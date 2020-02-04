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

import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.patch.PatchMain;
import com.baidu.titan.core.patch.PatchPolicy;
import com.baidu.titan.dex.MultiDexFileBytes;

import org.junit.Test;

import java.io.File;

/**
 * 测试全量patch
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/28
 */
public class FullPatchTest {

    @Test
    public void patch() throws Exception {

        File baseDir = new File("/Users/zhangdi07/dev/titan/v2/20180315");

        File oldApk = new File(baseDir, "old/apk.apk");
        File newApk = new File(baseDir, "new/apk.apk");

        PatchArgument patchArg = new PatchArgument();

        patchArg.setPatchPolicy(PatchPolicy.PATCH_POLICY_FULL_ONLY);

        patchArg.setDexOutDir(new File(baseDir, "dexout"));

        patchArg.setWorkDir(new File(baseDir, "work"));

        patchArg.oldProject.setOldApkFile(oldApk);

        patchArg.oldProject.setManifestFromOldApk(true);

        patchArg.oldProject.setOldInstrumentedDexs(MultiDexFileBytes.createFromZipFile(oldApk));

        patchArg.newProject.setNewApkFile(newApk);
        patchArg.newProject.setManifestFromNewApkFile(true);
        patchArg.newProject.setNewOrgDexs(MultiDexFileBytes.createFromZipFile(newApk));

        patchArg.setBootClassPath(TestUtil.getBootClassPath());

        PatchMain.doPatch(patchArg);

        System.out.println("finished!\n output " + patchArg.getDexOutDir());



    }

}
