@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose

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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Copy of [androidx.compose.material3.LargeTopAppBar] supporting CollapsingToolbarLayout features.
 */
@Composable
fun CollapsingToolbar(
    modifier: Modifier = Modifier,
    maxHeight: Dp,
    pinnedHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior,
    content: @Composable BoxScope.(progress: Float) -> Unit,
) {
    val pinnedHeightPx: Float
    val maxHeightPx: Float
    LocalDensity.current.run {
        pinnedHeightPx = pinnedHeight.toPx()
        maxHeightPx = maxHeight.toPx()
    }

    LaunchedEffect(pinnedHeightPx, maxHeightPx) {
        scrollBehavior.state.heightOffsetLimit = pinnedHeightPx - maxHeightPx
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
fun EnterAlwaysTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    content: @Composable BoxScope.() -> Unit,
) {
    var heightOffsetLimit by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(heightOffsetLimit) {
        scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                .clipToBounds()
                .onSizeChanged {
                    if (heightOffsetLimit == 0f) {
                        heightOffsetLimit = -it.height.toFloat()
                    }
                }
                .offset {
                    IntOffset(x = 0, y = scrollBehavior.state.heightOffset.toInt())
                },
            content = content,
        )
    }
}

@Composable
fun EnterAlwaysTopAppBarHeightChange(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    content: @Composable BoxScope.() -> Unit,
) {
    var heightOffsetLimit by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(heightOffsetLimit) {
        scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
    }

    Box(modifier = modifier) {
        val height =
            LocalDensity.current.run { -heightOffsetLimit.toDp() + scrollBehavior.state.heightOffset.toDp() }
        Box(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                .clipToBounds()
                .onSizeChanged {
                    if (heightOffsetLimit == 0f) {
                        heightOffsetLimit = -it.height.toFloat()
                    }
                }
                .conditionally(heightOffsetLimit != 0f) { height(height) },
        ) {
            Box(
                modifier = Modifier.wrapContentHeight(align = Alignment.Bottom, unbounded = true),
                content = content,
            )
        }
    }
}

@Composable
fun EnterAlwaysNavigationBar(
    scrollBehavior: NavigationBarEnterAlwaysScrollBehavior,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var heightOffsetLimit by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(heightOffsetLimit) {
        scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
    }

    val density = LocalDensity.current
    val offset by remember {
        derivedStateOf {
            density.run { -scrollBehavior.state.heightOffset.toDp() }
        }
    }

    NavigationBar(
        modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = offset
                        .toPx()
                        .toInt()
                )
            }
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
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMedium),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
) = NavigationBarEnterAlwaysScrollBehavior(
    state = rememberTopAppBarState(),
    snapAnimationSpec = snapAnimationSpec,
    flingAnimationSpec = flingAnimationSpec,
    canScroll = canScroll
)

class NavigationBarEnterAlwaysScrollBehavior(
    val state: TopAppBarState,
    val snapAnimationSpec: AnimationSpec<Float>?,
    val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true },
) {

    var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canScroll()) return Offset.Zero
                state.heightOffset += available.y
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.heightOffset += consumed.y
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return super.onPostFling(consumed, available) +
                        settle(state, available.y, snapAnimationSpec, flingAnimationSpec)
            }
        }
}

class BottomNavigationState(private val scrollBehavior: NavigationBarEnterAlwaysScrollBehavior) {

    val nestedScrollConnection get() = scrollBehavior.nestedScrollConnection

    @Composable
    fun bottomNavBarPadding(): Dp {
        val density = LocalDensity.current
        return remember {
            derivedStateOf {
                // Force a snapshot read so that this recomposes
                // https://android-review.googlesource.com/c/platform/frameworks/support/+/3123371
                scrollBehavior.state.heightOffset
                scrollBehavior.state.heightOffsetLimit
                    .takeUnless { it == -Float.MAX_VALUE }
                    ?.let { density.run { -it.toDp() } }
                    ?: 80.dp
            }
        }.value
    }

    @Composable
    fun bottomOffset(): Dp {
        val density = LocalDensity.current
        return remember {
            derivedStateOf {
                density.run { scrollBehavior.state.heightOffset.toDp() }
            }
        }.value
    }

    @Composable
    fun bottomOffsetPadding() = (bottomNavBarPadding() + bottomOffset()).coerceAtLeast(0.dp)

    fun reset() {
        scrollBehavior.state.heightOffset = 0f
    }
}

class NestedScrollSplitter(
    private val primary: NestedScrollConnection?,
    private val secondary: NestedScrollConnection? = null,
    private val consumeNone: Boolean = false,
) : NestedScrollConnection {
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) =
        (primary?.onPostFling(consumed, available)
            ?.also { secondary?.onPostFling(consumed, available) }
            ?: secondary?.onPostFling(consumed, available)
            ?: super.onPostFling(consumed, available))
            .takeUnless { consumeNone } ?: Velocity.Zero

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ) = (primary?.onPostScroll(consumed, available, source)
        ?.also { secondary?.onPostScroll(consumed, available, source) }
        ?: secondary?.onPostScroll(consumed, available, source)
        ?: super.onPostScroll(consumed, available, source))
        .takeUnless { consumeNone } ?: Offset.Zero

    override suspend fun onPreFling(available: Velocity) = (primary?.onPreFling(available)
        ?.also { secondary?.onPreFling(available) }
        ?: secondary?.onPreFling(available)
        ?: super.onPreFling(available))
        .takeUnless { consumeNone } ?: Velocity.Zero

    override fun onPreScroll(available: Offset, source: NestedScrollSource) =
        (primary?.onPreScroll(available, source)
            ?.also { secondary?.onPreScroll(available, source) }
            ?: secondary?.onPreScroll(available, source)
            ?: super.onPreScroll(available, source))
            .takeUnless { consumeNone } ?: Offset.Zero
}

private suspend fun settle(
    state: TopAppBarState,
    velocity: Float,
    snapAnimationSpec: AnimationSpec<Float>?,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
): Velocity {
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
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
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}
