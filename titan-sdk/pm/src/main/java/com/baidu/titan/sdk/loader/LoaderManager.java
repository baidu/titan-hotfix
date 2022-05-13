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
import android.text.TextUtils;
import android.util.Log;

import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.internal.util.Closes;
import com.baidu.titan.sdk.internal.util.Files;
import com.baidu.titan.sdk.pm.PatchClassInfo;
import com.baidu.titan.sdk.pm.PatchInstallInfo;
import com.baidu.titan.sdk.pm.PatchMetaInfo;
import com.baidu.titan.sdk.pm.PatchVerifier;
import com.baidu.titan.sdk.pm.TitanPaths;
import com.baidu.titan.sdk.runtime.ClassClinitInterceptable;
import com.baidu.titan.sdk.runtime.ClassClinitInterceptorDelegate;
import com.baidu.titan.sdk.runtime.ClassClinitInterceptorStorage;
import com.baidu.titan.sdk.runtime.Interceptable;
import com.baidu.titan.sdk.runtime.InterceptableDelegate;
import com.baidu.titan.sdk.stat.LoaderTimeStat;
import com.baidu.titan.sdk.verifier.SignatureVerifier;
import com.baidu.titan.sdk.verifier.SignatureVerifierKITKAT;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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
    /** 异步加载patch，状态先返回*/
    public static final int LOAD_STATE_ERROR_ASYNC_LOAD = -7;

    private static LoaderManager sInstance;

    private final Context mContext;

    private PatchInstallInfo mPatchInstallInfo;

    private volatile Future<Integer> mLoadFuture;
    /** patch加载状态 */
    private volatile int mLoadState = LOAD_STATE_ERROR_NOPATCH;

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
        mLoadState = result;
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
        mLoadState = result;
        return result;
    }

    /**
     * 加载patch
     *
     * @return 加载错误码
     */
    private int loadInternal(final boolean loadInTime) {

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
        final PatchInstallInfo installInfo = new PatchInstallInfo(patchDir);

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

        PatchMetaInfo metaInfo = PatchMetaInfo.createFromPatch(installInfo.getPatchFile());


        if (loadInTime || metaInfo.bootLoadSyncPolicy == TitanConstant.PATCH_BOOT_LOAD_SYNC_POLICY_SYNC) {
            return loadPatch(loadInTime, installInfo);
        } else {
            PatchClassInfo patchClassInfo = PatchClassInfo
                    .createFromPatch(installInfo.getPatchFile());
            setInterceptorDelegate(patchClassInfo);
            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
            mLoadFuture = singleThreadExecutor.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    int loadState = loadPatch(false, installInfo);
                    mLoadState = loadState;
                    return loadState;
                }
            });
            singleThreadExecutor.shutdown();
            return LOAD_STATE_ERROR_ASYNC_LOAD;
        }

    }

    /**
     * 获得当前patch加载状态
     *
     * @return patch加载状态
     */
    public int getLoadState() {
        return mLoadState;
    }

    /**
     * 等待patch被加载
     */
    private void waitLoad() {
        if (mLoadFuture == null) {
            return;
        }

        synchronized (this) {
            if (mLoadFuture != null) {
                try {
                    mLoadFuture.get();
                    mLoadFuture = null;
                    return;
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    /**
     * patch加载前，给需要设置delegate的类添加delegate类
     *
     * @param patchClassInfo patch中修复的类信息
     */
    private void setInterceptorDelegate(final PatchClassInfo patchClassInfo) {
        ClassClinitInterceptable classClinitStub = new ClassClinitInterceptorDelegate() {
            @Override
            public boolean waitLoad(int hashCode, String typeDesc) {
                if (patchClassInfo.lazyClassNames.contains(typeDesc)) {
                    LoaderManager.this.waitLoad();
                }
                return false;
            }
        };
        ClassClinitInterceptorStorage.$ic = classClinitStub;

        for (final String className : patchClassInfo.instantClassNames) {
            Interceptable delegate = new InterceptableDelegate() {
                @Override
                public boolean waitLoad() {
                    LoaderManager.this.waitLoad();
                    return true;
                }
            };

            try {
                Class fixClass = Class.forName(className);
                Field icField = fixClass.getDeclaredField("$ic");
                icField.setAccessible(true);
                icField.set(null, delegate);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 进行签名校验和使用classloader加载patch
     *
     * @param loadInTime 是否是实时加载
     * @param installInfo
     * @return
     */
    private int loadPatch(boolean loadInTime, PatchInstallInfo installInfo) {
        long lastTime;
        long currentTime = System.currentTimeMillis();
        SignatureVerifier sigVerifier = getSignatureVerifier(mContext, mPatchInstallInfo);
        /** titan 签名校验*/
        int verifyResult = sigVerifier.verifySignature();

        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        LoaderTimeStat timeStat = LoaderTimeStat.getInstance();
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
