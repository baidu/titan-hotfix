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

package com.baidu.titan.sdk.initer;

import android.app.Application;
import android.content.Context;

import com.baidu.titan.sdk.runtime.annotation.DisableIntercept;

/**
 * 负责初始化相关逻辑
 *
 * @author zhangdi07
 * @since 2017/4/26
 */
@DisableIntercept
public class TitanIniter {

    private static Context sContext;

    public static void init(Context c) {
        if (c instanceof Application) {
            sContext = c;
        } else {
            sContext = c.getApplicationContext();
        }
        if (sContext == null) {
            throw new IllegalStateException("context.getApplicationContext == null");
        }
    }

    public static Context getAppContext() {
        if (sContext == null) {
            throw new IllegalStateException("must call TitanIniter.init");
        }
        return sContext;
    }

}
