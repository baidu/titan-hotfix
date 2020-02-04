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

package com.baidu.titan.sdk.stat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于统计patch加载耗时
 *
 * @author shanghuibo
 * @since 2019/07/11
 */
public class LoaderTimeStat {

    /** titan中的分阶段时长数据key*/
    private static final String TITAN_READ_HEAD_CONTENT = "titanReadContent";
    private static final String TITAN_CREATE_LAODER_HEAD = "titanCreateLoaderHead";
    private static final String TITAN_GET_APK_ID = "titanGetApkId";
    private static final String TITAN_GET_PATCH_DIR = "titanGetPatchDir";
    private static final String TITAN_LOCK = "titanLock";
    private static final String TITAN_GET_DEX_PATH = "titanGetDexPath";
    private static final String TITAN_NEW_CLASSLOADER = "titanNewClassLoader";
    private static final String TITAN_WAIT_VERIFY = "titanWaitVerify";
    private static final String TITAN_LOAD_LOADER = "titanLoadLoader";
    private static final String TITAN_NEW_LOADER = "titanNewLoader";
    private static final String TITAN_APPLY = "titanApply";
    private static final String TITAN_VERIFY_SIGNATURE = "titanVerifySignature";

    /** 单例*/
    private static LoaderTimeStat sInstance;

    /** titan 读取 head content时间*/
    public long readHeadContent;
    /** titan 创建LoaderHead对象时间*/
    public long createLoaderHead;
    /** titan 获取apk id 时间*/
    public long getApkId;
    /** titan 获取patch dir时间*/
    public long getPatchDir;
    /** titan 加锁时间*/
    public long lock;
    /** titan 获取dex path时间*/
    public long getDexPath;
    /** titan new class loader时间*/
    public long newClassLoader;
    /** titan等待签名校验完成时间*/
    public long waitVerify;
    /** titan加载Loader类时间*/
    public long loadLoader;
    /** titan创建Loader对象时间 */
    public long newLoader;
    /** titan 应用patch时间*/
    public long apply;
    /** titan 签名校验*/
    public long verifySignature;

    /**
     * private constructor
     */
    private LoaderTimeStat() {

    }

    /**
     * 获取单例
     *
     * @return LoaderTimeStat 单例
     */
    public static synchronized LoaderTimeStat getInstance() {
        if (sInstance == null) {
            sInstance = new LoaderTimeStat();
        }
        return sInstance;
    }

    /**
     * 转换成json string
     *
     * @return json string
     */
    public String toJSONString() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("readHeadContent", readHeadContent);
            jobj.put("createLoaderHead", createLoaderHead);
            jobj.put("getApkId", getApkId);
            jobj.put("getPatchDir", getPatchDir);
            jobj.put("getDexPath", getDexPath);
            jobj.put("newClassLoader", newClassLoader);
            jobj.put("waitVerify", waitVerify);
            jobj.put("loadLoader", loadLoader);
            jobj.put("newLoader", newLoader);
            jobj.put("apply", apply);
            jobj.put("verifySignature", verifySignature);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj.toString();
    }

    /**
     * 输出为map
     *
     * @return 包含时长数据的map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(TITAN_READ_HEAD_CONTENT, String.valueOf(readHeadContent));
        map.put(TITAN_CREATE_LAODER_HEAD, String.valueOf(createLoaderHead));
        map.put(TITAN_GET_APK_ID, String.valueOf(getApkId));
        map.put(TITAN_GET_PATCH_DIR, String.valueOf(getPatchDir));
        map.put(TITAN_LOCK, String.valueOf(lock));
        map.put(TITAN_GET_DEX_PATH, String.valueOf(getDexPath));
        map.put(TITAN_NEW_CLASSLOADER, String.valueOf(newClassLoader));
        map.put(TITAN_WAIT_VERIFY, String.valueOf(waitVerify));
        map.put(TITAN_LOAD_LOADER, String.valueOf(loadLoader));
        map.put(TITAN_NEW_LOADER, String.valueOf(newLoader));
        map.put(TITAN_APPLY, String.valueOf(apply));
        map.put(TITAN_VERIFY_SIGNATURE, String.valueOf(verifySignature));

        return map;
    }
}
