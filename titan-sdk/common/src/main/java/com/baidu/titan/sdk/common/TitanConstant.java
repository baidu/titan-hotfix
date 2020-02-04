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

package com.baidu.titan.sdk.common;

/**
 * 常量类
 * @author zhangdi07@baidu.com
 * @since 2017/4/5
 */
public class TitanConstant {

    public static final int PATCH_STATUS_ENABLE = 1;

    public static final int PATCH_STATUS_DISABLE = 0;

    public static final String PATCH_INFO_PATH = "assets/patchinfo";

    public static final String APKID_PATH = "assets/titan/apkid";

    public static final String APKID_ASSETS_PATH = "titan/apkid";

    public static final String BUILDINFO_PATH = "assets/titan/buildinfo";

    public static final String VERIFY_CONFIG_ASSETS_PATH = "titan/verify-config";

    /**
     * 定义在这里是为了plugin与runtime能够共用这些常量
     */
    public static class PatchInfoConstant {

        public static final String KEY_TARGET_ID = "targetId";

        public static final String KEY_PATCH_STATUS = "status";

        public static final String KEY_VERSION_INFO = "versionInfo";

        public static final String KEY_LOAD_POLICY = "loadPolicy";

        public static final String KEY_PATCH_VERSIONCODE = "patchVersionCode";

        public static final String KEY_PATCH_VERSIONNAME = "patchVersionName";

        public static final String KEY_HOST_VERSIONNAME = "hostVersionName";

        public static final String KEY_HOST_VERSIONCODE = "hostVersionCode";

    }

    /** 冷启动生效  */
    public static final int PATCH_LOAD_POLICY_BOOT = 0;
    /** 立即生效 */
    public static final int PATCH_LOAD_POLICY_JUST_IN_TIME = 1;

}
