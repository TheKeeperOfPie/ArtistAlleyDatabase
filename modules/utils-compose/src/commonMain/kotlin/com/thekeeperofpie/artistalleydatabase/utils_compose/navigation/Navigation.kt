package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.serialization.decodeArguments
import androidx.navigation.serialization.generateNavArguments
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

class NavigationTypeMap(val typeMap: Map<KType, NavType<*>>)

inline fun <reified T : Any> SavedStateHandle.toDestination(typeMap: NavigationTypeMap) = toRoute<T>(typeMap.typeMap)

inline fun <reified T : Any> SavedStateHandle.toRoute(
    typeMap: Map<KType, NavType<*>> = emptyMap()
): T = internalToRoute(T::class, typeMap)

@OptIn(InternalSerializationApi::class)
fun <T : Any> SavedStateHandle.internalToRoute(
    route: KClass<T>,
    typeMap: Map<KType, NavType<*>>
): T {
    val map: MutableMap<String, NavType<*>> = mutableMapOf()
    val serializer = route.serializer()
    serializer.generateNavArguments(typeMap).onEach { map[it.name] = it.argument.type }
    return serializer.decodeArguments(this, map)
}

inline fun <reified T : NavDestination> NavGraphBuilder.sharedElementComposable(
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
