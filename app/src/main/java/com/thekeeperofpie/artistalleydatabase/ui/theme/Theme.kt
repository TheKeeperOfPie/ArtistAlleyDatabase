package com.thekeeperofpie.artistalleydatabase.ui.theme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.view.WindowCompat
import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.R

@Composable
fun ArtistAlleyDatabaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme =
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(
                (view.context as Activity).window,
                view
            ).isAppearanceLightStatusBars = darkTheme
        }
    }

    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            } catch (e: Exception) {
                Log.d(CustomApplication.TAG, "Error launching URI $uri", e)
                Toast.makeText(context, R.string.error_launching_generic_uri, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    CompositionLocalProvider(
        LocalUriHandler provides uriHandler
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}