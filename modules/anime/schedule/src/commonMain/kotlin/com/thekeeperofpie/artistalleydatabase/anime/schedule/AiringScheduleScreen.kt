package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.schedule.generated.resources.Res
import artistalleydatabase.modules.anime.schedule.generated.resources.anime_airing_schedule_label
import artistalleydatabase.modules.anime.schedule.generated.resources.anime_airing_schedule_today
import artistalleydatabase.modules.anime.schedule.generated.resources.anime_airing_schedule_tomorrow
import artistalleydatabase.modules.anime.schedule.generated.resources.anime_seasonal_icon_content_description
import com.anilist.data.AiringScheduleQuery
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.SeasonalCurrentRoute
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AiringScheduleScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: SortFilterState<*>,
        onRefresh: () -> Unit,
        upIconOption: UpIconOption?,
        itemsForPage: @Composable (page: Int) -> LazyPagingItems<Entry<MediaEntry>>,
        mediaRow: @Composable (
            Entry<MediaEntry>?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        seasonalCurrentRoute: SeasonalCurrentRoute,
    ) {
        val initialDayIndex = remember {
            6 + Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber
        }
        val pagerState = rememberPagerState(initialPage = initialDayIndex, pageCount = { 21 })

        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column {
                            AppBar(
                                text = stringResource(Res.string.anime_airing_schedule_label),
                                upIconOption = upIconOption,
                                actions = {
                                    val navigationController = LocalNavigationController.current
                                    IconButton(onClick = {
                                        navigationController.navigate(seasonalCurrentRoute())
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
                                val today = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                val dayData = remember(today) {
                                    val tomorrow = today.plus(1, DateTimeUnit.DAY)
                                    val startOfWeek =
                                        today.minus(
                                            today.dayOfWeek.isoDayNumber.toLong() - 1,
                                            DateTimeUnit.DAY
                                        )
                                    val endOfWeek = startOfWeek.plus(1, DateTimeUnit.WEEK)
                                    val startDay = startOfWeek.minus(1, DateTimeUnit.WEEK)
                                    (0 until pagerState.pageCount).map {
                                        when (val day =
                                            startDay.plus(
                                                it.toLong(),
                                                DateTimeUnit.DAY
                                            )) {
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
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) { page ->
                    val gridState = rememberLazyGridState()
                    sortFilterState.ImmediateScrollResetEffect(gridState)
                    val content = itemsForPage(page)
                    VerticalList(
                        itemHeaderText = null,
                        items = content,
                        itemKey = { it.data.id },
                        gridState = gridState,
                        onRefresh = onRefresh,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 72.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        mediaRow(it, onClickListEdit)
                    }
                }
            }
        }
    }

    data class Entry<MediaEntry>(
        val data: AiringScheduleQuery.Data.Page.AiringSchedule,
        val media: MediaEntry?,
    ) {
        val nextAiringEpisode = NextAiringEpisode(
            episode = data.episode,
            airingAt = Instant.fromEpochSeconds(data.airingAt.toLong()),
        )
    }
}
