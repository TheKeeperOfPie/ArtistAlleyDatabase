package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEachReversed
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyEditDestination.Home.serializer())
            subclass(serializer = AlleyEditDestination.ArtistEdit.serializer())
        }
    }
}

@Composable
fun rememberArtistAlleyEditTwoWayStack(): ArtistAlleyEditTwoWayStack {
    val backStack = rememberNavBackStack(
        SavedStateConfig,
        AlleyEditDestination.Home,
    )
    val forwardStack = rememberNavBackStack(SavedStateConfig)
    return remember(backStack, forwardStack) {
        ArtistAlleyEditTwoWayStack(backStack, forwardStack)
    }
}

@Stable
class ArtistAlleyEditTwoWayStack internal constructor(
    val navBackStack: NavBackStack<NavKey>,
    val navForwardStack: NavBackStack<NavKey>,
) : NavigationEventHandler<NavigationEventInfo>(
    initialInfo = NavigationEventInfo.None,
    isBackEnabled = true,
    isForwardEnabled = true,
) {
    fun navigate(destination: AlleyEditDestination) {
        if (destination == navForwardStack.lastOrNull()) {
            onForwardCompleted()
        } else {
            navForwardStack.clear()
            navBackStack += destination
        }
        updateInfo()
    }

    fun onBack() {
        if (navBackStack.size > 1) {
            navForwardStack += navBackStack.removeLast()
            updateInfo()
        }
    }

    fun onForward() {
        if (navForwardStack.isNotEmpty()) {
            navBackStack += navForwardStack.removeLast()
            updateInfo()
        }
    }

    private fun updateInfo() {
        val backInfo = mutableListOf<Route>()
        navBackStack.dropLast(1).forEach {
            backInfo += Route(
                AlleyEditDestination.toEncodedRoute(it as AlleyEditDestination)
                    ?: backInfo.lastOrNull()?.route.orEmpty()
            )
        }

        val currentInfo =
            AlleyEditDestination.toEncodedRoute(navBackStack.last() as AlleyEditDestination)
                ?.let(::Route)
                ?: backInfo.lastOrNull()
                ?: Route("")

        val forwardInfo = mutableListOf<Route>()
        navForwardStack.fastForEachReversed {
            forwardInfo += AlleyEditDestination.toEncodedRoute(it as AlleyEditDestination)
                ?.let(::Route)
                ?: forwardInfo.lastOrNull()
                        ?: currentInfo
        }

        setInfo(currentInfo = currentInfo, backInfo = backInfo, forwardInfo = forwardInfo)
    }

    override fun onBackCompleted() = onBack()

    override fun onForwardCompleted() = onForward()

    private data class Route(val route: String) : NavigationEventInfo()
}
