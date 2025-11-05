package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyEditDestination.Home.serializer())
            subclass(serializer = AlleyEditDestination.ArtistDetails.serializer())
        }
    }
}

@Stable
class ArtistAlleyEditTwoWayStack internal constructor(
    private val data: Data,
    internal val saveableStateHolder: SaveableStateHolder,
) {
    val navBackStack get() = data.navBackStack
    val forwardNavStack get() = data.forwardNavStack
    val forwardStateKeys get() = data.forwardStateKeys

    fun navigate(destination: AlleyEditDestination) {
        if (destination == forwardNavStack.lastOrNull()) {
            onForward()
        } else {
            forwardNavStack.clear()
            forwardStateKeys.forEach {
                saveableStateHolder.removeState(it)
            }
            forwardStateKeys.clear()
            navBackStack += destination
        }
    }

    fun onBack() {
        if (navBackStack.size > 1) {
            forwardNavStack += navBackStack.removeLast()
        }
    }

    fun onForward() {
        if (forwardNavStack.isNotEmpty()) {
            navBackStack += forwardNavStack.removeLast()
        }
    }

    internal class Data(
        internal val navBackStack: NavBackStack<NavKey> = NavBackStack(AlleyEditDestination.Home),
        internal val forwardNavStack: NavBackStack<NavKey> = NavBackStack(),
        internal val forwardStateKeys: SnapshotStateList<Any> = SnapshotStateList(),
    ) {
        object Saver : ComposeSaver<Data, Any> {
            private val serializer = NavBackStackSerializer(PolymorphicSerializer(NavKey::class))
            private val backStackSaver: ComposeSaver<NavBackStack<NavKey>, SavedState> = Saver(
                save = { encodeToSavedState(serializer, it, savedStateConfig) },
                restore = { decodeFromSavedState(serializer, it, savedStateConfig) },
            )

            override fun SaverScope.save(value: Data) = listOf(
                with(backStackSaver) { save(value.navBackStack) },
                with(backStackSaver) { save(value.forwardNavStack) },
                value.forwardStateKeys,
            )

            override fun restore(value: Any): Data {
                val (navBackStack, forwardStack, forwardStateKeyStack) = value as List<*>
                @Suppress("UNCHECKED_CAST")
                return Data(
                    navBackStack = with(backStackSaver) { restore(navBackStack as SavedState) }!!,
                    forwardNavStack = with(backStackSaver) { restore(forwardStack as SavedState) }!!,
                    forwardStateKeys = (forwardStateKeyStack as List<Any>).toMutableStateList(),
                )
            }
        }
    }
}

@Composable
fun rememberArtistAlleyEditTwoWayStack(): ArtistAlleyEditTwoWayStack {
    val data =
        rememberSaveable(ArtistAlleyEditTwoWayStack.Data.Saver) { ArtistAlleyEditTwoWayStack.Data() }
    val saveableStateHolder = rememberSaveableStateHolder()
    return remember(data, saveableStateHolder) {
        ArtistAlleyEditTwoWayStack(data, saveableStateHolder)
    }
}

@Composable
internal fun <T : Any> rememberTwoWaySaveableStateHolder(
    twoWayStack: ArtistAlleyEditTwoWayStack,
) = remember(twoWayStack) {
    NavEntryDecorator<T>(
        onPop = { twoWayStack.forwardStateKeys += it },
        decorate = {
            twoWayStack.saveableStateHolder.SaveableStateProvider(it.contentKey) { it.Content() }
        },
    )
}
