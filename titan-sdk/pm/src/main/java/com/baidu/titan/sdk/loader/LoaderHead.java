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

package com.baidu.titan.sdk.loader;

import org.json.JSONObject;

/**
 * 保存当前已安装Patch相关信息
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/27
 */

public class LoaderHead {

    public String patchHash;

    public String targetId;

    private static final String KEY_PATCH_HASH = "patchHash";

    private static final String KEY_TARGET_ID = "targetId";

    public static LoaderHead createFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            LoaderHead lh = new LoaderHead();
            lh.patchHash = jsonObject.getString(KEY_PATCH_HASH);
            lh.targetId = jsonObject.getString(KEY_TARGET_ID);
            return lh;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public String toJsonString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_PATCH_HASH, this.patchHash);
            jsonObject.put(KEY_TARGET_ID, this.targetId);
            return jsonObject.toString();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
