package com.thekeeperofpie.artistalleydatabase.alley.edit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.util.fastForEachReversed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRouteHistory
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberDecoratedNavEntries
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyEditDestination.Home.serializer())
            subclass(serializer = AlleyEditDestination.Admin.serializer())
            subclass(serializer = AlleyEditDestination.ArtistAdd.serializer())
            subclass(serializer = AlleyEditDestination.ArtistEdit.serializer())
            subclass(serializer = AlleyEditDestination.ArtistFormHistory.serializer())
            subclass(serializer = AlleyEditDestination.ArtistFormMerge.serializer())
            subclass(serializer = AlleyEditDestination.ArtistFormQueue.serializer())
            subclass(serializer = AlleyEditDestination.ArtistHistory.serializer())
            subclass(serializer = AlleyEditDestination.ImagesEdit.serializer())
            subclass(serializer = AlleyEditDestination.Series.serializer())
            subclass(serializer = AlleyEditDestination.SeriesAdd.serializer())
            subclass(serializer = AlleyEditDestination.SeriesEdit.serializer())
            subclass(serializer = AlleyEditDestination.Merch.serializer())
            subclass(serializer = AlleyEditDestination.MerchAdd.serializer())
            subclass(serializer = AlleyEditDestination.MerchEdit.serializer())
            subclass(serializer = AlleyEditDestination.TagResolution.serializer())
            subclass(serializer = AlleyEditDestination.SeriesResolution.serializer())
            subclass(serializer = AlleyEditDestination.MerchResolution.serializer())
            subclass(serializer = AlleyEditDestination.StampRallies.serializer())
            subclass(serializer = AlleyEditDestination.StampRallyAdd.serializer())
            subclass(serializer = AlleyEditDestination.StampRallyEdit.serializer())
        }
    }
}

@Composable
fun rememberArtistAlleyEditTopLevelStacks(): ArtistAlleyEditTopLevelStacks {
    val stacks = TopLevelStackKey.entries.map {
        key(it) {
            rememberTwoWayStack(it.initialDestination, SavedStateConfig)
        }
    }
    val topLevelStackIndex = rememberSaveable { mutableIntStateOf(0) }
    return remember(stacks, topLevelStackIndex) {
        ArtistAlleyEditTopLevelStacks(stacks, topLevelStackIndex)
    }
}

@Composable
fun rememberDecoratedNavEntries(
    stacks: ArtistAlleyEditTopLevelStacks,
    entryProvider: (key: NavKey) -> NavEntry<NavKey>,
) = TopLevelStackKey.entries.mapIndexed { index, key ->
    key(key) {
        rememberDecoratedNavEntries(stacks.twoWayStacks[index], entryProvider)
    }
}

@Stable
class ArtistAlleyEditTopLevelStacks internal constructor(
    internal val twoWayStacks: List<TwoWayStack>,
    topLevelStackIndex: MutableIntState,
) : NavigationEventHandler<NavigationEventInfo>(
    initialInfo = NavigationEventInfo.None,
    isBackEnabled = true,
    isForwardEnabled = true,
) {
    var topLevelStackIndex by topLevelStackIndex
        private set
    private val topLevelKeysStack = listOf(topLevelStackIndex.value).toMutableStateList()

    val routeHistory =
        MutableStateFlow(
            NavigationRouteHistory(
                current = NavigationRoute(""),
                back = emptyList(),
                forward = emptyList(),
            )
        )

    fun calculateBackStack(navEntries: List<List<NavEntry<NavKey>>>) =
        navEntries[topLevelStackIndex].take(twoWayStacks[topLevelStackIndex].navBackStack.size)

    fun navBackStack() = twoWayStacks[topLevelStackIndex].navBackStack

    fun moveToTopLevelStack(index: Int) {
        Snapshot.withMutableSnapshot {
            topLevelKeysStack -= index
            topLevelKeysStack += index
            topLevelStackIndex = index
            updateInfo()
        }
    }

    fun navigate(destination: NavKey) {
        val resetIndex =
            TopLevelStackKey.entries.indexOfFirst { it.initialDestination == destination }
        if (resetIndex > 0) {
            topLevelStackIndex = resetIndex
        } else {
            twoWayStacks[topLevelStackIndex].navigate(destination)
        }
        updateInfo()
    }

    fun navigateOnBrowserPop(destination: NavKey) {
        val resetIndex = twoWayStacks.indexOfFirst {
            it.navBackStack.find { it == destination } != null
        }
        if (resetIndex > 0) {
            twoWayStacks[resetIndex].navigateOnBrowserPop(destination)
            topLevelStackIndex = resetIndex
        } else {
            twoWayStacks[topLevelStackIndex].navigateOnBrowserPop(destination)
        }
        updateInfo()
    }

    private fun updateInfo() {
        val backInfo = mutableListOf<NavigationRoute>()
        val twoWayStack = twoWayStacks[topLevelStackIndex]
        val navBackStack = twoWayStack.navBackStack
        val navForwardStack = twoWayStack.navForwardStack
        navBackStack.dropLast(1).forEach {
            backInfo += NavigationRoute(
                AlleyEditDestination.toEncodedRoute(it as AlleyEditDestination)
                    ?: backInfo.lastOrNull()?.route.orEmpty()
            )
        }

        val currentInfo =
            AlleyEditDestination.toEncodedRoute(navBackStack.last() as AlleyEditDestination)
                ?.let(::NavigationRoute)
                ?: backInfo.lastOrNull()
                ?: NavigationRoute("")

        val forwardInfo = mutableListOf<NavigationRoute>()
        navForwardStack.fastForEachReversed {
            forwardInfo += AlleyEditDestination.toEncodedRoute(it as AlleyEditDestination)
                ?.let(::NavigationRoute)
                ?: forwardInfo.lastOrNull()
                        ?: currentInfo
        }

        routeHistory.value = NavigationRouteHistory(currentInfo, backInfo, forwardInfo)
        setInfo(currentInfo = currentInfo, backInfo = backInfo, forwardInfo = forwardInfo)
    }

    fun onBack() = onBackCompleted()
    fun onForward() = onForwardCompleted()

    override fun onBackCompleted() {
        if (twoWayStacks[topLevelStackIndex].onBack()) {
            updateInfo()
        } else if (topLevelKeysStack.size > 1) {
            topLevelKeysStack.removeLast()
            topLevelStackIndex = topLevelKeysStack.last()
            updateInfo()
        }
    }

    override fun onForwardCompleted() {
        if (twoWayStacks[topLevelStackIndex].onForward()) {
            updateInfo()
        }
    }
}
