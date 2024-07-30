package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumThreadSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

sealed interface AnimeDestination {
    companion object {
        val typeMap = mapOf(
            typeOf<CharacterHeaderParams?>() to CustomNavTypes.SerializableParcelableType<CharacterHeaderParams>(),
            typeOf<ForumThreadSortOption?>() to CustomNavTypes.NullableEnumType<ForumThreadSortOption>(),
            typeOf<MediaHeaderParams?>() to CustomNavTypes.SerializableParcelableType<MediaHeaderParams>(),
            typeOf<MediaListStatus?>() to CustomNavTypes.NullableEnumType<MediaListStatus>(),
            typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType<MediaSeason>(),
            typeOf<MediaSortOption?>() to CustomNavTypes.NullableEnumType<MediaSortOption>(),
            typeOf<MediaType>() to CustomNavTypes.NullableEnumType<MediaType>(),
            typeOf<MediaType?>() to CustomNavTypes.NullableEnumType<MediaType>(),
            typeOf<Seasonal.Type>() to CustomNavTypes.NullableEnumType<Seasonal.Type>(),
            typeOf<StaffHeaderParams?>() to CustomNavTypes.SerializableParcelableType<StaffHeaderParams>(),
            typeOf<UserHeaderParams?>() to CustomNavTypes.SerializableParcelableType<UserHeaderParams>(),
        )
    }

    @Serializable
    data object Activity : AnimeDestination

    @Serializable
    data class ActivityDetails(
        val activityId: String,
        val sharedTransitionScopeKey: String?,
    ) : AnimeDestination

    @Serializable
    data class CharacterDetails(
        val characterId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: CharacterHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class CharacterMedias(
        val characterId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: CharacterHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class ForumSearch(
        val title: String? = null,
        val titleRes: Int? = null,
        val sort: ForumThreadSortOption? = null,
        val categoryId: String? = null,
        val mediaCategoryId: String? = null,
    ) : AnimeDestination

    @Serializable
    data class MediaCharacters(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class MediaReviews(
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
        val sharedTransitionKey: SharedTransitionKey?,
    ) : AnimeDestination {
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
    data object News : AnimeDestination

    @Serializable
    data object Notifications : AnimeDestination

    @Serializable
    data object Recommendations : AnimeDestination

    @Serializable
    data class ReviewDetails(
        val reviewId: String,
        val sharedTransitionScopeKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data object Reviews : AnimeDestination

    @Serializable
    data class SearchMedia(
        val title: String? = null,
        val titleRes: Int? = null,
        val tagId: String? = null,
        val genre: String? = null,
        val mediaType: MediaType? = null,
        val sort: MediaSortOption? = null,
        val year: Int? = null,
        val lockSortOverride: Boolean? = null,
    ) : AnimeDestination

    @Serializable
    data class Seasonal(val type: Type) : AnimeDestination {
        enum class Type {
            LAST,
            THIS,
            NEXT,
        }
    }

    @Serializable
    data class StaffCharacters(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: StaffHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class StaffDetails(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: StaffHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class User(
        val userId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: UserHeaderParams? = null,
    ) : AnimeDestination

    @Serializable
    data class UserList(
        val userId: String?,
        val userName: String?,
        val mediaType: MediaType,
        val mediaListStatus: MediaListStatus? = null,
    ) : AnimeDestination
}
