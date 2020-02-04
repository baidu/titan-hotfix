### patch下载
可自行实现patch下发服务，客户端选择合适时机拉取patch，需要上传app版本号用于检查是否有对应版本patch。

客户端拉取patch时机实践：
1. 应用冷启动后，在启动流程完成后，开启线程发起patch拉取请求。
2. 应用崩溃时，启动一个容灾进程，发起patch拉取请求。该进程应尽量减少对应用中其它模块的依赖，尤其注意避免出现需要与主进程通信或者出现与主进程资源竞争的情况。比较典型的例子是，某模块使用DbHelper操作数据库，主进程和容灾进程都使用此模块，导致数据库访问时出现容灾进程对数据库加写锁，主进程访问数据库时，抛出DatabaseLock异常出现崩溃。
3. 考虑在长时间未拉取到patch时，主动调起容灾进程尝试拉取patch，避免主进程patch下发逻辑出现问题导致无法拉取patch。
4. 不建议在容灾进程中加载patch，避免因patch问题导致容灾进程失效。

### 安装patch

```
private void doInstall() {
    File patchFile = new File("patch.apk");
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
```

### 加载patch
在Application的attachBaseContext中加载
```
import com.baidu.titan.sdk.initer.TitanIniter;
import com.baidu.titan.sdk.loader.LoaderManager;

public class TitanApplication extends Application {
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ...
        TitanIniter.init(this);
        LoaderManager alm = LoaderManager.getInstance();
        alm.load();
    }
}
```