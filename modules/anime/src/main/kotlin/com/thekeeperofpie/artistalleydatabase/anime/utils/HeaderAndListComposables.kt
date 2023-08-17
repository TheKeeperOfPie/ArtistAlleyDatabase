@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

@Composable
fun <ListEntryType : Any> HeaderAndListScreen(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *>,
    @StringRes headerTextRes: Int?,
    header: @Composable BoxScope.(progress: Float) -> Unit,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    Scaffold(
        topBar = {
            CollapsingToolbar(
                maxHeight = 356.dp,
                pinnedHeight = 120.dp,
                scrollBehavior = scrollBehavior,
                content = header,
            )
        },
        snackbarHost = {
            val error = viewModel.error
            SnackbarErrorText(
                error?.first,
                error?.second,
                onErrorDismiss = { viewModel.error = null }
            )
        },
    ) {
        List(
            viewModel = viewModel,
            scrollBehavior = scrollBehavior,
            headerTextRes = headerTextRes,
            scaffoldPadding = it,
            itemKey = itemKey,
            item = item,
        )
    }
}

@Composable
fun <ListEntryType : Any> HeaderAndMediaListScreen(
    screenKey: String,
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *>,
    editViewModel: MediaEditViewModel,
    @StringRes headerTextRes: Int?,
    header: @Composable (BoxScope.(progress: Float) -> Unit),
    itemKey: (ListEntryType) -> Any,
    item: @Composable (LazyGridItemScope.(ListEntryType?) -> Unit),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val error = viewModel.error
    val errorString = error?.first?.let { stringResource(it) }
    LaunchedEffect(errorString) {
        if (errorString != null) {
            snackbarHostState.showSnackbar(
                message = errorString,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
        }
    }

    MediaEditBottomSheetScaffold(
        screenKey = screenKey,
        viewModel = editViewModel,
        topBar = {
            CollapsingToolbar(
                maxHeight = 356.dp,
                pinnedHeight = 120.dp,
                scrollBehavior = scrollBehavior,
                content = header,
            )
        },
        snackbarHostState = snackbarHostState,
    ) {
        List(
            viewModel = viewModel,
            scrollBehavior = scrollBehavior,
            headerTextRes = headerTextRes,
            scaffoldPadding = it,
            itemKey = itemKey,
            item = item,
        )
    }
}

@Composable
private fun <ListEntryType : Any> List(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *>,
    scrollBehavior: TopAppBarScrollBehavior,
    headerTextRes: Int?,
    scaffoldPadding: PaddingValues,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val items = viewModel.items.collectAsLazyPagingItems()
    when (val refreshState = items.loadState.refresh) {
        LoadState.Loading -> Unit
        is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                AnimeMediaListScreen.NoResults()
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(350.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(scaffoldPadding)
                ) {
                    if (headerTextRes != null) {
                        item("header") {
                            DetailsSectionHeader(text = stringResource(headerTextRes))
                        }
                    }

                    items(
                        count = items.itemCount,
                        key = items.itemKey { itemKey(it) },
                        contentType = items.itemContentType { "item" },
                    ) {
                        item(items[it])
                    }

                    when (items.loadState.append) {
                        is LoadState.Loading -> item("load_more_append") {
                            AnimeMediaListScreen.LoadingMore()
                        }
                        is LoadState.Error -> item("load_more_error") {
                            AnimeMediaListScreen.AppendError { items.retry() }
                        }
                        is LoadState.NotLoading -> Unit
                    }
                }
            }
        }
    }
}
