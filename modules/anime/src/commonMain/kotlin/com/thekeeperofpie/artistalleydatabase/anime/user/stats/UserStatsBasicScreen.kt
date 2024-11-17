package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_user_release_years_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_start_years_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_anime_days_watched
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_count
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_manga_chapters_read
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_mean_score
import artistalleydatabase.modules.anime.generated.resources.anime_user_stats_formats_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_stats_lengths_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_stats_scores_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_stats_statuses_label
import com.anilist.data.UserByIdQuery
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toColor
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.VerticalDivider
import com.thekeeperofpie.artistalleydatabase.utils_compose.charts.BarChart
import com.thekeeperofpie.artistalleydatabase.utils_compose.charts.PieChart
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NAME_SHADOWING")
object UserStatsBasicScreen {

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        isAnime: Boolean,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val statistics = statistics()
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp),
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            if (statistics == null) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    }
                }
                return@LazyColumn
            }

            val user = user()
            if (user != null) {
                if (isAnime) {
                    animeStatisticsSection(user)
                } else {
                    mangaStatisticsSection(user)
                }
            }

            scoresSection(statistics)
            lengthsSection(statistics)

            formatsSection(statistics)
            statusesSection(statistics, isAnime = isAnime)

            releaseYearsSection(statistics)
            startYearsSection(statistics)
        }
    }

    private fun LazyListScope.scoresSection(statistics: AniListUserScreen.Entry.Statistics) {
        barChartSection(
            titleRes = Res.string.anime_user_stats_scores_label,
            slices = statistics.scores,
            sliceToAmount = { it.count },
            sliceToColor = { _, _ -> MaterialTheme.colorScheme.surfaceTint },
            sliceToText = { it.score.toString() },
        )
    }

    private fun LazyListScope.lengthsSection(statistics: AniListUserScreen.Entry.Statistics) {
        barChartSection(
            titleRes = Res.string.anime_user_stats_lengths_label,
            slices = statistics.lengths,
            sliceToAmount = { it.count },
            sliceToColor = { _, _ -> MaterialTheme.colorScheme.surfaceTint },
            sliceToText = { it.length.orEmpty() },
        )
    }

    private fun LazyListScope.formatsSection(statistics: AniListUserScreen.Entry.Statistics) {
        pieChartSection(
            titleRes = Res.string.anime_user_stats_formats_label,
            slices = statistics.statistics.formats,
            sliceToKey = { it.format },
            sliceToAmount = { it.count },
            sliceToColor = { it.format.toColor() },
            sliceToText = { stringResource(it.format.toTextRes()) },
            keySave = { it?.rawValue.orEmpty() },
            keyRestore = { key ->
                MediaFormat.values().find { it.rawValue == key }
                    ?: MediaFormat.UNKNOWN__
            },
        )
    }

    private fun LazyListScope.statusesSection(
        statistics: AniListUserScreen.Entry.Statistics,
        isAnime: Boolean,
    ) {
        pieChartSection(
            titleRes = Res.string.anime_user_stats_statuses_label,
            slices = statistics.statistics.statuses,
            sliceToKey = { it.status },
            sliceToAmount = { it.count },
            sliceToColor = { it.status.toColor() },
            sliceToText = { stringResource(it.status.toTextRes(anime = isAnime)) },
            keySave = { it?.rawValue.orEmpty() },
            keyRestore = { key ->
                MediaListStatus.values().find { it.rawValue == key }
                    ?: MediaListStatus.UNKNOWN__
            },
        )
    }

    private fun LazyListScope.releaseYearsSection(statistics: AniListUserScreen.Entry.Statistics) {
        barChartSection(
            titleRes = Res.string.anime_user_release_years_label,
            slices = statistics.releaseYears,
            sliceToAmount = { it.count },
            sliceToColor = { _, _ -> MaterialTheme.colorScheme.surfaceTint },
            sliceToText = { it.releaseYear.toString() },
        )
    }

    private fun LazyListScope.startYearsSection(statistics: AniListUserScreen.Entry.Statistics) {
        barChartSection(
            titleRes = Res.string.anime_user_start_years_label,
            slices = statistics.startYears,
            sliceToAmount = { it.count },
            sliceToColor = { _, _ -> MaterialTheme.colorScheme.surfaceTint },
            sliceToText = { it.startYear.toString() },
        )
    }

    private fun <Value> LazyListScope.barChartSection(
        titleRes: StringResource,
        slices: List<Value?>?,
        sliceToAmount: (Value) -> Int,
        sliceToColor: @Composable (index: Int, value: Value) -> Color,
        sliceToText: @Composable (Value) -> String,
    ) {
        val slices = slices?.filterNotNull()?.ifEmpty { null } ?: return
        item {
            DetailsSectionHeader(stringResource(titleRes))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                BarChart(
                    slices = slices,
                    sliceToAmount = sliceToAmount,
                    sliceToColor = sliceToColor,
                    sliceToText = sliceToText,
                )
            }
        }
    }

    private fun <Key, Value> LazyListScope.pieChartSection(
        titleRes: StringResource,
        slices: List<Value?>?,
        sliceToKey: (Value) -> Key,
        sliceToAmount: (Value) -> Int,
        sliceToColor: (Value) -> Color,
        sliceToText: @Composable (Value) -> String,
        keySave: (Key) -> String,
        keyRestore: (String) -> Key,
    ) {
        val slices = slices?.filterNotNull()?.ifEmpty { null } ?: return
        item {
            DetailsSectionHeader(stringResource(titleRes))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                PieChart(
                    slices = slices,
                    sliceToKey = sliceToKey,
                    sliceToAmount = sliceToAmount,
                    sliceToColor = sliceToColor,
                    sliceToText = sliceToText,
                    keySave = keySave,
                    keyRestore = keyRestore,
                    pieMaxHeight = 180.dp,
                )
            }
        }
    }

    private fun LazyListScope.animeStatisticsSection(
        user: UserByIdQuery.Data.User,
    ) {
        val statistics = user.statistics?.anime ?: return
        item {
            val navigationCallback = LocalNavigationCallback.current
            StatsCard(
                statistics.count.toString() to Res.string.anime_user_statistics_count,
                String.format(
                    "%.1f",
                    statistics.minutesWatched.minutes.toDouble(DurationUnit.DAYS)
                ) to Res.string.anime_user_statistics_anime_days_watched,
                String.format(
                    "%.1f",
                    statistics.meanScore
                ) to Res.string.anime_user_statistics_mean_score,
                onClick = {
                    navigationCallback.navigate(
                        AnimeDestination.UserList(
                            userId = user.id.toString(),
                            userName = user.name,
                            mediaType = MediaType.ANIME,
                        )
                    )
                }
            )
        }
    }

    private fun LazyListScope.mangaStatisticsSection(
        user: UserByIdQuery.Data.User,
    ) {
        val statistics = user.statistics?.manga ?: return
        item {
            val navigationCallback = LocalNavigationCallback.current
            StatsCard(
                statistics.count.toString() to Res.string.anime_user_statistics_count,
                statistics.chaptersRead.toString() to
                        Res.string.anime_user_statistics_manga_chapters_read,
                statistics.meanScore
                    .let(BigDecimal::fromDouble)
                    .roundToDigitPositionAfterDecimalPoint(1, RoundingMode.FLOOR)
                    .toStringExpanded() to Res.string.anime_user_statistics_mean_score,
                onClick = {
                    navigationCallback.navigate(
                        AnimeDestination.UserList(
                            userId = user.id.toString(),
                            userName = user.name,
                            mediaType = MediaType.MANGA,
                        )
                    )
                }
            )
        }
    }

    @Composable
    private fun StatsCard(
        vararg pairs: Pair<String, StringResource>,
        onClick: () -> Unit,
    ) {
        ElevatedCard(
            onClick = onClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            StatsRow(*pairs)
        }
    }

    @Composable
    fun StatsRow(
        vararg pairs: Pair<String, StringResource>,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            pairs.forEachIndexed { index, pair ->
                if (index != 0) {
                    VerticalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.33f)
                        .padding(8.dp)
                ) {
                    Text(text = pair.first, color = MaterialTheme.colorScheme.surfaceTint)
                    Text(text = stringResource(pair.second))
                }
            }
        }
    }
}
