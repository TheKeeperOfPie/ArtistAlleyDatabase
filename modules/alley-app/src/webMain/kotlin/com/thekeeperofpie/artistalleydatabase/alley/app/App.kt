@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import artistalleydatabase.modules.alley_app.generated.resources.Res
import artistalleydatabase.modules.alley_app.generated.resources.service_worker_reload
import artistalleydatabase.modules.alley_app.generated.resources.service_worker_waiting_for_reload
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.size.Precision
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyAppScreen
import com.thekeeperofpie.artistalleydatabase.alley.VariableFontEffect
import com.thekeeperofpie.artistalleydatabase.alley.rememberAlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsDecoder
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsFetcher
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.WebResourcesConfiguration
import org.jetbrains.compose.resources.getString
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.time.Duration.Companion.seconds

private lateinit var artistImageCache: ArtistImageCache

@Composable
fun App(graph: ArtistAlleyWebGraph) {
    artistImageCache = graph.artistImageCache

    SideEffect {
        WebResourcesConfiguration.resourcePathMapping { "${window.location.origin}/$it" }
    }

    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .crossfade(false)
            .precision(Precision.INEXACT)
            .components {
                add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ ->
                    data.toString().toUri()
                })
                add(ImageWithDimensionsFetcher.factory)
                add(ImageWithDimensionsDecoder::create)
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(1000 * 1024 * 1024)
                    .build()
            }
            .build()
    }

    VariableFontEffect()
    Content(graph)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Content(graph: ArtistAlleyWebGraph) {
    val appTheme by graph.settings.appTheme.collectAsStateWithLifecycle()
    AlleyTheme(appTheme = { appTheme }) {
        val windowSize = LocalWindowInfo.current.containerSize
        val density = LocalDensity.current
        val windowConfiguration = remember(windowSize, density) {
            WindowConfiguration(
                screenWidthDp = density.run { windowSize.width.toDp() },
                screenHeightDp = density.run { windowSize.height.toDp() },
            )
        }

        CompositionLocalProvider(LocalWindowConfiguration provides windowConfiguration) {
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(snackbarHostState) {
                // If showSnackbar is called too early, the message will be dropped
                // TODO: Find a better solution
                delay(5.seconds)
                while (true) {
                    showWaitingChannel.receive()
                    val result = snackbarHostState.showSnackbar(
                        message = getString(Res.string.service_worker_waiting_for_reload),
                        actionLabel = getString(Res.string.service_worker_reload),
                        duration = SnackbarDuration.Indefinite,
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> Unit
                        SnackbarResult.ActionPerformed -> globalSkipWaitingBridge.skipWaiting()
                    }
                }
            }

            LaunchedEffect(Unit) {
                globalSkipWaitingBridge.onComposeReady(updateShowWaiting)
            }

            val navStack = rememberAlleyNavStack()
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                ArtistAlleyAppScreen(
                    graph = graph,
                    navStack = navStack,
                    modifier = Modifier.padding(it)
                )
            }

            // TODO: Translate legacy fragments
            val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            val browserInput = remember(navStack) {
                BrowserInput(
                    routeHistory = navStack.routeHistory,
                    parseRoute = AlleyDestination::parseRoute,
                    onPopNavigate = {
                        navStack.navigateOnBrowserPop(it) {
                            (it as? AlleyDestination)?.toEncodedRoute()
                        }
                    },
                )
            }
            DisposableEffect(navigationEventDispatcherOwner, browserInput) {
                val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                    ?: return@DisposableEffect onDispose {}
                dispatcher.addInput(browserInput)
                onDispose { dispatcher.removeInput(browserInput) }
            }
        }
    }
}
