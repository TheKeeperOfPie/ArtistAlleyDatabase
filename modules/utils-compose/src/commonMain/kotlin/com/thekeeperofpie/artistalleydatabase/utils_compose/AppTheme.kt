package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
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

@Composable
expect fun AppTheme(appTheme: () -> AppThemeSetting, content: @Composable () -> Unit)
