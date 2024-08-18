@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.compose.sharedtransition

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    throw IllegalStateException("SharedTransitionScope not provided")
}

val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope> {
    throw IllegalStateException("AnimatedVisibilityScope not provided")
}

private val LocalSharedTransitionKeys = compositionLocalOf<Pair<String, String>> {
    throw IllegalStateException("SharedTransition keys not provided")
}

val LocalSharedTransitionPrefixKeys = staticCompositionLocalOf<String> { "" }

@Composable
fun <T> T.SharedTransitionKeyScope(vararg prefixKeys: String?, content: @Composable T.() -> Unit) {
    val currentPrefix = LocalSharedTransitionPrefixKeys.current
    val suffix = if (prefixKeys.isEmpty()) "" else "-${prefixKeys.joinToString(separator = "-")}"
    val scopeKey = "$currentPrefix$suffix"
    if (scopeKey.isEmpty()) {
        content()
    } else {
        CompositionLocalProvider(LocalSharedTransitionPrefixKeys provides scopeKey) {
            content()
        }
    }
}

@Composable
fun SharedTransitionKeyScope(vararg prefixKeys: String?, content: @Composable () -> Unit) =
    Unit.SharedTransitionKeyScope(*prefixKeys) { content() }

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
        // This is a hack to avoid a navigation bug that occurs if the user
        // tries to navigate back before a shared element transition finishes
        val startTime = remember { System.currentTimeMillis() }
        var blockBackEvents by remember { mutableStateOf(true) }
        BackHandler(blockBackEvents) {
            if ((System.currentTimeMillis() - startTime) > 400) {
                blockBackEvents = false
            }
        }
        LaunchedEffect(Unit) {
            delay(400.milliseconds)
            blockBackEvents = false
        }
        content(it)
    }
}

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
        // This is a hack to avoid a navigation bug that occurs if the user
        // tries to navigate back before a shared element transition finishes
        val startTime = remember { System.currentTimeMillis() }
        var blockBackEvents by remember { mutableStateOf(true) }
        BackHandler(blockBackEvents) {
            if ((System.currentTimeMillis() - startTime) > 400) {
                blockBackEvents = false
            }
        }
        LaunchedEffect(Unit) {
            delay(400.milliseconds)
            blockBackEvents = false
        }
        content(it)
    }
}

@Composable
fun Modifier.sharedElement(
    key: SharedTransitionKey?,
    identifier: String,
    zIndexInOverlay: Float = 0f,
): Modifier {
    if (key?.key.isNullOrEmpty()) return this
    return with(LocalSharedTransitionScope.current) {
        sharedElement(
            state = rememberSharedContentState(listOf(key, identifier)),
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
    return with(LocalSharedTransitionScope.current) {
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
    return with(LocalSharedTransitionScope.current) {
        sharedElement(
            state = state,
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
    return with(LocalSharedTransitionScope.current) {
        rememberSharedContentState(key = listOf(sharedTransitionKey, identifier))
    }
}

@Composable
fun Modifier.skipToLookaheadSize() = with(LocalSharedTransitionScope.current) {
    skipToLookaheadSize()
}

@Composable
fun Modifier.renderInSharedTransitionScopeOverlay(
    zIndexInOverlay: Float = 0f,
    renderInOverlay: (() -> Boolean)? = null,
) = with(LocalSharedTransitionScope.current) {
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
    return with(LocalSharedTransitionScope.current) {
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
) = with(LocalAnimatedVisibilityScope.current) {
    animateEnterExit(enter, exit)
}
