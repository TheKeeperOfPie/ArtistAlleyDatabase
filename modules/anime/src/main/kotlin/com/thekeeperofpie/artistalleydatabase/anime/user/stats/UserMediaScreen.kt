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
import com.anilist.UserByIdQuery
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState

object UserMediaScreen {

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        state: AniListUserViewModel.States,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val isAnime = state is AniListUserViewModel.States.Anime
        Column {
            var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
            val values = UserStatsTab.values()
                .filter { !it.isAnimeOnly || isAnime }
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth(),
                divider = { /* No divider, manually draw so that it's full width */ }
            ) {
                values.forEachIndexed { index, tab ->
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
                    navigationCallback = navigationCallback,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                )
                UserStatsTab.GENRES -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.genres?.filterNotNull().orEmpty() },
                    state = state.genresState,
                    isAnime = isAnime,
                    navigationCallback = navigationCallback,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.genre.orEmpty() },
                    valueToCount = UserMediaStatistics.Genre::count,
                    valueToMinutesWatched = UserMediaStatistics.Genre::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Genre::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Genre::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ -> navigationCallback.onGenreClick(value.genre!!) },
                )
                UserStatsTab.TAGS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.tags?.filterNotNull().orEmpty() },
                    state = state.tagsState,
                    isAnime = isAnime,
                    navigationCallback = navigationCallback,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.tag?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Tag::count,
                    valueToMinutesWatched = UserMediaStatistics.Tag::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Tag::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Tag::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ ->
                        navigationCallback.onTagClick(
                            value.tag?.id.toString(),
                            value.tag?.name.orEmpty()
                        )
                    },
                )
                UserStatsTab.VOICE_ACTORS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.voiceActors?.filterNotNull().orEmpty() },
                    state = (state as AniListUserViewModel.States.Anime).voiceActorsState,
                    isAnime = true,
                    navigationCallback = navigationCallback,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.voiceActor?.name?.userPreferred.orEmpty() },
                    valueToCount = UserMediaStatistics.VoiceActor::count,
                    valueToMinutesWatched = UserMediaStatistics.VoiceActor::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.VoiceActor::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.VoiceActor::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, imageWidthToHeightRatio ->
                        navigationCallback.onStaffClick(value.voiceActor!!, imageWidthToHeightRatio)
                    },
                    initialItemImage = { it.voiceActor?.image?.large },
                )
                UserStatsTab.STUDIOS -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.studios?.filterNotNull().orEmpty() },
                    state = (state as AniListUserViewModel.States.Anime).studiosState,
                    isAnime = true,
                    navigationCallback = navigationCallback,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.studio?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Studio::count,
                    valueToMinutesWatched = UserMediaStatistics.Studio::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Studio::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Studio::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ ->
                        navigationCallback.onStudioClick(value.studio?.id.toString())
                    },
                )
                UserStatsTab.STAFF -> UserStatsDetailScreen(
                    statistics = statistics,
                    values = { it.statistics.staff?.filterNotNull().orEmpty() },
                    state = state.staffState,
                    isAnime = isAnime,
                    navigationCallback = navigationCallback,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.staff?.name?.userPreferred.orEmpty() },
                    valueToCount = UserMediaStatistics.Staff::count,
                    valueToMinutesWatched = UserMediaStatistics.Staff::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Staff::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Staff::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, imageWidthToHeightRatio ->
                        navigationCallback.onStaffClick(value.staff!!, imageWidthToHeightRatio)
                    },
                    initialItemImage = { it.staff?.image?.large },
                )
            }
        }
    }
}
