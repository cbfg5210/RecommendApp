package com.ue.recommend.util

import android.util.Log

/**
 * Created by hawk on 2017/11/27.
 */

object KLog {
    private var isShowLog: Boolean = false

    fun init(showLog: Boolean) {
        isShowLog = showLog
    }

    fun e(tag: String, msg: String) {
        if (isShowLog) {
            Log.e(tag, msg)
        }
    }
}
