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

package com.baidu.titan.sdk.sandbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.titan.sdk.pm.PatchManagerService;
import com.baidu.titan.sdk.pm.IPatchInstallObserver;
import com.baidu.titan.sdk.pm.IPatchManager;
import com.baidu.titan.sdk.config.TitanConfig;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 运行在sandbox进程的Service
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/29
 */

public class WorkerService extends JobIntentService {

    private static final boolean DEBUG = TitanConfig.DEBUG;

    private static final String TAG = DEBUG ? "WorkerService" : WorkerService.class.getSimpleName();

    /** Action：安装Patch */
    public static final String ACTION_INSTALL_PATCH = "action_install_patch";
    /** Action：清理Patch */
    public static final String ACTION_CLEAN_PATCH = "action_clean_patch";

    private static final String TOKEN = "token";

    /** WorkerService的父类也实现了onBind。
     * 以此字段来区分bind请求的是WorkerService的IBinder还是JobIntentService的IBinder
     **/
    public static final String REQUEST_WORKER_SERVICE_BINDER = "worker_service_binder";


    private final AtomicLong mTokenGen = new AtomicLong(1000);

    private HashMap<Long, WorkerService.InstallParameter> mInstallParameters = new HashMap<>();

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1001;


    private static class InstallParameter {
        public Uri uri;
        public int flages;
        public IPatchInstallObserver observer;
        public Bundle extra;

        @Override
        public String toString() {
            return "InstallParameter{"
                    + "uri=" + uri
                    + ", flages=" + flages
                    + ", observer=" + observer
                    + ", extra=" + extra
                    + '}';
        }
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, WorkerService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (ACTION_INSTALL_PATCH.equals(action)) {
            handleInstallPatch(intent);
        } else if (ACTION_CLEAN_PATCH.equals(action)) {
            handleCleanPatches();
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getBooleanExtra(REQUEST_WORKER_SERVICE_BINDER, false)) {
            return mPatchManager.asBinder();
        }
        return super.onBind(intent);
    }

    private void handleCleanPatches() {
        PatchManagerService apms = PatchManagerService.getInstance();
        apms.doCleanPatchsLocked();
    }

    private void handleInstallPatch(Intent intent) {
        Long token = intent.getLongExtra(TOKEN, -1);
        if (token < 0) {
            return;
        }
        WorkerService.InstallParameter ip = mInstallParameters.get(token);
        if (ip == null) {
            if (DEBUG) {
                Log.e(TAG, "install parameter is null, token = " + token);
            }
            return;
        }

        if (DEBUG) {
            Log.i(TAG, "do install, uri = " + ip.uri);
        }

        final IPatchInstallObserver remoteObserver = ip.observer;



        PatchManagerService installer = PatchManagerService.getInstance();
        Bundle resultExtra = new Bundle();
        int status = installer.installSyncLocked(ip.uri, ip.extra, resultExtra);


        try {
            remoteObserver.onPatchInstalled(status, resultExtra);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private IPatchManager.Stub mPatchManager = new IPatchManager.Stub() {

        @Override
        public void requestCleanPatches() throws RemoteException {
            Intent intent = new Intent(WorkerService.this, WorkerService.class);
            intent.setAction(ACTION_CLEAN_PATCH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WorkerService.enqueueWork(WorkerService.this, intent);
            } else {
                startService(intent);
            }
        }

        @Override
        public void install(Uri uri, int flags, Bundle extra, IPatchInstallObserver observer) throws RemoteException {
            Intent intent = new Intent(WorkerService.this, WorkerService.class);
            intent.setAction(ACTION_INSTALL_PATCH);
            Long token = mTokenGen.incrementAndGet();
            WorkerService.InstallParameter ip = new WorkerService.InstallParameter();
            ip.uri = uri;
            ip.flages = flags;
            ip.observer = observer;
            ip.extra = extra;
            mInstallParameters.put(token, ip);
            intent.putExtra(TOKEN, token);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WorkerService.enqueueWork(WorkerService.this, intent);
            } else {
                startService(intent);
            }
        }
    };

}
