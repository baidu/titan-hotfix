package com.baidu.titan.sample;

import android.content.Context;

public class ToastShower {
    public void showToast(Context context) {
        ToastUtil toastUtil = new ToastUtil(context, 0);
        ToastUtil.showToast(context, toastUtil.getName());
    }
}
