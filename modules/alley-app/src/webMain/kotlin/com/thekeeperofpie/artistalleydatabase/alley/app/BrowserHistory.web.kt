package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.savedstate.read
import androidx.savedstate.savedState
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.w3c.dom.Window

@OptIn(ExperimentalBrowserHistoryApi::class)
suspend fun bindToNavigationFixed(
    navHostController: NavHostController,
    deepLinker: DeepLinker
) {
    val route = window.location.hash.substringAfter('#', "")
    if (route.startsWith("import")) {
        navHostController.navigate(AlleyDestination.Import(route.removePrefix("import=")))
    }
    window.bindToNavigationFixed(navHostController, deepLinker, null)
}

@ExperimentalBrowserHistoryApi
suspend fun Window.bindToNavigationFixed(
    navController: NavController,
    deepLinker: DeepLinker,
    getBackStackEntryRoute: ((entry: NavBackStackEntry) -> String)?,
) {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    (this as BrowserWindow).bindToNavigationFixed(navController, deepLinker, getBackStackEntryRoute)
}

/**
 * Copied out of CMP core to fix a bug:
 * https://youtrack.jetbrains.com/issue/CMP-7768/Compose-Navigation-2.9.0-alpha14-url-encoding-issues#focus=Comments-27-12063408.0-0
 * Binds the browser window state to the given navigation controller.
 *
 * @param navController The [NavController] instance to bind to browser window navigation.
 * @param getBackStackEntryRoute A function that returns the route to show for a given [NavBackStackEntry].
 */
@ExperimentalBrowserHistoryApi
internal suspend fun BrowserWindow.bindToNavigationFixed(
    navController: NavController,
    deepLinker: DeepLinker,
    getBackStackEntryRoute: ((entry: NavBackStackEntry) -> String)?,
) {
    coroutineScope {
        val localWindow = this@bindToNavigationFixed
        val appAddress = with(localWindow.location) { origin + pathname }

        //initial route
        if (getBackStackEntryRoute == null) {
            navController.tryToNavigateToUrlFragment(deepLinker, localWindow)
        }

        launch {
            localWindow.popStateEvents().collect { event ->
                val state = event.state

                if (state == null) {
                    //if user manually put a new address or open a new page, then there is no state
                    //if there is no route customization we can try to find the route
                    if (getBackStackEntryRoute == null) {
                        navController.tryToNavigateToUrlFragment(deepLinker, localWindow)
                    }
                    return@collect
                }

                val restoredRoutes = state.lines()
                val currentBackStack = navController.currentBackStack.value
                val currentRoutes = currentBackStack.filter { it.destination !is NavGraph }
                    .mapNotNull { it.getRouteWithArgs() }
                    .map(::decodeURIComponent)

                var commonTail = -1
                restoredRoutes.forEachIndexed { index, restoredRoute ->
                    if (index >= currentRoutes.size) {
                        return@forEachIndexed
                    }
                    if (restoredRoute == currentRoutes[index]) {
                        commonTail = index
                    }
                }

                if (commonTail == -1) {
                    //clear full stack
                    currentRoutes.firstOrNull()?.let { root ->
                        navController.popBackStack(root, true)
                    }
                } else {
                    currentRoutes[commonTail].let { lastCommon ->
                        navController.popBackStack(lastCommon, false)
                    }
                }

                //restore stack
                if (commonTail < restoredRoutes.size - 1) {
                    val newRoutes = restoredRoutes.subList(commonTail + 1, restoredRoutes.size)
                    newRoutes.forEach { route -> navController.navigate(route) }
                }
            }
        }

        launch {
            navController.currentBackStack.collect { stack ->
                if (stack.isEmpty()) return@collect

                val entries = stack.filter { it.destination !is NavGraph }
                if (entries.isEmpty()) return@collect
                val routes = entries.map { it.getRouteWithArgs() ?: return@collect }
                    .map(::decodeURIComponent)

                val currentDestination = entries.last()
                val currentRoute = if (getBackStackEntryRoute != null) {
                    getBackStackEntryRoute(currentDestination)
                } else {
                    currentDestination.getRouteAsUrlFragment()
                }
                val newUri = appAddress + currentRoute
                val state = routes.joinToString("\n")

                val currentState = localWindow.history.state
                when (currentState) {
                    null -> {
                        //user manually put a new address or open a new page,
                        // we need to save the current state in the browser history
                        localWindow.history.replaceState(state, "", newUri)
                    }

                    state -> {
                        //this was a restoration of the state (back/forward browser navigation)
                        //the callback came from the popStateEvents
                        //the browser state is equal the app state, but we need to update shown uri
                        localWindow.history.replaceState(state, "", newUri)
                    }

                    else -> {
                        //the navigation happened in the compose app,
                        // we need to push the new state to the browser history
                        localWindow.history.pushState(state, "", newUri)
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
private fun BrowserWindow.popStateEvents(): Flow<BrowserPopStateEvent> = callbackFlow {
    val localWindow = this@popStateEvents
    val callback: (BrowserEvent) -> Unit = { event: BrowserEvent ->
        if (!isClosedForSend) {
            (event as? BrowserPopStateEvent)?.let { trySend(it) }
        }
    }

    localWindow.addEventListener("popstate", callback)
    awaitClose {
        localWindow.removeEventListener("popstate", callback)
    }
}

private val argPlaceholder = Regex("""\{.*?\}""")
private fun NavBackStackEntry.getRouteWithArgs(): String? {
    val entry = this
    val route = entry.destination.route ?: return null
    if (!route.contains(argPlaceholder)) return route
    val args = entry.arguments ?: savedState()
    val nameToTypedValue = entry.destination.arguments.mapValues { (name, arg) ->
        arg.type.serializeAsValue(arg.type[args, name])
    }

    val routeWithFilledArgs = route.replace(argPlaceholder) { match ->
        val key = match.value.trim('{', '}')
        val value = nameToTypedValue[key]
        //untyped args stored as strings
        //see: androidx.navigation.NavDeepLink.parseArgument
            ?: args.read { getStringOrNull(key) ?: "" }
        encodeURIComponent(value)
    }

    return routeWithFilledArgs
}

private fun NavBackStackEntry.getRouteAsUrlFragment() =
    getRouteWithArgs()?.let { r -> "#$r" }.orEmpty()

private suspend fun NavController.tryToNavigateToUrlFragment(
    deepLinker: DeepLinker,
    localWindow: BrowserWindow,
) {
    val route = decodeURIComponent(localWindow.location.hash.substringAfter('#', ""))
    // TODO: Couldn't figure out how to use actual deep link mechanism
    if (!deepLinker.processRoute(this, route)) {
        if (route.isNotEmpty()) {
            try {
                navigate(route)
            } catch (e: IllegalArgumentException) {
                localWindow.console.warn(
                    """
                Can't navigate to '$route'! Error: ${e.message}
                Check that the NavGraph is set up already in the NavController.
                A typical mistake is to call `bindToNavigation` before the NavHost function is called.
            """.trimIndent()
                )
            }
        }
    }
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
    val console: BrowserConsole
}

internal external interface BrowserConsole {
    fun warn(msg: String)
}

external fun decodeURIComponent(str: String): String
external fun encodeURIComponent(str: String): String
