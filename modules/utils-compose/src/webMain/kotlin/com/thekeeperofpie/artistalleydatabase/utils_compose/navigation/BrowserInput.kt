package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventInput
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event
import kotlin.js.JsAny

class BrowserInput<T : NavKey>(
    private val routeHistory: Flow<NavigationRouteHistory>,
    private val parseRoute: (String) -> T?,
    private val onPopNavigate: (T) -> Unit,
    private val routePrefix: String = "",
) :
    NavigationEventInput() {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private val browserWindow = window as BrowserWindow

    private var coroutineScope: CoroutineScope? = null
    private var currentHistory = NavigationRouteHistory(
        current = NavigationRoute(""),
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
        data class Push(val route: NavigationRoute) : Update
        data class Replace(val route: NavigationRoute) : Update

        companion object {
            fun diff(
                current: NavigationRouteHistory,
                updated: NavigationRouteHistory,
            ): Update? {
                if (current.forward.isNotEmpty()) {
                    val expectedForward = NavigationRouteHistory(
                        current = current.forward.first(),
                        back = current.back + current.current,
                        forward = current.forward.toMutableList().apply { removeAt(0) },
                    )
                    if (updated == expectedForward) {
                        return Forward
                    }
                }
                if (current.back.isNotEmpty()) {
                    val expectedBackward = NavigationRouteHistory(
                        current = current.back.last(),
                        back = current.back.toMutableList().apply { removeLast() },
                        forward = listOf(current.current) + current.forward,
                    )
                    if (updated == expectedBackward) {
                        return Backward
                    }
                }

                if (updated.forward.isEmpty()) {
                    val expectedPush = NavigationRouteHistory(
                        current = updated.current,
                        back = current.back + current.current,
                        forward = emptyList(),
                    )
                    if (expectedPush == updated) {
                        return Push(updated.current)
                    }
                }

                val expectedReplace = NavigationRouteHistory(
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
            browserWindow.history.replaceState(null, "", "${window.location.origin}$routePrefix")
            routeHistory.collectLatest { routeHistory ->
                val update = Update.diff(currentHistory, routeHistory)
                val currentRoute by lazy {
                    NavigationRoute(browserWindow.history.state.toString())
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
                            val delta = lastMatchingIndex - routeHistory.back.size
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

                        val deltaToCurrent = lastMatchingIndex + 1 - allRouteHistory.size
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

    private fun BrowserHistory.replaceState(route: NavigationRoute) {
        replaceState(
            route.route,
            "",
            "${window.location.origin}$routePrefix/${route.route}"
        )
    }

    private fun BrowserHistory.pushState(route: NavigationRoute) {
        pushState(
            route.route,
            "",
            "${window.location.origin}$routePrefix/${route.route}"
        )
    }

    override fun onRemoved() {
        coroutineScope?.cancel()
        currentHistory = NavigationRouteHistory(
            current = NavigationRoute(""),
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
        val stateRoute = parseRoute(state.toString())
        if (stateRoute == null) {
            browserWindow.history.go(-1)
            return
        }

        onPopNavigate(stateRoute)
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
