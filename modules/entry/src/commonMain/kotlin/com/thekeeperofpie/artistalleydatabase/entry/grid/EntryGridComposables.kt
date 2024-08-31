@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.entry.grid

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.delete_dialog_title
import artistalleydatabase.modules.entry.generated.resources.entry_search_edit_mode_clear
import artistalleydatabase.modules.entry.generated.resources.entry_search_edit_mode_delete
import artistalleydatabase.modules.entry.generated.resources.entry_search_edit_mode_edit
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint_edit_mode_selected
import artistalleydatabase.modules.utils_compose.generated.resources.cancel
import artistalleydatabase.modules.utils_compose.generated.resources.confirm
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import org.jetbrains.compose.resources.stringResource

@Composable
fun EntryGridAppBar(
    title: () -> String,
    upIconOption: UpIconOption,
    selectedItems: () -> Collection<Int>,
    onClickClear: () -> Unit,
    onClickEdit: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    val isEditMode by remember { derivedStateOf { selectedItems().isNotEmpty() } }
    TopAppBar(
        title = {
            Text(
                text = if (isEditMode) {
                    stringResource(
                        Res.string.entry_search_hint_edit_mode_selected,
                        selectedItems().size,
                    )
                } else {
                    title()
                },
                maxLines = 1
            )
        },
        navigationIcon = {
            if (isEditMode) {
                IconButton(onClick = onClickClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(
                            Res.string.entry_search_edit_mode_clear
                        ),
                    )
                }
            } else {
                UpIconButton(option = upIconOption)
            }
        },
        actions = {
            var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
            if (isEditMode) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            Res.string.entry_search_edit_mode_delete
                        )
                    )
                }

                if (showDeleteDialog) {
                    EntryGridDeleteDialog(
                        { showDeleteDialog = false },
                        onConfirmDelete
                    )
                }

                IconButton(onClick = onClickEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(
                            Res.string.entry_search_edit_mode_edit
                        )
                    )
                }
            }
        }
    )
}

@Composable
internal fun EntryGridDeleteDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_dialog_title)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirmDelete()
            }) {
                Text(stringResource(UtilsStrings.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(UtilsStrings.cancel))
            }
        },
    )
}
