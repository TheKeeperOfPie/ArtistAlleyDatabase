package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppTheme

@Composable
fun DesktopTheme(settings: DesktopSettingsProvider, content: @Composable () -> Unit) {
    val appTheme by settings.appTheme.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    val colorScheme = when (appTheme) {
        AppThemeSetting.AUTO -> if (systemInDarkTheme) darkColorScheme() else lightColorScheme()
        AppThemeSetting.DARK -> darkColorScheme()
        AppThemeSetting.LIGHT -> lightColorScheme()
        AppThemeSetting.BLACK -> darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
            surfaceBright = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color.Black,
            surfaceContainerHighest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceDim = Color.Black,
        )
    }
    val isDarkTheme = appTheme == AppThemeSetting.DARK
            || appTheme == AppThemeSetting.BLACK
            || (appTheme == AppThemeSetting.AUTO && systemInDarkTheme)

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
