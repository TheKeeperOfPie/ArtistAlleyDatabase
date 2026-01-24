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
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventInput
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
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.ArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsDecoder
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageWithDimensionsFetcher
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.WebResourcesConfiguration
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        DisposableEffect(Unit) {
            val listener: (Event) -> Unit = { it.preventDefault() }
            window.addEventListener("beforeunload", listener)
            onDispose {
                window.removeEventListener("beforeunload", listener)
            }
        }
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
                    add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ ->
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

            val scope = rememberCoroutineScope()
            val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            val browserInput = remember(scope, navStack) { BrowserInput(navStack) }
            DisposableEffect(navigationEventDispatcherOwner, browserInput) {
                val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                    ?: return@DisposableEffect onDispose {}
                dispatcher.addInput(browserInput)
                onDispose { dispatcher.removeInput(browserInput) }
            }
        }
    }
}

class BrowserInput(private val navStack: ArtistAlleyEditTopLevelStacks) :
    NavigationEventInput() {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private val browserWindow = window as BrowserWindow

    private var coroutineScope: CoroutineScope? = null
    private var currentHistory = ArtistAlleyEditTopLevelStacks.RouteHistory(
        current = ArtistAlleyEditTopLevelStacks.Route(""),
        back = emptyList(),
        forward = emptyList(),
    )
    private var processPopState = true

    private val popStateFlow = callbackFlow {
        val eventListener: (Event) -> Unit = { trySend(it as PopStateEvent) }
        window.addEventListener("popstate", eventListener)
        awaitClose { window.removeEventListener("popstate", eventListener) }
    }

    sealed interface Update {
        data object Forward : Update
        data object Backward : Update
        data class Push(val route: ArtistAlleyEditTopLevelStacks.Route) : Update
        data class Replace(val route: ArtistAlleyEditTopLevelStacks.Route) : Update

        companion object {
            fun diff(
                current: ArtistAlleyEditTopLevelStacks.RouteHistory,
                updated: ArtistAlleyEditTopLevelStacks.RouteHistory,
            ): Update? {
                if (current.forward.isNotEmpty()) {
                    val expectedForward = ArtistAlleyEditTopLevelStacks.RouteHistory(
                        current = current.forward.first(),
                        back = current.back + current.current,
                        forward = current.forward.toMutableList().apply { removeAt(0) },
                    )
                    if (updated == expectedForward) {
                        return Forward
                    }
                }
                if (current.back.isNotEmpty()) {
                    val expectedBackward = ArtistAlleyEditTopLevelStacks.RouteHistory(
                        current = current.back.last(),
                        back = current.back.toMutableList().apply { removeLast() },
                        forward = listOf(current.current) + current.forward,
                    )
                    if (updated == expectedBackward) {
                        return Backward
                    }
                }

                if (updated.forward.isEmpty()) {
                    val expectedPush = ArtistAlleyEditTopLevelStacks.RouteHistory(
                        current = updated.current,
                        back = current.back + current.current,
                        forward = emptyList(),
                    )
                    if (expectedPush == updated) {
                        return Push(updated.current)
                    }
                }

                val expectedReplace = ArtistAlleyEditTopLevelStacks.RouteHistory(
                    current = updated.current,
                    back = current.back,
                    forward = current.forward,
                )
                if (expectedReplace == updated) {
                    return Replace(updated.current)
                }

                return null
            }
        }
    }

    override fun onAdded(dispatcher: NavigationEventDispatcher) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch { popStateFlow.collect(::onPopState) }
        scope.launch {
            browserWindow.history.replaceState(null, "", "${window.location.origin}/edit")
            navStack.routeHistory.collectLatest { routeHistory ->
                val update = Update.diff(currentHistory, routeHistory)
                val currentRoute by lazy {
                    ArtistAlleyEditTopLevelStacks.Route(browserWindow.history.state.toString())
                }
                when (update) {
                    Update.Backward -> if (currentRoute != routeHistory.current) {
                        browserWindow.history.go(-1)
                    }
                    Update.Forward -> if (currentRoute != routeHistory.current) {
                        browserWindow.history.go(1)
                    }
                    is Update.Push -> {
                        browserWindow.history.pushState(update.route)
                    }
                    is Update.Replace -> {
                        browserWindow.history.replaceState(update.route)
                    }
                    null -> {
                        // There is no simple transform, try and preserve as much history as
                        // possible and then rebuild the rest
                        val allCurrentHistory =
                            currentHistory.back + currentHistory.current + currentHistory.forward
                        val allRouteHistory =
                            routeHistory.back + routeHistory.current + routeHistory.forward
                        var lastMatchingIndex = -1
                        for (index in 0 until allRouteHistory.size) {
                            if (allCurrentHistory[index] != allRouteHistory[index]) break
                            lastMatchingIndex = index
                        }
                        if (lastMatchingIndex == -1) {
                            // Nothing matches, reset the entire stack
                            val delta = -currentHistory.back.size
                            if (delta != 0) {
                                disableOnPopStateCallback {
                                    browserWindow.history.goAndWait(delta)
                                }
                            }
                            if (allRouteHistory.size == 1) {
                                val route = allRouteHistory.single()
                                browserWindow.history.replaceState(route)
                            } else {
                                allRouteHistory.forEach { route ->
                                    browserWindow.history.pushState(route)
                                }
                            }
                        } else {
                            val delta = lastMatchingIndex - currentHistory.back.size
                            if (delta != 0) {
                                disableOnPopStateCallback {
                                    browserWindow.history.goAndWait(delta)
                                }
                            }
                            for (index in lastMatchingIndex + 1 until allRouteHistory.size) {
                                val route = allRouteHistory[index]
                                browserWindow.history.pushState(route)
                            }
                        }

                        val deltaToCurrent = -routeHistory.forward.size
                        if (deltaToCurrent != 0) {
                            browserWindow.history.go(deltaToCurrent)
                        }
                    }
                }

                currentHistory = routeHistory
            }
        }
        coroutineScope = scope
    }

    private suspend fun BrowserHistory.goAndWait(delta: Int) {
        val deferred = CompletableDeferred<PopStateEvent>()
        val eventListener: (Event) -> Unit = { deferred.complete(it as PopStateEvent) }
        try {
            window.addEventListener("popstate", eventListener)
            go(delta)
            deferred.await()
        } finally {
            window.removeEventListener("popstate", eventListener)
        }
    }

    private fun BrowserHistory.replaceState(route: ArtistAlleyEditTopLevelStacks.Route) {
        replaceState(
            route.route,
            "",
            "${window.location.origin}/edit/${route.route}"
        )
    }

    private fun BrowserHistory.pushState(route: ArtistAlleyEditTopLevelStacks.Route) {
        pushState(
            route.route,
            "",
            "${window.location.origin}/edit/${route.route}"
        )
    }

    override fun onRemoved() {
        coroutineScope?.cancel()
        currentHistory = ArtistAlleyEditTopLevelStacks.RouteHistory(
            current = ArtistAlleyEditTopLevelStacks.Route(""),
            back = emptyList(),
            forward = emptyList(),
        )
        processPopState = true
    }

    private inline fun disableOnPopStateCallback(content: () -> Unit) {
        processPopState = false
        content()
        processPopState = true
    }

    private fun onPopState(popStateEvent: PopStateEvent) {
        if (!processPopState) {
            return
        }

        val state = popStateEvent.state ?: return
        val stateRoute = AlleyEditDestination.parseRoute(state.toString())
        if (stateRoute == null) {
            browserWindow.history.go(-1)
            return
        }

        navStack.navigateOnBrowserPop(stateRoute)
    }
}

internal external interface BrowserHistory {
    val state: JsAny?
    fun pushState(data: String?, title: String, url: String?)
    fun replaceState(data: String?, title: String, url: String?)
    fun go(delta: Int)
}

internal external interface BrowserWindow {
    val history: BrowserHistory
}
