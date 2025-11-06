package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
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
) {
    fun navigate(destination: AlleyEditDestination) {
        if (destination == navForwardStack.lastOrNull()) {
            onForward()
        } else {
            navForwardStack.clear()
            navBackStack += destination
        }
    }

    fun onBack() {
        if (navBackStack.size > 1) {
            navForwardStack += navBackStack.removeLast()
        }
    }

    fun onForward() {
        if (navForwardStack.isNotEmpty()) {
            navBackStack += navForwardStack.removeLast()
        }
    }

    val sceneStrategy = SceneStrategy<NavKey> { entries ->
        val (visible, invisible) = entries.partition { entry -> navBackStack.any { entry.contentKey == it.toString() } }
        if (visible.isEmpty()) return@SceneStrategy null
        SinglePaneScene(
            key = visible.last().contentKey,
            entry = visible.last(),
            previousEntries = visible.dropLast(1),
        )
    }

    /** Copy of Compose class since it's marked internal */
    private data class SinglePaneScene<T : Any>(
        override val key: Any,
        val entry: NavEntry<T>,
        override val previousEntries: List<NavEntry<T>>,
    ) : Scene<T> {
        override val entries: List<NavEntry<T>> = listOf(entry)

        override val content: @Composable () -> Unit = { entry.Content() }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as SinglePaneScene<*>

            return key == other.key &&
                    entry == other.entry &&
                    previousEntries == other.previousEntries &&
                    entries == other.entries
        }

        override fun hashCode(): Int {
            return key.hashCode() * 31 +
                    entry.hashCode() * 31 +
                    previousEntries.hashCode() * 31 +
                    entries.hashCode() * 31
        }

        override fun toString(): String {
            return "SinglePaneScene(key=$key, entry=$entry, previousEntries=$previousEntries, entries=$entries)"
        }
    }
}
