package com.thekeeperofpie.artistalleydatabase.anime

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
import com.thekeeperofpie.artistalleydatabase.anime.search.SearchDestinations
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.users.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable
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
            typeOf< SearchDestinations.SearchMedia.Title?>() to CustomNavTypes.SerializableType<SearchDestinations.SearchMedia.Title>(),
        )
    }

    @Serializable
    data object FeatureTiers : AnimeDestination

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
}
