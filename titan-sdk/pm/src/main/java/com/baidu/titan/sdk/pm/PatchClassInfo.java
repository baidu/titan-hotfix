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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * patch中修复的类信息
 *
 * @author shanghuibo
 * @since 2020/02/07
 */
public class PatchClassInfo {
    /** 懒加载的类的typeDesc*/
    public HashSet<String> lazyClassNames;
    /** 立即加载的类的类名*/
    public HashSet<String> instantClassNames;

    /**
     * 从json数据中读取数据并创建PatchClassInfo实例
     *
     * @param json 包含类信息的json数据
     * @return PatchClassInfo实例
     */
    public static PatchClassInfo createFromJson(String json) {
        try {
            PatchClassInfo patchClassInfo = new PatchClassInfo();

            JSONObject jsonObject = new JSONObject(json);
            JSONArray lazyArray = jsonObject.getJSONArray(TitanConstant.KEY_LAZY_INIT_CLASS);
            JSONArray instantArray = jsonObject.getJSONArray(TitanConstant.KEY_INSTANT_INIT_CLASS);
            patchClassInfo.lazyClassNames = new HashSet<>();
            patchClassInfo.instantClassNames = new HashSet<>();

            for (int i = 0; i < lazyArray.length(); i++) {
                patchClassInfo.lazyClassNames.add(lazyArray.getString(i));
            }

            for (int i = 0; i < instantArray.length(); i++) {
                patchClassInfo.instantClassNames.add(instantArray.getString(i));
            }
            return patchClassInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从patch文件中读取数据并创建PatchClassInfo实例
     *
     * @param patchFile patch文件
     * @return PatchClassInfo实例
     */
    public static PatchClassInfo createFromPatch(File patchFile) {
        BufferedReader reader = null;
        ZipFile patchZip = null;
        try {
            patchZip = new ZipFile(patchFile);
            ZipEntry zipEntry = new ZipEntry(TitanConstant.PATCH_CLASS_INFO_PATH);
            reader = new BufferedReader(new InputStreamReader(patchZip.getInputStream(zipEntry)));
            CharArrayWriter caw = new CharArrayWriter();
            int len;
            char[] buffer = new char[8 * 1024];
            while ((len = reader.read(buffer)) > 0) {
                caw.write(buffer, 0, len);
            }
            String content = caw.toString();
            PatchClassInfo pci = PatchClassInfo.createFromJson(content);
            return pci;
        } catch (Exception e) {
            if (TitanConfig.DEBUG) {
                Log.e("PatchClassInfo", "[getPachClassInfo] ex", e);
            }
        } finally {
            Closes.closeQuiet(reader);
            Closes.closeQuiet(patchZip);
        }
        return null;
    }
}
