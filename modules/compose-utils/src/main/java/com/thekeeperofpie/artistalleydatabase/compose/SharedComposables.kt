@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.text.Html
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.utils_compose.topBorder
import com.thekeeperofpie.compose_proxy.R
import de.charlex.compose.toAnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    text: String,
    upIconOption: UpIconOption? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    TopAppBar(
        title = { Text(text = text, maxLines = 1) },
        navigationIcon = {
            if (upIconOption != null) {
                UpIconButton(option = upIconOption)
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors,
    )
}

@Composable
fun ButtonFooter(onClick: () -> Unit, @StringRes textRes: Int) {
    ButtonFooter(textRes to onClick)
}

@Composable
fun ButtonFooter(vararg pairs: Pair<Int, () -> Unit>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .topBorder(color = MaterialTheme.colorScheme.inversePrimary, width = 1.dp)
    ) {
        pairs.forEach { (stringRes, onClick) ->
            TextButton(onClick = onClick) {
                Text(
                    stringResource(stringRes),
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
fun SnackbarErrorText(
    @StringRes errorRes: Int?,
    exception: Throwable?,
    onErrorDismiss: (() -> Unit)? = null,
) {
    errorRes ?: return
    SnackbarErrorText(
        error = { stringResource(errorRes) },
        exception = exception,
        onErrorDismiss = onErrorDismiss,
    )
}

@Composable
fun ChooseUriRow(
    @StringRes label: Int,
    uriString: String,
    onUriStringEdit: (String) -> Unit = {},
    onClickChoose: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClickChoose)
    ) {
        OutlinedTextField(
            value = uriString,
            onValueChange = onUriStringEdit,
            readOnly = true,
            label = { Text(stringResource(label)) },
            modifier = Modifier
                .weight(1f, true)
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        )

        IconButton(
            onClick = onClickChoose,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(
                    R.string.select_export_destination_content_description
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LinearProgressWithIndicator(text: String, progress: Float?) {
    AnimatedVisibility(
        visible = progress != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = 16.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )

                LinearProgressIndicator(
                    progress = { progress ?: 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp),
                )
            }

            Crossfade(targetState = progress == 1f, label = "Progress check fill fade") {
                Icon(
                    imageVector = if (it) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.CheckCircle
                    },
                    contentDescription = stringResource(R.string.progress_complete_content_description),
                    modifier = Modifier
                        .alpha(if (it) 1f else 0.38f)
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

/**
 * Copy of [ExposedDropdownMenuDefaults.TrailingIcon] to allow custom content descriptions.
 */
@Composable
fun TrailingDropdownIconButton(
    expanded: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ArrowDropDown,
    iconTint: Color = LocalContentColor.current,
    onClick: () -> Unit = {},
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            tint = iconTint,
            contentDescription = contentDescription,
            modifier = Modifier.rotate(if (expanded) 180f else 0f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ItemDropdown(
    value: T,
    @StringRes iconContentDescription: Int,
    modifier: Modifier = Modifier,
    @StringRes label: Int? = null,
    values: @Composable () -> Iterable<T> = { emptyList() },
    textForValue: @Composable (T) -> String = { "" },
    iconForValue: @Composable ((T) -> Unit)? = null,
    onSelectItem: (T) -> Unit = {},
    wrapWidth: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
) {
    var expanded by remember { mutableStateOf(false) }
    fun Modifier.wrapWidthIfRequested() = if (wrapWidth) wrapContentWidth() else fillMaxWidth()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.wrapWidthIfRequested(),
    ) {
        TextField(
            value = textForValue(value),
            onValueChange = {},
            readOnly = true,
            maxLines = maxLines,
            label = label?.let { { Text(stringResource(it)) } },
            leadingIcon = iconForValue?.let { { iconForValue(value) } },
            trailingIcon = {
                TrailingDropdownIcon(
                    expanded = expanded,
                    contentDescription = stringResource(iconContentDescription),
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .wrapWidthIfRequested()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapWidthIfRequested()
        ) {
            values().forEach { value ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectItem(value)
                    },
                    leadingIcon = iconForValue?.let { { iconForValue(value) } },
                    text = { Text(textForValue(value)) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.wrapWidthIfRequested(),
                )
            }
        }
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(DividerDefaults.Thickness)
            .background(color = DividerDefaults.color)
    )
}

@Composable
fun CustomHtmlText(
    modifier: Modifier = Modifier,
    text: String,
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
) {
    val annotatedString = remember(text) {
        Html.fromHtml(text.trim(), Html.FROM_HTML_MODE_LEGACY)
            .trim()
            .toAnnotatedString(urlSpanStyle, colorMapping)
            .let {
                if (minLines > 1 && maxLines < Int.MAX_VALUE) {
                    buildAnnotatedString {
                        append(it)
                        repeat(minLines - 1) {
                            append("\n")
                        }
                    }
                } else it
            }
    }

    HtmlText(
        modifier = modifier,
        annotatedString = annotatedString,
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
private fun HtmlText(
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
    @StringRes labelTextRes: Int,
    @StringRes contentDescriptionTextRes: Int?,
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

@Composable
fun ClickableBottomSheetDragHandle(scope: CoroutineScope, sheetState: SheetState) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .fillMaxWidth()
        .clickable {
            if (sheetState.currentValue == SheetValue.Expanded) {
                scope.launch {
                    try {
                        sheetState.hide()
                    } catch (ignored: Throwable) {
                        sheetState.partialExpand()
                    }
                }
            } else {
                scope.launch { sheetState.expand() }
            }
        }
    ) {
        BottomSheetDefaults.DragHandle()
    }
}
