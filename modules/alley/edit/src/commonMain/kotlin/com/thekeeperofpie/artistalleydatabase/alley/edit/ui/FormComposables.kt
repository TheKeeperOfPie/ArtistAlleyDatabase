package com.thekeeperofpie.artistalleydatabase.alley.edit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_duplicate_entry
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm.AddUniqueErrorState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
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
