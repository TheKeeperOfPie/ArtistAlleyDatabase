package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.compose_proxy.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VerticalScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
) {
    var height by remember { mutableIntStateOf(1) }
    val handleSize = LocalDensity.current.run { 40.dp.roundToPx() }
    val maxOffsetY = (height - handleSize).coerceAtLeast(1).toFloat()
    Box(modifier = modifier.onSizeChanged { height = it.height }) {
        val offsetFromGrid by remember(maxOffsetY) {
            derivedStateOf {
                (state.firstVisibleItemIndex.toFloat()
                        / state.layoutInfo.totalItemsCount.coerceAtLeast(1)
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
                val lastVisibleIndex = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                lastVisibleIndex != null && lastVisibleIndex < (state.layoutInfo.totalItemsCount - 1)
            }
        }
        val handleVisible = fillsViewPort && (state.isScrollInProgress || dragging)
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
                    state = rememberDraggableState(onDelta = {
                        val newOffset = (offsetY + it).coerceIn(0f, maxOffsetY)
                        if (offsetY == newOffset) return@rememberDraggableState
                        offsetY = newOffset
                        val ratio = offsetY / maxOffsetY
                        val scrollPositionFromOffset =
                            (ratio * state.layoutInfo.totalItemsCount.coerceAtLeast(1))
                                .roundToInt()
                        scope.launch {
                            state.scrollToItem(scrollPositionFromOffset)
                        }
                    }),
                    orientation = Orientation.Vertical,
                    interactionSource = interactionSource,
                )
        ) {
            Icon(
                imageVector = Icons.Filled.UnfoldMore,
                contentDescription = stringResource(R.string.scrollbar_handle_content_description),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
