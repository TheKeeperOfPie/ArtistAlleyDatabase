package com.thekeeperofpie.artistalleydatabase.alley.form

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
import coil3.memory.MemoryCache
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.VariableFontEffect
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.credentialsHttpClient
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeInit
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput2
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.WebResourcesConfiguration

@OptIn(ExperimentalComposeUiApi::class, ExperimentalCoilApi::class)
fun main() {
    ComposeInit.init()
    ComposeViewport(document.body!!) {
        SideEffect {
            WebResourcesConfiguration.resourcePathMapping { "${window.location.origin}/$it" }
        }

        val scope = rememberCoroutineScope()
        val graph = remember(scope) {
            createGraphFactory<ArtistAlleyFormWasmJsGraph.Factory>().create(scope)
        }

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it)
                .components {
                    val concurrentRequestStrategy = DeDupeConcurrentRequestStrategy()
                    add(
                        KtorNetworkFetcherFactory(
                            httpClient = { credentialsHttpClient() },
                            concurrentRequestStrategy = { concurrentRequestStrategy },
                        )
                    )
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
private fun Content(graph: ArtistAlleyFormGraph) {
    AlleyTheme(appTheme = { AppThemeSetting.AUTO }, graph) {
        val windowSize = LocalWindowInfo.current.containerSize
        val density = LocalDensity.current
        val windowConfiguration = remember(windowSize, density) {
            WindowConfiguration(
                screenWidthDp = density.run { windowSize.width.toDp() },
                screenHeightDp = density.run { windowSize.height.toDp() },
            )
        }

        val navStack = rememberFormTwoWayStack()
        LaunchedEffect(window.location.href) {
            val uri = Uri.parseOrNull(window.location.href)
            uri?.getQueryParameter(AlleyCryptography.ACCESS_KEY_PARAM)
                ?.let(ArtistFormAccessKey::setKey)
            uri?.getQueryParameter(AlleyCryptography.ACCESS_KEY_ENCRYPTED_PARAM)
                ?.let { ArtistFormAccessKey.setKeyEncrypted(it) }
            if (uri?.getQueryParameter("openForm").toBoolean()) {
                navStack.navigate(AlleyFormDestination.ArtistForm(DataYear.LATEST))
            }
        }

        CompositionLocalProvider(
            LocalWindowConfiguration provides windowConfiguration,
        ) {
            ArtistAlleyFormApp(graph = graph, navStack = navStack)

            val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            val browserInput = remember(navStack) {
                BrowserInput2(
                    navHistory = navStack.routeHistory,
                    restoreStack = {
                        navStack.restore(
                            back = it.back.mapNotNull { AlleyFormDestination.parseRoute(it.route) } +
                                    listOfNotNull(AlleyFormDestination.parseRoute(it.current.route)),
                            forward = it.forward.mapNotNull { AlleyFormDestination.parseRoute(it.route) },
                        )
                    },
                    navigateTo = {
                        AlleyFormDestination.parseRoute(it.route)?.let(navStack::navigate)
                    },
                    navigateBy = navStack::navigateBy,
                    routePrefix = "form",
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
