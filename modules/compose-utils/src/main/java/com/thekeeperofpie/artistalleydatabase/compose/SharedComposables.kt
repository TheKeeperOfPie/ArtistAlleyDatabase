@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.SystemClock
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
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
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.debugInspectorInfo
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.method.LinkMovementMethodCompat
import coil.Coil
import coil.request.ImageRequest
import com.thekeeperofpie.compose_proxy.R
import de.charlex.compose.toAnnotatedString

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
fun NavMenuIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(
                R.string.nav_drawer_icon_content_description
            ),
        )
    }
}

@Composable
fun ArrowBackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(
                R.string.app_bar_back_icon_content_description
            ),
        )
    }
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
    if (errorRes != null) {
        if (onErrorDismiss == null) {
            Row(modifier = Modifier.fillMaxSize()) {
                SnackbarErrorTextInner(errorRes = errorRes, exception = exception)
            }
        } else {
            val dismissState = rememberDismissState(errorRes, onErrorDismiss)
            SwipeToDismiss(state = dismissState, background = {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                }
            }) {
                SnackbarErrorTextInner(errorRes = errorRes, exception = exception)
            }
        }
    }
}

@Composable
private fun RowScope.SnackbarErrorTextInner(
    @StringRes errorRes: Int,
    exception: Throwable?,
) {
    Text(
        text = stringResource(id = errorRes),
        color = MaterialTheme.colorScheme.onSecondary,
        modifier = Modifier
            .weight(1f)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            )
    )

    if (exception != null) {
        val errorString = stringResource(id = errorRes)
        TextButton(
            onClick = { Log.d("ArtistAlleyDatabase", errorString, exception) },
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(R.string.log_exception).uppercase(),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
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
                .clickable(false) {}
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
                    progress = progress ?: 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp)
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

@Suppress("UnnecessaryComposedModifier")
fun Modifier.topBorder(color: Color, width: Dp = Dp.Hairline): Modifier = border(
    width,
    color,
    startOffsetX = { width.value * density },
    startOffsetY = { 0f },
    endOffsetX = { size.width - (width.value * density / 2) },
    endOffsetY = { 0f }
)

@Suppress("UnnecessaryComposedModifier")
fun Modifier.bottomBorder(color: Color, width: Dp = Dp.Hairline): Modifier = border(
    width,
    color,
    startOffsetX = { 0f },
    startOffsetY = { size.height - (width.value / 2 * density) },
    endOffsetX = { size.width },
    endOffsetY = { size.height - (width.value / 2 * density) }
)

@Suppress("UnnecessaryComposedModifier")
fun Modifier.border(
    width: Dp = Dp.Hairline,
    color: Color,
    startOffsetX: ContentDrawScope.() -> Float,
    startOffsetY: ContentDrawScope.() -> Float,
    endOffsetX: ContentDrawScope.() -> Float,
    endOffsetY: ContentDrawScope.() -> Float
): Modifier = composed(
    factory = {
        this.then(
            Modifier.drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawLine(
                        color = color,
                        start = Offset(startOffsetX(), startOffsetY()),
                        end = Offset(endOffsetX(), endOffsetY()),
                        strokeWidth = width.value * density,
                    )
                }
            }
        )
    },
    inspectorInfo = debugInspectorInfo {
        name = "border"
        properties["width"] = width
        properties["color"] = color.value
        value = color
        properties["shape"] = RectangleShape
    }
)

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

/**
 * Copy of [ExposedDropdownMenuDefaults.TrailingIcon] to allow custom content descriptions.
 */
@Composable
fun TrailingDropdownIcon(
    expanded: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ArrowDropDown,
    iconTint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = icon,
        tint = iconTint,
        contentDescription = contentDescription,
        modifier = modifier.rotate(if (expanded) 180f else 0f)
    )
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
                .menuAnchor()
                .clickable(false) {},
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
fun AutoResizeHeightText(
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
    val initialFontSize = fontSize.takeOrElse { style.fontSize }.value
    var realFontSize by remember { mutableStateOf(initialFontSize) }
    val initialLineHeight = lineHeight.takeOrElse { style.lineHeight }.value
    var realLineHeight by remember { mutableStateOf(initialLineHeight) }
    var readyToDraw by remember { mutableStateOf(false) }

    var stillCalculating by remember { mutableStateOf(true) }
    var decreasing by remember { mutableStateOf(true) }

    val cachedFontSizes = remember { mutableStateMapOf<IntSize, Float>() }
    var lastSize by remember { mutableStateOf<IntSize?>(null) }
    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .onSizeChanged {
                lastSize = boxSize
                val cachedFontSize = cachedFontSizes[boxSize]
                if (cachedFontSize != null) {
                    realFontSize = cachedFontSize
                    realLineHeight = cachedFontSize / initialFontSize * initialLineHeight
                    boxSize = it
                    stillCalculating = false
                    return@onSizeChanged
                }
                decreasing = it.height < boxSize.height || it.width < boxSize.width
                stillCalculating = true
                boxSize = it
            }
    ) {
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
            lineHeight = realLineHeight.sp,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout@{
                if (stillCalculating) {
                    val scale = if (it.didOverflowHeight
                        || (it.lineCount == 1 && it.didOverflowWidth)
                    ) {
                        if (decreasing) {
                            0.9f
                        } else {
                            // Reset to decreasing if overflowed since
                            // it doesn't make sense to increase from here
                            decreasing = true
                            0.9f
                        }
                    } else if (!decreasing) {
                        1 / 0.9f
                    } else 1f

                    if (scale != 1f) {
                        val nextSize = realFontSize * scale
                        if (nextSize > minTextSizeSp && nextSize <= initialFontSize) {
                            realFontSize = nextSize
                            realLineHeight *= scale
                            return@onTextLayout
                        }
                    }

                    stillCalculating = false
                    cachedFontSizes.putIfAbsent(boxSize, realFontSize)
                    readyToDraw = true
                }
            },
            style = style,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .drawWithCache { onDrawWithContent { if (readyToDraw) drawContent() } }
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
    var realFontSize by remember { mutableStateOf(fontSize.takeOrElse { style.fontSize }.value) }
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
    var realFontSize by remember { mutableStateOf(fontSize.takeOrElse { style.fontSize }.value) }
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
    var realFontSize by remember { mutableStateOf(fontSize.takeOrElse { style.fontSize }.value) }
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
fun StaticSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onSearch: (String) -> Unit = {},
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = false,
        onActiveChange = {},
        leadingIcon = leadingIcon,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        content = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
    val annotatedString = Html.fromHtml(text.trim(), Html.FROM_HTML_MODE_LEGACY)
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

private class CoilImageGetter(
    private val context: Context,
    private val onUpdate: () -> Unit,
) : Html.ImageGetter {
    private val loader = Coil.imageLoader(context)

    private val sourceToDrawable = mutableMapOf<String, Drawable>()

    private val brokenDrawable = context.getDrawable(R.drawable.baseline_broken_image_24)

    override fun getDrawable(source: String?): Drawable? {
        if (source == null) return CustomDrawableWrapper.EMPTY
        val drawable = sourceToDrawable[source]
        if (drawable != null) {
            return drawable
        }

        loader.enqueue(
            ImageRequest.Builder(context)
                .data(source)
                .listener(onSuccess = { _, result ->
                    sourceToDrawable[source] = result.drawable.apply {
                        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    }
                    onUpdate()
                })
                .build()
        )

        return brokenDrawable
    }
}

private class CustomDrawableWrapper : DrawableWrapper(null) {

    companion object {
        val EMPTY = CustomDrawableWrapper()
    }

    var delegate: Drawable? = null
        set(value) {
            field = value ?: return
            val width = value.intrinsicWidth
            val height = value.intrinsicHeight
            value.setBounds(0, 0, width, height)
            setBounds(0, 0, width, height)
        }

    override fun draw(canvas: Canvas) {
        delegate?.draw(canvas)
    }
}

private class ImageGetterWrapper(var delegate: Html.ImageGetter? = null) : Html.ImageGetter {
    override fun getDrawable(source: String?) =
        source?.let { delegate?.getDrawable(source) } ?: CustomDrawableWrapper.EMPTY
}

/**
 * [Text] doesn't support images, so instead use a [TextView].
 */
@Composable
fun ImageHtmlText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    maxLines: Int? = null,
) {
    val context = LocalContext.current
    var updateMillis by remember { mutableLongStateOf(-1L) }
    val imageGetter = remember {
        ImageGetterWrapper(CoilImageGetter(context) {
            updateMillis = SystemClock.uptimeMillis()
        })
    }
    DisposableEffect(context) {
        onDispose { imageGetter.delegate = null }
    }

    val htmlText = remember(text, updateMillis) {
        HtmlCompat.fromHtml(text.trim(), 0, imageGetter, null)
            .trim()
    }

    AndroidView(
        modifier = modifier,
        factory = ::TextView,
        update = {
            it.text = htmlText
            if (maxLines != null) {
                it.maxLines = maxLines
            }
            it.setTextColor(color.toArgb())
            it.ellipsize = TextUtils.TruncateAt.END
            it.movementMethod = LinkMovementMethodCompat.getInstance()
            it.setTextIsSelectable(true)
        }
    )
}

@Composable
fun DetailsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    onClickViewAll: (() -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (onClickViewAll != null) {
        Row(modifier = modifier) {
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
                    imageVector = Icons.Filled.OpenInNew,
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
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
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

/**
 * @return True if anything shown
 */
@Composable
fun twoColumnInfoText(
    labelOne: String, bodyOne: String?, onClickOne: (() -> Unit)? = null,
    labelTwo: String, bodyTwo: String?, onClickTwo: (() -> Unit)? = null,
    showDividerAbove: Boolean = true
): Boolean {
    if (!bodyOne.isNullOrBlank() && !bodyTwo.isNullOrBlank()) {
        if (showDividerAbove) {
            Divider()
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
        Divider()
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
    @StringRes contentDescriptionTextRes: Int,
    values: List<T>,
    valueToText: @Composable (T) -> String,
    onClick: ((T) -> Unit)? = null,
    showDividerAbove: Boolean = true,
    allowExpand: Boolean = values.size > 3
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
                Divider()
            }

            DetailsSubsectionHeader(stringResource(labelTextRes))

            values.take(if (expanded) Int.MAX_VALUE else 3).forEachIndexed { index, value ->
                if (index != 0) {
                    Divider(modifier = Modifier.padding(start = 16.dp))
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
                            top = 8.dp,
                            bottom = bottomPadding,
                        )
                )
            }
        }

        if (showExpand) {
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(contentDescriptionTextRes),
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }

    return true
}

@Composable
fun rememberZoomPanState() = rememberSaveable(LocalDensity.current, saver = ZoomPanState.Saver) {
    ZoomPanState()
}

class ZoomPanState(
    initialTranslationX: Float = 0f,
    initialTranslationY: Float = 0f,
    initialScale: Float = 1f,
) {
    companion object {
        val Saver: Saver<ZoomPanState, *> = listSaver(
            save = { listOf(it.translation.x, it.translation.y, it.scale) },
            restore = {
                ZoomPanState(
                    initialTranslationX = it[0],
                    initialTranslationY = it[1],
                    initialScale = it[2],
                )
            }
        )
    }

    var translation by mutableStateOf(Offset(initialTranslationX, initialTranslationY))
    var scale by mutableFloatStateOf(initialScale)

    fun canPanExternal(): Boolean {
        return scale < 1.1f
    }

    fun toggleZoom(offset: Offset, size: IntSize) {
        if (scale < 1.1f) {
            scale = 2.5f
            translation = calculateZoomOffset(offset, size)
        } else {
            scale = 1f
            translation = Offset.Zero
        }
    }

    private fun calculateZoomOffset(tapOffset: Offset, size: IntSize): Offset {
        val offsetX = (-(tapOffset.x - (size.width / 2f)) * 2f)
            .coerceIn(-size.width / 2f, size.width / 2f)
        val offsetY = (-(tapOffset.y - (size.height / 2f)) * 2f)
            .coerceIn(-size.height / 2f, size.height / 2f)
        return Offset(offsetX, offsetY)
    }
}

@Composable
fun ZoomPanBox(
    state: ZoomPanState = rememberZoomPanState(),
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    var maxTranslationX by remember(density) { mutableFloatStateOf(0f) }
    var maxTranslationY by remember(density) { mutableFloatStateOf(0f) }
    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            val translation = state.translation + panChange
            val scale = state.scale
            state.translation = translation.copy(
                x = translation.x.coerceIn(
                    -maxTranslationX * (scale - 1f),
                    maxTranslationX * (scale - 1f)
                ),
                y = translation.y.coerceIn(
                    -maxTranslationY * (scale - 1f),
                    maxTranslationY * (scale - 1f)
                ),
            )
            state.scale = (scale * zoomChange).coerceIn(1f, 5f)
        }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                maxTranslationX = it.width / 2f
                maxTranslationY = it.height / 2f
            }
            .transformable(
                state = transformableState,
                canPan = { state.scale > 1.1f },
                lockRotationOnZoomPan = true
            )
            .graphicsLayer(
                translationX = state.translation.x,
                translationY = state.translation.y,
                scaleX = state.scale,
                scaleY = state.scale,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        content = content,
    )
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
        interactionSource: InteractionSource
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
