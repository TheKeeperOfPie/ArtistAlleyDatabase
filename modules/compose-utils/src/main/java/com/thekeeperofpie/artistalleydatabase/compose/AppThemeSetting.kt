package com.thekeeperofpie.artistalleydatabase.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import com.thekeeperofpie.compose_proxy.R

enum class AppThemeSetting(@StringRes val textRes: Int) {
    AUTO(R.string.theme_auto),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
    BLACK(R.string.theme_black),
    ;
}

val LocalAppTheme = compositionLocalOf { AppThemeSetting.AUTO }
