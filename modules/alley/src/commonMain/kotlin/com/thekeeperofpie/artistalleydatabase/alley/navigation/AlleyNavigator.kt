package com.thekeeperofpie.artistalleydatabase.alley.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.platform.LocalInspectionMode
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination

val LocalAlleyNavigator = compositionLocalWithComputedDefaultOf<AlleyNavigator> {
    if (LocalInspectionMode.currentValue) {
        NoOpNavigator
    } else {
        throw IllegalStateException("No AlleyNavigator provided")
    }
}

interface AlleyNavigator {
    fun goBack()
    fun goUp()
    fun navigate(destination: AlleyDestination)
}

private object NoOpNavigator : AlleyNavigator {
    override fun goBack() {}
    override fun goUp() {}
    override fun navigate(destination: AlleyDestination) {}
}

private class AlleyNavigatorImpl(private val navStack: AlleyNavStack) : AlleyNavigator {
    // TODO: Handle up vs back
    override fun goBack() {
        navStack.onBack()
    }

    override fun goUp() {
        navStack.onBack()
    }

    override fun navigate(destination: AlleyDestination) = navStack.navigate(destination)
}

@Composable
fun rememberAlleyNavigator(navStack: AlleyNavStack): AlleyNavigator =
    retain(navStack) { AlleyNavigatorImpl(navStack) }
