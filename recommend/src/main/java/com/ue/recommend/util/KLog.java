package com.ue.recommend.util;

import android.util.Log;

/**
 * Created by hawk on 2017/11/27.
 */

public class KLog {
    private static boolean isShowLog;

    public static void init(boolean showLog) {
        isShowLog = showLog;
    }

    public static void e(String tag, String msg) {
        if (isShowLog) {
            Log.e(tag, msg);
        }
    }
}
