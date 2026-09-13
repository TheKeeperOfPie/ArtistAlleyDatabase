package com.thekeeperofpie.artistalleydatabase.utils_scrollbars

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.ScrollbarState
import com.composeunstyled.Thumb
import com.composeunstyled.UnstyledHorizontalScrollbar
import com.composeunstyled.UnstyledVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.currentWindowSizeClass
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun rememberScrollbarState(gridState: LazyStaggeredGridState): ScrollbarState =
    remember(gridState) {
        LazyStaggeredGridScrollbarState(gridState)
    }

private class LazyStaggeredGridScrollbarState(
    private val gridState: LazyStaggeredGridState,
) : ScrollbarState {
    override val scrollOffset: Double
        get() {
            val firstItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return 0.0
            val estimatedRow = firstItem.index / lanes
            return estimatedRow * averageVisibleItemHeight + firstItem.offset.y
        }
    override val contentSize: Double
        get() {
            val totalItems = gridState.layoutInfo.totalItemsCount
            if (totalItems == 0) return viewportSize
            val estimatedRows = (totalItems + lanes - 1) / lanes
            return estimatedRows * averageVisibleItemHeight +
                    gridState.layoutInfo.beforeContentPadding +
                    gridState.layoutInfo.afterContentPadding
        }
    override val viewportSize: Double
        get() = gridState.layoutInfo.viewportSize.height.toDouble()
    override val interactionSource: InteractionSource
        get() = gridState.interactionSource
    override val isScrollInProgress: Boolean
        get() = gridState.isScrollInProgress

    private val averageVisibleItemHeight
        get() = gridState.layoutInfo.visibleItemsInfo
            .map { it.size.height }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0

    private val lanes
        get() = gridState.layoutInfo.visibleItemsInfo
            .map { it.lane }
            .distinct()
            .count()
            .coerceAtLeast(1)

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

@Composable
fun PrimaryVerticalScrollbar(
    scrollbarState: ScrollbarState,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowSizeClass()
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    if (isExpanded) {
        val interactionSource = remember { MutableInteractionSource() }
        UnstyledVerticalScrollbar(
            scrollbarState = scrollbarState,
            interactionSource = interactionSource,
            modifier = modifier.fillMaxHeight()
        ) {
            val isHovered by interactionSource.collectIsHoveredAsState()
            val isDragging by interactionSource.collectIsDraggedAsState()
            Thumb(
                modifier = Modifier
                    .width(12.dp)
                    .background(
                        color = if (isHovered or isDragging) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(100),
                    ),
            )
        }
    }

    // TODO: Non-expanded scrollbar
}

@Composable
fun PrimaryHorizontalScrollbar(
    scrollbarState: ScrollbarState,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    UnstyledHorizontalScrollbar(
        scrollbarState = scrollbarState,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth()
    ) {
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isDragging by interactionSource.collectIsDraggedAsState()
        Thumb(
            modifier = Modifier
                .height(12.dp)
                .background(
                    color = if (isHovered or isDragging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(100),
                ),
        )
    }
}
