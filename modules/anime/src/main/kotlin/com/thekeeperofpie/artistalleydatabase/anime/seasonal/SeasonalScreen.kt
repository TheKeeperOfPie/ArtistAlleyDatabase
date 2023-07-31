package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object SeasonalScreen {

    @Composable
    operator fun invoke(
        viewModel: SeasonalViewModel = hiltViewModel<SeasonalViewModel>(),
        upIconOption: UpIconOption? = null,
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val pagerState = rememberPagerState(
            initialPage = viewModel.initialPage,
            pageCount = { Int.MAX_VALUE },
        )

        val currentSeasonYear = remember { AniListUtils.getCurrentSeasonYear() }
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.SEASONAL.id,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
        ) {
            val sortFilterController = viewModel.sortFilterController
            if (sortFilterController.airingDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = sortFilterController.airingDateShown,
                    onShownForStartDateChange = {
                        sortFilterController.airingDateShown = it
                    },
                    onDateChange = sortFilterController::onAiringDateChange,
                )
            }

            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = { TopBar(upIconOption, pagerState, currentSeasonYear) },
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
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        upIconOption: UpIconOption?,
        pagerState: PagerState,
        currentSeasonYear: Pair<MediaSeason, Int>,
    ) {
        Column {
            AppBar(
                text = stringResource(R.string.anime_seasonal_title),
                upIconOption = upIconOption,
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

            Divider()
        }
    }

    @Composable
    private fun ListContent(
        viewModel: SeasonalViewModel,
        editViewModel: MediaEditViewModel,
        content: LazyPagingItems<SeasonalViewModel.MediaEntry>,
        scaffoldPadding: PaddingValues,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val refreshing = content.loadState.refresh is LoadState.Loading
        val viewer by viewModel.viewer.collectAsState()
        AnimeMediaListScreen(
            refreshing = refreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.padding(scaffoldPadding),
        ) { onLongPressImage ->
            when (val refreshState = content.loadState.refresh) {
                LoadState.Loading -> Unit
                is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
                is LoadState.NotLoading -> {
                    if (content.itemCount == 0) {
                        AnimeMediaListScreen.NoResults()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                count = content.itemCount,
                                key = content.itemKey { it.media.id },
                                contentType = content.itemContentType { "media" }
                            ) { index ->

                                val item = content[index]
                                if (item == null) {
                                    AnimeMediaListRow<MediaPreview>(
                                        screenKey = AnimeNavDestinations.SEARCH.id,
                                        viewer = null,
                                        entry = null,
                                        onClickListEdit = {},
                                        onLongClick = {},
                                    )
                                } else {
                                    AnimeMediaListRow(
                                        screenKey = AnimeNavDestinations.SEASONAL.id,
                                        viewer = viewer,
                                        entry = item,
                                        onClickListEdit = { editViewModel.initialize(it.media) },
                                        onLongClick = viewModel::onMediaLongClick,
                                        onLongPressImage = onLongPressImage,
                                        colorCalculationState = colorCalculationState,
                                        navigationCallback = navigationCallback,
                                    )
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
        }
    }
}
