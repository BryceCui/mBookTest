package com.cuipengyu.mbooktest;

/**
 * Created by mingren on 2018/3/14.
 */

public class AppScreenUtil {
    private static AppScreenUtil mAppScreenUtil;

    public static AppScreenUtil getmAppScreenUtil() {
        if (mAppScreenUtil == null) {
            mAppScreenUtil = new AppScreenUtil();
        }
        return mAppScreenUtil;
    }

    public float getAppWidth() {
        return AppConnextUtil.getAppConnect().getResources().getDisplayMetrics().widthPixels;
    }

    public float getAppHeight() {
        return AppConnextUtil.getAppConnect().getResources().getDisplayMetrics().heightPixels;
    }
}
