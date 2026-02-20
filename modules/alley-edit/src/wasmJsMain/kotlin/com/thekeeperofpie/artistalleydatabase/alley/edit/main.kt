@file:OptIn(ExperimentalWasmJsInterop::class)

package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.ComposeViewport
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.toUri
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.VariableFontEffect
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.utils.PreventUnloadEffect
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsDecoder
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsFetcher
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.WebResourcesConfiguration

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        PreventUnloadEffect()
        SideEffect {
            WebResourcesConfiguration.resourcePathMapping { "${window.location.origin}/$it" }
        }

        val scope = rememberCoroutineScope()
        val graph = createGraphFactory<ArtistAlleyEditWasmJsGraph.Factory>()
            .create(scope)

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it)
                .crossfade(false)
                .components {
                    add(Mapper<Uri, coil3.Uri> { data, _ ->
                        data.toString().toUri()
                    })
                    add(Mapper<ImageWithDimensions, PlatformImageKey> { data, _ ->
                        data.coilImageModel as? PlatformImageKey
                    })
                    add(Mapper<PlatformImageKey, PlatformFile> { data, _ ->
                        PlatformImageCache[data]
                    })
                    addPlatformFileSupport()
                    add(ImageWithDimensionsFetcher.factory)
                    add(ImageWithDimensionsDecoder::create)
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizeBytes(1000 * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }

        VariableFontEffect()
        Content(graph)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun Content(graph: ArtistAlleyEditGraph) {
    AlleyTheme(appTheme = { AppThemeSetting.AUTO }) {
        val windowSize = LocalWindowInfo.current.containerSize
        val density = LocalDensity.current
        val windowConfiguration = remember(windowSize, density) {
            WindowConfiguration(
                screenWidthDp = density.run { windowSize.width.toDp() },
                screenHeightDp = density.run { windowSize.height.toDp() },
            )
        }

        CompositionLocalProvider(
            LocalWindowConfiguration provides windowConfiguration,
        ) {
            val navStack = rememberArtistAlleyEditTopLevelStacks()
            LaunchedEffect(Unit) {
                ConsoleLogger.log("path = ${window.location.href}")
                val path = Uri.parseOrNull(window.location.href)
                    ?.path
                    ?.removePrefix("/edit/")
                    ?: return@LaunchedEffect
                val route = AlleyEditDestination.parseRoute(path) ?: return@LaunchedEffect
                navStack.navigate(route)
            }
            ArtistAlleyEditApp(
                graph = graph,
                navStack = navStack,
                onDebugOpenForm = { window.open(it, "_self") },
            )

            val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            val browserInput = remember(navStack) {
                BrowserInput(
                    routeHistory = navStack.routeHistory,
                    parseRoute = AlleyEditDestination::parseRoute,
                    onPopNavigate = {
                        navStack.navigateOnBrowserPop(it) {
                            (it as? AlleyEditDestination)?.let {
                                AlleyEditDestination.toEncodedRoute(it)
                            }
                        }
                    },
                    routePrefix = "/edit",
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
