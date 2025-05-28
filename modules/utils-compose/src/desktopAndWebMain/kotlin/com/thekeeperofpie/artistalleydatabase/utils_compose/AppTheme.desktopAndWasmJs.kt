package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
actual fun AppTheme(appTheme: () -> AppThemeSetting, content: @Composable () -> Unit) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val appTheme = appTheme()
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
    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
