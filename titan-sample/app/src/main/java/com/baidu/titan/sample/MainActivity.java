package com.baidu.titan.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.baidu.titan.sample.titantest.TitanTestHelper;
import com.baidu.titan.sdk.pm.PatchManager;

import java.io.File;

public class MainActivity extends Activity {

    private static final String TAG = "Titan.Sample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        Button btnInstallPatch = findViewById(R.id.install_titan_patch);
        btnInstallPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doInstall();
            }
        });

        Button btnToastJava = findViewById(R.id.toast_java_text);
        btnToastJava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TitanTestHelper helper = new TitanTestHelper();
                ToastUtil.showToast(MainActivity.this, helper.getPatchText(MainActivity.this));
            }
        });
    }



    /**
     * M版本以上需要动态获取权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    /**
     * 安装titanpatch。patch包请提前push到/sdcard/titanpatch/patch.apk
     */
    private void doInstall() {
        File patchDir = new File(Environment.getExternalStorageDirectory(), "titanpatch");
        File patchFile = new File(patchDir, "patch.apk");

        PatchManager.getInstance().installPatch(Uri.fromFile(patchFile), null,
                new PatchManager.PatchInstallObserver() {
                    @Override
                    public void onPatchInstalled(int result, Bundle resultExtra) {
                        if (result == PatchManager.INSTALL_STATE_SUCCESS) {
                            ToastUtil.showToast(MainActivity.this, "Patch Install Success");
                        } else if (result == PatchManager.INSTALL_STATE_ALREADY_INSTALLED) {
                            ToastUtil.showToast(MainActivity.this, "Patch Install Already installed");
                        }

                        Log.d(TAG, "onPatchInstalled result code : " + result);
                    }
                });
    }
}
