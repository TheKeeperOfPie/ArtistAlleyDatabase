package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import com.anilist.UserByIdQuery
import com.anilist.fragment.UserMediaStatistics
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState

object UserMediaScreen {

    private const val SCREEN_KEY = "anime_user_media"

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        state: AniListUserViewModel.States,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val isAnime = state is AniListUserViewModel.States.Anime
        Column {
            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
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

            HorizontalDivider()

            val navigationCallback = LocalNavigationCallback.current
            val colorCalculationState = LocalColorCalculationState.current
            when (values[selectedTabIndex]) {
                UserStatsTab.STATS -> UserStatsBasicScreen(
                    user = user,
                    statistics = statistics,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                )
                UserStatsTab.GENRES -> UserStatsDetailScreen(
                    screenKey = SCREEN_KEY,
                    statistics = statistics,
                    values = { it.statistics.genres?.filterNotNull().orEmpty() },
                    state = state.genresState,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.genre.orEmpty() },
                    valueToText = { it.genre.orEmpty() },
                    valueToCount = UserMediaStatistics.Genre::count,
                    valueToMinutesWatched = UserMediaStatistics.Genre::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Genre::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Genre::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ ->
                        navigationCallback.onGenreClick(
                            if (isAnime) MediaType.ANIME else MediaType.MANGA,
                            value.genre!!,
                        )
                    },
                )
                UserStatsTab.TAGS -> UserStatsDetailScreen(
                    screenKey = SCREEN_KEY,
                    statistics = statistics,
                    values = { it.statistics.tags?.filterNotNull().orEmpty() },
                    state = state.tagsState,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.tag?.name.orEmpty() },
                    valueToText = { it.tag?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Tag::count,
                    valueToMinutesWatched = UserMediaStatistics.Tag::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Tag::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Tag::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ ->
                        navigationCallback.onTagClick(
                            if (isAnime) MediaType.ANIME else MediaType.MANGA,
                            value.tag?.id.toString(),
                            value.tag?.name.orEmpty()
                        )
                    },
                )
                UserStatsTab.VOICE_ACTORS -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        screenKey = SCREEN_KEY,
                        statistics = statistics,
                        values = { it.statistics.voiceActors?.filterNotNull().orEmpty() },
                        state = (state as AniListUserViewModel.States.Anime).voiceActorsState,
                        isAnime = true,
                        bottomNavigationState = bottomNavigationState,
                        valueToKey = { it.voiceActor?.id.toString() },
                        valueToText = { it.voiceActor?.name?.primaryName().orEmpty() },
                        valueToCount = UserMediaStatistics.VoiceActor::count,
                        valueToMinutesWatched = UserMediaStatistics.VoiceActor::minutesWatched,
                        valueToChaptersRead = UserMediaStatistics.VoiceActor::chaptersRead,
                        valueToMeanScore = UserMediaStatistics.VoiceActor::meanScore,
                        valueToMediaIds = { it.mediaIds.filterNotNull() },
                        onValueClick = { value, imageWidthToHeightRatio ->
                            val voiceActor = value.voiceActor
                            if (voiceActor != null) {
                                navigationCallback.navigate(
                                    AnimeDestinations.StaffDetails(
                                        staffId = voiceActor.id.toString(),
                                        headerParams = StaffHeaderParams(
                                            coverImageWidthToHeightRatio = imageWidthToHeightRatio,
                                            name = voiceActor.name?.primaryName(languageOptionStaff),
                                            subtitle = voiceActor.name?.subtitleName(languageOptionStaff),
                                            coverImage = voiceActor.image?.large,
                                            colorArgb = colorCalculationState.getColorsNonComposable(voiceActor.id.toString()).first.toArgb(),
                                            favorite = null,
                                        )
                                    )
                                )
                            }
                        },
                        initialItemId = { it.voiceActor?.id.toString() },
                        initialItemImage = { it.voiceActor?.image?.large },
                        initialItemSharedElementKey = { "anime_staff_${it.voiceActor?.id}_image" },
                    )
                }
                UserStatsTab.STUDIOS -> UserStatsDetailScreen(
                    screenKey = SCREEN_KEY,
                    statistics = statistics,
                    values = { it.statistics.studios?.filterNotNull().orEmpty() },
                    state = (state as AniListUserViewModel.States.Anime).studiosState,
                    isAnime = true,
                    bottomNavigationState = bottomNavigationState,
                    valueToKey = { it.studio?.name.orEmpty() },
                    valueToText = { it.studio?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Studio::count,
                    valueToMinutesWatched = UserMediaStatistics.Studio::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Studio::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Studio::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _ ->
                        value.studio?.let {
                            navigationCallback.onStudioClick(it.id.toString(), it.name)
                        }
                    },
                )
                UserStatsTab.STAFF -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        screenKey = SCREEN_KEY,
                        statistics = statistics,
                        values = { it.statistics.staff?.filterNotNull().orEmpty() },
                        state = state.staffState,
                        isAnime = isAnime,
                        bottomNavigationState = bottomNavigationState,
                        valueToKey = { it.staff?.id.toString() },
                        valueToText = { it.staff?.name?.primaryName().orEmpty() },
                        valueToCount = UserMediaStatistics.Staff::count,
                        valueToMinutesWatched = UserMediaStatistics.Staff::minutesWatched,
                        valueToChaptersRead = UserMediaStatistics.Staff::chaptersRead,
                        valueToMeanScore = UserMediaStatistics.Staff::meanScore,
                        valueToMediaIds = { it.mediaIds.filterNotNull() },
                        onValueClick = { value, imageWidthToHeightRatio ->
                            val staff = value.staff
                            if (staff != null) {
                                navigationCallback.navigate(
                                    AnimeDestinations.StaffDetails(
                                        staffId = staff.id.toString(),
                                        headerParams = StaffHeaderParams(
                                            coverImageWidthToHeightRatio = imageWidthToHeightRatio,
                                            name = staff.name?.primaryName(languageOptionStaff),
                                            subtitle = staff.name?.subtitleName(languageOptionStaff),
                                            coverImage = staff.image?.large,
                                            colorArgb = colorCalculationState.getColorsNonComposable(staff.id.toString()).first.toArgb(),
                                            favorite = null,
                                        )
                                    )
                                )
                            }
                        },
                        initialItemId = { it.staff?.id.toString() },
                        initialItemImage = { it.staff?.image?.large },
                        initialItemSharedElementKey = { "anime_staff_${it.staff?.id}_image" },
                    )
                }
            }
        }
    }
}
