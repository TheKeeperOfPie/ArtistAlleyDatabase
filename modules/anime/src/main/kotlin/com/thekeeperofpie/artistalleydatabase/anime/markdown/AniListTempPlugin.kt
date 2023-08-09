package com.thekeeperofpie.artistalleydatabase.anime.markdown

import android.text.Layout
import android.text.style.AlignmentSpan
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler


object AniListTempPlugin : AbstractMarkwonPlugin() {

    private val webLinkRegex = Regex("""
        \s(https?://(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.\S{2,}|https?://(?:www\.|(?!www))[a-zA-Z0-9]+\.\S{2,}|www\.[a-zA-Z0-9]+\.\S{2,})
    """.trimIndent())

    @Suppress("RestrictedApi")
    override fun processMarkdown(markdown: String) =
        markdown
            // AniList has custom img### methods which aren't handled in common Markdown
            .replace(Regex("""img(?:|\d\d[\d%])\((http[^)]*)\)""")) {
                // TODO: Support sizing declarations
                val imageUrl = it.groups[1]?.value
                "<a href=\"$imageUrl\"><img src=\"$imageUrl\" alt=\"$imageUrl\"/></a>\n"
            }
            // Markwon doesn't support parsing Markdown syntax inside of HTML tags, so manually
            // convert the common syntax into HTML equivalents
            .replace(Regex("""([^\\*])\*\*([^*][\S\s]*?[^\\*])\*\*([^_])""")) {
                "${it.groups[1]?.value}<b>${it.groups[2]?.value}</b>${it.groups[3]?.value}"
            }
            .replace(Regex("""([^\\_])__([^_][\S\s]*?[^\\_])__([^_])""")) {
                "${it.groups[1]?.value}<b>${it.groups[2]?.value}</b>${it.groups[3]?.value}"
            }
            .replace(Regex("""([^\\_])_([^_][\S\s]*?[^\\_])_([^_])""")) {
                "${it.groups[1]?.value}<i>${it.groups[2]?.value}</i>${it.groups[3]?.value}"
            }
            .replace(Regex("""([^\\])~~~([\S\s]*?[^\\])~~~""")) {
                "${it.groups[1]?.value}<center>${it.groups[2]?.value}</center>"
            }
            .replace(Regex("""([^\\])\+\+\+([\S\s]*?[^\\])\+\+\+""")) {
                "${it.groups[1]?.value}<center>${it.groups[2]?.value}</center>"
            }
            .replace(Regex("""\[(.*?)]\((.*?)\)""")) {
                "<a href=\"${it.groups[2]?.value}\">${it.groups[1]?.value}</a>"
            }
            .replace(webLinkRegex) { "<a href=\"${it.value.trim()}\">${it.value.trim()}</a>" }
            .replace("\n---\n", "\n<hr/>\n")
            // <center> seems to break stuff that comes after it
            .replace("</center>\n", "</center><br/>")
            // A new line isn't guaranteed after each heading, force it
            .replace("</h1>", "</h1>\n")
            .replace("</h2>", "</h2>\n")
            .replace("</h3>", "</h3>\n")
            .replace("</h4>", "</h4>\n")
            .replace("</h5>", "</h5>\n")
            .replace("</h6>", "</h6>\n")

    object CenterAlignTagHandler : SimpleTagHandler() {
        override fun getSpans(
            markwonConfiguration: MarkwonConfiguration,
            renderProps: RenderProps,
            htmlTag: HtmlTag,
        ) = AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)

        override fun supportedTags() = setOf("center")
    }
}
