package com.baidu.titan.sample.titantest;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by shanghuibo on 2018/9/11.
 */

public class TitanTestHelper {
    public static void init() {
        throw new RuntimeException("titan test crash");
    }

    public boolean isPatchLoad() {
        return true;
    }

    public Drawable getDrawable(Context context) {
        return null;
    }

    public String getPatchText(Context context) {
        return "patch is dex only";
    }
}
