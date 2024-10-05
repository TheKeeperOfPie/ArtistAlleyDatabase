package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikepenz.markdown.m3.markdownTypography

@Composable
actual fun MarkdownText(
    markdownText: MarkdownText?,
    modifier: Modifier,
    textColor: Color?,
    maxLines: Int?,
    onOverflowChange: (Boolean) -> Unit,
) {
    val typography = markdownTypography(
        paragraph = MaterialTheme.typography.bodyLarge.copy(
            color = textColor ?: Color.Unspecified
        )
    )
    // Desktop ignores maxLines since this library doesn't expose overflow
    com.mikepenz.markdown.m3.Markdown(
        content = markdownText?.value.orEmpty(),
        typography = typography,
        modifier = modifier,
    )
}
