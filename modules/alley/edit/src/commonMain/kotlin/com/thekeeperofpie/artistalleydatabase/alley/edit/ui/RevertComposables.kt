package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_revert_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_revert_action_dismiss
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_revert_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_revert_tooltip
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Stable
internal class RevertDialogState(
    positiveColor: Color,
    initialEntry: Any?,
    val initialValue: String,
) {
    var show by mutableStateOf(false)
    val outputTransformation: OutputTransformation? = if (initialEntry == null) null else {
        GreenOnChangedOutputTransformation(positiveColor, initialValue)
    }
}

@Composable
internal fun <T> rememberListRevertDialogState(initialItems: List<T>?) =
    remember(initialItems) { ListRevertDialogState(initialItems) }

@Stable
internal class ListRevertDialogState<T>(val initialItems: List<T>?) {
    var show by mutableStateOf(false)
}

context(scope: EntryFormScope)
@Composable
internal fun ShowRevertIconButton(
    dialogState: RevertDialogState,
    textState: EntryForm2.SingleTextState,
) {
    val show by remember(dialogState, textState) {
        derivedStateOf { textState.value.text.toString() != dialogState.initialValue }
    }
    if (show && !scope.forceLocked) {
        TooltipIconButton(
            icon = Icons.Default.History,
            tooltipText = stringResource(Res.string.alley_edit_revert_tooltip),
            onClick = { dialogState.show = true },
        )
    }
}

context(scope: EntryFormScope)
@Composable
internal fun <T> ShowListRevertIconButton(
    dialogState: ListRevertDialogState<T>,
    items: SnapshotStateList<T>,
) {
    val show by remember(items, dialogState) {
        derivedStateOf {
            items.toList().toSet() != dialogState.initialItems?.toSet()
        }
    }
    if (show && !scope.forceLocked) {
        TooltipIconButton(
            icon = Icons.Default.History,
            tooltipText = stringResource(Res.string.alley_edit_revert_tooltip),
            onClick = { dialogState.show = true },
        )
    }
}

@Immutable
private class GreenOnChangedOutputTransformation(
    private val color: Color,
    private val initialValue: String,
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        if (originalText.toString() != initialValue) {
            addStyle(SpanStyle(color = color), 0, length)
        }
    }
}

@Composable
internal fun <T> ListFieldRevertDialog(
    dialogState: ListRevertDialogState<T>,
    label: StringResource,
    items: SnapshotStateList<T>,
    itemsToText: (List<T>) -> String,
) {
    if (dialogState.show) {
        RevertDialog(
            label = label,
            text = itemsToText(dialogState.initialItems.orEmpty()),
            onDismiss = { dialogState.show = false },
            onRevert = { dialogState.initialItems?.toList()?.let(items::replaceAll) },
        )
    }
}

@Composable
internal fun FieldRevertDialog(
    dialogState: RevertDialogState,
    textState: EntryForm2.SingleTextState,
    label: StringResource,
) {
    if (dialogState.show) {
        RevertDialog(
            label = label,
            text = dialogState.initialValue,
            onDismiss = { dialogState.show = false },
            onRevert = { textState.value.setTextAndPlaceCursorAtEnd(dialogState.initialValue) },
        )
    }
}

@Composable
private fun RevertDialog(
    label: StringResource,
    text: String,
    onDismiss: () -> Unit,
    onRevert: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onRevert()
                onDismiss()
            }) {
                Text(stringResource(Res.string.alley_edit_revert_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.alley_edit_revert_action_dismiss))
            }
        },
        icon = { Icon(Icons.Default.History, null) },
        title = {
            Text(
                stringResource(
                    Res.string.alley_edit_revert_title,
                    stringResource(label),
                )
            )
        },
        text = if (text.isNotBlank()) {
            { Text(text) }
        } else null,
    )
}
