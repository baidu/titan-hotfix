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
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.internal.util.EncodeUtils;
import com.baidu.titan.sdk.internal.util.Files;
import com.baidu.titan.sdk.pm.PatchManager;
import com.baidu.titan.sdk.pm.PatchVerifier;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;


/**
 * patch包签名校验
 *
 * @author shanghuibo
 */
public class SignatureVerifier {
    /** debug tag */
    private static final String TAG = "SigVerifier";
    /** context */
    private Context mContext;
    /** 签名校验策略*/
    private SignaturePolicy mSignaturePolicy = SignaturePolicy.V2_ONLY;
    /** patch文件 */
    private File mPatchFile;
    /** 预存的签名数据*/
    private final HashSet<String> mAllowedSigs = new HashSet<>();

    public SignatureVerifier(Context context, File patch) {
        mContext = context;
        mPatchFile = patch;
        initSignatures();
    }

    /**
     * 对patch进行签名校验
     *
     * @return 校验结果
     */
    public int verifySignature() {
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "mSignaturePolicy = " + mSignaturePolicy);
        }
        switch (mSignaturePolicy) {
            case V2_FIRST:
                try {
                    if (hasSignatureSchemeV2()) {
                        if (!verifySignatureSchemeV2()) {
                            return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
                        }
                    } else if (!verifySignatureSchemeV1()) {
                        return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
                    }
                } catch (IOException e) {
                    return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
                }
                break;
            case V2_ONLY:
                if (!verifySignatureSchemeV2()) {
                    return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
                }
                break;
            case V1_ONLY:
                if (!verifySignatureSchemeV1()) {
                    return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
                }
                break;
            case NO_SIGNATURE:
                break;
            default:
                return PatchManager.INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH;
        }
        return PatchVerifier.VERIFY_OK;
    }

    /**
     * 初始化签名信息、签名策略
     */
    private void initSignatures() {
        String sigContent = Files.getAssetFileContent(mContext, TitanConstant.VERIFY_CONFIG_ASSETS_PATH);
        if (TextUtils.isEmpty(sigContent)) {
            Log.e(TAG, "cannot find sig-config");
            return;
        }
        try {
            JSONObject configJson = new JSONObject(sigContent);
            try {
                mSignaturePolicy = SignaturePolicy.valueOf(configJson.getString("signaturePolicy"));
            } catch (Exception e) {
                mSignaturePolicy = SignaturePolicy.V2_ONLY;
            }
            if (mSignaturePolicy != SignaturePolicy.NO_SIGNATURE) {
                JSONArray sigs = configJson.getJSONArray("sigs");
                int sigCount = sigs.length();
                for (int i = 0; i < sigCount; i++) {
                    String sig = sigs.getString(i);
                    mAllowedSigs.add(sig);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 校验v1版本签名
     *
     * @return 校验结果
     */
    private boolean verifySignatureSchemeV1() {
        long startTime = System.currentTimeMillis();
        try {
            Certificate[] certs = ApkSignatureSchemeV1Verifier.verify(this.mPatchFile);
            if (certs != null && compareSignature(certs)) {
                if (TitanConfig.DEBUG) {
                    Log.d(TAG, "verify signature cost " + (System.currentTimeMillis() - startTime));
                }
                return true;
            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verify signature v1 cost " + (System.currentTimeMillis() - startTime));
        }
        return false;
    }

    /**
     * 是否存在V2版本签名
     *
     * @return 是否存在V2版本签名
     * @throws IOException 读取文件异常
     */
    private boolean hasSignatureSchemeV2() throws IOException {
        return ApkSignatureSchemeV2Verifier.hasSignature(this.mPatchFile);
    }

    /**
     * 校验V2版本签名
     *
     * @return 是否校验成功
     */
    private boolean verifySignatureSchemeV2() {
        long startTime = System.currentTimeMillis();
        try {
            X509Certificate[][] certificates = ApkSignatureSchemeV2Verifier.verify(this.mPatchFile);

            for (int i = 0; i < certificates.length; i++) {
                X509Certificate[] subCertificates = certificates[i];
                if (subCertificates == null) {
                    continue;
                }

                if (compareSignature(subCertificates)) {
                    if (TitanConfig.DEBUG) {
                        Log.d(TAG, "verify signature v2 cost " + (System.currentTimeMillis() - startTime));
                    }
                    return true;
                }
            }

        } catch (ApkSignatureSchemeV2Verifier.SignatureNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        if (TitanConfig.DEBUG) {
            Log.d(TAG, "verify signature v2 fail cost " + (System.currentTimeMillis() - startTime));
        }
        return false;
    }

    /**
     * 将从patch读取的证书签名与预存签名进行对比
     *
     * @param certificates 从patch读取的证书
     * @return 从patch读取的证书签名是否与预存签名相符
     * @throws CertificateEncodingException
     */
    private boolean compareSignature(Certificate[] certificates) throws CertificateEncodingException {
        HashSet<String> patchSigSet = new HashSet<>();
        for (int j = 0; j < certificates.length; j++) {
            Signature sig = new Signature(certificates[j].getEncoded());
            String sigHash = EncodeUtils.bytesToHex(EncodeUtils.sha1(sig.toByteArray()));
            patchSigSet.add(sigHash);
        }
        if (mAllowedSigs.equals(patchSigSet)) {
            return true;
        }
        return false;
    }
}
