package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackGestureDispatcher
import androidx.compose.ui.backhandler.LocalBackGestureDispatcher
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import artistalleydatabase.modules.alley_app.generated.resources.Res
import artistalleydatabase.modules.alley_app.generated.resources.service_worker_reload
import artistalleydatabase.modules.alley_app.generated.resources.service_worker_waiting_for_reload
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

expect suspend fun bindToNavigationFixed(
    navHostController: NavHostController,
    deepLinker: DeepLinker,
)

private lateinit var artistImageCache: ArtistImageCache

@Composable
fun App(component: ArtistAlleyWebComponent, keyEvents: Channel<WrappedKeyboardEvent>) {
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

    val fontFamilyResolver = LocalFontFamilyResolver.current
    LaunchedEffect(Unit) {
        try {
            val fonts = listOf(
                "NotoSansJP-VariableFont_wght.ttf",
                "NotoSansKR-VariableFont_wght.ttf",
                "NotoSansSC-VariableFont_wght.ttf",
            ).map {
                Font(it, Res.readBytes("font/$it"))
            }
            fontFamilyResolver.preload(FontFamily(fonts))
        } catch (_: Throwable) {
        }
    }

    Content(component, keyEvents)
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
@Composable
private fun Content(
    component: ArtistAlleyWebComponent,
    keyEvents: Channel<WrappedKeyboardEvent>,
) {
    val deepLinker = component.deepLinker
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
            val rootSnackbarHostState = remember { SnackbarHostState() }
            val waitingMessage = stringResource(Res.string.service_worker_waiting_for_reload)
            val waitingAction = stringResource(Res.string.service_worker_reload)
            LaunchedEffect(rootSnackbarHostState, waitingMessage, waitingAction) {
                // If showSnackbar is called too early, the message will be dropped
                // TODO: Find a better solution
                delay(5.seconds)
                while (true) {
                    showWaitingChannel.receive()
                    val result = rootSnackbarHostState.showSnackbar(
                        message = waitingMessage,
                        actionLabel = waitingAction,
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

            ArtistAlleyAppScreen(
                component = component,
                navHostController = navHostController,
                rootSnackbarHostState = rootSnackbarHostState,
            )
            LaunchedEffect(navHostController, deepLinker) {
                bindToNavigationFixed(navHostController, deepLinker)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class WasmJsBackGestureDispatcher : BackGestureDispatcher() {
    fun onKeyboardEvent(event: WrappedKeyboardEvent) {
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
