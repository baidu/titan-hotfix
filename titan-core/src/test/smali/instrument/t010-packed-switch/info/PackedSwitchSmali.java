package com.baidu.titan.sample;

import android.util.Log;

public class PackedSwitchSmali {
    public void logInt(int value) {
        switch (value) {
            case 2:
                Log.d("ToastUtil", "hit 2");
                break;
            case 3:
                Log.d("ToastUtil", "hit 3");
                break;
            default:
                Log.d("ToastUtil", String.valueOf(value));
                break;
        }
    }
}
