
package com.baidu.titan.sdk.pm;

import android.net.Uri;
import com.baidu.titan.sdk.pm.IPatchInstallObserver;

// Declare any non-default types here with import statements

interface IPatchManager {

    void install(in Uri uri, int flags, in Bundle extra, in IPatchInstallObserver observer);

    void requestCleanPatches();

}
