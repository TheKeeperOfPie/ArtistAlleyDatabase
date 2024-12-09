package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavigationController = staticCompositionLocalOf<NavigationController> { throw IllegalStateException("No NavigationController provided")}

@Composable
fun rememberNavigationController(navHostController: NavHostController) =
    remember(navHostController) { NavigationController(navHostController) }

class NavigationController(private val navHostController: NavHostController) {

    fun navigateUp() = navHostController.navigateUp()

    fun navigate(navDestination: NavDestination) = navHostController.navigate(navDestination)
}
