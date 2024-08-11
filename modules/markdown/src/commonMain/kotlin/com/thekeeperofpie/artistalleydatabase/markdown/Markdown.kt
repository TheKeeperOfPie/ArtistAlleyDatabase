package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.runtime.staticCompositionLocalOf

expect class Markdown {
    fun convertMarkdownText(markdown: String): MarkdownText
}

expect class MarkdownText

val LocalMarkdown =
    staticCompositionLocalOf<Markdown> { throw IllegalStateException("Markdown not provided") }
