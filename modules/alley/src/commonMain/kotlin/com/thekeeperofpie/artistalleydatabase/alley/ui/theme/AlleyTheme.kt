package com.thekeeperofpie.artistalleydatabase.alley.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.LocalBuildConfig
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.LocalImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils_theme.AppTheme
import com.thekeeperofpie.artistalleydatabase.utils_theme.LocalAlleyColorScheme

@Composable
fun AlleyTheme(
    appTheme: () -> AppThemeSetting,
    graph: ArtistAlleyGraph,
    content: @Composable () -> Unit,
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val appTheme = appTheme()
    val colorScheme = when (appTheme) {
        AppThemeSetting.AUTO -> if (systemInDarkTheme) AlleyTheme.darkScheme else AlleyTheme.lightScheme
        AppThemeSetting.DARK -> AlleyTheme.darkScheme
        AppThemeSetting.LIGHT -> AlleyTheme.lightScheme
        AppThemeSetting.BLACK -> AlleyTheme.blackTheme
        AppThemeSetting.MIKU -> AppTheme.mikuTheme
    }

    val alleyColorScheme = when (appTheme) {
        AppThemeSetting.AUTO -> if (systemInDarkTheme) AlleyTheme.darkColorScheme else AlleyTheme.lightColorScheme
        AppThemeSetting.LIGHT -> AlleyTheme.lightColorScheme
        AppThemeSetting.DARK,
        AppThemeSetting.BLACK,
        AppThemeSetting.MIKU,
            -> AlleyTheme.darkColorScheme
    }

    val buildConfig = remember(graph) { graph.buildConfig }
    val imageColorsState = rememberImageColorsState()
    val currentUriHandler = LocalUriHandler.current
    val uriHandler = remember(currentUriHandler) {
        object : UriHandler {
            override fun openUri(uri: String) {
                try {
                    currentUriHandler.openUri(uri)
                } catch (t: Throwable) {
                    ConsoleLogger.log("Failed to open URI: ${t.message}")
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalBuildConfig provides buildConfig,
        LocalAlleyColorScheme provides alleyColorScheme,
        LocalImageColorsState provides imageColorsState,
        LocalUriHandler provides uriHandler,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
