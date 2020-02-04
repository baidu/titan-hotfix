// IPatchInstallObserver.aidl
package com.baidu.titan.sdk.pm;

// Declare any non-default types here with import statements

oneway interface IPatchInstallObserver {

    void onPatchInstalled(int statusCode, in Bundle resultExtra);
}
