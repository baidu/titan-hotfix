package com.baidu.titan.sample.titantest;

import android.util.Log;

/**
 * 用于测试static final类型变量的修改
 *
 * @author shanghuibo
 * @since 2019/02/13
 */
public class StaticFinalFieldTester {
    /** debug tag */
    public static final String TAG = "TitanTest";
    /** 当前时间，修改为以秒计*/
    private static final long curTime = System.currentTimeMillis() / 1000;

    /** 测试方法*/
    public static void testStaticFinalField() {
        Log.d(TAG, "class init time = " + curTime + " s");
    }
}
