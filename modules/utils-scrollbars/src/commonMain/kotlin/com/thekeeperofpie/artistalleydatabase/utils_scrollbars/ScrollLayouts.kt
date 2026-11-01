package com.thekeeperofpie.artistalleydatabase.utils_scrollbars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.ScrollbarState
import com.thekeeperofpie.artistalleydatabase.utils_compose.DisplayConstants
import com.thekeeperofpie.artistalleydatabase.utils_compose.currentWindowSizeClass

@Composable
fun VerticalScrollableWithBar(
    scrollbarState: ScrollbarState,
    modifier: Modifier = Modifier,
    content: @Composable (horizontalContentPadding: Dp) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val horizontalContentPadding = if (maxWidth > DisplayConstants.MAX_CONTENT_WIDTH) {
            (maxWidth - DisplayConstants.MAX_CONTENT_WIDTH) / 2
        } else {
            0.dp
        }
        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        if (isExpanded) {
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    content(horizontalContentPadding)
                }
                PrimaryVerticalScrollbar(scrollbarState = scrollbarState)
            }
        } else {
            content(horizontalContentPadding)
            PrimaryVerticalScrollbar(
                scrollbarState = scrollbarState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
