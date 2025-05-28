package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
actual fun CustomHtmlText(
    text: String,
    modifier: Modifier,
    urlSpanStyle: SpanStyle,
    colorMapping: Map<Color, Color>,
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
    overflow: TextOverflow,
    softWrap: Boolean,
    minLines: Int,
    maxLines: Int,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: (TextLayoutResult) -> Unit,
    style: TextStyle,
    onFallbackClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    detectTaps: Boolean,
) {
    // TODO: Actually parse HTML
    HtmlText(
        modifier = modifier,
        annotatedString = buildAnnotatedString { append(text) },
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        minLines = minLines,
        maxLines = maxLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style,
        onFallbackClick = onFallbackClick,
        onLongClick = onLongClick,
        detectTaps = detectTaps,
    )
}

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
