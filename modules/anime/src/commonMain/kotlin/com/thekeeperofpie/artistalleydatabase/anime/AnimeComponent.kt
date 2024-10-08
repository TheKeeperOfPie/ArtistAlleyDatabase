package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.AnimeCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.AnimeForumThreadsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumRootScreenViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumThreadCommentTreeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.history.MediaHistoryViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsComponent
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.AnimeMediaDetailsRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.media.MediaRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.AnimeMediaDetailsReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.media.MediaReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleViewModel
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.AnimeStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStudiosViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.follow.UserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.social.UserSocialViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreenViewModel
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

val LocalAnimeComponent = staticCompositionLocalOf<AnimeComponent> {
    throw IllegalArgumentException("No AnimeComponent provided")
}

interface AnimeComponent : AnimeNewsComponent {

    val activityDetailsViewModel: (SavedStateHandle) -> ActivityDetailsViewModel
    val airingScheduleViewModel: () -> AiringScheduleViewModel
    val aniListUserViewModel: (SavedStateHandle) -> AniListUserViewModel
    val animeActivityViewModel: () -> AnimeActivityViewModel
    val animeCharacterDetailsViewModel: (SavedStateHandle) -> AnimeCharacterDetailsViewModel
    val animeCharactersViewModel: (SavedStateHandle, AnimeMediaDetailsViewModel) -> AnimeCharactersViewModel
    val animeForumThreadsViewModel: (SavedStateHandle, AnimeMediaDetailsViewModel) -> AnimeForumThreadsViewModel
    val animeHomeMediaViewModelAnime: () -> AnimeHomeMediaViewModel.Anime
    val animeHomeMediaViewModelManga: () -> AnimeHomeMediaViewModel.Manga
    val animeHomeViewModel: () -> AnimeHomeViewModel
    val animeMediaDetailsActivityViewModel: (AnimeMediaDetailsViewModel) -> AnimeMediaDetailsActivityViewModel
    val animeMediaDetailsRecommendationsViewModel: (SavedStateHandle, AnimeMediaDetailsViewModel) -> AnimeMediaDetailsRecommendationsViewModel
    val animeMediaDetailsReviewsViewModel: (AnimeMediaDetailsViewModel) -> AnimeMediaDetailsReviewsViewModel
    val animeMediaDetailsViewModel: (SavedStateHandle) -> AnimeMediaDetailsViewModel
    val animeMediaIgnoreViewModel: (SavedStateHandle) -> AnimeMediaIgnoreViewModel
    val animeRootViewModel: () -> AnimeRootViewModel
    val animeSearchViewModel: (SavedStateHandle) -> AnimeSearchViewModel
    val animeSongsViewModel: (AnimeMediaDetailsViewModel) -> AnimeSongsViewModel
    val animeStaffViewModel: (SavedStateHandle, AnimeMediaDetailsViewModel) -> AnimeStaffViewModel
    val animeUserListViewModel: (
        SavedStateHandle,
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        status: MediaListStatus?,
    ) -> AnimeUserListViewModel
    val characterMediasViewModel: (SavedStateHandle) -> CharacterMediasViewModel
    val forumRootScreenViewModel: () -> ForumRootScreenViewModel
    val forumSearchViewModel: (SavedStateHandle) -> ForumSearchViewModel
    val forumThreadCommentTreeViewModel: (SavedStateHandle) -> ForumThreadCommentTreeViewModel
    val forumThreadViewModel: (SavedStateHandle) -> ForumThreadViewModel
    val mediaActivitiesViewModel: (SavedStateHandle) -> MediaActivitiesViewModel
    val mediaCharactersViewModel: (SavedStateHandle) -> MediaCharactersViewModel
    val mediaEditViewModel: () -> MediaEditViewModel
    val mediaHistoryViewModel: (SavedStateHandle) -> MediaHistoryViewModel
    val mediaRecommendationsViewModel: (SavedStateHandle) -> MediaRecommendationsViewModel
    val mediaReviewsViewModel: (SavedStateHandle) -> MediaReviewsViewModel
    val notificationsViewModel: () -> NotificationsViewModel
    val recommendationsViewModel: () -> RecommendationsViewModel
    val reviewDetailsViewModel: (SavedStateHandle) -> ReviewDetailsViewModel
    val reviewsViewModel: () -> ReviewsViewModel
    val seasonalViewModel: (SavedStateHandle) -> SeasonalViewModel
    val staffCharactersViewModel: (SavedStateHandle) -> StaffCharactersViewModel
    val staffDetailsViewModel: (SavedStateHandle) -> StaffDetailsViewModel
    val studioMediasViewModel: (SavedStateHandle) -> StudioMediasViewModel
    val unlockScreenViewModel: () -> UnlockScreenViewModel
    val userFavoriteCharactersViewModel: (SavedStateHandle) -> UserFavoriteCharactersViewModel
    val userFavoriteMediaViewModel: (SavedStateHandle) -> UserFavoriteMediaViewModel
    val userFavoriteStaffViewModel: (SavedStateHandle) -> UserFavoriteStaffViewModel
    val userFavoriteStudiosViewModel: (SavedStateHandle) -> UserFavoriteStudiosViewModel
    val userListViewModelFollowers: (SavedStateHandle) -> UserListViewModel.Followers
    val userListViewModelFollowing: (SavedStateHandle) -> UserListViewModel.Following
    val userSocialViewModelFollowers: (userId: String?) -> UserSocialViewModel.Followers
    val userSocialViewModelFollowing: (userId: String?) -> UserSocialViewModel.Following

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
