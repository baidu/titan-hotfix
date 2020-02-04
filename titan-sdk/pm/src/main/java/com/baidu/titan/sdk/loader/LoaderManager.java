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

import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.internal.util.Closes;
import com.baidu.titan.sdk.internal.util.Files;
import com.baidu.titan.sdk.pm.PatchInstallInfo;
import com.baidu.titan.sdk.pm.PatchVerifier;
import com.baidu.titan.sdk.pm.TitanPaths;
import com.baidu.titan.sdk.stat.LoaderTimeStat;
import com.baidu.titan.sdk.verifier.SignatureVerifier;
import com.baidu.titan.sdk.verifier.SignatureVerifierKITKAT;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Patch加载管理类
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2017/4/26
 */

public class LoaderManager {

    private static final boolean DEBUG = TitanConfig.DEBUG;

    private static final String TAG = "LoaderManager";

    private static final String PATCH_LOADER = "com.baidu.titan.patch.PatchLoader";

    /** 加载成功 */
    public static final int LOAD_STATE_SUCCESS = 0;
    /** Patch不存在 */
    public static final int LOAD_STATE_ERROR_NOPATCH = -1;

    public static final int LOAD_STATE_ERROR_HEAD_DAMAGE = -2;

    public static final int LOAD_STATE_ERROR_PATCH_INSTALL = -3;

    public static final int LOAD_STATE_ERROR_VERSION_DISMATCH = -4;

    public static final int LOAD_STATE_ERROR_LOAD_FAIL = -5;
    /** 签名校验失败*/
    public static final int LOAD_STATE_ERROR_SIGNATURE_VERIFY_FAIL = -6;

    private static LoaderManager sInstance;

    private final Context mContext;

    private PatchInstallInfo mPatchInstallInfo;

    private LoaderManager(Context c) {
        this.mContext = c;

    }

    public static LoaderManager getInstance() {
        synchronized (LoaderManager.class) {
            if (sInstance == null) {
                sInstance = new LoaderManager(TitanIniter.getAppContext());
            }
            return sInstance;
        }
    }


    public String getCurrentApkId(Context c) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(c.getAssets().open(TitanConstant.APKID_ASSETS_PATH)));
            return reader.readLine();
        } catch (Exception e) {
            // ignore
        } finally {
            Closes.closeQuiet(reader);
        }
        return null;
    }

    /**
     * 获取当前的apkid
     * @return
     */
    public String getCurrentApkId() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets()
                    .open(TitanConstant.APKID_ASSETS_PATH)));
            return reader.readLine();
        } catch (Exception e) {
            // ignore
        } finally {
            Closes.closeQuiet(reader);
        }
        return null;
    }


    public PatchInstallInfo getCurrentPatchInfo() {
        return mPatchInstallInfo;
    }

    /**
     * 加载patch
     *
     * @return 加载错误码
     */
    public int load() {
        int result = loadInternal(false);
        if (DEBUG) {
            Log.d(TAG, "load result = " + result);
            String stat = LoaderTimeStat.getInstance().toJSONString();
            Log.d(TAG, stat);
        }
        return result;
    }

    /**
     * 实时加载patch
     *
     * @return 加载错误码
     */
    public int loadInTime() {
        int result = loadInternal(true);
        if (DEBUG) {
            Log.d(TAG, "loadInTime result = " + result);
            String stat = LoaderTimeStat.getInstance().toJSONString();
            Log.d(TAG, stat);
        }
        return result;
    }

    /**
     * 加载patch
     *
     * @return 加载错误码
     */
    private int loadInternal(boolean loadInTime) {

        File headFile = TitanPaths.getHeadFile();
        if (!headFile.exists()) {
            if (DEBUG) {
                Log.d(TAG, "[load] head file don't exist, skip load");
            }
            return LOAD_STATE_ERROR_NOPATCH;
        }

        final LoaderTimeStat timeStat = LoaderTimeStat.getInstance();
        long currentTime = System.currentTimeMillis();
        long lastTime;
        String headContent = Files.getFileStringContent(headFile);

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.readHeadContent = currentTime - lastTime;

        if (DEBUG) {
            Log.d(TAG, "[load] headContent = " + headContent);
        }

        LoaderHead lh = LoaderHead.createFromJson(headContent);

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.createLoaderHead = currentTime - lastTime;
        if (lh == null || TextUtils.isEmpty(lh.targetId) || TextUtils.isEmpty(lh.patchHash)) {
            return LOAD_STATE_ERROR_HEAD_DAMAGE;
        }

        String curId = getCurrentApkId();

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.getApkId = currentTime - lastTime;
        if (DEBUG) {
            Log.d(TAG, "[load] curApk = " + curId);
        }

        if (!lh.targetId.equals(curId)) {
            return LOAD_STATE_ERROR_VERSION_DISMATCH;
        }

        File patchDir = TitanPaths.getPatchDir(lh.patchHash);

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.getPatchDir = currentTime - lastTime;
        PatchInstallInfo installInfo = new PatchInstallInfo(patchDir);

        if (!installInfo.exist() || !installInfo.finished()) {
            if (DEBUG) {
                Log.d(TAG, "[load] installInfo exist = " + installInfo.exist()
                        + " finished = " + installInfo.finished());
            }
            return LOAD_STATE_ERROR_PATCH_INSTALL;
        }

        if (DEBUG) {
            Log.i(TAG, "apply patch use " + installInfo.getPatchDir());
        }

        long lockBegin = System.currentTimeMillis();

        boolean lockRes = installInfo.shareLock();

        long lockEnd = System.currentTimeMillis();

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.lock = currentTime - lastTime;

        if (DEBUG) {
            Log.d(TAG, "lock res " + lockRes + " cost = " + (lockEnd - lockBegin));
        }

        mPatchInstallInfo = installInfo;



//        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
//        Future<Integer> verifyFuture = singleThreadExecutor.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() {
//                int originPriority = Process.getThreadPriority(Process.myTid());
//                try {
//                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
//                    SignatureVerifier sigVerifier = getSignatureVerifier(mContext, mPatchInstallInfo);
//
//                    long sigVerifyTime = System.currentTimeMillis();
//                    /** titan 签名校验*/
//                    int sigVerifyResult = sigVerifier.verifySignature();
//                    LoaderTimeStat.getInstance().verifySignature = System.currentTimeMillis() - sigVerifyTime;
//                    return sigVerifyResult;
//                } finally {
//                    Process.setThreadPriority(originPriority);
//                }
//            }
//        });


        SignatureVerifier sigVerifier = getSignatureVerifier(mContext, mPatchInstallInfo);
        /** titan 签名校验*/
        int verifyResult = sigVerifier.verifySignature();

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.verifySignature = System.currentTimeMillis() - lastTime;

        if (verifyResult != PatchVerifier.VERIFY_OK) {
            return LOAD_STATE_ERROR_SIGNATURE_VERIFY_FAIL;
        }


        String dexListPath = installInfo.getDexPath();

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeStat.getDexPath = currentTime - lastTime;

        try {
            DelegateClassLoader dcl = new DelegateClassLoader(dexListPath,
                    installInfo.getDexOptDir().getAbsolutePath(),
                    null,
                    Object.class.getClassLoader(),
                    LoaderManager.class.getClassLoader());
//            DexClassLoader dcl = new DexClassLoader(dexListPath, installInfo.getDexOptDir().getAbsolutePath(),
//                    null, LoaderManager.class.getClassLoader());

            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
            timeStat.newClassLoader = currentTime - lastTime;

//            int verifyResult  = verifyFuture.get();

//            singleThreadExecutor.shutdown();
//
//            if (verifyResult != PatchVerifier.VERIFY_OK) {
//                return LOAD_STATE_ERROR_SIGNATURE_VERIFY_FAIL;
//            }


            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
            timeStat.waitVerify = currentTime - lastTime;

            Class<?> loaderClass = dcl.loadClass(PATCH_LOADER);

            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
            timeStat.loadLoader = currentTime - lastTime;

            BaseLoader loader = (BaseLoader) loaderClass.newInstance();

            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
            timeStat.newLoader = currentTime - lastTime;

            if (loadInTime) {
                loader.applyInTime();
            } else {
                loader.apply();
            }

            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
            timeStat.apply = currentTime - lastTime;

            return LOAD_STATE_SUCCESS;
        } catch (Throwable e) {
            Log.e(TAG, "[load] uncatched exception", e);
            return LOAD_STATE_ERROR_LOAD_FAIL;
        }

    }

    /**
     * 根据android build version获取签名校验器
     *
     * @param context Context
     * @param installInfo patch安装信息
     * @return 签名校验器
     */
    private SignatureVerifier getSignatureVerifier(Context context, PatchInstallInfo installInfo) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return new SignatureVerifierKITKAT(context, installInfo);
        }

        return new SignatureVerifier(context, installInfo.getPatchFile());
    }
}
