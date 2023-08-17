package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object SeasonalScreen {

    private val SCREEN_KEY = AnimeNavDestinations.SEASONAL.id

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
            screenKey = SCREEN_KEY,
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
                    )
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        upIconOption: UpIconOption?,
        viewModel: SeasonalViewModel,
        pagerState: PagerState,
        currentSeasonYear: Pair<MediaSeason, Int>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.anime_seasonal_title)) },
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
                                    R.string.anime_media_view_option_icon_content_description
                                ),
                            )
                        }
                    }
                )

                val state =
                    rememberLazyListState(initialFirstVisibleItemIndex = pagerState.initialPage)
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
                                        R.string.anime_seasonal_season_year,
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
    ) {
        val refreshing = content.loadState.refresh is LoadState.Loading
        val viewer by viewModel.viewer.collectAsState()
        AnimeMediaListScreen(
            refreshing = refreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            when (val refreshState = content.loadState.refresh) {
                LoadState.Loading -> Unit
                is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
                is LoadState.NotLoading -> {
                    if (content.itemCount == 0) {
                        AnimeMediaListScreen.NoResults()
                    } else {
                        val columns = when (viewModel.mediaViewOption) {
                            MediaViewOption.SMALL_CARD,
                            MediaViewOption.LARGE_CARD,
                            MediaViewOption.COMPACT,
                            -> GridCells.Adaptive(300.dp)
                            MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                        }
                        LazyVerticalGrid(
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
                                key = content.itemKey { it.media.id },
                                contentType = content.itemContentType { "media" }
                            ) { index ->
                                val item = content[index]
                                MediaViewOptionRow(
                                    screenKey = SCREEN_KEY,
                                    mediaViewOption = viewModel.mediaViewOption,
                                    viewer = viewer,
                                    editViewModel = editViewModel,
                                    entry = item,
                                    onLongClick = viewModel.ignoreList::toggle,
                                )
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
        }
    }
}
