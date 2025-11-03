package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavType
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeActivitiesComponent
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharactersComponent
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumsComponent
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeHistoryDao
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryComponent
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreComponent
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.AnimeIgnoreDao
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.MangaUserListSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.MediaListSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersViewModel
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
import com.thekeeperofpie.artistalleydatabase.anime.search.SearchComponent
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalComponent
import com.thekeeperofpie.artistalleydatabase.anime.songs.SongsComponent
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffComponent
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudiosComponent
import com.thekeeperofpie.artistalleydatabase.anime.users.UsersComponent
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreenViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KType

val LocalAnimeComponent = staticCompositionLocalOf<AnimeComponent> {
    throw IllegalArgumentException("No AnimeComponent provided")
}

interface AnimeComponent : AnimeNewsComponent, AnimeActivitiesComponent, CharactersComponent,
    ForumsComponent, HistoryComponent, IgnoreComponent, NotificationsComponent,
    RecommendationsComponent, ReviewsComponent, ScheduleComponent, SearchComponent,
    SeasonalComponent, SongsComponent, StaffComponent, StudiosComponent, UsersComponent {

    val animeMediaDetailsActivityViewModelFactory: AnimeMediaDetailsActivityViewModel.Factory
    val animeMediaDetailsViewModelFactory: AnimeMediaDetailsViewModel.Factory
    val animeSearchSortFilterViewModelFactoryFactory: AnimeSearchSortFilterViewModel.TypedFactory.Factory
    val mangaSearchSortFilterViewModelFactoryFactory: MangaSearchSortFilterViewModel.TypedFactory.Factory
    val animeUserListSortFilterViewModelFactoryFactory: AnimeUserListSortFilterViewModel.TypedFactory.Factory
    val mangaUserListSortFilterViewModelFactoryFactory: MangaUserListSortFilterViewModel.TypedFactory.Factory
    val mediaActivitiesViewModelFactory: MediaActivitiesViewModel.Factory
    val mediaCharactersSortFilterViewModelFactory: MediaCharactersSortFilterViewModel.Factory
    val mediaCharactersViewModelFactory: MediaCharactersViewModel.Factory
    val mediaRecommendationsSortFilterViewModelFactory: MediaRecommendationsSortFilterViewModel.Factory
    val mediaRecommendationsViewModelFactoryFactory: MediaRecommendationsViewModel.TypedFactory.Factory
    val animeSortFilterViewModelFactoryFactory: AnimeSortFilterViewModel.TypedFactory.Factory

    val animeHomeMediaViewModelAnime: Provider<AnimeHomeMediaViewModel.Anime>
    val animeHomeMediaViewModelManga: Provider<AnimeHomeMediaViewModel.Manga>
    val animeHomeViewModel: Provider<AnimeHomeViewModel>
    val animeRootViewModel: Provider<AnimeRootViewModel>
    val mediaEditViewModel: Provider<MediaEditViewModel>
    val unlockScreenViewModel: Provider<UnlockScreenViewModel>

    // TODO; Move into users module?
    val animeUserListViewModelFactory: AnimeUserListViewModel.Factory

    @SingleIn(AppScope::class)
    @Provides
    fun provideAnimeHistoryDao(database: AnimeDatabase): AnimeHistoryDao =
        database.animeHistoryDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAnimeIgnoreDao(database: AnimeDatabase): AnimeIgnoreDao = database.animeIgnoreDao()

    @Provides
    @IntoSet
    fun provideNavigationTypeMap(): @JvmSuppressWildcards Map<KType, NavType<*>> =
        AnimeDestination.typeMap
}
