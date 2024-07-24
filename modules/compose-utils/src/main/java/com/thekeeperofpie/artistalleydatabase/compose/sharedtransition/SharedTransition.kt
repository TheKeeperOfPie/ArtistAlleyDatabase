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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.composed
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mxalbert.sharedelements.DefaultSharedElementsTransitionSpec
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsTransitionSpec
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
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
    CompositionLocalProvider(
        LocalSharedTransitionPrefixKeys provides "$currentPrefix-${prefixKeys.joinToString(separator = "-")}",
    ) {
        content()
    }
}

object SharedTransitionSignal {
    var navigating by mutableStateOf(false)
}

@Composable
fun AutoSharedElement(
    key: String,
    screenKey: String,
    transitionSpec: SharedElementsTransitionSpec = DefaultSharedElementsTransitionSpec,
    onFractionChanged: ((Float) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (true) {
        content()
        return
    }
    if (SharedTransition.USE_ANDROIDX) {
        CompositionLocalProvider(
            LocalSharedTransitionKeys provides (key to screenKey)
        ) {
            Box(modifier = Modifier.autoSharedElement(key)) {
                content()
            }
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
fun Modifier.autoSharedElement(key: String? = null) =
    if (SharedTransitionSignal.navigating && key?.contains("media") == true) {
        composed {
            val (localKey, screenKey) = LocalSharedTransitionKeys.current

            // Freezes with duplicate keys, works if given UUID.randomUuid().toString()
            val sharedElementKey = key ?: localKey
            with(LocalSharedTransitionScope.current) {
                sharedElement(
                    rememberSharedContentState(key = sharedElementKey),
                    LocalAnimatedVisibilityScope.current,
                )
            }
        }
    } else this

@Composable
fun Modifier.sharedElement(
    key: SharedTransitionKey?,
    identifier: String,
    zIndexInOverlay: Float = 0f,
): Modifier {
    if (key?.key.isNullOrEmpty()) return this
    return sharedElement(keys = arrayOf(key, identifier), zIndexInOverlay = zIndexInOverlay)
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
fun Modifier.sharedElement(vararg keys: Any?, zIndexInOverlay: Float = 0f): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    return with(LocalSharedTransitionScope.current) {
        sharedElement(
            rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun Modifier.sharedBounds(vararg keys: Any?, zIndexInOverlay: Float = 0f): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    // TODO: sharedBounds causes bugs with scrolling?
    return with(LocalSharedTransitionScope.current) {
        sharedBounds(
            rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
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
            renderInOverlay = { sharedContentState.isMatchFound },
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

object SharedTransition {
    const val USE_ANDROIDX = false
}
