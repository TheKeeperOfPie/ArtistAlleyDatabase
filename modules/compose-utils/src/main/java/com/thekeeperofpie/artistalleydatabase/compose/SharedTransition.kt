@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import com.mxalbert.sharedelements.DefaultSharedElementsTransitionSpec
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsRoot
import com.mxalbert.sharedelements.SharedElementsTransitionSpec

private val LocalSharedTransitionScope =
    staticCompositionLocalOf<Pair<SharedTransitionScope, AnimatedVisibilityScope>> {
        throw IllegalStateException("SharedTransitionScope not provided")
    }

private val LocalSharedTransitionKeys = compositionLocalOf<Pair<String, String>> {
    throw IllegalStateException("SharedTransition keys not provided")
}

object SharedTransitionSignal {
    var navigating by mutableStateOf(false)
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun AutoSharedElementsRoot(content: @Composable () -> Unit) {
    if (SharedTransition.USE_ANDROIDX) {
        AnimatedVisibility(visible = true) {
            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides (this@SharedTransitionLayout to this@AnimatedVisibility),
                ) {
                    content()
                }
                val isTransitionActive = this.isTransitionActive
                val navigating = SharedTransitionSignal.navigating
                var disableOnNext by remember { mutableStateOf(false) }
                LaunchedEffect(isTransitionActive, navigating) {
                    if (navigating && isTransitionActive) {
                        disableOnNext = true
                    }

                    if (!isTransitionActive && disableOnNext) {
                        disableOnNext = false
                        SharedTransitionSignal.navigating = false
                    }
                }
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

@Composable
fun Modifier.autoSharedElement(key: String? = null) = if (SharedTransitionSignal.navigating && key?.contains("media") == true) {
    composed {
        val (sharedTransitionScope, animatedVisibilityScope) = LocalSharedTransitionScope.current
        val (localKey, screenKey) = LocalSharedTransitionKeys.current

        // Freezes with duplicate keys, works if given UUID.randomUuid().toString()
        val sharedElementKey = key ?: localKey
        with(sharedTransitionScope) {
            sharedElement(
                rememberSharedContentState(key = sharedElementKey),
                animatedVisibilityScope,
            )
        }
    }
} else this

object SharedTransition {
    const val USE_ANDROIDX = false
}
