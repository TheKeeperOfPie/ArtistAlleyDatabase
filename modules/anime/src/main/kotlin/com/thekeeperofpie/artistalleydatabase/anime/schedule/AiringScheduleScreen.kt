package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.AiringScheduleQuery.Data.Page.AiringSchedule.Media.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AiringScheduleScreen {

    private const val SCREEN_KEY = "airingSchedule"

    @Composable
    operator fun invoke(
        viewModel: AiringScheduleViewModel = hiltViewModel(),
        onClickBack: () -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val initialDayIndex = remember { 6 + LocalDate.now().dayOfWeek.value }
        val pagerState = rememberPagerState(
            initialPage = initialDayIndex,
            pageCount = { 21 },
        )
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(R.string.anime_airing_schedule_label)) },
                        navigationIcon = {
                            ArrowBackIconButton(
                                onClick = onClickBack
                            )
                        },
                        actions = {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Sort,
                                    contentDescription = stringResource(
                                        R.string.anime_airing_schedule_sort_content_description,
                                    ),
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                AiringScheduleSort.values().forEach {
                                    val currentSort = viewModel.sort
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(it.textRes)) },
                                        onClick = { viewModel.sort = it },
                                        trailingIcon = {
                                            RadioButton(
                                                selected = currentSort == it,
                                                onClick = { viewModel.sort = it },
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )

                    ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                        val context = LocalContext.current
                        val dayData = remember(context) {
                            val today = LocalDate.now()
                            val tomorrow = today.plusDays(1)
                            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                            val endOfWeek = startOfWeek.plusWeeks(1)
                            val startDay = startOfWeek.minusWeeks(1)
                            (0..30).map {
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
        ) {
            HorizontalPager(
                state = pagerState, modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                val data = viewModel.items(it)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        count = data.itemCount,
                        key = data.itemKey { it.data.id },
                        contentType = data.itemContentType { "airingSchedule" },
                    ) { index ->
                        val schedule = data[index]
                        AnimeMediaListRow(
                            screenKey = SCREEN_KEY,
                            entry = schedule?.entry,
                            onLongClick = viewModel::onLongClickEntry,
                            nextAiringEpisode = schedule?.data?.let {
                                NextAiringEpisode(
                                    id = it.id,
                                    episode = it.episode,
                                    airingAt = it.airingAt,
                                )
                            },
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                        )
                    }
                }
            }
        }
    }
}
