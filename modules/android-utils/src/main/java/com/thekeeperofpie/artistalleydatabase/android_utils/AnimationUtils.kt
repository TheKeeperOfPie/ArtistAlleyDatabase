package com.thekeeperofpie.artistalleydatabase.android_utils

import android.content.Context
import android.provider.Settings

object AnimationUtils {
    private var animatorScale = -1f

    private fun getAnimatorScale(context: Context): Float {
        if (animatorScale < 0) {
            animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }

        return animatorScale
    }

    fun multipliedByAnimatorScale(context: Context, value: Int) =
        value * getAnimatorScale(context).toInt()

    fun multipliedByAnimatorScale(context: Context, value: Long) =
        value * getAnimatorScale(context).toLong()
}