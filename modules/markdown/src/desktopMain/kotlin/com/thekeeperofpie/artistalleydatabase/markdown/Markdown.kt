package com.thekeeperofpie.artistalleydatabase.markdown

import androidx.compose.runtime.Immutable
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class Markdown {
    actual fun convertMarkdownText(markdown: String): MarkdownText? {
        // TODO: Real implementation
        return MarkdownText(markdown)
    }
}

@Immutable
actual class MarkdownText(val value: String)
