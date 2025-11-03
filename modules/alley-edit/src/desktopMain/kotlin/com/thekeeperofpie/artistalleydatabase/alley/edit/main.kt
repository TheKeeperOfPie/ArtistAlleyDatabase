package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.Dispatchers

fun main() {
    application {
        val scope = rememberCoroutineScope { Dispatchers.Main }
        val graph = createGraphFactory<ArtistAlleyEditGraph.Factory>()
            .create(scope)

        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Artist Alley Edit",
            state = windowState,
        ) {
            AlleyTheme(appTheme = { AppThemeSetting.AUTO }) {
                val windowSize = windowState.size
                val windowConfiguration = remember(windowSize) {
                    WindowConfiguration(
                        screenWidthDp = windowSize.width.takeOrElse { 600.dp },
                        screenHeightDp = windowSize.height.takeOrElse { 800.dp },
                    )
                }

                CompositionLocalProvider(
                    LocalWindowConfiguration provides windowConfiguration,
                ) {
                    App(graph)
                }
            }
        }
    }
}
