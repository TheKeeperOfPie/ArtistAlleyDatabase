package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import artistalleydatabase.modules.anime.generated.resources.anime_media_view_option_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_seasonal_season_year
import artistalleydatabase.modules.anime.generated.resources.anime_seasonal_title
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object SeasonalScreen {

    @Composable
    operator fun invoke(
        viewModel: SeasonalViewModel = hiltViewModel<SeasonalViewModel>(),
        upIconOption: UpIconOption? = null,
    ) {
        val pagerState = rememberPagerState(
            initialPage = viewModel.initialPage,
            pageCount = { Int.MAX_VALUE },
        )

        val currentSeasonYear = remember { AniListUtils.getCurrentSeasonYear() }
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            val sortFilterController = viewModel.sortFilterController
            sortFilterController.PromptDialog()

            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    TopBar(
                        upIconOption = upIconOption,
                        viewModel = viewModel,
                        initialPage = viewModel.initialPage,
                        pagerState = pagerState,
                        currentSeasonYear = currentSeasonYear,
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val data = viewModel.items(it)
                    ListContent(
                        viewModel = viewModel,
                        editViewModel = editViewModel,
                        content = data,
                        scaffoldPadding = scaffoldPadding,
                        sortFilterController = sortFilterController,
                    )
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        upIconOption: UpIconOption?,
        viewModel: SeasonalViewModel,
        initialPage: Int,
        pagerState: PagerState,
        currentSeasonYear: Pair<MediaSeason, Int>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.anime_seasonal_title)) },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        val mediaViewOption = viewModel.mediaViewOption
                        val nextMediaViewOption = MediaViewOption.values()
                            .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                        IconButton(onClick = {
                            viewModel.mediaViewOption = nextMediaViewOption
                        }) {
                            Icon(
                                imageVector = nextMediaViewOption.icon,
                                contentDescription = stringResource(
                                    Res.string.anime_media_view_option_icon_content_description
                                ),
                            )
                        }
                    }
                )

                val state = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)
                val coroutineScope = rememberCoroutineScope()

                val settledPage = pagerState.settledPage
                LaunchedEffect(settledPage) {
                    val lastVisibleItem = state.layoutInfo.visibleItemsInfo.lastOrNull()
                    val lastVisibleIndex = lastVisibleItem?.index ?: 0
                    if (settledPage !in (state.firstVisibleItemIndex..lastVisibleIndex)) {
                        state.animateScrollToItem(settledPage)
                    }
                }

                LazyRow(
                    state = state,
                    flingBehavior = rememberSnapFlingBehavior(state),
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    items(
                        Int.MAX_VALUE,
                        key = { it },
                        contentType = { "seasonYear" },
                    ) { index ->
                        val selected = index == pagerState.currentPage
                        Tab(
                            selected = selected,
                            text = {
                                val (season, year) = AniListUtils.calculateSeasonYearWithOffset(
                                    seasonYear = currentSeasonYear,
                                    offset = Int.MAX_VALUE / 2 - index,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.anime_seasonal_season_year,
                                        stringResource(season.toTextRes()),
                                        year,
                                    ),
                                    color = if (index == Int.MAX_VALUE / 2) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        Color.Unspecified
                                    },
                                    maxLines = 1,
                                )
                            },
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.conditionally(selected) {
                                bottomBorder(
                                    color = MaterialTheme.colorScheme.primary,
                                    width = 3.dp,
                                )
                            }
                        )
                    }
                }

                HorizontalDivider()
            }
        }
    }

    @Composable
    private fun ListContent(
        viewModel: SeasonalViewModel,
        editViewModel: MediaEditViewModel,
        content: LazyPagingItems<MediaPreviewWithDescriptionEntry>,
        scaffoldPadding: PaddingValues,
        sortFilterController: AnimeSortFilterController<*>,
    ) {
        val refreshState = content.loadState.refresh
        val refreshing = content.loadState.refresh is LoadState.Loading
        val viewer by viewModel.viewer.collectAsState()
        AnimeMediaListScreen(
            refreshing = refreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            val columns = when (viewModel.mediaViewOption) {
                MediaViewOption.SMALL_CARD,
                MediaViewOption.LARGE_CARD,
                MediaViewOption.COMPACT,
                -> GridCells.Adaptive(300.dp)
                MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
            }
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
                when {
                    refreshState is LoadState.Error && content.itemCount == 0 ->
                        item {
                            AnimeMediaListScreen.ErrorContent(
                                errorTextRes = Res.string.anime_media_list_error_loading,
                                exception = refreshState.error,
                            )
                        }
                    refreshState is LoadState.NotLoading && content.itemCount == 0 ->
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
                            count = content.itemCount,
                            key = content.itemKey { it.media.id },
                            contentType = content.itemContentType { "media" }
                        ) { index ->
                            val item = content[index]
                            MediaViewOptionRow(
                                mediaViewOption = viewModel.mediaViewOption,
                                viewer = viewer,
                                onClickListEdit = editViewModel::initialize,
                                entry = item,
                            )
                        }
                    }
                }

                when (content.loadState.append) {
                    is LoadState.Loading -> item(key = "load_more_append") {
                        AnimeMediaListScreen.LoadingMore()
                    }
                    is LoadState.Error -> item(key = "load_more_error") {
                        AnimeMediaListScreen.AppendError { content.retry() }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}
