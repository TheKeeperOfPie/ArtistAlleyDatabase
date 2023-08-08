package com.thekeeperofpie.artistalleydatabase.anime.markdown

import android.text.Layout
import android.text.style.AlignmentSpan
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler


object AniListTempPlugin : AbstractMarkwonPlugin() {

    override fun processMarkdown(markdown: String) =
        markdown
            // AniList has custom img### methods which aren't handled in common Markdown
            .replace(Regex("""img(?:|\d\d[\d%])\((http[^)]*)\)""")) {
                // TODO: Support sizing declarations
                val imageUrl = it.groups[1]?.value
                "<img src=\"$imageUrl\" alt=\"$imageUrl\"/>\n"
//                "![$imageUrl]($imageUrl)\n"
            }
            // <center> seems to break a lot of parsing
            .replace("<center>", "<center>\n")
//            .replace("</center>", "")


    object CenterAlignTagHandler : SimpleTagHandler() {
        override fun getSpans(
            markwonConfiguration: MarkwonConfiguration,
            renderProps: RenderProps,
            htmlTag: HtmlTag,
        ) = AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)

        override fun supportedTags() = setOf("center")
    }
}
