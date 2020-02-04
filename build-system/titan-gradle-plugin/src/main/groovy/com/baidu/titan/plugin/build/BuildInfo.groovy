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

package com.baidu.titan.plugin.build;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 * BuildInfo实体类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/5
 */

public class BuildInfo {
    /** commit Id */
    public String commitId;
    /** dex列表 */
    public List<DexInfo> dexs = new ArrayList<>();
    /** proguard mapping hash */
    public String mappingSha256
    /** proguard enable? */
    public boolean proguardEnable

    private static final String KEY_COMMITID = "commitId"
    private static final String KEY_DEXS_NAME = "dexs";
    private static final String KEY_NAME = "name"
    private static final String KEY_SHA256 = "sha256"
    private static final String KEY_PROGUARD = "proguard"
    private static final String KEY_ENABLE = "enable"
    private static final String KEY_MAPPING_NAME = "mapping"


    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_COMMITID, this.commitId);
        JSONArray dexArray = new JSONArray();
        for (DexInfo dexInfo: dexs) {
            JSONObject dexInfoJson = new JSONObject();
            dexInfoJson.put(KEY_NAME, dexInfo.name);
            dexInfoJson.put(KEY_SHA256, dexInfo.sha256);
            dexArray.put(dexInfoJson);
        }
        jsonObject.put(KEY_DEXS_NAME, dexArray);

        JSONObject proguardJson = new JSONObject()

        proguardJson.put(KEY_ENABLE, proguardEnable)

        JSONObject mappingJson = new JSONObject();
        mappingJson.put(KEY_SHA256, mappingSha256)
        proguardJson.put(KEY_MAPPING_NAME, mappingJson)

        jsonObject.put(KEY_PROGUARD, proguardJson)
        return jsonObject.toString()
    }


    public static BuildInfo createFromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        BuildInfo buildInfo = new BuildInfo();
        buildInfo.commitId = jsonObject.getString(KEY_COMMITID)
        JSONArray dexsJson = jsonObject.getJSONArray(KEY_DEXS_NAME);
        int dexCount = dexsJson.size()
        for (int i = 0; i < dexCount; i++) {
            DexInfo dexInfo = new DexInfo();
            JSONObject dexJson = dexsJson.getJSONObject(i)
            dexInfo.name = dexJson.getString(KEY_NAME)
            dexInfo.sha256 = dexJson.getString(KEY_SHA256)
            buildInfo.dexs.add(dexInfo)
        }
        JSONObject proguardJson = jsonObject.get(KEY_PROGUARD)
        buildInfo.proguardEnable = proguardJson.get(KEY_ENABLE)
        JSONObject mappingJson = proguardJson.get(KEY_MAPPING_NAME)
        buildInfo.mappingSha256 = mappingJson.optString(KEY_SHA256)

        return buildInfo
    }

    @Override
    public String toString() {
        return "BuildInfo{" +
                "commitId='" + commitId + '\'' +
                ", dexs=" + dexs +
                ", mappingSha256='" + mappingSha256 + '\'' +
                '}';
    }


    public static class DexInfo {
        public String name;
        public String sha256;

        @Override
        public String toString() {
            return "DexInfo{" +
                    "name='" + name + '\'' +
                    ", sha256='" + sha256 + '\'' +
                    '}';
        }
    }




}
