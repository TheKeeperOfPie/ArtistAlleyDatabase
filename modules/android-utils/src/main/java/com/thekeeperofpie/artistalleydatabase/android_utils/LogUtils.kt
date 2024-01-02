package com.thekeeperofpie.artistalleydatabase.android_utils

import android.util.Log

object LogUtils {
    fun d(tag: String, message: String) {
        message.chunked(4000).forEach {
            Log.d(tag, it)
        }
    }
}
