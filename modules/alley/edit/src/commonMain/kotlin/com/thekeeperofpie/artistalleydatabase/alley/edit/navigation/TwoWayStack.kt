package com.thekeeperofpie.artistalleydatabase.alley.edit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigationevent.NavigationEventInfo
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
internal fun rememberTwoWayStack(initialDestination: AlleyEditDestination): TwoWayStack {
    val backStack = rememberNavBackStack(
        SavedStateConfig,
        initialDestination,
    )
    val forwardStack = rememberNavBackStack(SavedStateConfig)
    return remember(backStack, forwardStack) {
        TwoWayStack(backStack, forwardStack)
    }
}

private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyEditDestination.Home.serializer())
            subclass(serializer = AlleyEditDestination.ArtistAdd.serializer())
            subclass(serializer = AlleyEditDestination.ArtistEdit.serializer())
            subclass(serializer = AlleyEditDestination.ArtistForm.serializer())
            subclass(serializer = AlleyEditDestination.ArtistFormQueue.serializer())
            subclass(serializer = AlleyEditDestination.ArtistHistory.serializer())
            subclass(serializer = AlleyEditDestination.ImagesEdit.serializer())
            subclass(serializer = AlleyEditDestination.Series.serializer())
            subclass(serializer = AlleyEditDestination.SeriesAdd.serializer())
            subclass(serializer = AlleyEditDestination.SeriesEdit.serializer())
            subclass(serializer = AlleyEditDestination.Merch.serializer())
            subclass(serializer = AlleyEditDestination.MerchAdd.serializer())
            subclass(serializer = AlleyEditDestination.MerchEdit.serializer())
        }
    }
}

@Stable
internal class TwoWayStack internal constructor(
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

    private data class Route(val route: String) : NavigationEventInfo()
}
