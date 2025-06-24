package com.cqaz.adstatistics;

import android.util.Log;

public class FengliLog {
    public static boolean isDebug = BuildConfig.DEBUG;

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, Throwable tr) {
        if (isDebug) {
            Log.e(tag, "", tr);
        }
    }
}
