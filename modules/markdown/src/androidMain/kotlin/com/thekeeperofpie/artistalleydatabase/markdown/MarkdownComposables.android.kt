package com.thekeeperofpie.artistalleydatabase.markdown

import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun MarkdownText(
    markdownText: MarkdownText?,
    modifier: Modifier,
    textColor: Color?,
    maxLines: Int?,
    onOverflowChange: (Boolean) -> Unit,
) {
    val contentColor = LocalContentColor.current
    val color = (textColor?.takeOrElse { contentColor } ?: contentColor).toArgb()
    val markdown = LocalMarkdown.current
    val asString = remember(markdownText) { markdownText?.value.toString() }
    AndroidView(
        factory = {
            TextView(it).apply {
                setTextColor(color)
                ellipsize = TextUtils.TruncateAt.END
                setTextIsSelectable(true)
                if (markdownText != null) {
                    setTextColor(color)
                    markdown.setParsedMarkdown(this, markdownText)
                }
            }
        },
        update = {
            it.maxLines = maxLines ?: Int.MAX_VALUE
            if (markdownText != null) {
                it.setTextColor(color)
                markdown.setParsedMarkdown(it, markdownText)
            }
            if (maxLines != null) {
                it.post {
                    if (it.layout == null) {
                        it.post {
                            if (it.layout == null) {
                                onOverflowChange(true)
                            } else {
                                onOverflowChange(it.layout.text.toString() != asString
                                        || it.layout.lineCount > maxLines)
                            }
                        }
                    } else {
                        onOverflowChange(it.layout.text.toString() != asString
                                || it.layout.lineCount > maxLines)
                    }
                }
            }
        },
        modifier = modifier,
    )
}
