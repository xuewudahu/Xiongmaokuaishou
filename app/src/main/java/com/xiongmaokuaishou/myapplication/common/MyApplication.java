package com.xiongmaokuaishou.myapplication.common;

import android.app.Application;
import android.content.Context;

/**
 * Author: ZhouJianbin周健彬
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    /**
     * 获取全局上下文*/
    public static Context getContext() {
        return context;
    }
}