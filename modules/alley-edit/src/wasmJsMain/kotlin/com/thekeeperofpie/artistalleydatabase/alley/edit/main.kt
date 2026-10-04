package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.alley.VariableFontEffect
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.utils.PreventUnloadEffect
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeInit
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput2
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.WebResourcesConfiguration
import com.eygraber.uri.Uri as KmpUri

@OptIn(ExperimentalComposeUiApi::class, ExperimentalCoilApi::class)
fun main() {
    ComposeInit.init()
    ComposeViewport(document.body!!) {
        PreventUnloadEffect()
        SideEffect {
            WebResourcesConfiguration.resourcePathMapping { "${window.location.origin}/$it" }
        }

        val scope = rememberCoroutineScope()
        val graph = remember(scope) {
            createGraphFactory<ArtistAlleyEditWasmJsGraph.Factory>().create(scope)
        }

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it)
                .crossfade(false)
                .components {
                    val concurrentRequestStrategy = DeDupeConcurrentRequestStrategy()
                    add(
                        KtorNetworkFetcherFactory(
                            httpClient = { credentialsHttpClient() },
                            concurrentRequestStrategy = { concurrentRequestStrategy },
                        )
                    )
                    // TODO: Why is declaring this here required instead of relying on AlleyCoilInit?
                    add(Mapper<ImageWithDimensions, KmpUri> { data, _ ->
                        data.coilImageModel as? KmpUri
                    })
                    add(Mapper<KmpUri, coil3.Uri> { data, _ ->
                        data.toString().toUri()
                    })
                    graph.alleyEditCoilInit.addComponents()
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizeBytes(1000 * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }

        var fontFamilyResolver by rememberSaveable { mutableStateOf<FontFamily.Resolver?>(null) }
        VariableFontEffect(
            seriesEntryCache = graph.seriesEntryCache,
            onLoaded = { fontFamilyResolver = it },
        )
        CompositionLocalProvider(
            LocalFontFamilyResolver provides (fontFamilyResolver ?: LocalFontFamilyResolver.current)
        ) {
            Content(graph)
        }
    }
}

@Composable
private fun Content(graph: ArtistAlleyEditGraph) {
    AlleyTheme(appTheme = { AppThemeSetting.AUTO }, graph) {
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
                val path = KmpUri.parseOrNull(window.location.href)
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
                BrowserInput2(
                    navHistory = navStack.routeHistory,
                    restoreStack = {
                        navStack.restore(
                            back = it.back.mapNotNull { AlleyEditDestination.parseRoute(it.route) } +
                                    listOfNotNull(AlleyEditDestination.parseRoute(it.current.route)),
                            forward = it.forward.mapNotNull { AlleyEditDestination.parseRoute(it.route) },
                        )
                    },
                    navigateTo = {
                        AlleyEditDestination.parseRoute(it.route)?.let(navStack::navigate)
                    },
                    navigateBy = navStack::navigateBy,
                    routePrefix = "edit",
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
