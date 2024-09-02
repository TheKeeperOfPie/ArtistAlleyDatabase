package com.thekeeperofpie.artistalleydatabase.cds.browse.selection

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

object CdBrowseSelectionScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        upIconOption: UpIconOption,
        title: () -> String,
        loading: () -> Boolean,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        entries: @Composable () -> LazyPagingItems<CdEntryGridModel>,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: CdEntryGridModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: CdEntryGridModel) -> Unit = { _, _ -> },
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
                    label = "CdBrowseSelectionScreen content fade",
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
