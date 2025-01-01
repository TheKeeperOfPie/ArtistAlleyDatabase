package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeActivitiesComponent
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharactersComponent
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumsComponent
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryComponent
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreComponent
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.MangaUserListSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.MediaListSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSearchSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSearchSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsComponent
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsComponent
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationsComponent
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.media.MediaRecommendationsSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.media.MediaRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewsComponent
import com.thekeeperofpie.artistalleydatabase.anime.schedule.ScheduleComponent
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalComponent
import com.thekeeperofpie.artistalleydatabase.anime.songs.SongsComponent
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffComponent
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudiosComponent
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudiosSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.users.UsersComponent
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreenViewModel
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

val LocalAnimeComponent = staticCompositionLocalOf<AnimeComponent> {
    throw IllegalArgumentException("No AnimeComponent provided")
}

interface AnimeComponent : AnimeNewsComponent, AnimeActivitiesComponent, CharactersComponent,
    ForumsComponent, HistoryComponent, IgnoreComponent, NotificationsComponent,
    RecommendationsComponent, ReviewsComponent, ScheduleComponent, SeasonalComponent,
    SongsComponent, StaffComponent, StudiosComponent, UsersComponent {

    val animeHomeMediaViewModelAnime: () -> AnimeHomeMediaViewModel.Anime
    val animeHomeMediaViewModelManga: () -> AnimeHomeMediaViewModel.Manga
    val animeHomeViewModel: () -> AnimeHomeViewModel
    val animeMediaDetailsActivityViewModel: (mediaId: String) -> AnimeMediaDetailsActivityViewModel
    val animeMediaDetailsViewModel: (SavedStateHandle) -> AnimeMediaDetailsViewModel
    val animeRootViewModel: () -> AnimeRootViewModel
    val animeSearchSortFilterViewModelFactory: (SavedStateHandle) -> AnimeSearchSortFilterViewModel.Factory
    val mangaSearchSortFilterViewModelFactory: (SavedStateHandle) -> MangaSearchSortFilterViewModel.Factory
    val animeUserListSortFilterViewModelFactory: (SavedStateHandle, targetUserId: String?) -> AnimeUserListSortFilterViewModel.Factory
    val mangaUserListSortFilterViewModelFactory: (SavedStateHandle, targetUserId: String?) -> MangaUserListSortFilterViewModel.Factory
    val animeSearchViewModelFactory: (
        SavedStateHandle,
        MediaSortFilterViewModel<MediaSortOption>,
        MediaSortFilterViewModel<MediaSortOption>,
        StateFlow<CharacterSortFilterParams>,
        StateFlow<StaffSortFilterParams>,
        StateFlow<StudiosSortFilterParams>,
    ) -> AnimeSearchViewModel.Factory

    // TODO; Move into users module?
    val animeUserListViewModel: (
        SavedStateHandle,
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        status: MediaListStatus?,
        mediaSortFilterViewModel: MediaSortFilterViewModel<MediaListSortOption>,
    ) -> AnimeUserListViewModel
    val mediaActivitiesViewModel: (SavedStateHandle, ActivitySortFilterViewModel) -> MediaActivitiesViewModel
    val mediaCharactersSortFilterViewModel: (SavedStateHandle) -> MediaCharactersSortFilterViewModel
    val mediaCharactersViewModel: (SavedStateHandle, MediaCharactersSortFilterViewModel) -> MediaCharactersViewModel
    val mediaEditViewModel: () -> MediaEditViewModel
    val mediaRecommendationsSortFilterViewModel: (SavedStateHandle) -> MediaRecommendationsSortFilterViewModel
    val mediaRecommendationsViewModelFactory: (mediaId: String, MediaRecommendationsSortFilterViewModel) -> MediaRecommendationsViewModel.Factory
    val unlockScreenViewModel: () -> UnlockScreenViewModel

    val animeSortFilterViewModelFactory: (SavedStateHandle) -> AnimeSortFilterViewModel.Factory

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
