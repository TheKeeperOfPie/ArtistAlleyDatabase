package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.compositionLocalOf
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.theme_auto
import artistalleydatabase.modules.utils_compose.generated.resources.theme_black
import artistalleydatabase.modules.utils_compose.generated.resources.theme_dark
import artistalleydatabase.modules.utils_compose.generated.resources.theme_light
import org.jetbrains.compose.resources.StringResource

enum class AppThemeSetting(val textRes: StringResource) {
    AUTO(Res.string.theme_auto),
    LIGHT(Res.string.theme_light),
    DARK(Res.string.theme_dark),
    BLACK(Res.string.theme_black),
    ;
}

val LocalAppTheme = compositionLocalOf { AppThemeSetting.AUTO }
