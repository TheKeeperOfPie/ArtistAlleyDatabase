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
import com.anilist.data.UserByIdQuery
import com.anilist.data.fragment.UserMediaStatistics
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import org.jetbrains.compose.resources.stringResource

object UserMediaScreen {

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
            when (values[selectedTabIndex]) {
                UserStatsTab.STATS -> UserStatsBasicScreen(
                    user = user,
                    statistics = statistics,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                )
                UserStatsTab.GENRES -> UserStatsDetailScreen(
                    statistics = statistics,
                    state = state.genresState,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                    values = { it.statistics.genres?.filterNotNull().orEmpty() },
                    valueToKey = { it.genre.orEmpty() },
                    valueToText = { it.genre.orEmpty() },
                    valueToCount = UserMediaStatistics.Genre::count,
                    valueToMinutesWatched = UserMediaStatistics.Genre::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Genre::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Genre::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _, _ ->
                        val genre = value.genre!!
                        navigationCallback.navigate(
                            AnimeDestination.SearchMedia(
                                title = AnimeDestination.SearchMedia.Title.Custom(genre),
                                genre = genre,
                                mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                            )
                        )
                    },
                )
                UserStatsTab.TAGS -> UserStatsDetailScreen(
                    statistics = statistics,
                    state = state.tagsState,
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                    values = { it.statistics.tags?.filterNotNull().orEmpty() },
                    valueToKey = { it.tag?.name.orEmpty() },
                    valueToText = { it.tag?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Tag::count,
                    valueToMinutesWatched = UserMediaStatistics.Tag::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Tag::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Tag::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _, _ ->
                        value.tag?.let { tag ->
                            navigationCallback.navigate(
                                AnimeDestination.SearchMedia(
                                    title = AnimeDestination.SearchMedia.Title.Custom(tag.name),
                                    tagId = tag.id.toString(),
                                    mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA,
                                )
                            )
                        }
                    },
                )
                UserStatsTab.VOICE_ACTORS -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        statistics = statistics,
                        state = (state as AniListUserViewModel.States.Anime).voiceActorsState,
                        isAnime = true,
                        bottomNavigationState = bottomNavigationState,
                        values = { it.statistics.voiceActors?.filterNotNull().orEmpty() },
                        valueToKey = { it.voiceActor?.id.toString() },
                        valueToText = { it.voiceActor?.name?.primaryName().orEmpty() },
                        valueToCount = UserMediaStatistics.VoiceActor::count,
                        valueToMinutesWatched = UserMediaStatistics.VoiceActor::minutesWatched,
                        valueToChaptersRead = UserMediaStatistics.VoiceActor::chaptersRead,
                        valueToMeanScore = UserMediaStatistics.VoiceActor::meanScore,
                        valueToMediaIds = { it.mediaIds.filterNotNull() },
                        onValueClick = { value, imageState, sharedTransitionKey ->
                            val voiceActor = value.voiceActor
                            if (voiceActor != null) {
                                navigationCallback.navigate(
                                    StaffDestinations.StaffDetails(
                                        staffId = voiceActor.id.toString(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = StaffHeaderParams(
                                            name = voiceActor.name?.primaryName(languageOptionStaff),
                                            subtitle = voiceActor.name?.subtitleName(
                                                languageOptionStaff
                                            ),
                                            coverImage = imageState.toImageState(),
                                            favorite = null,
                                        )
                                    )
                                )
                            }
                        },
                        initialItemId = { it.voiceActor?.id.toString() },
                        initialItemImage = { it.voiceActor?.image?.large },
                        initialItemSharedTransitionKey = {
                            it.voiceActor?.id?.toString()
                                ?.let { SharedTransitionKey.makeKeyForId(it) }
                        },
                        initialItemSharedTransitionIdentifier = { "staff_image" },
                    )
                }
                UserStatsTab.STUDIOS -> UserStatsDetailScreen(
                    statistics = statistics,
                    state = (state as AniListUserViewModel.States.Anime).studiosState,
                    isAnime = true,
                    bottomNavigationState = bottomNavigationState,
                    values = { it.statistics.studios?.filterNotNull().orEmpty() },
                    valueToKey = { it.studio?.name.orEmpty() },
                    valueToText = { it.studio?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Studio::count,
                    valueToMinutesWatched = UserMediaStatistics.Studio::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Studio::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Studio::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _, _ ->
                        value.studio?.let {
                            navigationCallback.navigate(
                                AnimeDestination.StudioMedias(
                                    studioId = it.id.toString(),
                                    name = it.name,
                                )
                            )
                        }
                    },
                )
                UserStatsTab.STAFF -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        statistics = statistics,
                        state = state.staffState,
                        isAnime = isAnime,
                        bottomNavigationState = bottomNavigationState,
                        values = { it.statistics.staff?.filterNotNull().orEmpty() },
                        valueToKey = { it.staff?.id.toString() },
                        valueToText = { it.staff?.name?.primaryName().orEmpty() },
                        valueToCount = UserMediaStatistics.Staff::count,
                        valueToMinutesWatched = UserMediaStatistics.Staff::minutesWatched,
                        valueToChaptersRead = UserMediaStatistics.Staff::chaptersRead,
                        valueToMeanScore = UserMediaStatistics.Staff::meanScore,
                        valueToMediaIds = { it.mediaIds.filterNotNull() },
                        onValueClick = { value, imageState, sharedTransitionKey ->
                            val staff = value.staff
                            if (staff != null) {
                                navigationCallback.navigate(
                                    StaffDestinations.StaffDetails(
                                        staffId = staff.id.toString(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = StaffHeaderParams(
                                            name = staff.name?.primaryName(languageOptionStaff),
                                            subtitle = staff.name?.subtitleName(languageOptionStaff),
                                            coverImage = imageState.toImageState(),
                                            favorite = null,
                                        )
                                    )
                                )
                            }
                        },
                        initialItemId = { it.staff?.id.toString() },
                        initialItemImage = { it.staff?.image?.large },
                        initialItemSharedTransitionKey = {
                            it.staff?.id?.toString()
                                ?.let { SharedTransitionKey.makeKeyForId(it) }
                        },
                        initialItemSharedTransitionIdentifier = { "staff_image" },
                    )
                }
            }
        }
    }
}
