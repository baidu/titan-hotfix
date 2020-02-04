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

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.loader.DelegateClassLoader;
import com.baidu.titan.sdk.loader.LoaderHead;
import com.baidu.titan.sdk.loader.LoaderManager;
import com.baidu.titan.sdk.internal.util.Closes;
import com.baidu.titan.sdk.internal.util.EncodeUtils;
import com.baidu.titan.sdk.internal.util.Files;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexClassLoader;

/**
 * Patch管理服务，运行在沙箱进程
 *
 * @author zhangdi07
 * @author shanghuibo
 * @since 2017/4/28
 */

public class PatchManagerService {

    private static final boolean DEBUG = TitanConfig.DEBUG;

    private static final boolean USE_VERIFY = true;

    private static final String TAG = DEBUG ? "PatchManagerService" : PatchManagerService.class.getSimpleName();

    private static final String PATCH_INSTALL_TMP_PREFIX = "tmp_patch_install_";

    private Context mContext;

    private static PatchManagerService sInstance;

    /**
     * 获取单例
     * @return
     */
    public static PatchManagerService getInstance() {
        synchronized (PatchManagerService.class) {
            if (sInstance == null) {
                sInstance = new PatchManagerService(TitanIniter.getAppContext());
            }
            return sInstance;
        }
    }

    private PatchManagerService(Context c) {
        this.mContext = c.getApplicationContext();
    }

    /**
     * 同步安装Patch
     * @param uri
     * @param installExtra
     * @param resultExtra
     * @return
     */
    public int installSyncLocked(Uri uri, Bundle installExtra, Bundle resultExtra) {
        String scheme = uri.getScheme();
        InputStream in = null;
        try {
            if ("file".equals(scheme)) {
                String path = uri.getPath();
                if (path.startsWith("/android_asset/")) {
                    String assetName = path.substring("/android_asset/".length());
                    in = mContext.getAssets().open(assetName);
                } else {
                    in = new FileInputStream(path);
                }

            } else {
                throw new IllegalArgumentException("unkown uri");
            }
            return installSyncLocked(in, installExtra, resultExtra);
        } catch (IOException e) {
            Log.d(TAG, "[install] ERROR", e);
        } finally {
            Closes.closeQuiet(in);

            // 清理无用Patch
            doCleanPatchsLocked();
        }
        return PatchManager.INSTALL_STATE_ERROR_IO;
    }

    /**
     * installPatch in current thread
     *
     * @param in
     * @param installExtra
     * @return
     * @throws IOException
     */
    private int installSyncLocked(InputStream in, Bundle installExtra, Bundle resultExtra) throws IOException {
        File tmpPatch = null;
        JSONObject logJson = new JSONObject();
        try {

            tmpPatch = prepareInstallTempPatch(in, logJson);

            if (tmpPatch == null || !tmpPatch.exists()) {
                return PatchManager.INSTALL_STATE_ERROR_IO;
            }

            String curApkId = LoaderManager.getInstance().getCurrentApkId();

            if (TextUtils.isEmpty(curApkId)) {
                return PatchManager.INSTALL_STATE_ERROR_APKID_FETCH;
            }

            if (USE_VERIFY) {
                PatchVerifier verifier = new PatchVerifier(mContext, tmpPatch, curApkId, logJson);
                int verifyResult = verifier.verify();

                if (TitanConfig.DEBUG) {
                    Log.i(TAG, "[install] verify res = " + verifyResult);
                }

                if (verifyResult != PatchVerifier.VERIFY_OK) {
                    return verifyResult;
                }
            }

            PatchMetaInfo pmi = PatchMetaInfo.createFromPatch(tmpPatch);

            if (pmi == null) {
                return PatchManager.INSTALL_STATE_ERROR_IO;
            }

            // 降级检测
            File headFile = TitanPaths.getHeadFile();
            if (headFile.exists()) {
                String headContent = Files.getFileStringContent(headFile);
                if (DEBUG) {
                    Log.d(TAG, "[load] headContent = " + headContent);
                }
                LoaderHead lh = LoaderHead.createFromJson(headContent);
                if (lh != null) {
                    // 如果当前HEAD指针指向的Patch是之前版本的，也就是说targetId != curId
                    // 则忽略patchVersion检查
                    if (TextUtils.equals(curApkId, lh.targetId)) {
                        File patchDir = TitanPaths.getPatchDir(lh.patchHash);
                        PatchInstallInfo installInfo = new PatchInstallInfo(patchDir);
                        PatchMetaInfo installedPmi = PatchMetaInfo.createFromPatch(installInfo.getPatchFile());
                        if (installedPmi != null) {
                            int currentPatchVersion = installedPmi.versionInfo.patchVersionCode;
                            if (pmi.versionInfo.patchVersionCode <= currentPatchVersion) {
                                if (logJson != null) {
                                    try {
                                        logJson.put("curPatchInfo", installedPmi.toJsonString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return PatchManager.INSTALL_STATE_PATCH_VERSION_DOWNGRADE;
                            }
                        }
                    }
                    // else {
                        // do nothing，执行安装完成后会调用 doCleanPatchsLocked(),清空当前Patch包
                    // }
                }
            }

            if (pmi.status == TitanConstant.PATCH_STATUS_DISABLE) {
                // TODO 这里会有一个bad case:
                // 如果该patch是一个禁用的patch包，则允许patchVersion降级的可能
                TitanPaths.getHeadFile().delete();
                return PatchManager.INSTALL_STATE_SUCCESS;
            }

            String patchDirName = EncodeUtils.bytesToHex(EncodeUtils.sha256(tmpPatch));
            PatchInstallInfo installInfo = createPatchInstallInfo(patchDirName);

            if (installInfo.finished()) {
                if (TitanConfig.DEBUG) {
                    Log.i(TAG, "[install] finished already");
                }
                return PatchManager.INSTALL_STATE_ALREADY_INSTALLED;
            }

            if (installInfo.exist()) {
                installInfo.cleanIfNeed();
            }
            installInfo.prepare();

            File patchFile = installInfo.getPatchFile();
            if (!tmpPatch.renameTo(patchFile)) {
                return PatchManager.INSTALL_STATE_ERROR_IO;
            }

            if (!extraceDexsIfNeeded(installInfo, patchFile)) {
                return PatchManager.INSTALL_STATE_ERROR_EXTRACT_DEX;
            }

            if (!dexOpt(installInfo, logJson)) {
                return PatchManager.INSTALL_STATE_PATCH_ERROR_DEXOPT;
            }

            installInfo.getStatusFile().createNewFile();

            LoaderHead lh = new LoaderHead();
            lh.targetId = curApkId;
            lh.patchHash = patchDirName;

            // TODO 原子性更新？
            FileWriter fw = new FileWriter(TitanPaths.getHeadFile());
            fw.write(lh.toJsonString());
            fw.close();
        } finally {
            if (resultExtra != null) {
                resultExtra.putString(PatchManager.INSTALL_RESULT_EXTRA_KEY, logJson.toString());
            }
            if (tmpPatch != null) {
                tmpPatch.delete();
            }
        }

        return PatchManager.INSTALL_STATE_SUCCESS;
    }

    /**
     * 如果满足条件，则将dex提取出来INSTALL_STATE_PATCH_VERSION_DOWNGRADE
     *
     * @param installInfo 安装信息
     * @param patchFile patch文件
     * @return 操作是否成功
     */
    private boolean extraceDexsIfNeeded(PatchInstallInfo installInfo, File patchFile) {

        boolean result = true;
        ZipFile patchZip = null;
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                patchZip = new ZipFile(patchFile);
                int dexCount = getDexCount(patchZip);
                if (dexCount > 1) {
                    result = extractDexs(patchZip, installInfo.getPatchDir());
                    if (!result) {
                        return false;
                    }
                }
                result = installInfo.saveDexCount(dexCount);
            }
            return result;
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closes.closeQuiet(patchZip);
        }
        return false;
    }

    /**
     * 准备Patch安装临时文件
     * @param in
     * @return
     */
    private File prepareInstallTempPatch(InputStream in, JSONObject logJson) {
        File tmpBaseDir = TitanPaths.getTempBaseDir();
        tmpBaseDir.mkdirs();
        File tmpPatch = null;

        FileOutputStream tmpOut = null;
        byte[] buf = new byte[16 * 1024];
        int len;
        try {
            tmpPatch = File.createTempFile(PATCH_INSTALL_TMP_PREFIX, null, tmpBaseDir);
            tmpOut = new FileOutputStream(tmpPatch);
            while ((len = in.read(buf)) > 0) {
                tmpOut.write(buf, 0, len);
            }
            return tmpPatch;
        } catch (Exception e) {
            if (logJson != null) {
                try {
                    logJson.put("prepare_patch_ex", Log.getStackTraceString(e));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            Closes.closeQuiet(tmpOut);
        }
        if (tmpPatch != null) {
            tmpPatch.delete();
        }

        return null;
    }


    /**
     * do dex opt
     * @param installInfo
     * @return
     */
    private boolean dexOpt(PatchInstallInfo installInfo, JSONObject logJson) {
        File dexOptDir = installInfo.getDexOptDir();
        dexOptDir.mkdirs();
        try {
            DexClassLoader dexClassLoader = new DelegateClassLoader(
                    installInfo.getDexPath(),
                    dexOptDir.getAbsolutePath(),
                    null,
                    Object.class.getClassLoader(),
                    installInfo.getClass().getClassLoader());
            // 只在kitkat及以下版本校验opt 文件
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                installInfo.saveOptFileDigests(dexOptDir);
            }
            return true;
        } catch (Throwable t) {
            if (logJson != null) {
                try {
                    logJson.put("dexopt_ex", Log.getStackTraceString(t));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * extract dexs
     * @param patchZip
     * @param patchDir
     * @return
     */
    private boolean extractDexs(ZipFile patchZip, File patchDir) {
        try {
            ZipEntry mainDex = patchZip.getEntry("classes.dex");
            if (mainDex != null) {
                extractDex(patchZip, mainDex, patchDir);
            }
            int idx = 2;
            while (true) {
                ZipEntry dex = patchZip.getEntry("classes" + idx + ".dex");
                if (dex == null) {
                    break;
                }
                extractDex(patchZip, dex, patchDir);
                idx++;
            }
            return true;
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    /**
     * extract single dex
     *
     * @param patchZip
     * @param entry
     * @param patchDir
     * @throws IOException
     */
    private void extractDex(ZipFile patchZip, ZipEntry entry, File patchDir) throws IOException {
        InputStream zis = null;
        ZipOutputStream zos = null;
        File out = new File(patchDir, entry.getName().replace("dex", "jar"));
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
            zis = patchZip.getInputStream(entry);
            ZipEntry classesDex = new ZipEntry("classes.dex");
            // keep zip entry time since it is the criteria used by Dalvik
            classesDex.setTime(entry.getTime());
            zos.putNextEntry(classesDex);

            byte[] buf = new byte[16 * 1024];
            int len;
            while ((len = zis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }


    public PatchInstallInfo createPatchInstallInfo(String hash) {
        File patchDir = new File(TitanPaths.getPatchsDir(), hash);
        return new PatchInstallInfo(patchDir);
    }

    /**
     * clean unused patches
     */
    public void doCleanPatchsLocked() {

        File headFile = TitanPaths.getHeadFile();
        String currentVaildPatchHash = null;
        if (headFile.exists()) {
            String headContent = Files.getFileStringContent(headFile);
            if (!TextUtils.isEmpty(headContent)) {
                LoaderHead lh = LoaderHead.createFromJson(headContent);
                if (DEBUG) {
                    Log.d(TAG, "[clean patches] headContent = " + lh);
                }
                if (lh != null && !TextUtils.isEmpty(lh.targetId) && !TextUtils.isEmpty(lh.patchHash)) {
                    String curApkId = LoaderManager.getInstance().getCurrentApkId();
                    if (TextUtils.equals(curApkId, lh.targetId)) {
                        currentVaildPatchHash = lh.patchHash;
                    }
                }
            }

            // 如果currentVaildPatchHash == null,则说明当前head所指向的Patch文件是无效的，需要删除
            if (TextUtils.isEmpty(currentVaildPatchHash)) {
                headFile.delete();
            }
        }

        File patchBaseDir = TitanPaths.getPatchsDir();

        boolean needCleanLater = false;

        File[] patchDirs = patchBaseDir.listFiles();
        if (patchDirs != null) {
            for (File patchDir : patchDirs) {
                PatchInstallInfo patchInfo = new PatchInstallInfo(patchDir);
                if (!TextUtils.isEmpty(currentVaildPatchHash)) {
                    if (patchInfo.getId().equals(currentVaildPatchHash)) {
                        // 忽略当前正在安装的patch
                        continue;
                    }
                }

                boolean acquire = patchInfo.writeLock();
                if (acquire) {
                    patchInfo.cleanIfNeed();
                    patchInfo.releaseWriteLock();
                } else {
                    needCleanLater = true;
                }
            }
        }

        File pendingCleanFile = PatchManager.getPendingCleanFile();
        if (needCleanLater) {
            try {
                pendingCleanFile.createNewFile();
            } catch (IOException e) {
                // igonre
            }
        } else {
            pendingCleanFile.delete();
        }
    }

    /**
     * 获取patch包中的dex个数
     *
     * @param patchFile patch包
     * @return patch包中的Dex个数
     */
    private int getDexCount(ZipFile patchFile) {
        int count = 0;
        String dexName = "classes.dex";
        ZipEntry entry = patchFile.getEntry(dexName);
        if (entry != null) {
            count = 1;
        } else {
            return count;
        }

        int idx = 2;
        while (true) {
            dexName = "classes" + idx + ".dex";
            if (patchFile.getEntry(dexName) == null) {
                break;
            }
            count++;
            idx++;
        }
        return count;
    }

}
