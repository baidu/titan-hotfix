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

package com.baidu.titan.plugin.patch.extensions;

/**
 * patch包的版本信息
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/15
 */

public class PatchVersionInfo {
    /** patch的VersionName */
    String patchVersionName;
    /** patch的VersionCode  */
    int patchVersionCode;
    /** 与patch包一起打出的禁用patch包的版本号*/
    int disablePatchVersionCode;
    /** 宿主的VersionName */
    String hostVersionName;
    /** 宿主的VersionCode*/
    int hostVersionCode;
}
