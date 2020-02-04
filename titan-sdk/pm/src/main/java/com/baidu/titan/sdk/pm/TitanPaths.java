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

package com.baidu.titan.sdk.pm;

import com.baidu.titan.sdk.runtime.annotation.DisableIntercept;
import com.baidu.titan.sdk.initer.TitanIniter;

import java.io.File;

/**
 * Titan
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/29
 */
@DisableIntercept
public class TitanPaths {


    public static File getBaseDir() {
        return new File(TitanIniter.getAppContext().getApplicationInfo().dataDir, "titan");
    }

    public static File getPatchsDir() {
        return new File(getBaseDir(), "patches");
    }

    public static File getTempBaseDir() {
        return new File(getBaseDir(), "tmp");
    }

    public static File getPatchDir(String patchHash) {
        return new File(getPatchsDir(), patchHash);
    }

    public static File getHeadFile() {
        return new File(getBaseDir(), "head");
    }

    public static final String TITAN_SANDBOX_PROCESS_NAME_SUFFIX = ":titanSandbox";

}
