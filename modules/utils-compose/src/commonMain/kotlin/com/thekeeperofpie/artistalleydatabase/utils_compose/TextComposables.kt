package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        MaterialTheme.colorScheme.onSurface
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides LocalTextSelectionColors.current) {
        @OptIn(ExperimentalMaterial3Api::class)
        (BasicTextField(
        value = value,
        modifier = if (label != null) {
            modifier
                // Merge semantics at the beginning of the modifier chain to ensure padding is
                // considered part of the text field.
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp)
        } else {
            modifier
        },
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = contentPadding,
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = shape,
                    )
                },
            )
        }
    ))
    }
}

/**
 * [Text] doesn't support images, so instead use a [TextView].
 */
@Composable
expect fun ImageHtmlText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int? = null,
    onClickFallback: () -> Unit = {},
)

@Composable
fun DetailsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    onClickViewAll: (() -> Unit)? = null,
    viewAllContentDescriptionTextRes: StringResourceCompat? = null,
) {
    if (onClickViewAll != null) {
        Row(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClickViewAll)
                .recomposeHighlighter()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
            )
            IconButton(
                onClick = onClickViewAll,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = viewAllContentDescriptionTextRes?.let {
                        ComposeResourceUtils.stringResourceCompat(it)
                    },
                )
            }
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)
                .recomposeHighlighter()
        )
    }
}

@Composable
fun DetailsSubsectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.surfaceTint,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
    )
}

// TODO: Replace other autosize methods
@Composable
fun AutoResizeHeightText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    minTextSizeSp: Float = 2f,
    textAlignment: Alignment = Alignment.CenterStart,
) {
    val initialFontSize = style.fontSize
    var realFontSize by remember { mutableStateOf(initialFontSize) }
    val initialLineHeight = style.lineHeight
    var realLineHeight by remember { mutableStateOf(initialLineHeight) }

    val textMeasurer = rememberTextMeasurer()

    Box(
        contentAlignment = textAlignment,
        modifier = modifier
            .onSizeChanged {
                var fontSize = initialFontSize
                var lineHeight = initialLineHeight
                val constraints = Constraints(
                    minWidth = it.width,
                    maxWidth = it.width,
                    minHeight = it.height,
                    maxHeight = it.height,
                )
                var result = textMeasurer.measure(
                    text = text,
                    style = style,
                    overflow = overflow,
                    maxLines = maxLines,
                    constraints = constraints,
                )
                var attempts = 0
                while (attempts++ < 25
                    && (result.didOverflowWidth || result.didOverflowHeight)
                    && fontSize > minTextSizeSp.sp
                ) {
                    fontSize *= 0.95f
                    lineHeight *= 0.95f
                    result = textMeasurer.measure(
                        text = text,
                        style = style.copy(
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            textMotion = TextMotion.Animated,
                        ),
                        overflow = overflow,
                        maxLines = maxLines,
                        constraints = constraints,
                    )
                }
                realFontSize = fontSize
                realLineHeight = lineHeight
            }
    ) {
        Text(
            text = text,
            color = color,
            fontSize = realFontSize,
            lineHeight = realLineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            style = style.copy(textMotion = TextMotion.Animated),
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        )
    }
}

@Composable
fun AutoHeightText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    minTextSizeSp: Float = 2f,
) {
    var realFontSize by remember { mutableFloatStateOf(fontSize.takeOrElse { style.fontSize }.value) }
    var readyToDraw by remember { mutableStateOf(false) }
    var realOverflow by remember { mutableStateOf(overflow) }

    Text(
        text = text,
        color = color,
        fontSize = realFontSize.sp,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = realOverflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout@{
            if (!readyToDraw) {
                if (it.didOverflowHeight) {
                    val nextSize = realFontSize - 1f
                    if (nextSize > minTextSizeSp) {
                        realFontSize = nextSize
                        realOverflow = TextOverflow.Ellipsis
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            }
        },
        style = style,
        modifier = modifier.drawWithCache { onDrawWithContent { if (readyToDraw) drawContent() } }
    )
}

@Composable
fun AutoWidthText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    minTextSizeSp: Float = 2f,
) {
    var realFontSize by remember { mutableFloatStateOf(fontSize.takeOrElse { style.fontSize }.value) }
    var readyToDraw by remember { mutableStateOf(false) }
    var realOverflow by remember { mutableStateOf(overflow) }

    Text(
        text = text,
        color = color,
        fontSize = realFontSize.sp,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = realOverflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout@{
            if (!readyToDraw) {
                if (it.didOverflowWidth) {
                    val nextSize = realFontSize - 1f
                    if (nextSize > minTextSizeSp) {
                        realFontSize = nextSize
                        realOverflow = TextOverflow.Ellipsis
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            }
        },
        style = style,
        modifier = modifier.drawWithCache { onDrawWithContent { if (readyToDraw) drawContent() } }
    )
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    minTextSizeSp: Float = 2f,
) {
    var realFontSize by remember { mutableFloatStateOf(fontSize.takeOrElse { style.fontSize }.value) }
    var readyToDraw by remember { mutableStateOf(false) }
    var realOverflow by remember { mutableStateOf(overflow) }

    Text(
        text = text,
        color = color,
        fontSize = realFontSize.sp,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = realOverflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout@{
            if (!readyToDraw) {
                if (it.didOverflowHeight || it.didOverflowWidth) {
                    val nextSize = realFontSize - 1f
                    if (nextSize > minTextSizeSp) {
                        realFontSize = nextSize
                        realOverflow = TextOverflow.Ellipsis
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            }
        },
        style = style,
        modifier = modifier.drawWithCache { onDrawWithContent { if (readyToDraw) drawContent() } }
    )
}
