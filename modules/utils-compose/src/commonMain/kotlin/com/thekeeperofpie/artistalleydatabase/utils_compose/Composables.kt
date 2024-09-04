package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.log_exception
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.stringResource

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
                text = stringResource(Res.string.log_exception).uppercase(),
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
