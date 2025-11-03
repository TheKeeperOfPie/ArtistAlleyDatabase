package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
actual class Markdown {
    actual fun convertMarkdownText(markdown: String): MarkdownText? {
        // TODO: Real implementation
        return MarkdownText(markdown)
    }
}

@Immutable
actual class MarkdownText(val value: String)
