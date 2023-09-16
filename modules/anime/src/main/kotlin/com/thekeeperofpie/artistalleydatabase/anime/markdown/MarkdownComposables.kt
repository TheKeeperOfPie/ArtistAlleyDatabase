package com.thekeeperofpie.artistalleydatabase.anime.markdown

import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(
    markdownText: StableSpanned?,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    maxLines: Int? = null,
    onOverflowChange: (Boolean) -> Unit = {},
) {
    val contentColor = LocalContentColor.current
    val color = (textColor?.takeOrElse { contentColor } ?: contentColor).toArgb()
    val markwon = LocalMarkwon.current
    val asString = remember(markdownText) { markdownText?.value.toString() }
    AndroidView(
        factory = {
            TextView(it).apply {
                setTextColor(color)
                ellipsize = TextUtils.TruncateAt.END
                setTextIsSelectable(true)
                if (markdownText != null) {
                    setTextColor(color)
                    markwon.setParsedMarkdown(this, markdownText.value)
                }
            }
        },
        update = {
            it.maxLines = maxLines ?: Int.MAX_VALUE
            if (markdownText != null) {
                it.setTextColor(color)
                markwon.setParsedMarkdown(it, markdownText.value)
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

val LocalMarkwon = staticCompositionLocalOf<Markwon> { throw IllegalStateException() }
