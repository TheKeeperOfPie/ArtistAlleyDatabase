package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeActivitiesComponent
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharactersComponent
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumsComponent
import com.thekeeperofpie.artistalleydatabase.anime.history.MediaHistoryViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsComponent
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationsComponent
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.media.MediaRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewsComponent
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleViewModel
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffComponent
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudiosComponent
import com.thekeeperofpie.artistalleydatabase.anime.users.UsersComponent
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreenViewModel
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

val LocalAnimeComponent = staticCompositionLocalOf<AnimeComponent> {
    throw IllegalArgumentException("No AnimeComponent provided")
}

interface AnimeComponent : AnimeNewsComponent, AnimeActivitiesComponent, CharactersComponent,
    ForumsComponent, RecommendationsComponent, ReviewsComponent, StaffComponent, StudiosComponent,
    UsersComponent {

    val airingScheduleViewModel: () -> AiringScheduleViewModel
    val animeHomeMediaViewModelAnime: () -> AnimeHomeMediaViewModel.Anime
    val animeHomeMediaViewModelManga: () -> AnimeHomeMediaViewModel.Manga
    val animeHomeViewModel: () -> AnimeHomeViewModel
    val animeMediaDetailsActivityViewModel: (mediaId: String) -> AnimeMediaDetailsActivityViewModel
    val animeMediaDetailsViewModel: (SavedStateHandle) -> AnimeMediaDetailsViewModel
    val animeMediaIgnoreViewModel: (SavedStateHandle) -> AnimeMediaIgnoreViewModel
    val animeRootViewModel: () -> AnimeRootViewModel
    val animeSearchViewModel: (SavedStateHandle) -> AnimeSearchViewModel
    val animeSongsViewModel: (AnimeMediaDetailsViewModel) -> AnimeSongsViewModel

    // TODO; Move into users module?
    val animeUserListViewModel: (
        SavedStateHandle,
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        status: MediaListStatus?,
    ) -> AnimeUserListViewModel
    val mediaActivitiesViewModel: (SavedStateHandle, MediaDetailsRoute) -> MediaActivitiesViewModel
    val mediaCharactersViewModel: (SavedStateHandle) -> MediaCharactersViewModel
    val mediaEditViewModel: () -> MediaEditViewModel
    val mediaHistoryViewModel: (SavedStateHandle) -> MediaHistoryViewModel
    val mediaRecommendationsViewModelFactory: (mediaId: String) -> MediaRecommendationsViewModel.Factory
    val notificationsViewModel: () -> NotificationsViewModel
    val seasonalViewModel: (SavedStateHandle) -> SeasonalViewModel
    val unlockScreenViewModel: () -> UnlockScreenViewModel

    @SingletonScope
    @Provides
    fun provideAnimeHistoryDao(database: AnimeDatabase) = database.animeHistoryDao()

    @SingletonScope
    @Provides
    fun provideAnimeIgnoreDao(database: AnimeDatabase) = database.animeIgnoreDao()

    @Provides
    @IntoSet
    fun provideNavigationTypeMap(): @JvmSuppressWildcards Map<KType, NavType<*>> =
        AnimeDestination.typeMap
}
