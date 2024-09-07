package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_search
import artistalleydatabase.modules.anime.generated.resources.anime_forum_search_clear
import artistalleydatabase.modules.anime.generated.resources.anime_forum_search_error_loading
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
)
object ForumSearchScreen {

    @Composable
    operator fun invoke(
        viewModel: ForumSearchViewModel,
        upIconOption: UpIconOption?,
        title: AnimeDestination.ForumSearch.Title?,
    ) {
        val sortFilterController = viewModel.sortFilterController
        sortFilterController.PromptDialog()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val sheetState: SheetState = rememberStandardBottomSheetState()
        val content = viewModel.content.collectAsLazyPagingItems()
        val refreshState = content.loadState.refresh
        val refreshing = refreshState is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = content::refresh,
        )
        SortFilterBottomScaffold(
            sortFilterController = sortFilterController,
            topBar = { TopBar(viewModel, title, upIconOption, scrollBehavior, sheetState) },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pullRefresh(pullRefreshState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (content.itemCount == 0) {
                    if (!refreshing) {
                        if (refreshState is LoadState.Error) {
                            AnimeMediaListScreen.Error(
                                errorTextRes = Res.string.anime_forum_search_error_loading,
                                exception = refreshState.error,
                            )
                        } else {
                            AnimeMediaListScreen.NoResults()
                        }
                    }
                } else {
                    val columns = GridCells.Adaptive(300.dp)
                    val gridState = rememberLazyGridState()
                    sortFilterController.ImmediateScrollResetEffect(gridState)
                    LazyVerticalGrid(
                        state = gridState,
                        columns = columns,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = content.itemCount,
                            key = content.itemKey { it.id },
                            contentType = content.itemContentType { "thread" },
                        ) {
                            val thread = content[it]
                            ThreadCard(
                                thread = thread,
                            )
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

    @Composable
    private fun TopBar(
        viewModel: ForumSearchViewModel,
        title: AnimeDestination.ForumSearch.Title?,
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
        sheetState: SheetState,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            val isNotEmpty by remember { derivedStateOf { viewModel.query.isNotEmpty() } }
            BackHandler(
                isNotEmpty && !WindowInsets.isImeVisibleKmp
                        // Need to manually check sheet state because top bar
                        // takes precedence over all other handlers
                        && sheetState.targetValue != SheetValue.Expanded
            ) {
                viewModel.query = ""
            }
            StaticSearchBar(
                query = viewModel.query,
                onQueryChange = { viewModel.query = it },
                leadingIcon = if (upIconOption != null) {
                    { UpIconButton(upIconOption) }
                } else null,
                trailingIcon = {
                    IconButton(onClick = { viewModel.query = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(
                                Res.string.anime_forum_search_clear
                            ),
                        )
                    }
                },
                placeholder = {
                    Text(text = title?.text() ?: stringResource(Res.string.anime_forum_search))
                },
            )
        }
    }
}
