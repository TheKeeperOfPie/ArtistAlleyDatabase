package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.UserByIdQuery
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen

object UserMediaScreen {

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        genresState: UserStatsDetailsState<UserMediaStatistics.Genre>,
        tagsState: UserStatsDetailsState<UserMediaStatistics.Tag>,
        voiceActorsState: UserStatsDetailsState<UserMediaStatistics.VoiceActor>?,
        studiosState: UserStatsDetailsState<UserMediaStatistics.Studio>?,
        staffState: UserStatsDetailsState<UserMediaStatistics.Staff>,
        isAnime: Boolean,
        callback: AniListUserScreen.Callback,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
    ) {
        Column {
            var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
            val values = UserStatsTab.values().filter { !it.isAnimeOnly || isAnime }
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth(),
                divider = { /* No divider, manually draw so that it's full width */ }
            ) {
                values.forEachIndexed { index, tab ->
                    if (tab.isAnimeOnly && !isAnime) return@forEachIndexed
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                    )
                }
            }

            Divider()

            when (values[selectedTabIndex]) {
                UserStatsTab.STATS -> UserStatsBasicScreen(
                    user = user,
                    statistics = statistics,
                    callback = callback,
                    isAnime = isAnime,
                    bottomNavBarPadding = bottomNavBarPadding,
                )
                UserStatsTab.GENRES -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.genres?.filterNotNull().orEmpty() },
                    state = genresState,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                    valueToKey = { it.genre.orEmpty() },
                    valueToCount = UserMediaStatistics.Genre::count,
                    valueToMinutesWatched = UserMediaStatistics.Genre::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Genre::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Genre::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { callback.onGenreClick(it.genre!!) },
                )
                UserStatsTab.TAGS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.tags?.filterNotNull().orEmpty() },
                    state = tagsState,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                    valueToKey = { it.tag?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Tag::count,
                    valueToMinutesWatched = UserMediaStatistics.Tag::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Tag::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Tag::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = {
                        callback.onTagClick(
                            it.tag?.id.toString(),
                            it.tag?.name.orEmpty()
                        )
                    },
                )
                UserStatsTab.VOICE_ACTORS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.voiceActors?.filterNotNull().orEmpty() },
                    state = voiceActorsState!!,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                    valueToKey = { it.voiceActor?.name?.userPreferred.orEmpty() },
                    valueToCount = UserMediaStatistics.VoiceActor::count,
                    valueToMinutesWatched = UserMediaStatistics.VoiceActor::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.VoiceActor::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.VoiceActor::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { callback.onStaffClicked(it.voiceActor?.id.toString()) },
                    initialItemImage = { it.voiceActor?.image?.large },
                )
                UserStatsTab.STUDIOS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.studios?.filterNotNull().orEmpty() },
                    state = studiosState!!,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                    valueToKey = { it.studio?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Studio::count,
                    valueToMinutesWatched = UserMediaStatistics.Studio::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Studio::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Studio::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { callback.onStudioClicked(it.studio?.id.toString()) },
                )
                UserStatsTab.STAFF -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.staff?.filterNotNull().orEmpty() },
                    state = staffState,
                    callback = callback,
                    bottomNavBarPadding = bottomNavBarPadding,
                    valueToKey = { it.staff?.name?.userPreferred.orEmpty() },
                    valueToCount = UserMediaStatistics.Staff::count,
                    valueToMinutesWatched = UserMediaStatistics.Staff::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Staff::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Staff::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { callback.onStaffClicked(it.staff?.id.toString()) },
                    initialItemImage = { it.staff?.image?.large },
                )
            }
        }
    }
}
