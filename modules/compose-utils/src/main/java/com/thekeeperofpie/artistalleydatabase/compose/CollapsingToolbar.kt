package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

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
        state = rememberDraggableState { delta ->
            scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffset + delta
        },
    )

    Box(modifier = modifier.then(appBarDragModifier)) {
        content(progress)
    }
}
