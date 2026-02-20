package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEachReversed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRouteHistory
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyDestination.Home.serializer())
            subclass(serializer = AlleyDestination.ArtistDetails.serializer())
            subclass(serializer = AlleyDestination.ArtistMap.serializer())
            subclass(serializer = AlleyDestination.ArtistsList.serializer())
            subclass(serializer = AlleyDestination.Changelog.serializer())
            subclass(serializer = AlleyDestination.Export.serializer())
            subclass(serializer = AlleyDestination.Images.serializer())
            subclass(serializer = AlleyDestination.Import.serializer())
            subclass(serializer = AlleyDestination.Series.serializer())
            subclass(serializer = AlleyDestination.SeriesMap.serializer())
            subclass(serializer = AlleyDestination.Merch.serializer())
            subclass(serializer = AlleyDestination.MerchMap.serializer())
            subclass(serializer = AlleyDestination.Settings.serializer())
            subclass(serializer = AlleyDestination.StampRallies.serializer())
            subclass(serializer = AlleyDestination.StampRallyDetails.serializer())
            subclass(serializer = AlleyDestination.StampRallyMap.serializer())
        }
    }
}

@Composable
fun rememberAlleyNavStack(): AlleyNavStack {
    val twoWayStack = rememberTwoWayStack(AlleyDestination.Home, SavedStateConfig)
    return remember(twoWayStack) { AlleyNavStack(twoWayStack) }
}

@Stable
class AlleyNavStack(
    internal val twoWayStack: TwoWayStack,
) : NavigationEventHandler<NavigationEventInfo>(
    initialInfo = NavigationEventInfo.None,
    isBackEnabled = true,
    isForwardEnabled = true,
) {
    val routeHistory =
        MutableStateFlow(
            NavigationRouteHistory(
                current = NavigationRoute(""),
                back = emptyList(),
                forward = emptyList(),
            )
        )

    fun calculateBackStack(navEntries: List<NavEntry<NavKey>>) =
        navEntries.take(twoWayStack.navBackStack.size)

    fun navBackStack() = twoWayStack.navBackStack

    fun navigate(destination: NavKey) {
        twoWayStack.navigate(destination)
        updateInfo()
    }

    fun <T : NavKey> navigateOnBrowserPop(destination: T, toRoute: (NavKey) -> String?) {
        twoWayStack.navigateOnBrowserPop(destination, toRoute)
        updateInfo()
    }

    private fun updateInfo() {
        val backInfo = mutableListOf<NavigationRoute>()
        val twoWayStack = twoWayStack
        val navBackStack = twoWayStack.navBackStack
        val navForwardStack = twoWayStack.navForwardStack
        navBackStack.dropLast(1).forEach {
            backInfo += NavigationRoute((it as AlleyDestination).toEncodedRoute())
        }

        val currentInfo =
            NavigationRoute((navBackStack.last() as AlleyDestination).toEncodedRoute())

        val forwardInfo = mutableListOf<NavigationRoute>()
        navForwardStack.fastForEachReversed {
            forwardInfo += NavigationRoute((it as AlleyDestination).toEncodedRoute())
        }

        routeHistory.value = NavigationRouteHistory(currentInfo, backInfo, forwardInfo)
        setInfo(currentInfo = currentInfo, backInfo = backInfo, forwardInfo = forwardInfo)
    }

    fun onBack() = onBackCompleted()
    fun onForward() = onForwardCompleted()

    override fun onBackCompleted() {
        if (twoWayStack.onBack()) {
            updateInfo()
        }
    }

    override fun onForwardCompleted() {
        if (twoWayStack.onForward()) {
            updateInfo()
        }
    }
}
