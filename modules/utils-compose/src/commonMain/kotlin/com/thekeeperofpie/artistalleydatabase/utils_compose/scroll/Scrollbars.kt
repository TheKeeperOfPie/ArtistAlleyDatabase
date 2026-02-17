package com.thekeeperofpie.artistalleydatabase.utils_compose.scroll

import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.scrollbar_handle_content_description
import com.composables.core.ScrollAreaState
import com.thekeeperofpie.artistalleydatabase.utils.AnimationUtils
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun VerticalScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    alwaysVisible: Boolean = false,
) = VerticalScrollbar(
    firstVisibleItemIndex = { firstVisibleItemIndex },
    totalItemsCount = { layoutInfo.totalItemsCount },
    lastVisibleIndex = { layoutInfo.visibleItemsInfo.lastOrNull()?.index },
    scrollToItem = { scrollToItem(it) },
    alwaysVisible = alwaysVisible,
    state = state,
    modifier = modifier,
)

@Composable
fun VerticalScrollbar(
    state: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
    alwaysVisible: Boolean = false,
) = VerticalScrollbar(
    firstVisibleItemIndex = { firstVisibleItemIndex },
    totalItemsCount = { layoutInfo.totalItemsCount },
    lastVisibleIndex = { layoutInfo.visibleItemsInfo.lastOrNull()?.index },
    scrollToItem = { scrollToItem(it) },
    alwaysVisible = alwaysVisible,
    state = state,
    modifier = modifier,
)

@Composable
fun VerticalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    alwaysVisible: Boolean = false,
) = VerticalScrollbar(
    firstVisibleItemIndex = { firstVisibleItemIndex },
    totalItemsCount = { layoutInfo.totalItemsCount },
    lastVisibleIndex = { layoutInfo.visibleItemsInfo.lastOrNull()?.index },
    scrollToItem = { scrollToItem(it) },
    alwaysVisible = alwaysVisible,
    state = state,
    modifier = modifier,
)

@Composable
fun <State : ScrollableState> VerticalScrollbar(
    firstVisibleItemIndex: State.() -> Int,
    totalItemsCount: State.() -> Int,
    lastVisibleIndex: State.() -> Int?,
    scrollToItem: suspend State.(index: Int) -> Unit,
    alwaysVisible: Boolean = false,
    state: State,
    modifier: Modifier = Modifier,
) {
    var height by remember { mutableIntStateOf(1) }
    val handleSize = LocalDensity.current.run { 40.dp.roundToPx() }
    val maxOffsetY = (height - handleSize).coerceAtLeast(1).toFloat()
    Box(modifier = modifier.onSizeChanged { height = it.height }) {
        val offsetFromGrid by remember(maxOffsetY) {
            derivedStateOf {
                (state.firstVisibleItemIndex().toFloat()
                        / state.totalItemsCount().coerceAtLeast(1)
                        * maxOffsetY)
                    .roundToInt()
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        var offsetY by remember { mutableFloatStateOf(0f) }
        val dragging by interactionSource.collectIsDraggedAsState()
        LaunchedEffect(dragging, offsetFromGrid) {
            if (!dragging) {
                offsetY = offsetFromGrid.toFloat()
            }
        }
        val scope = rememberCoroutineScope()
        val fillsViewPort by remember {
            derivedStateOf {
                @Suppress("NAME_SHADOWING")
                val lastVisibleIndex = state.lastVisibleIndex()
                lastVisibleIndex != null && lastVisibleIndex < (state.totalItemsCount() - 1)
            }
        }
        val hovered by interactionSource.collectIsHoveredAsState()
        val handleVisible by remember {
            derivedStateOf { fillsViewPort && (state.isScrollInProgress || dragging || hovered) }
        }
        val handleAlpha by animateFloatAsState(
            targetValue = if (alwaysVisible || handleVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (handleVisible) {
                    DefaultDurationMillis / 2
                } else {
                    DefaultDurationMillis
                },
                delayMillis = if (handleVisible) 0 else DefaultDurationMillis * 5,
            ),
            label = "Scrollbar handle alpha",
        )

        val draggableState = rememberDraggableState(onDelta = {
            val newOffset = (offsetY + it).coerceIn(0f, maxOffsetY)
            if (offsetY == newOffset) return@rememberDraggableState
            offsetY = newOffset
            val ratio = offsetY / maxOffsetY
            val scrollPositionFromOffset =
                (ratio * state.totalItemsCount().coerceAtLeast(1))
                    .roundToInt()
            scope.launch {
                state.scrollToItem(scrollPositionFromOffset)
            }
        })
        val show by remember { derivedStateOf { handleAlpha > 0f } }
        if (show) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(handleAlpha)
                    .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
                    .size(width = 32.dp, height = 40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                    )
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        interactionSource = interactionSource,
                    )
                    .hoverable(interactionSource)
            ) {
                Icon(
                    imageVector = Icons.Filled.UnfoldMore,
                    contentDescription = stringResource(Res.string.scrollbar_handle_content_description),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
fun HorizontalScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier,
) {
    var width by remember { mutableIntStateOf(1) }
    val handleSize = LocalDensity.current.run { 40.dp.roundToPx() }
    val maxOffsetX by remember {
        derivedStateOf { (width - handleSize).coerceAtLeast(1).toFloat() }
    }
    Box(modifier = modifier.onSizeChanged { width = it.width }) {
        val interactionSource = remember { MutableInteractionSource() }
        val scope = rememberCoroutineScope()
        val handleVisible = state.canScrollForward || state.canScrollBackward
        val handleAlpha by animateFloatAsState(
            targetValue = if (handleVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (handleVisible) {
                    DefaultDurationMillis / 2
                } else {
                    DefaultDurationMillis
                },
                delayMillis = if (handleVisible) 0 else DefaultDurationMillis * 3,
            ),
            label = "Scrollbar handle alpha",
        )
        val scrollbarOffset by remember {
            derivedStateOf {
                val raw =
                    AnimationUtils.lerp(0f, maxOffsetX, state.value.toFloat() / state.maxValue)
                if (raw.isNaN()) {
                    0
                } else {
                    raw.roundToInt()
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .alpha(handleAlpha)
                .offset { IntOffset(x = scrollbarOffset, y = 0) }
                .size(width = 40.dp, height = 32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)
                )
                .draggable(
                    state = rememberDraggableState(onDelta = {
                        val deltaInGridSpace = it / maxOffsetX * state.maxValue
                        scope.launch { state.scrollBy(deltaInGridSpace) }
                    }),
                    orientation = Orientation.Horizontal,
                    interactionSource = interactionSource,
                )
        ) {
            Icon(
                imageVector = Icons.Filled.UnfoldMore,
                contentDescription = stringResource(Res.string.scrollbar_handle_content_description),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.rotate(90f)
            )
        }
    }
}

@Composable
fun rememberScrollAreaState(lazyStaggeredGridState: LazyStaggeredGridState): ScrollAreaState =
    remember(lazyStaggeredGridState) {
        LazyStaggeredGridScrollAreaScrollAreaState(lazyStaggeredGridState)
    }

private class LazyStaggeredGridScrollAreaScrollAreaState(
    private val gridState: LazyStaggeredGridState,
) : ScrollAreaState {
    override val scrollOffset: Double
        get() = ((gridState.firstVisibleItemIndex * itemSize) +
                gridState.firstVisibleItemScrollOffset)
    override val contentSize: Double
        get() = (gridState.layoutInfo.totalItemsCount * itemSize) +
                gridState.layoutInfo.beforeContentPadding +
                gridState.layoutInfo.afterContentPadding
    override val viewportSize: Double
        get() = gridState.layoutInfo.viewportSize.height.toDouble()
    override val interactionSource: InteractionSource
        get() = gridState.interactionSource
    override val isScrollInProgress: Boolean
        get() = gridState.isScrollInProgress

    private val visibleItems
        get() = gridState.layoutInfo.let {
            (it.visibleItemsInfo.lastOrNull()?.index ?: 0) -
                    (it.visibleItemsInfo.firstOrNull()?.index ?: 0)
        }
    private val itemSize
        get() = if (visibleItems == 0) {
            0.0
        } else {
            (gridState.layoutInfo.viewportSize.height / visibleItems)
                .toDouble()
                .coerceAtLeast(0.0)
        }

    override suspend fun scrollTo(scrollOffset: Double) {
        val distance = scrollOffset - this.scrollOffset
        if (abs(distance) <= viewportSize) {
            gridState.scrollBy(distance.toFloat())
        } else {
            gridState.scrollToItem(
                (scrollOffset / contentSize * gridState.layoutInfo.totalItemsCount).roundToInt()
            )
        }
    }
}
