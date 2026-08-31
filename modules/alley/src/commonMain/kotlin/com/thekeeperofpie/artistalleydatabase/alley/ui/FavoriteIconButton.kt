package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_no
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_yes
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Favorite
import com.thekeeperofpie.artistalleydatabase.icons.filled.FavoriteBorder
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoriteIconButton(
    entryText: () -> String?,
    favorite: () -> Boolean?,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        enabled = favorite() != null,
        onClick = {
            if (favorite() == true) {
                showDialog = true
            } else {
                onFavoriteToggle(true)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (favorite() == true) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            },
            contentDescription = stringResource(
                Res.string.alley_stamp_rally_favorite_icon_content_description
            ),
        )
    }

    if (showDialog) {
        val text = stringResource(Res.string.alley_unfavorite_dialog_text, entryText().orEmpty())
        UnfavoriteDialog(
            text = text,
            onDismissRequest = { showDialog = false },
            onRemoveFavorite = { onFavoriteToggle(false) },
        )
    }
}

@Composable
internal fun UnfavoriteDialog(
    text: String,
    onDismissRequest: () -> Unit,
    onRemoveFavorite: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onRemoveFavorite()
                    onDismissRequest()
                },
            ) {
                Text(stringResource(Res.string.alley_unfavorite_dialog_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.alley_unfavorite_dialog_no))
            }
        },
    )
}
