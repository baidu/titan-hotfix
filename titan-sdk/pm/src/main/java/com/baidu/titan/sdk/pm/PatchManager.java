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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.baidu.titan.sdk.config.TitanConfig;
import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.sandbox.WorkerService;

import java.io.File;


/**
 * 负责Patch的安装、清理等逻辑
 *
 * @author zhangdi07
 * @since 2017/4/28
 */
public class PatchManager {

    private static final boolean DEBUG = TitanConfig.DEBUG;

    private static final String TAG = DEBUG ? "PatchManager" : PatchManager.class.getSimpleName();
    /** Patch安装成功 */
    public static final int INSTALL_STATE_SUCCESS = 0;
    /** Patch已经安装 */
    public static final int INSTALL_STATE_ALREADY_INSTALLED = 1;
    /** Patch校检失败 */
    public static final int INSTALL_STATE_VERIFY_ERROR_OTHER = -1;
    /** 获取APKID失败 */
    public static final int INSTALL_STATE_ERROR_APKID_FETCH = -2;
    /** IO发生位置错误 */
    public static final int INSTALL_STATE_ERROR_IO = -3;
    /** Patch ID与Apk ID不匹配 */
    public static final int INSTALL_STATE_VERIFY_ERROR_PATCH_ID_DISMATCH = -4;
    /** Patch签名不匹配 */
    public static final int INSTALL_STATE_VERIFY_ERROR_SIGNATURE_DISMATCH = -5;
    /** Patch版本降级 */
    public static final int INSTALL_STATE_PATCH_VERSION_DOWNGRADE = -6;
    /** DexOpt错误 */
    public static final int INSTALL_STATE_PATCH_ERROR_DEXOPT = -7;
    /** 解压出来的dex校验失败*/
    public static final int INSTALL_STATE_VERIFY_ERROR_EXTRACT_DEX = -8;
    /** 校验opt dex失败*/
    public static final int INSTALL_STATE_VERIFY_ERROR_OPT_DEX = -9;
    /** 解压dex时出错*/
    public static final int INSTALL_STATE_ERROR_EXTRACT_DEX = -10;

    public static final String INSTALL_RESULT_EXTRA_KEY = "install_result_extra";

    private final Context mContext;

    private static PatchManager sInstance;

    public static final String ACTION_INSTALL_PATCH = "action_install_patch";

    public static final String ACTION_CLEAN_PATCH = "action_clean_patch";

    public static final String PENDING_CLEAN_FILE = ".PENDING_CLEAN";

    /**
     * 获取单例
     * @return
     */
    public static PatchManager getInstance() {
        synchronized (PatchManager.class) {
            if (sInstance == null) {
                sInstance = new PatchManager(TitanIniter.getAppContext());
            }
            return sInstance;
        }
    }

    private PatchManager(Context c) {
        this.mContext = c.getApplicationContext();
    }


    public interface PatchInstallObserver {
        void onPatchInstalled(int statusCode, Bundle extra);
    }

    public void installPatch(final Uri uri, final Bundle extra, final PatchInstallObserver observer) {
        PatchInstallWrapper piw = new PatchInstallWrapper(mContext, uri, extra, observer);
        piw.bind();
    }

    public void requestCleanPatchs() {
        PatchCleanWrapper pcw = new PatchCleanWrapper(mContext);
        pcw.bind();
    }


    public static File getPendingCleanFile() {
        return new File(TitanPaths.getBaseDir(), PENDING_CLEAN_FILE);
    }

    public boolean needClean() {
        File pendingCleanFile = getPendingCleanFile();
        return pendingCleanFile.exists();
    }


    abstract static class RemoteServiceWrapper {

        private String mAction;

        protected Context mContext;

        private ServiceConnectionImpl mServiceConnection;

        public RemoteServiceWrapper(Context context, String action) {
            this.mAction = action;
            this.mContext = context;
            mServiceConnection = new ServiceConnectionImpl();
        }

        protected abstract void onServiceConnected(ComponentName name, IBinder service);

        class ServiceConnectionImpl implements ServiceConnection {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                RemoteServiceWrapper.this.onServiceConnected(name, service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // mUiHandler.obtainMessage(MSG_WHAT_PATCH_INSTALL, -101, 0, null).sendToTarget();
            }
        }

        public void bind() {
            Intent bindWorkerIntent = new Intent(mContext, WorkerService.class)
                    .setAction(mAction).putExtra(WorkerService.REQUEST_WORKER_SERVICE_BINDER, true);
            mContext.bindService(bindWorkerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        public void unbind() {
            mContext.unbindService(mServiceConnection);
        }
    }


    static class PatchInstallWrapper extends RemoteServiceWrapper {

        private PatchInstallObserverImpl mRemoteObserver;

        PatchInstallObserver mLocalObserver;

        Uri mUri;

        Bundle mExtra;

        private static final int MSG_WHAT_PATCH_INSTALL = 1;

        public PatchInstallWrapper(Context context, final Uri uri, final Bundle extra,
                                   final PatchInstallObserver localObsever) {
            super(context, ACTION_INSTALL_PATCH);
            this.mUri = uri;
            this.mExtra = extra;
            this.mLocalObserver = localObsever;
            mRemoteObserver = new PatchInstallObserverImpl();
        }

        private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_WHAT_PATCH_INSTALL: {
                        mLocalObserver.onPatchInstalled(msg.arg1, (Bundle) msg.obj);
                        break;
                    }
                    default:
                        break;
                }

            }
        };

        class PatchInstallObserverImpl extends IPatchInstallObserver.Stub {

            @Override
            public void onPatchInstalled(int statusCode, Bundle extra) throws RemoteException {
                Message msg = mUiHandler.obtainMessage(MSG_WHAT_PATCH_INSTALL);
                msg.arg1 = statusCode;
                msg.arg2 = 1;
                msg.obj = extra;
                msg.sendToTarget();

                unbind();
            }
        }

        @Override
        protected void onServiceConnected(ComponentName name, IBinder service) {
            IPatchManager pm = IPatchManager.Stub.asInterface(service);
            try {
                pm.install(mUri, 0, mExtra, mRemoteObserver);
            } catch (RemoteException e) {
                e.printStackTrace();
                mUiHandler.obtainMessage(MSG_WHAT_PATCH_INSTALL, -100, 0, null).sendToTarget();
            }
        }
    }

    static class PatchCleanWrapper extends RemoteServiceWrapper {

        private static final int MSG_WHAT_UNBIND = 1;

        public PatchCleanWrapper(Context context) {
            super(context, ACTION_CLEAN_PATCH);
        }

        private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_WHAT_UNBIND: {
                        unbind();
                        break;
                    }
                    default:
                        break;
                }

            }
        };

        @Override
        protected void onServiceConnected(ComponentName name, IBinder service) {
            IPatchManager pm = IPatchManager.Stub.asInterface(service);
            try {
                pm.requestCleanPatches();
            } catch (RemoteException e) {
                e.printStackTrace();
                mUiHandler.obtainMessage(MSG_WHAT_UNBIND, -100, 0, null).sendToTarget();
            }
        }
    }

}
