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
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGrid

object CdBrowseSelectionScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
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
            topBar = { AppBar(text = title()) },
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) { paddingValues ->
            Box {
                Crossfade(
                    targetState = loading(),
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
                            imageScreenKey = "selection",
                            paddingValues = paddingValues,
                            entries = entries,
                            entriesSize = { entries().itemCount },
                            selectedItems = selectedItems,
                            onClickEntry = onClickEntry,
                            onLongClickEntry = onLongClickEntry,
                            onClickClear = onClickClear,
                            onClickEdit = onClickEdit,
                            onConfirmDelete = onConfirmDelete,
                            lazyStaggeredGridState =
                            LazyStaggeredGrid.rememberLazyStaggeredGridState(columnCount = 2),
                        )
                    }
                }
            }
        }
    }
}