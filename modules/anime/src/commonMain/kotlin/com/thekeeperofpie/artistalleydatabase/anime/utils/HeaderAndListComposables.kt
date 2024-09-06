@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.StringResource

@Composable
fun <ListEntryType : Any> HeaderAndListScreen(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    headerTextRes: StringResource?,
    header: @Composable BoxScope.(progress: Float) -> Unit,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    // TODO: Show root entry error
    val sortFilterController = viewModel.sortFilterController
    SortFilterBottomScaffold(
        topBar = {
            CollapsingToolbar(
                maxHeight = 356.dp,
                pinnedHeight = 120.dp,
                scrollBehavior = scrollBehavior,
                content = header,
            )
        },
        sortFilterController = sortFilterController,
        modifier = modifier,
    ) {
        List(
            viewModel = viewModel,
            scrollBehavior = scrollBehavior,
            headerTextRes = headerTextRes,
            scaffoldPadding = it,
            itemKey = itemKey,
            item = item,
            sortFilterController = sortFilterController,
        )
    }
}

@Composable
fun <ListEntryType : Any> HeaderAndMediaListScreen(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    editViewModel: MediaEditViewModel,
    headerTextRes: StringResource?,
    header: @Composable() (BoxScope.(progress: Float) -> Unit),
    itemKey: (ListEntryType) -> Any,
    item: @Composable() (LazyGridItemScope.(ListEntryType?) -> Unit),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val error = viewModel.entry.error
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

    val sortFilterController = viewModel.sortFilterController
    MediaEditBottomSheetScaffold(
        viewModel = editViewModel,
        snackbarHostState = snackbarHostState,
    ) {
        SortFilterBottomScaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                    content = header,
                )
            },
            sortFilterController = sortFilterController,
        ) {
            List(
                viewModel = viewModel,
                scrollBehavior = scrollBehavior,
                headerTextRes = headerTextRes,
                scaffoldPadding = it,
                itemKey = itemKey,
                item = item,
                sortFilterController = sortFilterController,
            )
        }
    }
}

@Composable
private fun <ListEntryType : Any> List(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    scrollBehavior: TopAppBarScrollBehavior,
    headerTextRes: StringResource?,
    scaffoldPadding: PaddingValues,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
    sortFilterController: SortFilterController<*>,
) {
    val gridState = rememberLazyGridState()
    val items = viewModel.items.collectAsLazyPagingItems()
    sortFilterController.ImmediateScrollResetEffect(gridState)
    val refreshState = items.loadState.refresh

    val refreshing = refreshState is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(refreshing, viewModel::refresh)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
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
            when {
                refreshState is LoadState.Error && items.itemCount == 0 ->
                    item {
                        AnimeMediaListScreen.ErrorContent(
                            errorTextRes = Res.string.anime_media_list_error_loading,
                            exception = refreshState.error,
                        )
                    }
                refreshState is LoadState.NotLoading && items.itemCount == 0 ->
                    item {
                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(Res.string.anime_media_list_no_results),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                ),
                            )
                        }
                    }
                else -> {
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
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
