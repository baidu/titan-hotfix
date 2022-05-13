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

package com.baidu.titan.core.patch;

import com.baidu.titan.core.patch.full.FullPatch;
import com.baidu.titan.core.patch.light.LightPatch;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.writer.MultiDexFileWriter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * Patch生成入口类
 *
 * @author zhangdi07
 * @since 2017/10/12
 */

public class PatchMain {

    public static void doPatch(PatchArgument argument) {
        if (argument.getWorkDir() == null) {
            throw new IllegalArgumentException();
        }

        argument.getWorkDir().mkdirs();


        PatchPolicy patchPolicy = argument.getPatchPolicy();

        boolean doLightPatch = false;
        boolean doFullPatch = false;

        FullPatch fullPatch = null;
        LightPatch lightPatch = null;

        switch (patchPolicy) {
            case PATCH_POLICY_FULL_ONLY : {
                doLightPatch = false;
                doFullPatch = true;
                break;
            }
            case PATCH_POLICY_LIGHT_ONLY: {
                lightPatch = new LightPatch(argument);
                boolean success = lightPatch.analyze();
                if (!success) {
                    throw new IllegalStateException("patch fail, incompatiable change exists, " +
                            "check logs for detail!");
                }
                doLightPatch = true;
                doFullPatch = false;
                break;
            }
            case PATCH_POLICY_LIGHT_THEN_FULL: {
                lightPatch = new LightPatch(argument);
                boolean success = lightPatch.analyze();
                if (success) {
                    doLightPatch = true;
                    doFullPatch = false;
                } else {
                    doLightPatch = false;
                    doFullPatch = true;
                }

                break;
            }
            default: {
                break;
            }
        }

        if (doLightPatch) {
            lightPatch.doPatch();
            MultiDexFileNode dexs = lightPatch.getOutputs();
            MultiDexFileWriter fileWriter = new MultiDexFileWriter();
            dexs.accept(fileWriter);
            try {
                fileWriter.getMultiDexFileBytes().writeToDir(argument.getDexOutDir());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            JSONObject classInfo = lightPatch.getClassInfo();
            File classInfoFile = new File(argument.getWorkDir(), "classInfo.json");
            FileWriter fw = null;
            try {
                fw = new FileWriter(classInfoFile);
                fw.write(classInfo.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (doFullPatch) {
            fullPatch = new FullPatch(argument);
            fullPatch.doPatch();
        }
    }

}
