package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val LocalNavigationController =
    staticCompositionLocalOf<NavigationController> { throw IllegalStateException("No NavigationController provided") }

@Composable
fun rememberNavigationController(navHostController: NavHostController) =
    remember(navHostController) { NavigationController(navHostController) }

class NavigationController(private val navHostController: NavHostController) {

    private var lastNavDestination: Any? = null
    private var lastNavTime = Instant.DISTANT_PAST

    fun navigateUp() = navHostController.navigateUp()

    fun navigate(navDestination: NavDestination) {
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
}
