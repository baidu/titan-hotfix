package com.baidu.titan.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.loader.LoaderManager;
import com.baidu.titan.sdk.runtime.annotation.DisableIntercept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by shanghuibo on 18-1-18.
 */
@DisableIntercept
public class TitanSampleApplication extends Application {
    /**
     * 当前进程的进程名
     */
    private static String sProcessName;
    /**
     * 是否主进程
     */
    private static boolean sIsMainProcess;
    /** cmdline name */
    private static final String CMDLINENAME = "/proc/self/cmdline";
    /** 进程名长度 */
    private static final int PROCESSNAMELENGTH = 500;
    /** 进程类型 */
    /** package */ static int sProcessType;
    /** 进程类型:主进程 */
    private static final int PROCESS_TYPE_MAIN = 0;
    /** 进程类型: Titan sandbox */
    private static final int PROCESS_TYPE_TITAN_SANDBOX = 1;
    /** 进程类型：未知类型 */
    private static final int PROCESS_TYPE_UNKOWN = 2;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initProcessName();
        TitanIniter.init(this);
        // 针对非Titan沙箱进程进行修复
        if (sProcessType != PROCESS_TYPE_TITAN_SANDBOX) {
            LoaderManager alm = LoaderManager.getInstance();
            alm.load();
        }
    }

    private void initProcessName() {
        sProcessName = getProcessNameFromFile();
        if (TextUtils.isEmpty(sProcessName)) {
            sProcessName = getProcessNameFromAm(this);
        }

        sIsMainProcess = checkIsMainProcess(sProcessName, this.getApplicationInfo().processName);
        if (sIsMainProcess) {
            sProcessType = PROCESS_TYPE_MAIN;
        } else {
            if (sProcessName != null) {
                if (sProcessName.contains("sandbox")) {
                    sProcessType = PROCESS_TYPE_TITAN_SANDBOX;
                } else {
                    sProcessType = PROCESS_TYPE_UNKOWN;
                }
            }

        }
    }

    /**
     * 获得当前进程的进程名
     * @param context context
     * @return 当前进程的进程名
     */
    private String getProcessNameFromAm(Context context) {
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcess = activityManager.getRunningAppProcesses();
        if (runningProcess == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo info : runningProcess) {
            if (info.pid == pid) {
                return info.processName;
            }
        }

        return null;
    }

    /**
     * 从文件中读取进程名，经过测试发现更快一些
     * @return 进程名
     */
    private String getProcessNameFromFile() {
        String processName = null;
        File file = new File(CMDLINENAME);
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(file);
            byte[] buffer = new byte[PROCESSNAMELENGTH];
            int len = fin.read(buffer);
            processName = new String(buffer, 0, len).trim();
        } catch (Exception e) {
            processName = null;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return processName;
    }

    /**
     * 判断是否是主进程名， processName有可能包含换行等符
     * @param processName
     * @param mainProcessName
     * @return 如果是返回true
     */
    private boolean checkIsMainProcess(String processName, String mainProcessName) {
        if (TextUtils.equals(processName, mainProcessName)) {
            return true;
        }
        else if (processName.startsWith(mainProcessName) && !(processName.contains(":"))) {
            return true;
        }
        return false;
    }
}
