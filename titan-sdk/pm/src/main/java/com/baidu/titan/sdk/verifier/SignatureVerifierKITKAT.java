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

package com.baidu.titan.sdk.verifier;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.internal.util.Closes;
import com.baidu.titan.sdk.internal.util.EncodeUtils;
import com.baidu.titan.sdk.pm.PatchInstallInfo;
import com.baidu.titan.sdk.pm.PatchManager;
import com.baidu.titan.sdk.pm.PatchVerifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 对KITKAT以下版本加载时进行签名验证
 *
 * @author shanghuibo
 */
public class SignatureVerifierKITKAT extends SignatureVerifier {
    /** debug tag */
    private static final String TAG = "SigVerifier";
    /** patch 文件*/
    private File mPatchFile;
    /** context */
    private Context mContext;
    /** patch安装信息 */
    private PatchInstallInfo mInstallInfo;


    public SignatureVerifierKITKAT(Context context, PatchInstallInfo installInfo) {
        super(context, installInfo.getPatchFile());
        mContext = context;
        mPatchFile = installInfo.getPatchFile();
        mInstallInfo = installInfo;
    }

    @Override
    public int verifySignature() {
        // 复用父类逻辑，校验patch包
        long startTime = System.currentTimeMillis();
        int verifyResult = super.verifySignature();
        long verifySignatureTime = System.currentTimeMillis();
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verify signature cost " + (verifySignatureTime - startTime) + " ms");
        }
        if (verifyResult != PatchVerifier.VERIFY_OK) {
            return verifyResult;
        }

        // 检查是否将dex解压到了patch目录中，如果没有解压，说明只有一个dex，只需要校验patch包
        List<File> dexList = mInstallInfo.getOrderedDexList();
        long getOrderedDexListTime = System.currentTimeMillis();
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "getOrderedDexList cost " + (getOrderedDexListTime - verifySignatureTime) + " ms");
        }
        if (!dexList.isEmpty()) {
            // 校验解压的dex是否被篡改
            verifyResult = verifyExtractedDex(dexList);
            long verifyExtractedDexTime = System.currentTimeMillis();
            if (TitanConfig.DEBUG) {
                Log.d(TAG, "verifyExtractedDex cost " + (verifyExtractedDexTime - getOrderedDexListTime) + " ms");
                Log.d(TAG, "verifyExtractedDex verify result = " + verifyResult);
            }
            if (verifyResult != PatchVerifier.VERIFY_OK) {
                return verifyResult;
            }
        }

        long beforeVerifyOptDex = System.currentTimeMillis();
        // 校验opt dex是否被篡改
        verifyResult = verifyOptDex();
        long afterVerifyOptDex = System.currentTimeMillis();
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verifyOptDex cost " + (afterVerifyOptDex - beforeVerifyOptDex) + " ms");
            Log.d(TAG, "verifyOptDex verify result = " + verifyResult);
        }
        return verifyResult;
    }

    /**
     * 校验opt 目录文件是否被篡改
     *
     * @return 是否校验成功
     */
    private int verifyOptDex() {
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verifyOptDex start");
        }
        File dexOptDir = mInstallInfo.getDexOptDir();

        HashMap<String, String> digestMap = mInstallInfo.readOptDigests();

        File[] optFiles = dexOptDir.listFiles();
        for (int i = 0; i < optFiles.length; i++) {
            File file = optFiles[i];
            if (file.isDirectory()) {
                continue;
            }
            String sha256 = EncodeUtils.bytesToHex(EncodeUtils.sha256(file));
            String recordSha256 = digestMap.get(file.getName());
            if (TitanConfig.DEBUG) {
                Log.d("Titan", "verifyOptDex verify " + file.getName() + " " + sha256 + " " + recordSha256);
            }
            if (!TextUtils.equals(sha256, recordSha256)) {
                return PatchManager.INSTALL_STATE_VERIFY_ERROR_OPT_DEX;
            }
        }


        return PatchVerifier.VERIFY_OK;
    }


    /**
     * 校验从patch文件中解压出来的dex文件，每个dex文件会被转存到单独的jar包中,需要从jar包中读出dex文件与patch中的文件进行对比
     *
     * @param dexList dex jar文件列表
     * @return 是否校验成功
     */
    private int verifyExtractedDex(List<File> dexList) {
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verifyExtractedDex start");
        }
        ZipFile patchZip = null;
        try {
            patchZip = new ZipFile(mPatchFile);
            for (File dexJar : dexList) {

                ZipFile dexJarFile = new ZipFile(dexJar);
                InputStream dexIs = null;
                InputStream dexPatchIs = null;
                try {
                    String dexName = dexJar.getName().replace(".jar", ".dex");
                    ZipEntry dexEntry = dexJarFile.getEntry(dexName);
                    ZipEntry dexPatchEntry = patchZip.getEntry(dexName);
                    if (dexEntry != null && dexPatchEntry != null) {
                        if (TitanConfig.DEBUG) {
                            Log.d(TAG, "verifyExtractedDex verify " + dexName);
                        }
                        dexIs = dexJarFile.getInputStream(dexEntry);
                        byte[] extractedDigest = EncodeUtils.sha256(dexIs);
                        dexPatchIs = patchZip.getInputStream(dexPatchEntry);
                        byte[] patchDexDigest = EncodeUtils.sha256(dexPatchIs);
                        if (!Arrays.equals(extractedDigest, patchDexDigest)) {
                            return PatchManager.INSTALL_STATE_VERIFY_ERROR_EXTRACT_DEX;
                        }
                    } else {
                        if (TitanConfig.DEBUG) {
                            Log.d(TAG, "zip entry " + dexName + " not exist");
                        }
                        return PatchManager.INSTALL_STATE_VERIFY_ERROR_EXTRACT_DEX;
                    }
                } finally {
                    Closes.closeQuiet(dexJarFile);
                    Closes.closeQuiet(dexIs);
                    Closes.closeQuiet(dexPatchIs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return PatchManager.INSTALL_STATE_VERIFY_ERROR_EXTRACT_DEX;
        } finally {
            Closes.closeQuiet(patchZip);
        }


        return PatchVerifier.VERIFY_OK;
    }
}
