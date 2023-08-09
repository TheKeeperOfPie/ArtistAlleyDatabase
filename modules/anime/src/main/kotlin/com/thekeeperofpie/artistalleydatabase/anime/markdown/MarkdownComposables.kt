package com.thekeeperofpie.artistalleydatabase.anime.markdown

import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(
    markwon: Markwon,
    text: Spanned?,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    maxLines: Int? = null,
) {
    val contentColor = LocalContentColor.current
    val color = (textColor?.takeOrElse { contentColor } ?: contentColor).toArgb()
    AndroidView(
        factory = {
            TextView(it).apply {
                if (maxLines != null) {
                    this.maxLines = maxLines
                }
                setTextColor(color)
                ellipsize = TextUtils.TruncateAt.END
            }
        },
        update = {
            if (text != null) {
                it.setTextColor(color)
                markwon.setParsedMarkdown(it, text)
            }
        },
        modifier = modifier,
    )
}
