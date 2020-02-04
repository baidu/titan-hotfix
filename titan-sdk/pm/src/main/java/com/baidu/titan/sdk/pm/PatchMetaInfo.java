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

import android.util.Log;

import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.internal.util.Closes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Patch包元信息实体类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/27
 */

public class PatchMetaInfo {

    private static final String KEY_TARGET_ID = TitanConstant.PatchInfoConstant.KEY_TARGET_ID;

    private static final String KEY_PATCH_STATUS = TitanConstant.PatchInfoConstant.KEY_PATCH_STATUS;

    private static final String KEY_VERSION_INFO = TitanConstant.PatchInfoConstant.KEY_VERSION_INFO;

    private static final String KEY_LOAD_POLICY = TitanConstant.PatchInfoConstant.KEY_LOAD_POLICY;

    public String targetId;

    public int status;

    public int loadPolicy = TitanConstant.PATCH_LOAD_POLICY_BOOT;

//    public int patchVersion;

    public VersionInfo versionInfo;

    /**
     * 版本信息
     */
    public static class VersionInfo {

        static final String KEY_PATCH_VERSIONCODE = TitanConstant.PatchInfoConstant.KEY_PATCH_VERSIONCODE;

        static final String KEY_PATCH_VERSIONNAME = TitanConstant.PatchInfoConstant.KEY_PATCH_VERSIONNAME;

        static final String KEY_HOST_VERSIONNAME = TitanConstant.PatchInfoConstant.KEY_HOST_VERSIONNAME;

        static final String KEY_HOST_VERSIONCODE = TitanConstant.PatchInfoConstant.KEY_HOST_VERSIONCODE;


        public String hostVersionName;
        public int hostVersionCode;
        public String patchVersionName;
        public int patchVersionCode;

        public String toJsonString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_PATCH_VERSIONCODE, this.patchVersionCode);
                jsonObject.put(KEY_PATCH_VERSIONNAME, this.patchVersionName);
                jsonObject.put(KEY_HOST_VERSIONNAME, this.hostVersionName);
                jsonObject.put(KEY_HOST_VERSIONCODE, this.hostVersionCode);
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }

        /**
         * 转换为json对象
         *
         * @return json对象
         */
        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_PATCH_VERSIONCODE, this.patchVersionCode);
                jsonObject.put(KEY_PATCH_VERSIONNAME, this.patchVersionName);
                jsonObject.put(KEY_HOST_VERSIONNAME, this.hostVersionName);
                jsonObject.put(KEY_HOST_VERSIONCODE, this.hostVersionCode);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

    }


    public static PatchMetaInfo createFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            PatchMetaInfo patchMetaInfo = new PatchMetaInfo();
            patchMetaInfo.targetId = jsonObject.getString(KEY_TARGET_ID);
            patchMetaInfo.status = jsonObject.getInt(KEY_PATCH_STATUS);
            patchMetaInfo.loadPolicy = jsonObject.optInt(KEY_LOAD_POLICY, TitanConstant.PATCH_LOAD_POLICY_BOOT);

            JSONObject versionInfoJson = jsonObject.getJSONObject(KEY_VERSION_INFO);

            VersionInfo versionInfo = new VersionInfo();
            versionInfo.hostVersionName = versionInfoJson.optString(VersionInfo.KEY_HOST_VERSIONNAME);
            versionInfo.hostVersionCode = versionInfoJson.getInt(VersionInfo.KEY_HOST_VERSIONCODE);
            versionInfo.patchVersionName = versionInfoJson.optString(VersionInfo.KEY_PATCH_VERSIONNAME);
            versionInfo.patchVersionCode = versionInfoJson.getInt(VersionInfo.KEY_PATCH_VERSIONCODE);

            patchMetaInfo.versionInfo = versionInfo;
            return patchMetaInfo;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }


    public static PatchMetaInfo createFromPatch(File patchFile) {
        BufferedReader reader = null;
        ZipFile patchZip = null;
        try {
            patchZip = new ZipFile(patchFile);
            ZipEntry zipEntry = new ZipEntry(TitanConstant.PATCH_INFO_PATH);
            reader = new BufferedReader(new InputStreamReader(patchZip.getInputStream(zipEntry)));
            CharArrayWriter caw = new CharArrayWriter();
            int len;
            char[] buffer = new char[8 * 1024];
            while ((len = reader.read(buffer)) > 0) {
                caw.write(buffer, 0, len);
            }
            String content = caw.toString();
//            if (TitanConfig.DEBUG) {
//                Log.i("PatchMetaInfo", "[getPachMetaInfo] content = " + content + " curId = " + mCurId);
//            }
            PatchMetaInfo pmi = PatchMetaInfo.createFromJson(content);
            return pmi;
        } catch (Exception e) {
            if (TitanConfig.DEBUG) {
                Log.e("PatchMetaInfo", "[getPachMetaInfo] ex", e);
            }
        } finally {
            Closes.closeQuiet(reader);
            Closes.closeQuiet(patchZip);
        }
        return null;
    }


    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TARGET_ID, this.targetId);
            jsonObject.put(KEY_PATCH_STATUS, this.status);
            jsonObject.put(KEY_LOAD_POLICY, this.loadPolicy);
            if (this.versionInfo != null) {
                jsonObject.put(KEY_VERSION_INFO, this.versionInfo.toJsonString());
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 转换为json对象
     *
     * @return json对象
     */
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TARGET_ID, this.targetId);
            jsonObject.put(KEY_PATCH_STATUS, this.status);
            jsonObject.put(KEY_LOAD_POLICY, this.loadPolicy);
            if (this.versionInfo != null) {
                jsonObject.put(KEY_VERSION_INFO, this.versionInfo.toJson());
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
