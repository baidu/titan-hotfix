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

package com.baidu.titan.plugin.patch.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * proguard applymapping相关逻辑
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/17
 */

public class ProguardConfigTask extends DefaultTask {

    File applyMappingFile;
    def applicationVariant
    File proguardConfigFile

    @TaskAction
    void writeProguardConfig() {
        project.logger.error("run proguard config, mapping dir = " + applyMappingFile)

        proguardConfigFile.delete()
        proguardConfigFile.getParentFile().mkdirs()

        def writer = proguardConfigFile.newWriter()
        writer.write("-applymapping " + applyMappingFile.getAbsolutePath())
        writer.write("\n")
        writer.close()

        applicationVariant.getBuildType().buildType.proguardFiles(proguardConfigFile)
        def files = applicationVariant.getBuildType().buildType.getProguardFiles()

        project.logger.error("new proguard files is ${files}")
    }
}
