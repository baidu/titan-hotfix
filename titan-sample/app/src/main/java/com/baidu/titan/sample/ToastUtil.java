package com.baidu.titan.sample;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by shanghuibo on 18-1-18.
 */

public class ToastUtil {

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
