package com.thekeeperofpie.artistalleydatabase.anime.user.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
object UserListScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: UserListViewModel,
        title: @Composable () -> String,
    ) {
        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            SortFilterBottomScaffold(
                sortFilterController = viewModel.sortFilterController,
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(text = title(), maxLines = 1) },
                            navigationIcon = {
                                if (upIconOption != null) {
                                    UpIconButton(option = upIconOption)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                                )
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .padding(it)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { sortFilterScaffoldPadding ->
                val viewer by viewModel.viewer.collectAsState()
                val users = viewModel.users.collectAsLazyPagingItems(CustomDispatchers.IO)
                val refreshing = users.loadState.refresh is LoadState.Loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = users::refresh
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    val gridState = rememberLazyGridState()
                    viewModel.sortFilterController.ImmediateScrollResetEffect(gridState)
                    when (val refreshState = users.loadState.refresh) {
                        LoadState.Loading -> Unit
                        is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
                        is LoadState.NotLoading -> {
                            if (users.itemCount == 0) {
                                AnimeMediaListScreen.NoResults()
                            } else {
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Adaptive(350.dp),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                                        .padding(sortFilterScaffoldPadding)
                                ) {
                                    items(
                                        count = users.itemCount,
                                        key = users.itemKey { it.user.id },
                                        contentType = users.itemContentType { "item" },
                                    ) {
                                        UserListRow(
                                            viewer = viewer,
                                            entry = users[it],
                                            onClickListEdit = editViewModel::initialize,
                                        )
                                    }

                                    when (users.loadState.append) {
                                        is LoadState.Loading -> item("load_more_append") {
                                            AnimeMediaListScreen.LoadingMore()
                                        }
                                        is LoadState.Error -> item("load_more_error") {
                                            AnimeMediaListScreen.AppendError { users.retry() }
                                        }
                                        is LoadState.NotLoading -> Unit
                                    }
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
        }
    }
}
