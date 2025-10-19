@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    throw IllegalStateException("SharedTransitionScope not provided")
}

val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope> {
    throw IllegalStateException("AnimatedVisibilityScope not provided")
}

fun NavGraphBuilder.sharedElementComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    content: @Composable (NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
) {
    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
        content(it)
    }
}

@Composable
private fun Modifier.withSharedTransitionScopeOrDoNothingInPreview(
    block: @Composable SharedTransitionScope.() -> Modifier
) = if (LocalInspectionMode.current) this else with(LocalSharedTransitionScope.current) { block() }

@Composable
fun Modifier.sharedElement(
    key: SharedTransitionKey?,
    identifier: String,
    zIndexInOverlay: Float = 0f,
): Modifier {
    if (key?.key.isNullOrEmpty()) return this
    return withSharedTransitionScopeOrDoNothingInPreview {
        sharedElement(
            sharedContentState = rememberSharedContentState(listOf(key, identifier)),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun Modifier.sharedBounds(
    key: SharedTransitionKey?,
    identifier: String,
    zIndexInOverlay: Float = 0f,
): Modifier {
    if (key?.key.isNullOrEmpty()) return this
    // TODO: sharedBounds broken
    if (true) return this
    return withSharedTransitionScopeOrDoNothingInPreview {
        sharedBounds(
            sharedContentState = rememberSharedContentState(arrayOf(key, identifier)),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun Modifier.sharedElement(
    state: SharedTransitionScope.SharedContentState?,
    zIndexInOverlay: Float = 0f,
): Modifier {
    state ?: return this
    return withSharedTransitionScopeOrDoNothingInPreview {
        sharedElement(
            sharedContentState = state,
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun rememberSharedContentState(
    sharedTransitionKey: SharedTransitionKey?,
    identifier: String,
): SharedTransitionScope.SharedContentState? {
    sharedTransitionKey ?: return null
    return if (LocalInspectionMode.current) {
        null
    } else {
        with(LocalSharedTransitionScope.current) {
            rememberSharedContentState(key = listOf(sharedTransitionKey, identifier))
        }
    }
}

@Composable
fun Modifier.skipToLookaheadSize() = withSharedTransitionScopeOrDoNothingInPreview {
    skipToLookaheadSize()
}

@Composable
fun Modifier.renderMaybeInSharedTransitionScopeOverlay(
    zIndexInOverlay: Float = 0f,
    renderInOverlay: (() -> Boolean)? = null,
): Modifier = withSharedTransitionScopeOrDoNothingInPreview {
    renderInSharedTransitionScopeOverlay(
        renderInOverlay = renderInOverlay ?: { isTransitionActive },
        zIndexInOverlay = zIndexInOverlay,
    )
}

@Composable
fun Modifier.animateSharedTransitionWithOtherState(
    sharedContentState: SharedTransitionScope.SharedContentState?,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    zIndexInOverlay: Float = 1f,
    disableAnimateEnterExit: Boolean = false,
): Modifier {
    sharedContentState ?: return this
    return withSharedTransitionScopeOrDoNothingInPreview {
        renderInSharedTransitionScopeOverlay(
            renderInOverlay = { isTransitionActive && sharedContentState.isMatchFound },
            zIndexInOverlay = zIndexInOverlay,
        )
            .conditionally(!disableAnimateEnterExit && sharedContentState.isMatchFound) {
                animateEnterExit(enter = enter, exit = exit)
            }
    }
}

@Composable
fun Modifier.animateEnterExit(
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
) = if (LocalInspectionMode.current) this else with(LocalAnimatedVisibilityScope.current) {
    animateEnterExit(enter, exit)
}
