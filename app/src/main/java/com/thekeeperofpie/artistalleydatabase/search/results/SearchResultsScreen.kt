package com.thekeeperofpie.artistalleydatabase.search.results

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
import androidx.paging.compose.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid

object SearchResultsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        loading: () -> Boolean,
        errorRes: () -> Pair<Int, Exception?>? = { null },
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
                AppBar(
                    text = stringResource(R.string.search_results),
                    upIconOption = upIconOption,
                )
            },
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
                    label = "SearchResultsScreen content fade",
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
                            imageScreenKey = "results",
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
