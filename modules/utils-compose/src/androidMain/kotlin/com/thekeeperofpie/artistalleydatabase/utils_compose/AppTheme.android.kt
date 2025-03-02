package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun AppTheme(
    appTheme: () -> AppThemeSetting,
    content: @Composable () -> Unit,
) {
    val appTheme = appTheme()
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    val colorScheme = when (appTheme) {
        AppThemeSetting.AUTO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (systemInDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            if (systemInDarkTheme) darkColorScheme() else lightColorScheme()
        }
        AppThemeSetting.DARK -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            darkColorScheme()
        }
        AppThemeSetting.LIGHT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            lightColorScheme()
        }
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController((view.context as Activity).window, view)
                .isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
