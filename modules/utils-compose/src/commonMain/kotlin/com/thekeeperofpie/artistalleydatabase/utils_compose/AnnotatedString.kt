package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.ui.text.AnnotatedString

fun AnnotatedString.Builder.appendParagraph(paragraph: String) {
    appendLine(paragraph)
    appendLine()
}
