@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Build
import android.os.SystemClock
import android.text.Html
import android.text.NoCopySpan
import android.text.Selection
import android.text.Spannable
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.geometry.lerp
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.BuildCompat
import androidx.core.text.HtmlCompat
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asDrawable
import coil3.request.ImageRequest
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
fun NavMenuIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
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
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
                if (it != SwipeToDismissBoxValue.Settled) {
                    onErrorDismiss()
                }
                true
            })
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                },
            ) {
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
    endOffsetY: ContentDrawScope.() -> Float,
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
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = false,
                onExpandedChange = {},
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        },
        expanded = false,
        onExpandedChange = {},
        content = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
    var maxWidth: Int,
    private val onUpdate: (String) -> Unit,
) : Html.ImageGetter {

    private val loader = SingletonImageLoader.get(context)

    private val sourceToDrawable = mutableMapOf<String, Drawable>()

    private val brokenDrawable = context.getDrawable(R.drawable.baseline_broken_image_24)

    @OptIn(ExperimentalCoilApi::class)
    override fun getDrawable(source: String?): Drawable {
        if (source == null) return CustomDrawableWrapper.EMPTY
        val drawable = sourceToDrawable[source]
        if (drawable != null) {
            return drawable
        }
        val wrapper = CustomDrawableWrapper()

        loader.enqueue(
            ImageRequest.Builder(context)
                .data(source)
                .listener(
                    onSuccess = { request, result ->
                        val newDrawable = result.image.asDrawable(context.resources).apply {
                            val widthToHeightRatio = intrinsicHeight / intrinsicWidth
                            val width = intrinsicWidth.coerceAtMost(maxWidth)
                            setBounds(0, 0, width, width * widthToHeightRatio)
                        }
                        sourceToDrawable[source] = newDrawable
                        wrapper.delegate = newDrawable
                        onUpdate(source)
                    },
                    onError = { _, _ -> wrapper.delegate = brokenDrawable },
                )
                .build()
        )

        return wrapper
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

class LinkMovementMethodWithOnClick(private val onClickFallback: () -> Unit) :
    ScrollingMovementMethod() {
    override fun canSelectArbitrarily(): Boolean {
        return true
    }

    override fun handleMovementKey(
        widget: TextView, buffer: Spannable, keyCode: Int,
        movementMetaState: Int, event: KeyEvent,
    ): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> if (KeyEvent.metaStateHasNoModifiers(
                    movementMetaState
                )
            ) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && action(
                        CLICK,
                        widget,
                        buffer
                    )
                ) {
                    return true
                }
            }
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event)
    }

    override fun up(widget: TextView, buffer: Spannable): Boolean {
        return if (action(UP, widget, buffer)) true else super.up(widget, buffer)
    }

    override fun down(widget: TextView, buffer: Spannable): Boolean {
        return if (action(DOWN, widget, buffer)) true else super.down(widget, buffer)
    }

    override fun left(widget: TextView, buffer: Spannable): Boolean {
        return if (action(UP, widget, buffer)) true else super.left(widget, buffer)
    }

    override fun right(widget: TextView, buffer: Spannable): Boolean {
        return if (action(DOWN, widget, buffer)) true else super.right(widget, buffer)
    }

    private fun action(what: Int, widget: TextView, buffer: Spannable): Boolean {
        val layout = widget.layout
        val padding = widget.totalPaddingTop +
                widget.totalPaddingBottom
        val areaTop = widget.scrollY
        val areaBot = areaTop + widget.height - padding
        val lineTop = layout.getLineForVertical(areaTop)
        val lineBot = layout.getLineForVertical(areaBot)
        val first = layout.getLineStart(lineTop)
        val last = layout.getLineEnd(lineBot)
        val candidates = buffer.getSpans(
            first, last,
            ClickableSpan::class.java
        )
        val a = Selection.getSelectionStart(buffer)
        val b = Selection.getSelectionEnd(buffer)
        var selStart = Math.min(a, b)
        var selEnd = Math.max(a, b)
        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selEnd = buffer.length
                selStart = selEnd
            }
        }
        if (selStart > last) {
            selEnd = Int.MAX_VALUE
            selStart = selEnd
        }
        if (selEnd < first) {
            selEnd = -1
            selStart = selEnd
        }
        var bestStart: Int
        var bestEnd: Int
        when (what) {
            CLICK -> {
                if (selStart == selEnd) {
                    return false
                }
                val links = buffer.getSpans(
                    selStart, selEnd,
                    ClickableSpan::class.java
                )
                if (links.size != 1) {
                    return false
                }
                val link = links[0]
//                if (link is TextLinkSpan) {
//                    link.onClick(widget, TextLinkSpan.INVOCATION_METHOD_KEYBOARD)
//                } else {
                link.onClick(widget)
//                }
            }
            UP -> {
                bestStart = -1
                bestEnd = -1
                var i = 0
                while (i < candidates.size) {
                    val end = buffer.getSpanEnd(candidates[i])
                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestEnd) {
                            bestStart = buffer.getSpanStart(candidates[i])
                            bestEnd = end
                        }
                    }
                    i++
                }
                if (bestStart >= 0) {
                    Selection.setSelection(buffer, bestEnd, bestStart)
                    return true
                }
            }
            DOWN -> {
                bestStart = Int.MAX_VALUE
                bestEnd = Int.MAX_VALUE
                var i = 0
                while (i < candidates.size) {
                    val start = buffer.getSpanStart(candidates[i])
                    if (start > selStart || selStart == selEnd) {
                        if (start < bestStart) {
                            bestStart = start
                            bestEnd = buffer.getSpanEnd(candidates[i])
                        }
                    }
                    i++
                }
                if (bestEnd < Int.MAX_VALUE) {
                    Selection.setSelection(buffer, bestStart, bestEnd)
                    return true
                }
            }
        }
        return false
    }

    @BuildCompat.PrereleaseSdkCheck
    @OptIn(BuildCompat.PrereleaseSdkCheck::class)
    @SuppressLint("UnsafeOptInUsageError")
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (!BuildCompat.isAtLeastV()) {
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout = widget.layout
                val isOutOfLineBounds: Boolean = if (y < 0 || y > layout.height) {
                    true
                } else {
                    val line = layout.getLineForVertical(y)
                    (x < layout.getLineLeft(line)
                            || x > layout.getLineRight(line))
                }
                if (isOutOfLineBounds) {
                    Selection.removeSelection(buffer)
                    if (action == MotionEvent.ACTION_UP) {
                        onClickFallback()
                    }

                    // The same as super.onTouchEvent() in LinkMovementMethod.onTouchEvent(), i.e.
                    // ScrollingMovementMethod.onTouchEvent().
                    return Touch.onTouchEvent(widget, buffer, event)
                }
            }
        }
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(
                off, off,
                ClickableSpan::class.java
            )
            if (links.isNotEmpty()) {
                val link = links[0]
                if (action == MotionEvent.ACTION_UP) {
//                    if (link is TextLinkSpan) {
//                        link.onClick(
//                            widget, TextLinkSpan.INVOCATION_METHOD_TOUCH
//                        )
//                    } else {
                    link.onClick(widget)
//                    }
                } else {
                    if (widget.context.applicationInfo.targetSdkVersion
                        >= Build.VERSION_CODES.P
                    ) {
                        // Selection change will reposition the toolbar. Hide it for a few ms for a
                        // smoother transition.
//                        widget.hideFloatingToolbar(HIDE_FLOATING_TOOLBAR_DELAY_MS)
                    }
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link),
                        buffer.getSpanEnd(link)
                    )
                }
                return true
            } else {
                Selection.removeSelection(buffer)
                if (action == MotionEvent.ACTION_UP) {
                    onClickFallback()
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
        text.removeSpan(FROM_BELOW)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {
        Selection.removeSelection(text)
        if (dir and View.FOCUS_BACKWARD != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT)
        } else {
            text.removeSpan(FROM_BELOW)
        }
    }

    companion object {
        private const val CLICK = 1
        private const val UP = 2
        private const val DOWN = 3

        //        private const val HIDE_FLOATING_TOOLBAR_DELAY_MS = 200
        private val FROM_BELOW: Any = NoCopySpan.Concrete()
    }
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
    onClickFallback: () -> Unit = {},
) {
    val context = LocalContext.current
    var updateMillis by remember { mutableLongStateOf(-1L) }
    val screenWidth =
        LocalDensity.current.run { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val imageGetter = remember {
        ImageGetterWrapper(CoilImageGetter(context, maxWidth = screenWidth) {
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

    val textColor = color.takeOrElse { LocalContentColor.current }.toArgb()
    val linkMovementMethod = remember { LinkMovementMethodWithOnClick(onClickFallback) }
    AndroidView(
        modifier = modifier.onSizeChanged {
            (imageGetter.delegate as? CoilImageGetter)?.maxWidth = it.width
        },
        factory = {
            TextView(it).apply {
                if (maxLines != null) {
                    this.maxLines = maxLines
                }
                setTextColor(textColor)
                ellipsize = TextUtils.TruncateAt.END
                movementMethod = linkMovementMethod
            }
        },
        update = { it.text = htmlText },
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
fun rememberZoomPanState() = rememberSaveable(LocalDensity.current, saver = ZoomPanState.Saver) {
    ZoomPanState()
}

class ZoomPanState(
    initialTranslationX: Float = 0f,
    initialTranslationY: Float = 0f,
    initialScale: Float = 1f,
    var maxTranslationX: Float = 0f,
    var maxTranslationY: Float = 0f,
) {
    var transformableState = TransformableState { zoomChange, panChange, _ ->
        val translation = translation + panChange
        val scale = this.scale
        this.translation = translation.copy(
            x = translation.x.coerceIn(
                -maxTranslationX * (scale - 1f),
                maxTranslationX * (scale - 1f),
            ),
            y = translation.y.coerceIn(
                -maxTranslationY * (scale - 1f),
                maxTranslationY * (scale - 1f),
            ),
        )
        this.scale = (scale * zoomChange).coerceIn(1f, 5f)
    }

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

    suspend fun toggleZoom(offset: Offset, size: IntSize) {
        val scaleTarget: Float
        val translationTarget: Offset
        if (scale < 1.1f) {
            scaleTarget = 2.5f
            translationTarget = calculateZoomOffset(offset, size, scaleTarget)
        } else {
            scaleTarget = 1f
            translationTarget = Offset.Zero
        }
        transformableState.transform(MutatePriority.UserInput) {
            val scaleStart = scale
            val translationStart = translation
            Animatable(0f).animateTo(1f) {
                scale = lerp(scaleStart, scaleTarget, value)
                translation = lerp(translationStart, translationTarget, value)
            }
        }
    }

    private fun calculateZoomOffset(tapOffset: Offset, size: IntSize, scale: Float): Offset {
        val offsetX = (-(tapOffset.x - (size.width / 2f)) * 2f)
            .coerceIn(-maxTranslationX * (scale - 1f), maxTranslationX * (scale - 1f))
        val offsetY = (-(tapOffset.y - (size.height / 2f)) * 2f)
            .coerceIn(-maxTranslationY * (scale - 1f), maxTranslationY * (scale - 1f))
        return Offset(offsetX, offsetY)
    }
}

@Composable
fun ZoomPanBox(
    state: ZoomPanState = rememberZoomPanState(),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                state.maxTranslationX = it.width / 2f
                state.maxTranslationY = it.height / 2f
            }
            .transformable(
                state = state.transformableState,
                canPan = { state.scale > 1.1f },
                lockRotationOnZoomPan = true
            )
            .graphicsLayer(
                translationX = state.translation.x,
                translationY = state.translation.y,
                scaleX = state.scale,
                scaleY = state.scale,
            )
            .conditionally(onClick != null) {
                clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick!!,
                )
            },
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
