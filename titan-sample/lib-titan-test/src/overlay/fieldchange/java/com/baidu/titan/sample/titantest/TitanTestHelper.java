package com.baidu.titan.sample.titantest;

import android.content.Context;
import android.graphics.drawable.Drawable;


/**
 * 用于在debug页面对预设的patch进行测试
 *
 * @author shanghuibo
 * @since 2018/09/11
 */

public class TitanTestHelper {

    /** debug tag*/
    public static final String TAG = "TitanTest";
    /** toast content */
    private String content = "field change";
    /** toast static content */
    private static String sContent = "static content change";

    /**
     * 初始化
     */
    public static void init() {

    }

    /**
     * patch 是否加载
     *
     * @return 只在nopatch中返回false，其它都应返回true
     */
    public boolean isPatchLoad() {
        return true;
    }

    /**
     * 获取toast中需要展示的logo
     *
     * @param context context
     * @return toast中需要展示的logo
     */
    public Drawable getDrawable(Context context) {
        return null;
    }

    /**
     * 获取toast 中需要展示的文字，同时成为了测试其它场景的入口
     *
     * @param context Context
     * @return  toast 中需要展示的文字
     */
    public String getPatchText(Context context) {
        StaticFinalFieldTester.testStaticFinalField();
        return content + " " + sContent;
    }
}
