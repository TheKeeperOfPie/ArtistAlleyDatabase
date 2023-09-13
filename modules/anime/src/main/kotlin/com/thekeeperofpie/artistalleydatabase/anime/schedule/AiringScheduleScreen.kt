package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.AiringScheduleQuery.Data.Page.AiringSchedule.Media.NextAiringEpisode
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
object AiringScheduleScreen {

    private val SCREEN_KEY = AnimeNavDestinations.AIRING_SCHEDULE.id

    @Composable
    operator fun invoke(
        viewModel: AiringScheduleViewModel = hiltViewModel(),
        onClickBack: () -> Unit,
    ) {
        val initialDayIndex = remember { 6 + LocalDate.now().dayOfWeek.value }
        val pagerState = rememberPagerState(
            initialPage = initialDayIndex,
            pageCount = { 21 },
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    Column {
                        TopAppBar(
                            title = { Text(stringResource(R.string.anime_airing_schedule_label)) },
                            navigationIcon = {
                                ArrowBackIconButton(
                                    onClick = onClickBack
                                )
                            },
                            actions = {
                                val navigationCallback = LocalNavigationCallback.current
                                IconButton(onClick = navigationCallback::onSeasonalClick) {
                                    Icon(
                                        imageVector = when (AniListUtils.getCurrentSeasonYear().first) {
                                            MediaSeason.WINTER -> Icons.Filled.AcUnit
                                            MediaSeason.SPRING -> Icons.Filled.Grass
                                            MediaSeason.SUMMER -> Icons.Filled.WbSunny
                                            // TODO: Use a better leaf
                                            MediaSeason.FALL -> Icons.Filled.EnergySavingsLeaf
                                            MediaSeason.UNKNOWN__ -> Icons.Filled.WbSunny
                                        },
                                        contentDescription = stringResource(
                                            R.string.anime_seasonal_icon_content_description
                                        ),
                                    )
                                }
                            }
                        )

                        ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                            val context = LocalContext.current
                            val dayData = remember(context) {
                                val today = LocalDate.now()
                                val tomorrow = today.plusDays(1)
                                val startOfWeek =
                                    today.minusDays(today.dayOfWeek.value.toLong() - 1)
                                val endOfWeek = startOfWeek.plusWeeks(1)
                                val startDay = startOfWeek.minusWeeks(1)
                                (0 until pagerState.pageCount).map {
                                    when (val day = startDay.plusDays(it.toLong())) {
                                        today -> day to Either.Left<Int, String>(
                                            R.string.anime_airing_schedule_today
                                        )
                                        tomorrow -> day to Either.Left(
                                            R.string.anime_airing_schedule_tomorrow
                                        )
                                        in startOfWeek..endOfWeek -> day to Either.Right(
                                            MediaUtils.formatShortWeekday(context, day)
                                        )
                                        else -> day to Either.Right(
                                            MediaUtils.formatShortDay(context, day)
                                        )
                                    }
                                }
                            }

                            val coroutineScope = rememberCoroutineScope()
                            dayData.forEachIndexed { index, day ->
                                Tab(
                                    selected = index == pagerState.currentPage,
                                    text = {
                                        val either = day.second
                                        Text(
                                            if (either is Either.Left) {
                                                stringResource(either.value)
                                            } else {
                                                either.rightOrNull()!!
                                            }
                                        )
                                    },
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            },
        ) { scaffoldPadding ->
            SortFilterBottomScaffold(
                sortFilterController = viewModel.sortFilterController,
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { innerScaffoldPadding ->
                val viewer by viewModel.viewer.collectAsState()
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(innerScaffoldPadding)
                        .fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val data = viewModel.items(page)
                        val loading = data.loadState.refresh is LoadState.Loading
                        val pullRefreshState = rememberPullRefreshState(
                            refreshing = loading,
                            onRefresh = { viewModel.refresh.tryEmit(page) },
                        )
                        when (val refreshState = data.loadState.refresh) {
                            LoadState.Loading -> Unit
                            is LoadState.Error -> AnimeMediaListScreen.Error(
                                exception = refreshState.error,
                                modifier = Modifier.pullRefresh(pullRefreshState)
                            )
                            is LoadState.NotLoading -> {
                                if (data.itemCount == 0) {
                                    AnimeMediaListScreen.NoResults(
                                        modifier = Modifier.pullRefresh(pullRefreshState)
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 16.dp,
                                            bottom = 72.dp,
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.pullRefresh(pullRefreshState)
                                    ) {
                                        items(
                                            count = data.itemCount,
                                            key = data.itemKey { it.data.id },
                                            contentType = data.itemContentType { "airingSchedule" },
                                        ) { index ->
                                            val schedule = data[index]
                                            AnimeMediaListRow(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                entry = schedule?.entry,
                                                onClickListEdit = editViewModel::initialize,
                                                nextAiringEpisode = schedule?.data?.let {
                                                    NextAiringEpisode(
                                                        episode = it.episode,
                                                        airingAt = it.airingAt,
                                                    )
                                                },
                                                showDate = false,
                                            )
                                        }

                                        when (data.loadState.append) {
                                            is LoadState.Loading -> item("load_more_append") {
                                                AnimeMediaListScreen.LoadingMore()
                                            }
                                            is LoadState.Error -> item("load_more_error") {
                                                AnimeMediaListScreen.AppendError { data.retry() }
                                            }
                                            is LoadState.NotLoading -> Unit
                                        }
                                    }
                                }
                            }
                        }
                        PullRefreshIndicator(
                            refreshing = loading,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}
