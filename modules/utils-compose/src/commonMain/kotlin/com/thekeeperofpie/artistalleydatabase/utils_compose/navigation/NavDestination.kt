package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.navigation.NavGraphBuilder

/** Marker interface for all type safe navigation routes */
interface NavDestination

interface NavDestinationProvider {
    companion object {
        operator fun invoke(
            destination: NavGraphBuilder.(NavigationTypeMap) -> Unit,
        ): NavDestinationProvider = object : NavDestinationProvider {
            override fun composable(
                navGraphBuilder: NavGraphBuilder,
                navigationTypeMap: NavigationTypeMap,
            ) = navGraphBuilder.destination(navigationTypeMap)
        }
    }

    fun composable(navGraphBuilder: NavGraphBuilder, navigationTypeMap: NavigationTypeMap)
}
