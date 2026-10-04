package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventInput
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import kotlin.coroutines.CoroutineContext

private const val DEBUG = false

class BrowserInput(
    private val navHistory: StateFlow<NavigationRouteHistory>,
    private val restoreStack: (NavigationRouteHistory) -> Unit,
    private val navigateTo: (NavigationRoute) -> Unit,
    private val navigateBy: (Int) -> Unit,
    private val routePrefix: String = "",
    private val coroutineContext: CoroutineContext = Dispatchers.Main,
) : NavigationEventInput() {

    private fun NavigationDestination.print() =
        "NavigationDestination[id = $id, key = $key, index = $index, url = $url]"

    private var coroutineScope: CoroutineScope? = null
    private var navigateListener: (Event) -> Unit = onEvent@{
        val event = it.unsafeCast<NavigateEvent>()
        if (DEBUG) {
            ConsoleLogger.log(
                "event canIntercept = ${event.canIntercept}," +
                        " userInitiated = ${event.userInitiated}," +
                        " destination = ${event.destination.print()}," +
                        " navigationType = ${event.navigationType}," +
                        " info = ${event.info}"
            )
        }
        if (!event.canIntercept) return@onEvent

        val destinationPath = URL(event.destination.url).pathname

        // TODO: Generalize this
        if ((destinationPath.startsWith("form") && routePrefix != "form") ||
            (destinationPath.startsWith("edit") && routePrefix != "edit")) {
            event.intercept(InterceptOptions { window.open(event.destination.url) })
            return@onEvent
        }

        if (!destinationPath.removePrefix("/").startsWith(routePrefix)) return@onEvent

        val userOrTestInitiated = event.userInitiated || event.info == TestNavigationOptions.info
        when {
            userOrTestInitiated && event.navigationType == NavigationType.TRAVERSE -> {
                val currentIndex = window.navigation!!.currentEntry?.index ?: 0
                if (event.destination.index == currentIndex) {
                    event.intercept()
                } else {
                    val target = event.destination.index - currentIndex
                    event.intercept(InterceptOptions { navigateBy(target) })
                }
            }
            userOrTestInitiated && event.navigationType == NavigationType.PUSH -> {
                val route = destinationPath.toNavRoute()
                event.intercept(InterceptOptions { navigateTo(NavigationRoute(route)) })
            }
            userOrTestInitiated && event.navigationType == NavigationType.REPLACE ->
                event.intercept()
            event.navigationType != NavigationType.RELOAD -> event.intercept()
        }
    }

    override fun onAdded(dispatcher: NavigationEventDispatcher) {
        super.onAdded(dispatcher)
        // TODO: Verify that the stack is empty/default before restoring?
        val currentHistory = currentHistory()
        if (currentHistory.back.isNotEmpty() || currentHistory.forward.isNotEmpty()) {
            try {
                restoreStack(currentHistory)
            } catch (t: Throwable) {
                ConsoleLogger.log("Failed to restore history ${t.message}")
                t.printStackTrace()
            }
        }
        // TODO: Handle API unavailable?
        window.navigation?.addEventListener("navigate", navigateListener)
        coroutineScope = CoroutineScope(coroutineContext).apply {
            launch {
                navHistory.collectLatest {
                    try {
                        handleUpdate(it)
                    } catch (t: Throwable) {
                        currentCoroutineContext().ensureActive()
                        ConsoleLogger.log("Failed to update history ${t.message}")
                        t.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRemoved() {
        super.onRemoved()
        coroutineScope?.cancel()
        window.navigation?.removeEventListener("navigate", navigateListener)
    }

    private suspend fun navigate(route: NavigationRoute) {
        val path = if (routePrefix.isEmpty()) "/${route.route}" else "/$routePrefix/${route.route}"
        window.navigation?.navigate("${window.location.origin}$path")?.committed?.await()
    }

    private fun currentHistory(): NavigationRouteHistory {
        val navigation = window.navigation ?: return fallbackHistory()
        val entries = navigation.entries().toArray()
            .map { NavigationRoute(URL(it.url).pathname.toNavRoute()) }
            .takeIf { it.isNotEmpty() }
            ?: return fallbackHistory()
        val currentIndex = navigation.currentEntry?.index ?: return fallbackHistory()
        return NavigationRouteHistory(
            current = entries[currentIndex],
            back = entries.take(currentIndex),
            forward = entries.drop(currentIndex + 1),
        )
    }

    private fun fallbackHistory() = NavigationRouteHistory(
        current = NavigationRoute(window.location.pathname.toNavRoute()),
        back = emptyList(),
        forward = emptyList(),
    )

    private fun String.toNavRoute() = removePrefix("/").removePrefix(routePrefix).removePrefix("/")

    private suspend fun handleUpdate(updated: NavigationRouteHistory) {
        val current = currentHistory()
        val update = Update.diff(current, updated)
        if (DEBUG) ConsoleLogger.log("Handling update $current -> $updated: $update")
        val navigation = window.navigation!!
        when (update) {
            Update.Backward -> navigation.back().committed.await()
            Update.Forward -> navigation.forward().committed.await()
            is Update.Push -> navigate(update.route)
            is Update.Traverse -> navigation.traverseTo(update.key).committed.await()
            is Update.Truncate -> {
                if (update.key != null) {
                    navigation.traverseTo(update.key).committed.await()
                }
                update.pushRoutes.forEach { navigate(it) }
            }
            Update.DoNothing -> Unit
            null -> ConsoleLogger.log("Unknown navigation update")
        }
    }

    sealed interface Update {
        data object Forward : Update
        data object Backward : Update
        data class Push(val route: NavigationRoute) : Update
        data class Traverse(val key: String) : Update
        data class Truncate(val key: String?, val pushRoutes: List<NavigationRoute>) : Update
        data object DoNothing : Update

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

                val currentEntries = current.back + current.current + current.forward
                val updatedEntries = updated.back + updated.current + updated.forward
                if (currentEntries == updatedEntries) {
                    val updatedIndexInCurrent = currentEntries.indexOfLast { it == updated.current }
                    if (updatedIndexInCurrent == current.back.size) {
                        return DoNothing
                    }
                    val key = window.navigation?.entries()?.toArray()
                        ?.getOrNull(updatedIndexInCurrent)?.key
                    if (key != null) {
                        return Traverse(key)
                    }
                }

                // If none of the above cases pass, find the longest tail, jump to it, and push
                var lastCommonIndex = currentEntries.lastIndex
                while (currentEntries.getOrNull(lastCommonIndex) != null &&
                    currentEntries.getOrNull(lastCommonIndex) != updatedEntries.getOrNull(
                        lastCommonIndex
                    )
                ) {
                    lastCommonIndex--
                }
                if (lastCommonIndex >= 0) {
                    val navigation = window.navigation
                    val key = navigation?.entries()?.toArray()?.getOrNull(lastCommonIndex)?.key
                            ?.takeIf { it != navigation.currentEntry?.key }
                    return Truncate(key, updatedEntries.drop(lastCommonIndex + 1))
                }

                return null
            }
        }
    }
}
