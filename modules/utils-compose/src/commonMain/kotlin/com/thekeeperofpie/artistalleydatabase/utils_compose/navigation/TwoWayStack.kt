package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.toString
import androidx.compose.ui.util.fastForEachReversed
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
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.PolymorphicSerializer
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

@Composable
inline fun <reified T : NavKey> rememberTwoWayStack(
    vararg initialDestinations: T,
    savedStateConfiguration: SavedStateConfiguration,
    noinline encode: (T) -> String,
): TwoWayStack<T> = viewModel(key = currentCompositeKeyHashCode.toString(36)) {
    TwoWayStackViewModelHolder(
        navKeyClass = T::class,
        initialDestinations = initialDestinations,
        savedStateConfiguration = savedStateConfiguration,
        savedStateHandle = createSavedStateHandle(),
        encode = encode,
    )
}.twoWayStack

@Composable
fun <T : NavKey> rememberDecoratedNavEntries(
    twoWayStack: TwoWayStack<T>,
    entryProvider: (key: T) -> NavEntry<T>,
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
class TwoWayStack<T : NavKey>(
    val navBackStack: NavBackStack<T>,
    val navForwardStack: NavBackStack<T>,
    val encode: (T) -> String,
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

    init {
        updateInfo()
    }

    fun restore(
        back: List<T>,
        forward: List<T>,
    ): Unit = Snapshot.withMutableSnapshot {
        navBackStack.clear()
        navBackStack.addAll(back)
        navForwardStack.clear()
        navForwardStack.addAll(forward)
        updateInfo()
    }

    fun navigate(destination: T) = Snapshot.withMutableSnapshot {
        if (destination == navForwardStack.lastOrNull()) {
            onForward()
        } else {
            navForwardStack.clear()
            navBackStack += destination
        }
        updateInfo()
    }

    fun navigateOnBrowserPop(destination: T, toRoute: (NavKey) -> String?) =
        Snapshot.withMutableSnapshot {
            if (destination == navForwardStack.lastOrNull()) {
                onForward()
            } else {
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

    fun calculateBackStack(navEntries: List<NavEntry<T>>) =
        navEntries.take(navBackStack.size)

    fun onBack(): Boolean = Snapshot.withMutableSnapshot {
        val canGoBack = navBackStack.size > 1
        if (canGoBack) navForwardStack += navBackStack.removeLast()
        updateInfo()
        return canGoBack
    }

    fun onForward(): Boolean = Snapshot.withMutableSnapshot {
        val canGoForward = navForwardStack.isNotEmpty()
        if (canGoForward) navBackStack += navForwardStack.removeLast()
        updateInfo()
        return canGoForward
    }

    override fun onBackCompleted() {
        onBack()
    }

    override fun onForwardCompleted() {
        onForward()
    }

    fun navigateBy(target: Int) {
        when {
            target < 0 -> repeat(target.absoluteValue) { onBack() }
            target > 0 -> repeat(target) { onForward() }
        }
    }

    private fun updateInfo() {
        val backInfo = mutableListOf<NavigationRoute>()
        navBackStack.dropLast(1).forEach {
            backInfo += NavigationRoute(encode(it))
        }

        val currentInfo =
            NavigationRoute(encode(navBackStack.last()))

        val forwardInfo = mutableListOf<NavigationRoute>()
        navForwardStack.fastForEachReversed {
            forwardInfo += NavigationRoute(encode(it))
        }

        routeHistory.value = NavigationRouteHistory(currentInfo, backInfo, forwardInfo)
        setInfo(currentInfo = currentInfo, backInfo = backInfo, forwardInfo = forwardInfo)
    }
}

class TwoWayStackViewModelHolder<T : NavKey>(
    navKeyClass: KClass<T>,
    initialDestinations: Array<out T>,
    savedStateConfiguration: SavedStateConfiguration,
    savedStateHandle: SavedStateHandle,
    encode: (T) -> String,
) : ViewModel() {
    private val serializer = NavBackStackSerializer(PolymorphicSerializer(navKeyClass))
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

    val twoWayStack = TwoWayStack(
        navBackStack = backStack,
        navForwardStack = frontStack,
        encode = encode,
    )
}
