package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_active_title
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_new_title
import artistalleydatabase.modules.anime.generated.resources.anime_forum_root_releases_title
import artistalleydatabase.modules.anime.generated.resources.anime_home_last_added_screen_title
import artistalleydatabase.modules.anime.generated.resources.anime_home_suggestion_popular_all_time
import artistalleydatabase.modules.anime.generated.resources.anime_home_suggestion_top
import artistalleydatabase.modules.anime.generated.resources.anime_home_top_released_this_year_title
import artistalleydatabase.modules.anime.generated.resources.anime_home_trending_screen_title
import com.anilist.data.fragment.AniListDate
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumThreadSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.typeOf

sealed interface AnimeDestination : NavDestination {
    companion object {
        val typeMap = mapOf(
            typeOf<AniListDate?>() to CustomNavTypes.SerializableType<AniListDate>(),
            typeOf<CharacterHeaderParams?>() to CustomNavTypes.SerializableType<CharacterHeaderParams>(),
            typeOf<ForumThreadSortOption?>() to CustomNavTypes.NullableEnumType(
                ForumThreadSortOption::valueOf
            ),
            typeOf<MediaHeaderParams?>() to CustomNavTypes.SerializableType<MediaHeaderParams>(),
            typeOf<MediaListStatus?>() to CustomNavTypes.NullableEnumType(MediaListStatus::valueOf),
            typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType(MediaSeason::valueOf),
            typeOf<MediaSortOption?>() to CustomNavTypes.NullableEnumType(MediaSortOption::valueOf),
            typeOf<MediaType>() to CustomNavTypes.NullableEnumType(MediaType::valueOf),
            typeOf<MediaType?>() to CustomNavTypes.NullableEnumType(MediaType::valueOf),
            typeOf<Seasonal.Type>() to CustomNavTypes.NullableEnumType(Seasonal.Type::valueOf),
            typeOf<StaffHeaderParams?>() to CustomNavTypes.SerializableType<StaffHeaderParams>(),
            typeOf<UserHeaderParams?>() to CustomNavTypes.SerializableType<UserHeaderParams>(),
            typeOf<ForumSearch.Title?>() to CustomNavTypes.SerializableType<ForumSearch.Title>(),
            typeOf<SearchMedia.Title?>() to CustomNavTypes.SerializableType<SearchMedia.Title>(),
        )
    }

    @Serializable
    data object AiringSchedule : AnimeDestination

    @Serializable
    data object FeatureTiers : AnimeDestination

    @Serializable
    data object Forum : AnimeDestination

    @Serializable
    data class ForumSearch(
        val title: Title? = null,
        val sort: ForumThreadSortOption? = null,
        val categoryId: String? = null,
        val mediaCategoryId: String? = null,
    ) : AnimeDestination {
        @Serializable
        sealed interface Title {
            @Composable
            fun text(): String

            @Serializable
            data object Active : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_active_title)
            }

            @Serializable
            data object New : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_new_title)
            }

            @Serializable
            data object Releases : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_forum_root_releases_title)
            }

            @Serializable
            data class Custom(val title: String) : Title {
                @Composable
                override fun text() = title
            }
        }
    }

    @Serializable
    data class ForumThread(
        val threadId: String,
        val title: String? = null,
    ) : AnimeDestination

    @Serializable
    data class ForumThreadComment(
        val threadId: String,
        val commentId: String,
        val title: String? = null,
    ) : AnimeDestination

    @Serializable
    data class Ignored(
        val mediaType: MediaType?,
    ) : AnimeDestination

    @Serializable
    data class MediaCharacters(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class MediaRecommendations(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class MediaActivities(
        val mediaId: String,
        val showFollowing: Boolean,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class MediaDetails(
        val mediaId: String,
        val title: String? = null,
        val coverImage: ImageState? = null,
        // TODO: Corner radius animation
        val headerParams: MediaHeaderParams? = null,
        val sharedTransitionKey: SharedTransitionKey? = null,
    ) : AnimeDestination {
        companion object {
            val route: MediaDetailsRoute =
                { mediaNavigationData, coverImage, languageOptionMedia, sharedTransitionKey ->
                    MediaDetails(
                        mediaId = mediaNavigationData.id.toString(),
                        title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
                        coverImage = coverImage,
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = MediaHeaderParams(
                            title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
                            coverImage = coverImage,
                        ),
                    )
                }
        }
        constructor(
            mediaNavigationData: MediaNavigationData,
            coverImage: ImageState?,
            languageOptionMedia: AniListLanguageOption,
            sharedTransitionKey: SharedTransitionKey?,
        ) : this(
            mediaId = mediaNavigationData.id.toString(),
            title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
            coverImage = coverImage,
            sharedTransitionKey = sharedTransitionKey,
            headerParams = MediaHeaderParams(
                title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
                coverImage = coverImage,
            ),
        )
    }

    @Serializable
    data class MediaHistory(
        val mediaType: MediaType?,
    ) : AnimeDestination

    @Serializable
    data object Notifications : AnimeDestination

    @Serializable
    data class SearchMedia(
        val title: Title? = null,
        val tagId: String? = null,
        val genre: String? = null,
        val mediaType: MediaType? = null,
        val sort: MediaSortOption? = null,
        val year: Int? = null,
        val lockSortOverride: Boolean? = null,
    ) : AnimeDestination {
        // TODO: Find a way to serialize StringResource
        @Serializable
        sealed interface Title {
            @Composable
            fun text(): String

            @Serializable
            data object HomeSuggestionPopularAllTime : Title {
                @Composable
                override fun text() =
                    stringResource(Res.string.anime_home_suggestion_popular_all_time)
            }

            @Serializable
            data object HomeSuggestionTop : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_suggestion_top)
            }

            @Serializable
            data object HomeTrending : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_trending_screen_title)
            }

            @Serializable
            data object HomeLastAdded : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_last_added_screen_title)
            }

            @Serializable
            data object HomeReleasedThisYear : Title {
                @Composable
                override fun text() =
                    stringResource(Res.string.anime_home_top_released_this_year_title)
            }

            @Serializable
            data class Custom(val title: String) : Title {
                @Composable
                override fun text() = title
            }
        }
    }

    @Serializable
    data class Seasonal(val type: Type) : AnimeDestination {
        @Serializable
        enum class Type {
            LAST,
            THIS,
            NEXT,
        }
    }

    @Serializable
    data class StudioMedias(
        val studioId: String,
        val name: String? = null,
        // TODO: Favorite is never actually passed in
        val favorite: Boolean? = null,
    ) : AnimeDestination

    @Serializable
    data class User(
        val userId: String? = null,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: UserHeaderParams? = null,
    ) : AnimeDestination {
        companion object {
            val route: UserRoute = { id, userSharedTransitionKey, name, imageState ->
                AnimeDestination.User(
                    userId = id,
                    sharedTransitionKey = userSharedTransitionKey,
                    headerParams = UserHeaderParams(
                        name = name,
                        bannerImage = null,
                        coverImage = imageState,
                    )
                )
            }
        }
    }

    @Serializable
    data class UserFavoriteCharacters(
        val userId: String?,
        val userName: String? = null,
    ) : AnimeDestination

    @Serializable
    data class UserFavoriteMedia(
        val userId: String?,
        val userName: String? = null,
        val mediaType: MediaType,
    ) : AnimeDestination

    @Serializable
    data class UserFavoriteStaff(
        val userId: String?,
        val userName: String? = null,
    ) : AnimeDestination

    @Serializable
    data class UserFavoriteStudios(
        val userId: String?,
        val userName: String? = null,
    ) : AnimeDestination

    @Serializable
    data class UserFollowers(
        val userId: String?,
        val userName: String? = null,
    ) : AnimeDestination

    @Serializable
    data class UserFollowing(
        val userId: String?,
        val userName: String? = null,
    ) : AnimeDestination

    @Serializable
    data class UserList(
        val userId: String?,
        val userName: String?,
        val mediaType: MediaType,
        val mediaListStatus: MediaListStatus? = null,
    ) : AnimeDestination
}
