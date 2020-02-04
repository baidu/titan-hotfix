package com.baidu.titan.sample.titantest;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于测试匿名内部类的增减改
 *
 * @author shanghuibo
 * @since 2019/02/13
 */
public class AnonymousTester {
    /** debug tag */
    public static final String TAG = "TitanTest";
    /** 执行任务的单线程池*/
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    /** 单例*/
    private static volatile AnonymousTester sInstance;

    /**
     * 私有构造方法
     */
    private AnonymousTester() {

    }

    /**
     * 获取单例对象方法
     *
     * @return 单例对象
     */
    public static synchronized AnonymousTester getInstance() {
        if (sInstance == null) {
            sInstance = new AnonymousTester();
        }

        return sInstance;
    }

    /**
     * 测试匿名类
     */
    public void testAnonymousClass() {
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "runnable 1");
            }
        });

        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "runnable 2");
            }
        });

        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "runnable 3");
            }
        });

    }
}
