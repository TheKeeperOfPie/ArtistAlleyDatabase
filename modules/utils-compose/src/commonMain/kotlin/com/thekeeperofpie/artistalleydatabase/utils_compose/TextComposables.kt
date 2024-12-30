package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

@Composable
expect fun CustomHtmlText(
    text: String,
    modifier: Modifier = Modifier,
    urlSpanStyle: SpanStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        textDecoration = TextDecoration.Underline
    ),
    colorMapping: Map<Color, Color> = emptyMap(),
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
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    onFallbackClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    detectTaps: Boolean = true,
)

@Composable
internal fun HtmlText(
    modifier: Modifier = Modifier,
    annotatedString: AnnotatedString,
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
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    onFallbackClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    detectTaps: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val urls = remember(layoutResult, annotatedString) {
        annotatedString.getStringAnnotations("url", 0, annotatedString.lastIndex)
    }

    Text(
        modifier = modifier.then(
            Modifier
                .conditionally(detectTaps) {
                    pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { pos ->
                                layoutResult.value?.let { layoutResult ->
                                    val position = layoutResult.getOffsetForPosition(pos)
                                    val annotated = annotatedString
                                        .getStringAnnotations(position, position)
                                        .firstOrNull()
                                    if (annotated?.tag == "url") {
                                        uriHandler.openUri(annotated.item)
                                    } else {
                                        onFallbackClick()
                                    }
                                }
                            },
                            onLongPress = if (onLongClick != null) {
                                { onLongClick() }
                            } else null,
                        )
                    }
                        .semantics {
                            if (urls.size == 1) {
                                role = Role.Button
                                onClick(
                                    "Link (${
                                        annotatedString.substring(
                                            urls[0].start,
                                            urls[0].end
                                        )
                                    }"
                                ) {
                                    uriHandler.openUri(urls[0].item)
                                    true
                                }
                            } else {
                                customActions = urls.map {
                                    CustomAccessibilityAction(
                                        "Link (${
                                            annotatedString.substring(
                                                it.start,
                                                it.end
                                            )
                                        })"
                                    ) {
                                        uriHandler.openUri(it.item)
                                        true
                                    }
                                }
                            }
                        }
                }
        ),
        text = annotatedString,
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
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
        style = style
    )
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
    viewAllContentDescriptionTextRes: StringResource? = null,
    useHorizontalPadding: Boolean = true,
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
                    .padding(
                        start = if (useHorizontalPadding) 16.dp else 0.dp,
                        end = if (useHorizontalPadding) 16.dp else 0.dp,
                        top = 16.dp,
                        bottom = 10.dp,
                    ),
            )
            IconButton(
                onClick = onClickViewAll,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = viewAllContentDescriptionTextRes?.let {
                        stringResource(it)
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
                .padding(
                    start = if (useHorizontalPadding) 16.dp else 0.dp,
                    end = if (useHorizontalPadding) 16.dp else 0.dp,
                    top = 16.dp,
                    bottom = 10.dp,
                )
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

/**
 * @return True if anything shown
 */
@Composable
fun twoColumnInfoText(
    labelOne: String, bodyOne: String?, onClickOne: (() -> Unit)? = null,
    labelTwo: String, bodyTwo: String?, onClickTwo: (() -> Unit)? = null,
    showDividerAbove: Boolean = true,
): Boolean {
    if (!bodyOne.isNullOrBlank() && !bodyTwo.isNullOrBlank()) {
        if (showDividerAbove) {
            HorizontalDivider()
        }
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .optionalClickable(onClickOne)
            ) {
                InfoText(label = labelOne, body = bodyOne, showDividerAbove = false)
            }

            VerticalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .optionalClickable(onClickTwo)
            ) {
                InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = false)
            }
        }
    } else if (!bodyOne.isNullOrBlank()) {
        Column(modifier = Modifier.optionalClickable(onClickOne)) {
            InfoText(label = labelOne, body = bodyOne, showDividerAbove = showDividerAbove)
        }
    } else if (!bodyTwo.isNullOrBlank()) {
        Column(modifier = Modifier.optionalClickable(onClickTwo)) {
            InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = showDividerAbove)
        }
    } else {
        return false
    }

    return true
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.InfoText(
    label: String,
    body: String,
    showDividerAbove: Boolean = true,
) {
    if (showDividerAbove) {
        HorizontalDivider()
    }

    DetailsSubsectionHeader(label)

    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
    )
}

/**
 * @return True if anything shown
 */
@Composable
fun <T> expandableListInfoText(
    labelTextRes: StringResource,
    contentDescriptionTextRes: StringResource?,
    values: List<T>,
    valueToText: @Composable (T) -> String,
    onClick: ((T) -> Unit)? = null,
    showDividerAbove: Boolean = true,
    allowExpand: Boolean = values.size > 3,
    header: (@Composable () -> Unit)? = { DetailsSubsectionHeader(stringResource(labelTextRes)) },
): Boolean {
    if (values.isEmpty()) return false

    var expanded by remember { mutableStateOf(!allowExpand) }
    val showExpand = allowExpand && values.size > 3

    Box {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .conditionally(showExpand) {
                    clickable { expanded = !expanded }
                        .fadingEdgeBottom(show = !expanded)
                        .animateContentSize()
                }
        ) {
            if (showDividerAbove) {
                HorizontalDivider()
            }

            header?.invoke()

            values.take(if (expanded) Int.MAX_VALUE else 3).forEachIndexed { index, value ->
                if (index != 0) {
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                }

                val bottomPadding = if (index == values.size - 1) {
                    12.dp
                } else {
                    8.dp
                }

                Text(
                    text = valueToText(value),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .optionalClickable(
                            onClick = onClick
                                ?.takeIf { expanded }
                                ?.let { { onClick(value) } }
                        )
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            // If only 1 value, mirror InfoText
                            top = if (values.size == 1) 0.dp else 8.dp,
                            bottom = bottomPadding,
                        )
                )
            }
        }

        if (showExpand) {
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = contentDescriptionTextRes?.let { stringResource(it) },
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }

    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinWidthTextField(
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
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    minWidth: Dp,
) {
    @Composable
    fun TextFieldColors.textColor(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource,
    ): State<Color> {
        val focused by interactionSource.collectIsFocusedAsState()

        val targetValue = when {
            !enabled -> disabledTextColor
            isError -> errorTextColor
            focused -> focusedTextColor
            else -> unfocusedTextColor
        }
        return rememberUpdatedState(targetValue)
    }

    @Composable
    fun TextFieldColors.cursorColor(isError: Boolean): State<Color> {
        return rememberUpdatedState(if (isError) errorCursorColor else cursorColor)
    }

    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled, isError, interactionSource).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            modifier = modifier
                .defaultMinSize(
                    minWidth = minWidth,
                    minHeight = TextFieldDefaults.MinHeight
                ),
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError).value),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            decorationBox = @Composable { innerTextField ->
                // places leading icon, text field with label and placeholder, trailing icon
                TextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                )
            }
        )
    }
}
