@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DoubleDrawerValue {
    Start,
    End,
    Closed,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleModalNavigationDrawer(
    startDrawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    endDrawerContent: (@Composable () -> Unit)? = null,
    drawerState: DrawerState = rememberDrawerState(DoubleDrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    scrimColor: Color = DrawerDefaults.scrimColor,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
//    val navigationMenu = getString(Strings.NavigationMenu)
    val density = LocalDensity.current
    val navigationDrawerWidth = with(density) { 360.dp.toPx() }

    val width = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.roundToPx()
    }
    SideEffect {
        drawerState.density = density
        drawerState.anchoredDraggableState.updateAnchors(
            DraggableAnchors {
                DoubleDrawerValue.Start at navigationDrawerWidth
                DoubleDrawerValue.Closed at 0f
                DoubleDrawerValue.End at -navigationDrawerWidth
            }
        )
    }

    Box(
        modifier
            .fillMaxSize()
            .anchoredDraggable(
                state = drawerState.anchoredDraggableState,
                orientation = Orientation.Horizontal,
                enabled = gesturesEnabled,
                startDragImmediately = drawerState.isAnimationRunning,
            )
    ) {
        Box {
            content()
        }
        Scrim(
            open = drawerState.currentValue != DoubleDrawerValue.Closed,
            onClose = {
                if (
                    gesturesEnabled
//                    && drawerState.anchoredDraggableState.confirmValueChange(DoubleDrawerValue.Closed)
                ) {
                    scope.launch { drawerState.close() }
                }
            },
            fraction = {
                calculateFraction(
                    0f,
                    -navigationDrawerWidth,
                    drawerState.requireOffset().coerceIn(-navigationDrawerWidth, 0f),
                ).coerceAtLeast(
                    calculateFraction(
                        0f,
                        navigationDrawerWidth,
                        drawerState.requireOffset().coerceIn(0f, navigationDrawerWidth),
                    )
                )
            },
            color = scrimColor
        )
        Box(
            Modifier
                .offset {
                    IntOffset(
                        x = (-navigationDrawerWidth + drawerState
                            .requireOffset())
                            .coerceIn(-navigationDrawerWidth, 0f)
                            .roundToInt(),
                        y = 0,
                    )
                }
                .semantics {
//                    paneTitle = navigationMenu
                    if (drawerState.currentValue == DoubleDrawerValue.Start) {
                        dismiss {
//                            if (drawerState.anchoredDraggableState.confirmValueChange(
//                                    DoubleDrawerValue.Closed
//                                )
//                            ) {
                            scope.launch { drawerState.close() }
//                            };
                            true
                        }
                    }
                },
        ) {
            startDrawerContent()
        }
        if (endDrawerContent != null) {
            Box(
                Modifier
                    .offset {
                        IntOffset(
                            x = width + drawerState
                                .requireOffset()
                                .coerceIn(-navigationDrawerWidth, 0f)
                                .roundToInt(),
                            y = 0,
                        )
                    }
                    .semantics {
//                    paneTitle = navigationMenu
                        if (drawerState.currentValue == DoubleDrawerValue.End) {
                            dismiss {
//                            if (drawerState.anchoredDraggableState.confirmValueChange(
//                                    DoubleDrawerValue.Closed
//                                )
//                            ) {
                                scope.launch { drawerState.close() }
//                            };
                                true
                            }
                        }
                    },
            ) {
                endDrawerContent()
            }
        }
    }
}

private fun calculateFraction(a: Float, b: Float, pos: Float) =
    ((pos - a) / (b - a)).coerceIn(0f, 1f)

private val AnimationSpec = TweenSpec<Float>(durationMillis = 256)

@Composable
fun rememberDrawerState(
    initialValue: DoubleDrawerValue,
    confirmStateChange: (DoubleDrawerValue) -> Boolean = { true },
): DrawerState {
    return rememberSaveable(saver = DrawerState.Saver(confirmStateChange)) {
        DrawerState(initialValue, confirmStateChange)
    }
}

private const val DrawerPositionalThreshold = 0.5f
private val DrawerVelocityThreshold = 400.dp

@Suppress("NotCloseable")
@Stable
@OptIn(ExperimentalMaterial3Api::class)
class DrawerState(
    initialValue: DoubleDrawerValue,
    confirmStateChange: (DoubleDrawerValue) -> Boolean = { true },
) {

    internal val anchoredDraggableState = AnchoredDraggableState(
        initialValue = initialValue,
        anchors = DraggableAnchors {},
        snapAnimationSpec = TweenSpec<Float>(durationMillis = 256),
        decayAnimationSpec = exponentialDecay(),
        confirmValueChange = confirmStateChange,
        positionalThreshold = { distance: Float -> distance * DrawerPositionalThreshold },
        velocityThreshold = { with(requireDensity()) { DrawerVelocityThreshold.toPx() } }
    )

    /**
     * The current value of the state.
     *
     * If no swipe or animation is in progress, this corresponds to the start the drawer
     * currently in. If a swipe or an animation is in progress, this corresponds the state drawer
     * was in before the swipe or animation started.
     */
    val currentValue: DoubleDrawerValue
        get() {
            return anchoredDraggableState.currentValue
        }

    val isAnimationRunning: Boolean
        get() {
            return anchoredDraggableState.isAnimationRunning
        }

    suspend fun open() = animateTo(DoubleDrawerValue.Start)
    suspend fun close() = animateTo(DoubleDrawerValue.Closed)

    /**
     * The target value of the drawer state.
     *
     * If a swipe is in progress, this is the value that the Drawer would animate to if the
     * swipe finishes. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    val targetValue: DoubleDrawerValue
        get() = anchoredDraggableState.targetValue

    /**
     * The current position (in pixels) of the drawer sheet, or Float.NaN before the offset is
     * initialized.
     *
     * @see [AnchoredDraggableState.offset] for more information.
     */
    val currentOffset: Float get() = anchoredDraggableState.offset

    internal var density: Density? by mutableStateOf(null)

    private fun requireDensity() = requireNotNull(density) {
        "The density on BottomDrawerState ($this) was not set. Did you use BottomDrawer" +
                " with the BottomDrawer composable?"
    }

    internal fun requireOffset(): Float = anchoredDraggableState.requireOffset()

    private suspend fun animateTo(
        targetValue: DoubleDrawerValue,
        animationSpec: AnimationSpec<Float> = AnimationSpec,
        velocity: Float = anchoredDraggableState.lastVelocity,
    ) {
        anchoredDraggableState.anchoredDrag(targetValue = targetValue) { anchors, latestTarget ->
            val targetOffset = anchors.positionOf(latestTarget)
            if (!targetOffset.isNaN()) {
                var prev = if (currentOffset.isNaN()) 0f else currentOffset
                animate(prev, targetOffset, velocity, animationSpec) { value, velocity ->
                    // Our onDrag coerces the value within the bounds, but an animation may
                    // overshoot, for example a spring animation or an overshooting interpolator
                    // We respect the user's intention and allow the overshoot, but still use
                    // DraggableState's drag for its mutex.
                    dragTo(value, velocity)
                    prev = value
                }
            }
        }
    }

    companion object {
        /**
         * The default [Saver] implementation for [DrawerState].
         */
        fun Saver(confirmStateChange: (DoubleDrawerValue) -> Boolean) =
            Saver<DrawerState, DoubleDrawerValue>(
                save = { it.currentValue },
                restore = { DrawerState(it, confirmStateChange) }
            )
    }
}

@Composable
private fun Scrim(
    open: Boolean,
    onClose: () -> Unit,
    fraction: () -> Float,
    color: Color,
) {
//    val closeDrawer = getString(Strings.CloseDrawer)
    val dismissDrawer = if (open) {
        Modifier
            .pointerInput(onClose) { detectTapGestures { onClose() } }
            .semantics(mergeDescendants = true) {
//                contentDescription = closeDrawer
                onClick { onClose(); true }
            }
    } else {
        Modifier
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .then(dismissDrawer)
    ) {
        drawRect(color, alpha = fraction())
    }
}
