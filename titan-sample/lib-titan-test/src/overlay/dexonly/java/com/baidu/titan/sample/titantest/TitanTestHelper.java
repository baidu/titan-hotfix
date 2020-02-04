package com.baidu.titan.sample.titantest;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by shanghuibo on 2018/9/11.
 */

public class TitanTestHelper {

    private String content = "no patch";

    private static String sContent = "static content";

    public static void init() {

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
