package com.thekeeperofpie.artistalleydatabase.art.browse.selection

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object ArtBrowseSelectionScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption,
        title: () -> String,
        loading: () -> Boolean,
        errorRes: () -> Pair<StringResource, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        entries: @Composable () -> LazyPagingItems<ArtEntryGridModel>,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onClickEdit: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        Scaffold(
            topBar = {
                EntryGridAppBar(
                    title = title,
                    upIconOption = upIconOption,
                    selectedItems = selectedItems,
                    onClickClear = onClickClear,
                    onClickEdit = onClickEdit,
                    onConfirmDelete = onConfirmDelete,
                )
            },
            snackbarHost = {
                SnackbarErrorText(
                    { errorRes()?.first?.let { stringResource(it) } },
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) { paddingValues ->
            Box {
                Crossfade(
                    targetState = loading(),
                    label = "ArtBrowseSelectionScreen content fade",
                    modifier = Modifier.align(Alignment.TopCenter),
                ) {
                    if (it) {
                        CircularProgressIndicator(
                            Modifier
                                .padding(16.dp)
                                .align(Alignment.TopCenter)
                        )
                    } else {
                        EntryGrid(
                            entries = entries,
                            entriesSize = { entries().itemCount },
                            paddingValues = paddingValues,
                            selectedItems = selectedItems,
                            onClickEntry = onClickEntry,
                            onLongClickEntry = onLongClickEntry,
                        )
                    }
                }
            }
        }
    }
}
