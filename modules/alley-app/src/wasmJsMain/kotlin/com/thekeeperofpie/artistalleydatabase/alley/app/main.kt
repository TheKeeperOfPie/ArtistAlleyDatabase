package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackGestureDispatcher
import androidx.compose.ui.backhandler.LocalBackGestureDispatcher
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyAppScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationController
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

private lateinit var artistImageCache: ArtistImageCache

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val keyEvents = remember {
            Channel<KeyboardEvent>(capacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }
        DisposableEffect(keyEvents) {
            val callback: (Event) -> Unit = {
                if (it is KeyboardEvent) {
                    keyEvents.trySend(it)
                }
            }

            document.addEventListener("keydown", callback)
            onDispose { document.removeEventListener("keydown", callback) }
        }

        val scope = rememberCoroutineScope()
        val component = ArtistAlleyWasmJsComponent::class.create(scope)
        artistImageCache = component.artistImageCache

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .crossfade(false)
                .components {
                    add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ ->
                        data.toString().toUri()
                    })
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizeBytes(1000 * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }

        val appTheme by component.settings.appTheme.collectAsStateWithLifecycle()
        AppTheme(appTheme = { appTheme }) {
            val windowSize = LocalWindowInfo.current.containerSize
            val density = LocalDensity.current
            val windowConfiguration = remember(windowSize, density) {
                WindowConfiguration(
                    screenWidthDp = density.run { windowSize.width.toDp() },
                    screenHeightDp = density.run { windowSize.height.toDp() },
                )
            }

            val navHostController = rememberNavController()
            val navigationController = rememberNavigationController(navHostController)
            val backGestureDispatcher = remember { WasmJsBackGestureDispatcher() }
            LaunchedEffect(backGestureDispatcher, keyEvents) {
                while (isActive) {
                    val event = keyEvents.receive()
                    backGestureDispatcher.onKeyboardEvent(event)
                }
            }

            CompositionLocalProvider(
                LocalWindowConfiguration provides windowConfiguration,
                LocalNavigationController provides navigationController,
                LocalBackGestureDispatcher provides backGestureDispatcher,
            ) {
                ArtistAlleyAppScreen(component, navHostController)
                LaunchedEffect(navHostController) {
                    window.bindToNavigation(navHostController)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class WasmJsBackGestureDispatcher : BackGestureDispatcher() {
    fun onKeyboardEvent(event: KeyboardEvent) {
        if (event.key == "Escape") {
            onBack()
        }
    }

    private fun onBack() {
        activeListener?.let {
            it.onStarted()
            it.onCompleted()
        }
    }
}
