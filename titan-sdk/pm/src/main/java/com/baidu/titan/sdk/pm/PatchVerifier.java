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
import android.text.TextUtils;

import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.internal.util.EncodeUtils;
import com.baidu.titan.sdk.verifier.SignatureVerifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 负责Patch的校检逻辑，包括签名、patchid的校验
 *
 * @author zhangdi07
 * @author shanghuibo
 * @since 2017/4/28
 */
public class PatchVerifier {

    private static final boolean DEBUG = TitanConfig.DEBUG;
    private static final String TAG = "PatchVerifier";

    private File mPatchFile;
    private String mCurId;
    private JSONObject mLogJson;
    private SignatureVerifier mSigVerifier;

    public static final int VERIFY_OK = 0;

    public PatchVerifier(Context context, File patch, String curId, JSONObject logJson) {
        this.mPatchFile = patch;
        this.mCurId = curId;
        this.mLogJson = logJson;
        this.mSigVerifier = new SignatureVerifier(context, patch);
    }

    public int verify() {
        JSONObject patchInfoJson = new JSONObject();
        if (mLogJson != null) {
            try {
                mLogJson.put("patchInfo", patchInfoJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String patchHash = EncodeUtils.bytesToHex(EncodeUtils.sha256(mPatchFile));
            try {
                patchInfoJson.put("pathHash", patchHash);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int verifyResult = mSigVerifier.verifySignature();
        if (verifyResult != VERIFY_OK) {
            return verifyResult;
        }

        PatchMetaInfo pmi = PatchMetaInfo.createFromPatch(mPatchFile);
        if (pmi == null) {
            return PatchManager.INSTALL_STATE_VERIFY_ERROR_OTHER;
        }
        if (mLogJson != null) {
            try {
                patchInfoJson.put("metaInfo", pmi.toJson());
                patchInfoJson.put("curApkId", mCurId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!verifyApkId(pmi)) {
            return PatchManager.INSTALL_STATE_VERIFY_ERROR_PATCH_ID_DISMATCH;
        }

        return VERIFY_OK;
    }

    private boolean verifyApkId(PatchMetaInfo pmi) {
        return pmi.targetId != null && TextUtils.equals(pmi.targetId, mCurId);
    }
}
