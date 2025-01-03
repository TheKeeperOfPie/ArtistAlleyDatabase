@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.log_exception
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SnackbarErrorText(
    errorRes: StringResource?,
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
fun SnackbarErrorText(
    error: @Composable () -> String?,
    exception: Throwable?,
    onErrorDismiss: (() -> Unit)? = null,
) {
    val errorMessage = error() ?: return
    if (onErrorDismiss == null) {
        Row(modifier = Modifier.fillMaxSize()) {
            SnackbarErrorTextInner(error = { errorMessage }, exception = exception)
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
            SnackbarErrorTextInner(error = { errorMessage }, exception = exception)
        }
    }
}

@Composable
private fun RowScope.SnackbarErrorTextInner(
    error: @Composable () -> String,
    exception: Throwable?,
) {
    val errorString = error()
    Text(
        text = errorString,
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
        TextButton(
            onClick = { Logger.d("ArtistAlleyDatabase", exception) { errorString } },
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(Res.string.log_exception).toUpperCase(Locale.current),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
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

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(DividerDefaults.Thickness)
            .background(color = DividerDefaults.color)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ItemDropdown(
    value: T,
    iconContentDescription: StringResource,
    modifier: Modifier = Modifier,
    label: StringResource? = null,
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
fun ButtonFooter(onClick: () -> Unit, textRes: StringResource) {
    ButtonFooter(textRes to onClick)
}

@Composable
fun ButtonFooter(vararg pairs: Pair<StringResource, () -> Unit>) {
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
