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

package com.baidu.titan.core;

/**
 * Titan中使用的常量
 *
 * @author zhangdi07
 * @since 2017/9/14
 */
public class Constant {

    public static final String EXTRA_KEY_DEX_ID = "dex-id";

    public static final String SUFFIX_GENESIS_TYPE = "$genesis";

    public static final int INIT_CONTEXT_FLAG_INTERCEPTED = 1 << 0;

    public static final int INIT_CONTEXT_FLAG_BUDDY = 1 << 1;

    public static final int INTERCEPT_RESULT_FLAG_INTERCEPTED = 1 << 0;

}
