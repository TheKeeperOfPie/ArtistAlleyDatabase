package com.thekeeperofpie.artistalleydatabase.compose.sharedtransition

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope

inline fun <reified T : Any> NavGraphBuilder.sharedElementComposable(
    navigationTypeMap: NavigationTypeMap,
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up
        )
    },
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Down
        )
    },
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) = composable<T>(
    typeMap = navigationTypeMap.typeMap,
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
) {
    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
        content(it)
    }
}
