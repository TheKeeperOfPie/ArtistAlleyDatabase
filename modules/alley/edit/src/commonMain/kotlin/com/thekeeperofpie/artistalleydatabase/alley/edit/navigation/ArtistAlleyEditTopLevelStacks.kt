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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination

@Composable
fun rememberArtistAlleyEditTopLevelStacks(): ArtistAlleyEditTopLevelStacks {
    val stacks = TopLevelStackKey.entries.map {
        key(it) {
            rememberTwoWayStack(it.initialDestination)
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

@Composable
private fun rememberDecoratedNavEntries(
    twoWayStack: TwoWayStack,
    entryProvider: (key: NavKey) -> NavEntry<NavKey>,
) = (twoWayStack.navBackStack + twoWayStack.navForwardStack)
    .flatMap {
        key(it.toString()) {
            rememberDecoratedNavEntries(
                backStack = listOf(it),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider,
            )
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

    fun navigate(destination: AlleyEditDestination) {
        val resetIndex =
            TopLevelStackKey.entries.indexOfFirst { it.initialDestination == destination }
        if (resetIndex > 0) {
            topLevelStackIndex = resetIndex
        } else {
            twoWayStacks[topLevelStackIndex].navigate(destination)
        }
        updateInfo()
    }

    fun navigateOnBrowserBack(destination: AlleyEditDestination) {
        val resetIndex = twoWayStacks.indexOfFirst {
            it.navBackStack.find { it == destination } != null
        }
        if (resetIndex > 0) {
            twoWayStacks[resetIndex].navigate(destination)
            topLevelStackIndex = resetIndex
        } else {
            twoWayStacks[topLevelStackIndex].navigate(destination)
        }
        updateInfo()
    }

    private fun updateInfo() {
        val backInfo = mutableListOf<Route>()
        twoWayStacks.dropLast(1).forEach {
            (it.navBackStack + it.navForwardStack).forEach {
                backInfo += Route(
                    AlleyEditDestination.toEncodedRoute(it as AlleyEditDestination)
                        ?: backInfo.lastOrNull()?.route.orEmpty()
                )
            }
        }
        val twoWayStack = twoWayStacks[topLevelStackIndex]
        val navBackStack = twoWayStack.navBackStack
        val navForwardStack = twoWayStack.navForwardStack
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

    data class Route(val route: String) : NavigationEventInfo()
}
