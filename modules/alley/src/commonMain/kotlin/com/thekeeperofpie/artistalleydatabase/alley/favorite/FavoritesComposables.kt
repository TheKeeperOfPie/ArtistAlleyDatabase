package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_no
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_yes
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun <T : SearchScreen.SearchEntryModel> UnfavoriteDialog(
    entry: () -> T?,
    onClearEntry: () -> Unit,
    onRemoveFavorite: (T) -> Unit,
) {
    val entry = entry()
    if (entry != null) {
        val name = when (entry) {
            is ArtistEntryGridModel -> entry.artist.name
            is StampRallyEntryGridModel ->
                "${entry.stampRally.hostTable}-${entry.stampRally.fandom}"
            else -> throw IllegalArgumentException()
        }
        val text = stringResource(Res.string.alley_unfavorite_dialog_text, name)
        UnfavoriteDialog(
            text = text,
            onDismissRequest = onClearEntry,
            onRemoveFavorite = { onRemoveFavorite(entry) }
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
