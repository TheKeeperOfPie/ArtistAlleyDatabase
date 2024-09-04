package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun ImageHtmlText(
    text: String,
    color: Color,
    modifier: Modifier,
    maxLines: Int?,
    onClickFallback: () -> Unit,
) {
    Text(text = text, color = color)
}
