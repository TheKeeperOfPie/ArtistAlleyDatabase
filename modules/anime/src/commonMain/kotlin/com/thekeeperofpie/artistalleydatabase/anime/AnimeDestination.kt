package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.generated.resources.Res
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
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumDestinations
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumThreadSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsByIdRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.typeOf

sealed interface AnimeDestination : NavDestination {
    companion object {
        // TODO: Move these into their module classes?
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
            typeOf<SeasonalDestinations.Seasonal.Type>() to CustomNavTypes.NullableEnumType(SeasonalDestinations.Seasonal.Type::valueOf),
            typeOf<StaffHeaderParams?>() to CustomNavTypes.SerializableType<StaffHeaderParams>(),
            typeOf<UserHeaderParams?>() to CustomNavTypes.SerializableType<UserHeaderParams>(),
            typeOf<ForumDestinations.ForumSearch.Title?>() to CustomNavTypes.SerializableType<ForumDestinations.ForumSearch.Title>(),
            typeOf<SearchMedia.Title?>() to CustomNavTypes.SerializableType<SearchMedia.Title>(),
        )
    }

    @Serializable
    data object FeatureTiers : AnimeDestination

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

            val routeById: MediaDetailsByIdRoute = { mediaId, sharedTransitionKey ->
                MediaDetails(
                    mediaId = mediaId,
                    title = null,
                    coverImage = null,
                    headerParams = null,
                    sharedTransitionKey = sharedTransitionKey,
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
    data class SearchMedia(
        val title: Title? = null,
        val tagId: String? = null,
        val genre: String? = null,
        val mediaType: MediaType? = null,
        val sort: MediaSortOption? = null,
        val year: Int? = null,
        val lockSortOverride: Boolean? = null,
    ) : AnimeDestination {
        companion object {
            val genreRoute: SearchMediaGenreRoute = { genre, mediaType ->
                SearchMedia(
                    title = Title.Custom(genre),
                    genre = genre,
                    mediaType = mediaType,
                )
            }
            val tagRoute: SearchMediaTagRoute = { tagId, tagName, mediaType ->
                SearchMedia(
                    title = Title.Custom(tagName),
                    tagId = tagId,
                    mediaType = mediaType,
                )
            }
        }

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
}
