package com.cuipengyu.mbooktest;

import android.app.Application;
import android.content.Context;

/**
 * Created by mingren on 2018/3/14.
 */

public class AppConnextUtil extends Application {
    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        AppConnextUtil.context = getApplicationContext();
    }

    public static Context getAppConnect() {
        return AppConnextUtil.context;
    }

}
