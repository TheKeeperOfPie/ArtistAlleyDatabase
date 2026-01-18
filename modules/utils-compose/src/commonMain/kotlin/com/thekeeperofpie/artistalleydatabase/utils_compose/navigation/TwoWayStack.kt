package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration

@Composable
fun rememberTwoWayStack(
    initialDestination: NavKey,
    savedStateConfiguration: SavedStateConfiguration,
): TwoWayStack {
    val backStack = rememberNavBackStack(
        savedStateConfiguration,
        initialDestination,
    )
    val forwardStack = rememberNavBackStack(savedStateConfiguration)
    return remember(backStack, forwardStack) {
        TwoWayStack(backStack, forwardStack)
    }
}

@Composable
fun rememberDecoratedNavEntries(
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
class TwoWayStack internal constructor(
    val navBackStack: NavBackStack<NavKey>,
    val navForwardStack: NavBackStack<NavKey>,
) {
    fun navigate(destination: NavKey) {
        if (destination == navForwardStack.lastOrNull()) {
            onForward()
        } else {
            navForwardStack.clear()
            navBackStack += destination
        }
    }

    fun navigateOnBrowserPop(destination: NavKey) {
        if (destination == navForwardStack.lastOrNull()) {
            onForward()
        } else {
            Snapshot.withMutableSnapshot {
                val lastIndex = navBackStack.lastIndexOf(destination)
                if (lastIndex >= 0) {
                    if (lastIndex + 1 <= navBackStack.lastIndex) {
                        navForwardStack.clear()
                    }
                    while (lastIndex + 1 <= navBackStack.lastIndex) {
                        navForwardStack += navBackStack.removeAt(lastIndex + 1)
                    }
                } else {
                    navigate(destination)
                }
            }
        }
    }

    fun onBack(): Boolean {
        if (navBackStack.size > 1) {
            navForwardStack += navBackStack.removeLast()
            return true
        }
        return false
    }

    fun onForward(): Boolean {
        if (navForwardStack.isNotEmpty()) {
            navBackStack += navForwardStack.removeLast()
            return true
        }
        return false
    }
}
