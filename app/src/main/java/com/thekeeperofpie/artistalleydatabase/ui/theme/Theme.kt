package com.thekeeperofpie.artistalleydatabase.ui.theme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.compose.LocalAppTheme
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider

@Composable
fun ArtistAlleyDatabaseTheme(
    settings: SettingsProvider,
    navHostController: NavHostController,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val appTheme by settings.appTheme.collectAsState()
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

    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            try {
                val deepLinkUri = Uri.parse(uri)
                if (deepLinkUri.getQueryParameter(UriUtils.FORCE_EXTERNAL_URI_PARAM) != "true"
                    && navHostController.graph.hasDeepLink(deepLinkUri)
                ) {
                    navHostController.navigate(deepLinkUri)
                } else {
                    val strippedUri = uri.replace("?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true", "")
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strippedUri)))
                }
            } catch (e: Exception) {
                Log.d(CustomApplication.TAG, "Error launching URI $uri", e)
                Toast.makeText(context, R.string.error_launching_generic_uri, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    CompositionLocalProvider(
        LocalUriHandler provides uriHandler,
        LocalAppTheme provides appTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
