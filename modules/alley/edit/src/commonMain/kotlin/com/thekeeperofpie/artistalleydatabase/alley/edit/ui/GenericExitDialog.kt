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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_action_exit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_action_save
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenericExitDialog(
    onClickBack: () -> Unit,
    onClickSave: () -> Unit,
    saveErrorMessage: () -> String? = { null },
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
    ) {
        showDialog = true
    }
    if (showDialog) {
        val saveErrorMessage = saveErrorMessage()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(stringResource(Res.string.alley_edit_save_changes_title))
            },
            text = saveErrorMessage?.let { { Text(saveErrorMessage) } },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        if (saveErrorMessage == null) {
                            onClickSave()
                        }
                    },
                ) {
                    Text(
                        stringResource(
                            if (saveErrorMessage == null) {
                                Res.string.alley_edit_save_changes_action_save
                            } else {
                                Res.string.alley_edit_save_changes_action_cancel
                            }
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onClickBack()
                }) {
                    Text(stringResource(Res.string.alley_edit_save_changes_action_exit))
                }
            }
        )
    }
}
