@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.util.fastAny
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mxalbert.sharedelements.DefaultSharedElementsTransitionSpec
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsRoot
import com.mxalbert.sharedelements.SharedElementsTransitionSpec
import com.thekeeperofpie.artistalleydatabase.compose.sharedelement.androidx.SharedTransitionLayout
import com.thekeeperofpie.artistalleydatabase.compose.sharedelement.androidx.SharedTransitionScope

private val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    throw IllegalStateException("SharedTransitionScope not provided")
}

private val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope> {
    throw IllegalStateException("AnimatedVisibilityScope not provided")
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun AutoSharedElementsRoot(content: @Composable () -> Unit) {
    if (SharedTransition.USE_ANDROIDX) {
        SharedTransitionLayout(
            keyToId = { (it as? SharedElementKey)?.id ?: it },
            matcher = { states ->
                states/*
                    .fastDistinctBy { (it.sharedElement.key as? SharedElementKey)?.screenKey ?: it }
                    */.size > 1
                        && states.fastAny { it.boundsAnimation.target }
            }
        ) {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
            ) {
                content()
            }
        }
    } else {
        SharedElementsRoot {
            content()
        }
    }
}

@Composable
fun AutoSharedElement(
    key: String,
    screenKey: String,
    transitionSpec: SharedElementsTransitionSpec = DefaultSharedElementsTransitionSpec,
    onFractionChanged: ((Float) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (SharedTransition.USE_ANDROIDX) {
        Box(modifier = Modifier.autoSharedElement(key, screenKey)) {
            content()
        }
    } else {
        SharedElement(
            key = key,
            screenKey = screenKey,
            transitionSpec = transitionSpec,
            onFractionChanged = onFractionChanged,
            content = content,
        )
    }
}

@Composable
fun Modifier.autoSharedElement(key: String, screenKey: String): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    if (key != "anime_media_136804_image") return this
    return composed {
        with(sharedTransitionScope) {
            sharedElement(
                rememberSharedContentState(key = SharedElementKey(key, screenKey)),
                animatedVisibilityScope,
            )
        }
    }
}

fun NavGraphBuilder.sharedElementComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        enterTransition,
    popExitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        exitTransition,
    sizeTransform: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform,
    ) {
        CompositionLocalProvider(
            LocalAnimatedVisibilityScope provides this,
        ) {
            content(it)
        }
    }
}

data class SharedElementKey(
    val id: String,
    val screenKey: String,
)

object SharedTransition {
    const val USE_ANDROIDX = true
}
