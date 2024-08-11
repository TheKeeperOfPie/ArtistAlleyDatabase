package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun MarkdownText(
    markdownText: MarkdownText?,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    maxLines: Int? = null,
    onOverflowChange: (Boolean) -> Unit = {},
)
