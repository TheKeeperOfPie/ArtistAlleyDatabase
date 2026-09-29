package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer

@Composable
fun rememberTwoWayStack(
    vararg initialDestinations: NavKey,
    savedStateConfiguration: SavedStateConfiguration,
): TwoWayStack = viewModel {
    ViewModelHolder(
        initialDestinations = initialDestinations,
        savedStateConfiguration = savedStateConfiguration,
        savedStateHandle = createSavedStateHandle(),
    )
}.twoWayStack

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
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberRetainedValuesStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
    }

@Composable
private fun <T : Any> rememberRetainedValuesStoreNavEntryDecorator(
    registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry(),
): RetainedValuesStoreNavEntryDecorator<T> {
    return remember(registry) {
        RetainedValuesStoreNavEntryDecorator(registry)
    }
}

private class RetainedValuesStoreNavEntryDecorator<T : Any>(
    registry: RetainedValuesStoreRegistry,
) : NavEntryDecorator<T>(
    onPop = { key ->
        registry.clearChild(key)
    },
    decorate = { entry ->
        registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
    },
)

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

    fun <T : NavKey> navigateOnBrowserPop(destination: T, toRoute: (NavKey) -> String?) {
        if (destination == navForwardStack.lastOrNull()) {
            onForward()
        } else {
            Snapshot.withMutableSnapshot {
                val lastIndex = navBackStack.map(toRoute).lastIndexOf(toRoute(destination))
                if (lastIndex >= 0) {
                    repeat(navBackStack.lastIndex - lastIndex) {
                        navForwardStack += navBackStack.removeLast()
                    }
                } else {
                    val forwardLastIndex = navForwardStack.map(toRoute)
                        .lastIndexOf(toRoute(destination))
                    if (forwardLastIndex >= 0) {
                        repeat(navForwardStack.lastIndex - lastIndex) {
                            navBackStack += navForwardStack.removeLast()
                        }
                    } else {
                        navigate(destination)
                    }
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

private class ViewModelHolder(
    initialDestinations: Array<out NavKey>,
    savedStateConfiguration: SavedStateConfiguration,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val serializer = NavBackStackSerializer(PolymorphicSerializer(NavKey::class))
    val backStack by savedStateHandle.saved(
        serializer = serializer,
        configuration = savedStateConfiguration,
    ) {
        NavBackStack(*initialDestinations)
    }

    val frontStack by savedStateHandle.saved(
        serializer = serializer,
        configuration = savedStateConfiguration,
    ) {
        NavBackStack()
    }

    val twoWayStack = TwoWayStack(navBackStack = backStack, navForwardStack = frontStack)
}
