package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
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
}
