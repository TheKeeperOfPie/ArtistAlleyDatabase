package com.thekeeperofpie.artistalleydatabase.markdown

import android.app.Application
import android.text.Spanned
import android.widget.TextView
import androidx.compose.runtime.Immutable
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin

actual class Markdown(application: Application) {

    private val markwon = Markwon.builderNoCore(application)
        .usePlugin(CorePlugin.create())
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(HtmlPlugin.create().apply {
            allowNonClosedTags(true)
            addHandler(AniListTempPlugin.CenterAlignTagHandler)
        })
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(TablePlugin.create(application))
        .usePlugin(MovementMethodPlugin.create(TableAwareMovementMethod.create()))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(AniListTempPlugin)
        .usePlugin(AniListSpoilerPlugin)
        .usePlugin(CoilImagesMarkwonPlugin.create(application))
        .build()

    actual fun convertMarkdownText(markdown: String) =
        MarkdownText(markwon.toMarkdown(markdown))

    fun setParsedMarkdown(textView: TextView, text: MarkdownText) =
        markwon.setParsedMarkdown(textView, text.value)
}

@Immutable
actual data class MarkdownText(val value: Spanned)
