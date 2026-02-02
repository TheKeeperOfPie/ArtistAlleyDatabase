package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_delete_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_delete_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_delete_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_delete_text
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_delete_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_history_abandon_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_history_abandon_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_history_action_abandon
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_history_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_history_action_view_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_refresh_abandon_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_refresh_abandon_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_refresh_action_abandon
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_refresh_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_refresh_action_view_tooltip
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

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

@Composable
internal fun DeleteButton(onConfirmDelete: () -> Unit, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    FilledTonalButton(onClick = { showDialog = true }, modifier = modifier) {
        Text(stringResource(Res.string.alley_edit_artist_edit_delete_action_delete))
    }

    var loading by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(Res.string.alley_edit_artist_edit_delete_title)) },
            text = { Text(stringResource(Res.string.alley_edit_artist_edit_delete_text)) },
            confirmButton = {
                val countdown by produceState(5) {
                    (4 downTo 0).forEach {
                        delay(1.seconds)
                        value = it
                    }
                }
                TextButton(
                    onClick = {
                        if (countdown <= 0) {
                            loading = true
                            onConfirmDelete()
                        }
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val textAlpha by animateFloatAsState(if (countdown <= 0) 1f else 0f)
                        Text(
                            text = stringResource(Res.string.alley_edit_artist_edit_delete_action_confirm),
                            modifier = Modifier.graphicsLayer { alpha = textAlpha }
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = countdown > 0,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(countdown.toString())
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = loading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.alley_edit_artist_edit_delete_action_cancel))
                }
            },
        )
    }
}

@Composable
internal fun FormRefreshButton(hasPendingChanges: () -> Boolean, onClickRefresh: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    TooltipIconButton(
        icon = Icons.Default.Refresh,
        tooltipText = stringResource(Res.string.alley_edit_refresh_action_view_tooltip),
        onClick = {
            if (hasPendingChanges()) {
                showDialog = true
            } else {
                onClickRefresh()
            }
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(Res.string.alley_edit_refresh_abandon_title)) },
            text = { Text(stringResource(Res.string.alley_edit_refresh_abandon_description)) },
            confirmButton = {
                TextButton(onClick = {
                    onClickRefresh()
                    showDialog = false
                }) {
                    Text(stringResource(Res.string.alley_edit_refresh_action_abandon))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.alley_edit_refresh_action_cancel))
                }
            }
        )
    }
}

@Composable
internal fun FormHistoryButton(hasPendingChanges: () -> Boolean, onClickHistory: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    TooltipIconButton(
        icon = Icons.Default.History,
        tooltipText = stringResource(Res.string.alley_edit_history_action_view_tooltip),
        onClick = {
            if (hasPendingChanges()) {
                showDialog = true
            } else {
                onClickHistory()
            }
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(Res.string.alley_edit_history_abandon_title)) },
            text = { Text(stringResource(Res.string.alley_edit_history_abandon_description)) },
            confirmButton = {
                TextButton(onClick = {
                    onClickHistory()
                    showDialog = false
                }) {
                    Text(stringResource(Res.string.alley_edit_history_action_abandon))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.alley_edit_history_action_cancel))
                }
            }
        )
    }
}
