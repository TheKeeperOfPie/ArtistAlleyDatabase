package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

/**
 * Copy of [androidx.compose.material3.LargeTopAppBar] supporting CollapsingToolbarLayout features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbar(
    modifier: Modifier = Modifier,
    maxHeight: Dp,
    pinnedHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior,
    content: @Composable (progress: Float) -> Unit,
) {
    val pinnedHeightPx: Float
    val maxHeightPx: Float
    LocalDensity.current.run {
        pinnedHeightPx = pinnedHeight.toPx()
        maxHeightPx = maxHeight.toPx()
    }

    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != pinnedHeightPx - maxHeightPx) {
            scrollBehavior.state.heightOffsetLimit = pinnedHeightPx - maxHeightPx
        }
    }

    val progress = scrollBehavior.state.collapsedFraction
    val appBarDragModifier = Modifier.draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { scrollBehavior.state.heightOffset += it },
    )

    Box(modifier = modifier.then(appBarDragModifier)) {
        content(progress)
    }
}

@Composable
fun EnterAlwaysNavigationBar(
    scrollBehavior: NavigationBarEnterAlwaysScrollBehavior,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var heightOffsetLimit by remember { mutableStateOf(0f) }
    LaunchedEffect(heightOffsetLimit) {
        scrollBehavior.heightOffsetLimit = heightOffsetLimit
    }

    val appBarDragModifier = Modifier.draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { scrollBehavior.heightOffset += it },
        onDragStopped = { scrollBehavior.settle(it) }
    )

    val offset = LocalDensity.current.run { -scrollBehavior.heightOffset.toDp() }
    NavigationBar(
        modifier
            .offset(y = offset)
            .then(appBarDragModifier)
            .onSizeChanged {
                heightOffsetLimit = -it.height.toFloat()
            }
    ) {
        content()
    }
}

@Composable
fun navigationBarEnterAlwaysScrollBehavior(
    canScroll: () -> Boolean = { true },
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
) = NavigationBarEnterAlwaysScrollBehavior(
    snapAnimationSpec = snapAnimationSpec,
    flingAnimationSpec = flingAnimationSpec,
    canScroll = canScroll
)

class NavigationBarEnterAlwaysScrollBehavior(
    val snapAnimationSpec: AnimationSpec<Float>?,
    val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
) {
    var heightOffsetLimit by mutableStateOf(0f)

    var heightOffset: Float
        get() = _heightOffset.value
        set(newOffset) {
            _heightOffset.value = newOffset.coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            )
        }

    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            heightOffset / heightOffsetLimit
        } else {
            0f
        }

    var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canScroll()) return Offset.Zero
                heightOffset += available.y
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!canScroll()) return Offset.Zero
                heightOffset += consumed.y
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return super.onPostFling(consumed, available) + settle(available.y)
            }
        }

    private var _heightOffset = mutableStateOf(0f)

    suspend fun settle(velocity: Float): Velocity {
        if (collapsedFraction < 0.01f || collapsedFraction == 1f) {
            return Velocity.Zero
        }
        var remainingVelocity = velocity
        if (flingAnimationSpec != null && abs(velocity) > 1f) {
            var lastValue = 0f
            AnimationState(
                initialValue = 0f,
                initialVelocity = velocity,
            )
                .animateDecay(flingAnimationSpec) {
                    val delta = value - lastValue
                    val initialHeightOffset = heightOffset
                    heightOffset = initialHeightOffset + delta
                    val consumed = abs(initialHeightOffset - heightOffset)
                    lastValue = value
                    remainingVelocity = this.velocity
                    if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
                }
        }
        if (snapAnimationSpec != null) {
            if (heightOffset < 0 &&
                heightOffset > heightOffsetLimit
            ) {
                AnimationState(initialValue = heightOffset).animateTo(
                    if (collapsedFraction < 0.5f) {
                        0f
                    } else {
                        heightOffsetLimit
                    },
                    animationSpec = snapAnimationSpec
                ) { heightOffset = value }
            }
        }

        return Velocity(0f, remainingVelocity)
    }
}
