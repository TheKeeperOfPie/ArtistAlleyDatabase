package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.ComposeViewport
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
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.WebResourcesConfiguration
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
        val graph = createGraphFactory<ArtistAlleyFormWasmJsGraph.Factory>()
            .create(scope)

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it)
                .crossfade(false)
                .components {
                    add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ ->
                        data.toString().toUri()
                    })
                    add(Mapper<PlatformImageKey, PlatformFile> { data, _ ->
                        PlatformImageCache[data]
                    })
                    addPlatformFileSupport()
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
private fun Content(graph: ArtistAlleyFormGraph) {
    AlleyTheme(appTheme = { AppThemeSetting.AUTO }) {
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
            if (uri?.getQueryParameter("openForm").toBoolean()) {
                navStack.navigate(AlleyFormDestination.ArtistForm(DataYear.ANIME_EXPO_2026))
            }
        }

        CompositionLocalProvider(
            LocalWindowConfiguration provides windowConfiguration,
        ) {
            ArtistAlleyFormApp(graph = graph, navStack = navStack, onOpenForm = {
                window.open(window.location.origin + "/form/login?openForm=true")
            })

            val scope = rememberCoroutineScope()
            val browserInput = remember(scope, navStack) { BrowserInput(scope, navStack) }
            val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            DisposableEffect(navigationEventDispatcherOwner, browserInput) {
                val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                    ?: return@DisposableEffect onDispose {}
                dispatcher.addInput(browserInput)
                onDispose { dispatcher.removeInput(browserInput) }
            }
        }
    }
}

class BrowserInput(scope: CoroutineScope, navStack: TwoWayStack) :
    NavigationEventInput() {
    init {
        scope.launch {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            val localWindow = window as BrowserWindow
            val baseUrl = localWindow.location.run { origin + pathname }

            launch {
                localWindow.popStateEvents().collect { event ->
                    val state = event.state
                    if (state == null) {
                        val route = localWindow.location.pathname.substringAfter("form/")
                        val destination = AlleyFormDestination.parseRoute(route)
                        if (destination != null) {
                            navStack.navigate(destination)
                        }
                        return@collect
                    }

                    Snapshot.withMutableSnapshot {
                        val restoredRoutes = state.lines()
                            .map(AlleyFormDestination::parseRoute)
                        val currentRoutes = navStack.navBackStack

                        var commonTail = -1
                        restoredRoutes.forEachIndexed { index, restoredRoute ->
                            if (index >= currentRoutes.size) {
                                return@forEachIndexed
                            }
                            if (restoredRoute == currentRoutes[index]) {
                                commonTail = index
                            }
                        }

                        when (commonTail) {
                            currentRoutes.size - 2 -> dispatchOnBackCompleted()
                            -1, 0 -> {
                                val root = currentRoutes.removeFirst()
                                navStack.navBackStack.clear()
                                navStack.navBackStack += root
                            }
                            else -> ((currentRoutes.size - 1) downTo commonTail + 1).forEach {
                                currentRoutes.removeAt(it)
                            }
                        }

                        if (commonTail < restoredRoutes.size - 1) {
                            restoredRoutes.subList(commonTail + 1, restoredRoutes.size)
                                .filterNotNull()
                                .forEach(navStack::navigate)
                        }
                    }
                }
            }

            launch {
                snapshotFlow { navStack.navBackStack.toList() }
                    .collect {
                        val routes =
                            it.map { AlleyFormDestination.toEncodedRoute(it as AlleyFormDestination) }
                        val currentRoute = routes.last()
                        val newUri = "$baseUrl/$currentRoute"
                        val state = routes.joinToString("\n")

                        val currentState = localWindow.history.state
                        if (currentState == null || currentState == state) {
                            localWindow.history.replaceState(state, "", newUri)
                        } else {
                            localWindow.history.pushState(state, "", newUri)
                        }
                    }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun BrowserWindow.popStateEvents(): Flow<BrowserPopStateEvent> = callbackFlow {
    val callback: (BrowserEvent) -> Unit = { event: BrowserEvent ->
        if (!isClosedForSend) {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            (event as? BrowserPopStateEvent)?.let { trySend(it) }
        }
    }

    addEventListener("popstate", callback)
    awaitClose { removeEventListener("popstate", callback) }
}

internal external interface BrowserLocation {
    val origin: String
    val pathname: String
    val hash: String
}

internal external interface BrowserHistory {
    val state: String?
    fun pushState(data: String?, title: String, url: String?)
    fun replaceState(data: String?, title: String, url: String?)
}

internal external interface BrowserEvent
internal external interface BrowserPopStateEvent : BrowserEvent {
    val state: String?
}

internal external interface BrowserEventTarget {
    fun addEventListener(type: String, callback: ((BrowserEvent) -> Unit)?)
    fun removeEventListener(type: String, callback: ((BrowserEvent) -> Unit)?)
}

internal external interface BrowserWindow : BrowserEventTarget {
    val location: BrowserLocation
    val history: BrowserHistory
}
