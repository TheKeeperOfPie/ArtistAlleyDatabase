package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun MarkdownText(
    markdownText: MarkdownText?,
    modifier: Modifier,
    textColor: Color?,
    maxLines: Int?,
    onOverflowChange: (Boolean) -> Unit,
) {
    Text(
        text = markdownText?.value.orEmpty(),
        maxLines = maxLines ?: Int.MAX_VALUE,
        color = textColor ?: Color.Unspecified,
        modifier = modifier
    )
}
