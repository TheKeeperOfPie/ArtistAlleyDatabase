package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_delete_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_edit_tooltip
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

context(formScope: EntryFormScope)
@Composable
internal fun LinksSection(
    state: EntryForm2.SingleTextState,
    title: StringResource,
    header: @Composable () -> Unit,
    listRevertDialogState: ListRevertDialogState<LinkModel>,
    items: SnapshotStateList<LinkModel>,
    label: @Composable (() -> Unit)?,
    pendingErrorMessage: () -> String?,
) {
    MultiTextSection(
        state = state,
        header = header,
        items = items,
        itemToCommitted = LinkModel::parse,
        removeLastItem = { items.removeLastOrNull()?.link },
        item = { index, value ->
            LinkRow(
                link = value,
                isLast = index == items.lastIndex && !state.lockState.editable,
                color = if (listRevertDialogState.initialItems?.contains(value) != false) {
                    Color.Unspecified
                } else {
                    AlleyTheme.colorScheme.positive
                },
                additionalActions = {
                    FormEditActions(
                        state = state,
                        forceLocked = formScope.forceLocked,
                        items = items,
                        item = value,
                        itemToText = LinkModel::link,
                    )
                },
            )
        },
        label = label,
        inputTransformation = InputTransformation {
            if (asCharSequence().any { it.isWhitespace() || it == ',' }) {
                revertAllChanges()
            }
        },
        pendingErrorMessage = pendingErrorMessage,
        additionalHeaderActions = {
            with(formScope) {
                ShowListRevertIconButton(listRevertDialogState, items)
            }
        },
    )

    ListFieldRevertDialog(
        dialogState = listRevertDialogState,
        label = title,
        items = items,
        itemsToText = { it.joinToString { it.link } },
    )
}

@Composable
fun <T> RowScope.FormEditActions(
    state: EntryForm2.SingleTextState,
    forceLocked: Boolean,
    items: SnapshotStateList<T>,
    item: T,
    itemToText: (T) -> String,
) {
    AnimatedVisibility(
        visible = state.lockState.editable && !forceLocked,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        TooltipIconButton(
            icon = Icons.Default.Edit,
            tooltipText = stringResource(Res.string.alley_edit_row_edit_tooltip),
            onClick = {
                items.remove(item)
                state.value.setTextAndPlaceCursorAtEnd(itemToText(item))
                state.focusRequester.requestFocus()
            },
        )
    }

    AnimatedVisibility(
        visible = state.lockState.editable && !forceLocked,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        TooltipIconButton(
            icon = Icons.Default.Delete,
            tooltipText = stringResource(Res.string.alley_edit_row_delete_tooltip),
            onClick = { items.remove(item) },
        )
    }
}
