package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.FloatingToolbarState
import androidx.compose.material3.rememberFloatingToolbarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.hoc081098.flowext.interval
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.Duration.Companion.seconds


@Suppress("UnusedReceiverParameter")
@Composable
fun FloatingToolbarDefaults.exitAlwaysScrollBehaviorAutoReset(): FloatingToolbarScrollBehavior {
    val baseFloatingToolbarState = rememberFloatingToolbarState()
    val floatingToolbarState = remember(baseFloatingToolbarState) {
        object : FloatingToolbarState by baseFloatingToolbarState {
            override var offsetLimit: Float
                get() = baseFloatingToolbarState.offsetLimit
                set(value) {
                    // Fix bug where this is sometimes assigned a positive offsetLimit
                    baseFloatingToolbarState.offsetLimit = value.coerceAtMost(0f)
                }
        }
    }
    val scrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            exitDirection = FloatingToolbarExitDirection.Bottom,
            state = floatingToolbarState,
        )
    val isHiding by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.offset != 0f }
    }
    // In case the user hides the bar and can't get it back, return it after a delay
    LaunchedEffect(scrollBehavior) {
        snapshotFlow {
            // Read contentOffset so that the interval resets on every scroll
            scrollBehavior.state.contentOffset to isHiding
        }
            .flatMapLatest {
                if (it.second) {
                    interval(5.seconds, 5.seconds)
                } else {
                    emptyFlow()
                }
            }
            .collectLatest {
                val state = scrollBehavior.state
                AnimationState(state.offset)
                    .animateTo(0f, scrollBehavior.snapAnimationSpec) {
                        state.offset = value
                    }
            }
    }
    return scrollBehavior
}

@Composable
fun Modifier.exitAlwaysScrollBehaviorAutoReset(scrollBehavior: FloatingToolbarScrollBehavior) =
    pointerInput(scrollBehavior) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val delta = event.changes.firstOrNull()?.scrollDelta
                    if (delta != null) {
                        scrollBehavior.onPostScroll(
                            -delta,
                            Offset.Zero,
                            NestedScrollSource.UserInput,
                        )
                    }
                }
            }
        }
    }.nestedScroll(scrollBehavior)
