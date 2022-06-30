package com.thekeeperofpie.artistalleydatabase.browse.selection

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
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryGrid
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.ui.AppBar
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText

object BrowseSelectionScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        title: () -> String,
        loading: () -> Boolean,
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = {},
        entries: LazyPagingItems<ArtEntryModel>,
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        Scaffold(
            topBar = { AppBar(text = title()) },
            snackbarHost = {
                SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
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
                        ArtEntryGrid(
                            paddingValues = paddingValues,
                            entries = entries,
                            selectedItems = selectedItems,
                            onClickEntry = onClickEntry,
                            onLongClickEntry = onLongClickEntry,
                            onClickClear = onClickClear,
                            onConfirmDelete = onConfirmDelete,
                        )
                    }
                }
            }
        }
    }
}