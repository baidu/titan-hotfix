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

package com.baidu.titan.plugin.patch.extensions

/**
 * patch配置信息
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/15
 */

public class PatchExtension {

    Closure oldApkFile

    boolean enable = true

    boolean patchEnable = true

    // external | external
    String prepareOldProjectPolicy = "just-build"

    String buildVariant = "release"

    boolean checkMappingFile = true

    String patchPackageName

    // boot | just-in-time
    String loadPolicy = "boot"

    Closure patchSignAction

    Closure bootClassPath

    Closure newApkManifestFile

    Closure workDir

    /**
     * 过滤不需要生成patch的类
     */
    Closure classPatchFilter

}
