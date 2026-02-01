package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_delete_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_duplicate_entry
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesPrediction
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm.AddUniqueErrorState
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FormSaveButton(
    enabled: Boolean,
    saveTaskState: TaskState<*>,
    tooltip: StringResource,
    onClickSave: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        val isSaving = saveTaskState.isActive && !saveTaskState.isManualTrigger
        TooltipIconButton(
            icon = Icons.Default.Save,
            tooltipText = stringResource(tooltip),
            enabled = enabled,
            onClick = onClickSave,
            modifier = Modifier.alpha(if (isSaving) 0.5f else 1f)
        )

        AnimatedVisibility(
            visible = isSaving,
            modifier = Modifier.matchParentSize()
        ) {
            CircularWavyProgressIndicator(modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
internal fun FormHeaderIconAndTitle(icon: ImageVector, title: StringResource) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(stringResource(title))
    }
}


context(formScope: EntryFormScope)
@Composable
internal fun <T> MultiTextSection(
    state: EntryForm2.SingleTextState,
    header: @Composable () -> Unit,
    items: SnapshotStateList<T>,
    showItems: () -> Boolean = { true },
    itemToCommitted: ((String) -> T)? = null,
    removeLastItem: () -> String?,
    item: @Composable (index: Int, T) -> Unit,
    entryPredictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
    prediction: @Composable (index: Int, T) -> Unit = item,
    sortValue: ((T) -> String)? = null,
    label: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    pendingErrorMessage: () -> String? = { null },
    preferPrediction: Boolean = true,
    additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
) {
    val addUniqueErrorState =
        rememberAddUniqueErrorState(state = state, items = items, sortValue = sortValue)
    MultiTextSection(
        state = state,
        headerText = header,
        items = items.takeIf { showItems() },
        onItemCommitted = if (itemToCommitted != null) {
            {
                addUniqueErrorState.addAndEnforceUnique(itemToCommitted(it))
            }
        } else null,
        removeLastItem = removeLastItem,
        item = item,
        entryPredictions = entryPredictions,
        prediction = prediction,
        preferPrediction = preferPrediction,
        onPredictionChosen = addUniqueErrorState::addAndEnforceUnique,
        label = label,
        inputTransformation = inputTransformation,
        pendingErrorMessage = { addUniqueErrorState.errorMessage ?: pendingErrorMessage() },
        additionalHeaderActions = additionalHeaderActions,
    )
}

context(formScope: EntryFormScope)
@Composable
internal fun <T> BasicMultiTextSection(
    state: EntryForm2.SingleTextState,
    header: @Composable () -> Unit,
    initialItems: List<T>?,
    items: SnapshotStateList<T>,
    showItems: () -> Boolean = { true },
    predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
    itemToText: (T) -> String,
    itemToSubText: (T) -> String?,
    itemToSerializedValue: (T) -> String,
    itemToCommitted: ((String) -> T)? = null,
    leadingIcon: @Composable ((T) -> Unit)? = null,
    predictionToText: (T) -> String = itemToText,
    label: @Composable (() -> Unit)? = null,
    pendingErrorMessage: () -> String? = { null },
    preferPrediction: Boolean = true,
    equalsComparison: (T) -> Any? = { it },
    listRevertDialogState: ListRevertDialogState<T>,
    additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
) {
    MultiTextSection(
        state = state,
        header = header,
        items = items,
        showItems = showItems,
        entryPredictions = predictions,
        itemToCommitted = itemToCommitted,
        removeLastItem = { items.removeLastOrNull()?.let { itemToSerializedValue(it) } },
        sortValue = itemToText,
        item = { _, item ->
            Box {
                val existed = initialItems?.any {
                    equalsComparison(it) == equalsComparison(item)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    leadingIcon?.invoke(item)
                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            text = itemToText(item),
                            style = LocalTextStyle.current.copy(
                                color = if (listRevertDialogState.initialItems == null || existed != false) {
                                    LocalTextStyle.current.color
                                } else {
                                    AlleyTheme.colorScheme.positive
                                }
                            ),
                        )
                        val subText = itemToSubText(item)
                        if (!subText.isNullOrBlank()) {
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }
                    }

                    FormEditActions(
                        state = state,
                        forceLocked = formScope.forceLocked,
                        items = items,
                        item = item,
                        itemToText = itemToSerializedValue,
                    )
                }
            }
        },
        prediction = { _, value ->
            Column {
                Text(text = predictionToText(value))
                val subText = itemToSubText(value)
                if (!subText.isNullOrBlank()) {
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
        },
        label = label,
        pendingErrorMessage = pendingErrorMessage,
        preferPrediction = preferPrediction,
        additionalHeaderActions = {
            with(formScope) {
                ShowListRevertIconButton(listRevertDialogState, items)
            }
            additionalHeaderActions?.invoke(this)
        },
    )
}

@Composable
private fun <T, R : Comparable<R>> rememberAddUniqueErrorState(
    items: SnapshotStateList<T>,
    state: EntryForm2.SingleTextState,
    sortValue: ((T) -> R)?,
): AddUniqueErrorState<T, R> {
    val scope = rememberCoroutineScope()
    val errorMessageText = stringResource(Res.string.alley_edit_row_duplicate_entry)
    return remember(items, state, sortValue, scope, errorMessageText) {
        AddUniqueErrorState(
            items = items,
            state = state,
            sortValue = sortValue,
            scope = scope,
            errorMessageText = errorMessageText,
        )
    }
}


context(formScope: EntryFormScope)
@Composable
internal fun SeriesSection(
    state: EntryForm2.SingleTextState,
    title: StringResource,
    header: @Composable () -> Unit,
    listRevertDialogState: ListRevertDialogState<SeriesInfo>,
    items: SnapshotStateList<SeriesInfo>,
    showItems: () -> Boolean = { true },
    predictions: suspend (String) -> Flow<List<SeriesInfo>>,
    image: (SeriesInfo) -> String?,
    additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
) {
    MultiTextSection(
        state = state,
        header = header,
        items = items,
        itemToCommitted = SeriesInfo::fake,
        showItems = showItems,
        entryPredictions = predictions,
        removeLastItem = { items.removeLastOrNull()?.titlePreferred },
        prediction = { _, value ->
            SeriesPrediction(query = state.value.text.toString(), series = value)
        },
        sortValue = { it.titlePreferred },
        item = { _, value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val existed = listRevertDialogState.initialItems?.any { it.id == value.id } != false
                val textStyle = if (listRevertDialogState.initialItems == null || existed) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyMedium.copy(color = AlleyTheme.colorScheme.positive)
                }
                SeriesRow(
                    series = value,
                    image = { image(value) },
                    textStyle = textStyle,
                    showAllTitles = true,
                    modifier = Modifier.weight(1f)
                )

                AnimatedVisibility(
                    visible = state.lockState.editable && !formScope.forceLocked,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TooltipIconButton(
                        icon = Icons.Default.Delete,
                        tooltipText = stringResource(Res.string.alley_edit_row_delete_tooltip),
                        onClick = { items.remove(value) },
                    )
                }
            }
        },
        additionalHeaderActions = {
            with(formScope) {
                ShowListRevertIconButton(listRevertDialogState, items)
            }
            additionalHeaderActions?.invoke(this)
        },
    )

    ListFieldRevertDialog(
        dialogState = listRevertDialogState,
        label = title,
        items = items,
        itemsToText = { it.joinToString { it.titlePreferred } },
    )
}

context(scope: EntryFormScope)
@Composable
internal fun NotesSection(
    state: EntryForm2.SingleTextState,
    revertDialogState: RevertDialogState,
    header: StringResource,
    label: @Composable (() -> Unit)?,
) {
    scope.LongTextSection(
        state = state,
        headerText = { FormHeaderIconAndTitle(Icons.AutoMirrored.Default.Notes, header) },
        label = label,
        outputTransformation = revertDialogState.outputTransformation,
        additionalHeaderActions = {
            with(scope) {
                ShowRevertIconButton(revertDialogState, state)
            }
        },
    )
    FieldRevertDialog(revertDialogState, state, header)
}
