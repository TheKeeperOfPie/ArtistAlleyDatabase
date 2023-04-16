package com.thekeeperofpie.artistalleydatabase.compose

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import com.thekeeperofpie.compose_proxy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    text: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onClickNav: (() -> Unit)? = null,
    onClickBack: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(text = text, maxLines = 1) },
        navigationIcon = {
            onClickNav?.let { NavMenuIconButton(it) }
                ?: onClickBack?.let { ArrowBackIconButton(it) }
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
    exception: Exception?,
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
    exception: Exception?,
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
    startOffsetX = { width.value * density },
    startOffsetY = { size.height - (width.value * density) },
    endOffsetX = { size.width - (width.value * density) },
    endOffsetY = { size.height }
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
    value: String,
    @StringRes iconContentDescription: Int,
    modifier: Modifier = Modifier,
    @StringRes label: Int? = null,
    values: @Composable () -> Iterable<T> = { emptyList() },
    textForValue: @Composable (T) -> String = { "" },
    onSelectItem: (T) -> Unit = {},
    wrapWidth: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    fun Modifier.wrapWidthIfRequested() = if (wrapWidth) wrapContentWidth() else fillMaxWidth()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.wrapWidthIfRequested(),
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = label?.let { { Text(stringResource(it)) } },
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
            values().forEach {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectItem(it)
                    },
                    text = { Text(textForValue(it)) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.wrapWidthIfRequested(),
                )
            }
        }
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