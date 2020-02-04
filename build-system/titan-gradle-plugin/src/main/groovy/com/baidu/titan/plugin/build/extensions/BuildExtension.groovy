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

package com.baidu.titan.plugin.build.extensions;

/**
 * BuildExtension
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/5
 */

public class BuildExtension {

    boolean enable = true;

    String controlVersion;

    Closure commitId;

    Closure apkId;

    Closure enableForVariant;

    Closure classInstrumentFilter;

    Closure methodInstrumentFilter;

    Closure bootClassPath

    Closure manifestFile

    Closure maindexList

    /** 是否过滤meizu push sdk，不对其进行插桩 */
    boolean filterMeizuPush = true

}
