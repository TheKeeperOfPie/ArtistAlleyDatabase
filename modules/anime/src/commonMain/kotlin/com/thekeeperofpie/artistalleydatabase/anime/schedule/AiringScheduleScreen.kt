package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_label
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_today
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_tomorrow
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import artistalleydatabase.modules.anime.generated.resources.anime_seasonal_icon_content_description
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AiringScheduleScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: AiringScheduleViewModel = viewModel { animeComponent.airingScheduleViewModel() },
        onClickBack: () -> Unit,
    ) {
        val initialDayIndex = remember { 6 + Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.value }
        val pagerState = rememberPagerState(
            initialPage = initialDayIndex,
            pageCount = { 21 },
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        // TODO: Use a better leaf
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    Column {
                        TopAppBar(
                            title = { Text(stringResource(Res.string.anime_airing_schedule_label)) },
                            navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
                            actions = {
                                val navigationCallback = LocalNavigationCallback.current
                                IconButton(onClick = {
                                    navigationCallback.navigate(
                                        AnimeDestination.Seasonal(
                                            type = AnimeDestination.Seasonal.Type.THIS,
                                        )
                                    )
                                }) {
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
                                            Res.string.anime_seasonal_icon_content_description
                                        ),
                                    )
                                }
                            }
                        )

                        ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                            val dateTimeFormatter = LocalDateTimeFormatter.current
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val dayData = remember(today) {
                                val tomorrow = today.plus(1, DateTimeUnit.DAY)
                                val startOfWeek =
                                    today.minus(today.dayOfWeek.value.toLong() - 1, DateTimeUnit.DAY)
                                val endOfWeek = startOfWeek.plus(1, DateTimeUnit.WEEK)
                                val startDay = startOfWeek.minus(1, DateTimeUnit.WEEK)
                                (0 until pagerState.pageCount).map {
                                    when (val day = startDay.plus(it.toLong(), DateTimeUnit.DAY)) {
                                        today -> day to Either.Left<StringResource, String>(
                                            Res.string.anime_airing_schedule_today
                                        )
                                        tomorrow -> day to Either.Left(
                                            Res.string.anime_airing_schedule_tomorrow
                                        )
                                        in startOfWeek..endOfWeek -> day to Either.Right(
                                            dateTimeFormatter.formatShortWeekday(day)
                                        )
                                        else -> day to Either.Right(
                                            dateTimeFormatter.formatShortDay(day)
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
                        val content = viewModel.items(page)
                        val refreshState = content.loadState.refresh
                        val loading = refreshState is LoadState.Loading
                        val pullRefreshState = rememberPullRefreshState(
                            refreshing = loading,
                            onRefresh = viewModel::refresh,
                        )
                        val listState = rememberLazyListState()
                        viewModel.sortFilterController.ImmediateScrollResetEffect(listState)
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 72.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.pullRefresh(pullRefreshState)
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
                                        key = content.itemKey { it.data.id },
                                        contentType = content.itemContentType { "airingSchedule" },
                                    ) { index ->
                                        val schedule = content[index]
                                        AnimeMediaListRow(
                                            entry = schedule?.media,
                                            viewer = viewer,
                                            onClickListEdit = editViewModel::initialize,
                                            nextAiringEpisode = schedule?.nextAiringEpisode,
                                            showDate = false,
                                        )
                                    }
                                }
                            }

                            when (content.loadState.append) {
                                is LoadState.Loading -> item("load_more_append") {
                                    AnimeMediaListScreen.LoadingMore()
                                }
                                is LoadState.Error -> item("load_more_error") {
                                    AnimeMediaListScreen.AppendError { content.retry() }
                                }
                                is LoadState.NotLoading -> Unit
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
