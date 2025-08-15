package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.navigation.NavHostController
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

val LocalNavigationController = compositionLocalWithComputedDefaultOf<NavigationController> {
    if (LocalInspectionMode.currentValue) {
        object : NavigationController{
            override fun navigateUp() = false
            override fun navigate(navDestination: NavDestination) = Unit
            override fun popBackStack() = false
            override fun popBackStack(navDestination: NavDestination) = false
        }
    } else {
        throw IllegalStateException("No NavigationController provided")
    }
}

@Composable
fun rememberNavigationController(navHostController: NavHostController): NavigationController =
    remember(navHostController) { NavigationControllerImpl(navHostController) }

interface NavigationController {
    fun navigateUp(): Boolean
    fun navigate(navDestination: NavDestination)
    fun popBackStack(): Boolean
    fun popBackStack(navDestination: NavDestination): Boolean
}

class NavigationControllerImpl(
    internal val navHostController: NavHostController,
) : NavigationController {

    private var lastNavDestination: Any? = null
    private var lastNavTime = Instant.DISTANT_PAST

    override fun navigateUp() = navHostController.navigateUp()

    override fun navigate(navDestination: NavDestination) {
        val navTime = Clock.System.now()
        val timeDifference = (navTime - lastNavTime).absoluteValue
        val blockNavigation = if (lastNavDestination == navDestination) {
            // If navigating to the exact same destination, assume it's
            // a double click error and block for significantly longer.
            timeDifference < 1.seconds
        } else {
            timeDifference < 300.milliseconds
        }
        if (blockNavigation) return

        lastNavDestination = navDestination
        lastNavTime = navTime
        navHostController.navigate(navDestination)
    }

    override fun popBackStack() = navigateBack()

    override fun popBackStack(navDestination: NavDestination) =
        navHostController.popBackStack(route = navDestination, inclusive = false)
}

internal expect fun NavigationControllerImpl.navigateBack(): Boolean
