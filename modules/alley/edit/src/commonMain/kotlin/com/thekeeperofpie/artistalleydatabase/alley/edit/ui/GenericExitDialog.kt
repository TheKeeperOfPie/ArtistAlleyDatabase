package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_save_changes_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_action_exit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_action_save
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GenericExitDialog(
    onClickBack: () -> Unit,
    onClickSave: () -> Unit,
) {
    var showBackDialog by rememberSaveable { mutableStateOf(false) }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
    ) {
        showBackDialog = true
    }
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = {
                Text(stringResource(Res.string.alley_edit_artist_save_changes_title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackDialog = false
                        onClickSave()
                    },
                ) {
                    Text(stringResource(Res.string.alley_edit_image_save_changes_action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackDialog = false
                    onClickBack()
                }) {
                    Text(stringResource(Res.string.alley_edit_image_save_changes_action_exit))
                }
            }
        )
    }
}
