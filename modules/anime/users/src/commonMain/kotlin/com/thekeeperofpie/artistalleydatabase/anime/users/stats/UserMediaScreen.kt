package com.thekeeperofpie.artistalleydatabase.anime.users.stats

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
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StudioMediasRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

object UserMediaScreen {

    @Composable
    operator fun invoke(
        user: () -> UserByIdQuery.Data.User?,
        statsEntry: () -> UserStatsBasicScreen.Entry?,
        isAnime: Boolean,
        genresState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Genre>,
        staffState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Staff>,
        tagsState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Tag>,
        animeVoiceActorsState: () -> UserStatsDetailScreen.State<UserMediaStatistics.VoiceActor>,
        animeStudiosState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Studio>,
        mediaDetailsRoute: MediaDetailsRoute,
        searchMediaGenreRoute: SearchMediaGenreRoute,
        searchMediaTagRoute: SearchMediaTagRoute,
        staffDetailsRoute: StaffDetailsRoute,
        studioMediasRoute: StudioMediasRoute,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        Column {
            var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
            val values = UserStatsTab.entries
                .filter { !it.isAnimeOnly || isAnime }
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.Companion
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

            val navigationController = LocalNavigationController.current
            when (values[selectedTabIndex]) {
                UserStatsTab.STATS -> UserStatsBasicScreen(
                    user = user,
                    entry = statsEntry(),
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                )
                UserStatsTab.GENRES -> {
                    UserStatsDetailScreen<UserMediaStatistics.Genre>(
                        state = genresState(),
                        isAnime = isAnime,
                        bottomNavigationState = bottomNavigationState,
                        values = statsEntry()?.statistics?.genres?.filterNotNull(),
                        valueToKey = { it.genre.orEmpty() },
                        valueToText = { it.genre.orEmpty() },
                        valueToCount = UserMediaStatistics.Genre::count,
                        valueToMinutesWatched = UserMediaStatistics.Genre::minutesWatched,
                        valueToChaptersRead = UserMediaStatistics.Genre::chaptersRead,
                        valueToMeanScore = UserMediaStatistics.Genre::meanScore,
                        valueToMediaIds = { it.mediaIds.filterNotNull() },
                        onValueClick = { value, _, _ ->
                            val genre = value.genre!!
                            navigationController.navigate(
                                searchMediaGenreRoute(
                                    genre,
                                    if (isAnime) MediaType.ANIME else MediaType.MANGA,
                                )
                            )
                        },
                        mediaDetailsRoute = mediaDetailsRoute,
                    )
                }
                UserStatsTab.TAGS -> UserStatsDetailScreen(
                    state = tagsState(),
                    isAnime = isAnime,
                    bottomNavigationState = bottomNavigationState,
                    values = statsEntry()?.statistics?.tags?.filterNotNull(),
                    valueToKey = { it.tag?.name.orEmpty() },
                    valueToText = { it.tag?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Tag::count,
                    valueToMinutesWatched = UserMediaStatistics.Tag::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Tag::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Tag::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _, _ ->
                        value.tag?.let { tag ->
                            navigationController.navigate(
                                searchMediaTagRoute(
                                    tag.id.toString(),
                                    tag.name,
                                    if (isAnime) MediaType.ANIME else MediaType.MANGA,
                                )
                            )
                        }
                    },
                    mediaDetailsRoute = mediaDetailsRoute,
                )
                UserStatsTab.VOICE_ACTORS -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        state = animeVoiceActorsState(),
                        isAnime = true,
                        bottomNavigationState = bottomNavigationState,
                        values = statsEntry()?.statistics?.voiceActors?.filterNotNull(),
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
                                navigationController.navigate(
                                    staffDetailsRoute(
                                        voiceActor.id.toString(),
                                        sharedTransitionKey,
                                        voiceActor.name?.primaryName(languageOptionStaff),
                                        voiceActor.name?.subtitleName(languageOptionStaff),
                                        imageState.toImageState(),
                                        null,
                                    )
                                )
                            }
                        },
                        initialItemId = { it.voiceActor?.id.toString() },
                        initialItemImage = { it.voiceActor?.image?.large },
                        initialItemSharedTransitionKey = {
                            it.voiceActor?.id?.toString()
                                ?.let { SharedTransitionKey.Companion.makeKeyForId(it) }
                        },
                        initialItemSharedTransitionIdentifier = { "staff_image" },
                        mediaDetailsRoute = mediaDetailsRoute,
                    )
                }
                UserStatsTab.STUDIOS -> UserStatsDetailScreen(
                    state = animeStudiosState(),
                    isAnime = true,
                    bottomNavigationState = bottomNavigationState,
                    values = statsEntry()?.statistics?.studios?.filterNotNull(),
                    valueToKey = { it.studio?.name.orEmpty() },
                    valueToText = { it.studio?.name.orEmpty() },
                    valueToCount = UserMediaStatistics.Studio::count,
                    valueToMinutesWatched = UserMediaStatistics.Studio::minutesWatched,
                    valueToChaptersRead = UserMediaStatistics.Studio::chaptersRead,
                    valueToMeanScore = UserMediaStatistics.Studio::meanScore,
                    valueToMediaIds = { it.mediaIds.filterNotNull() },
                    onValueClick = { value, _, _ ->
                        value.studio?.let {
                            navigationController.navigate(
                                studioMediasRoute(it.id.toString(), it.name)
                            )
                        }
                    },
                    mediaDetailsRoute = mediaDetailsRoute,
                )
                UserStatsTab.STAFF -> {
                    val languageOptionStaff = LocalLanguageOptionStaff.current
                    UserStatsDetailScreen(
                        state = staffState(),
                        isAnime = isAnime,
                        bottomNavigationState = bottomNavigationState,
                        values = statsEntry()?.statistics?.staff?.filterNotNull(),
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
                                navigationController.navigate(
                                    staffDetailsRoute(
                                        staff.id.toString(),
                                        sharedTransitionKey,
                                        staff.name?.primaryName(languageOptionStaff),
                                        staff.name?.subtitleName(languageOptionStaff),
                                        imageState.toImageState(),
                                        null,
                                    )
                                )
                            }
                        },
                        initialItemId = { it.staff?.id.toString() },
                        initialItemImage = { it.staff?.image?.large },
                        initialItemSharedTransitionKey = {
                            it.staff?.id?.toString()
                                ?.let { SharedTransitionKey.Companion.makeKeyForId(it) }
                        },
                        initialItemSharedTransitionIdentifier = { "staff_image" },
                        mediaDetailsRoute = mediaDetailsRoute,
                    )
                }
            }
        }
    }
}
