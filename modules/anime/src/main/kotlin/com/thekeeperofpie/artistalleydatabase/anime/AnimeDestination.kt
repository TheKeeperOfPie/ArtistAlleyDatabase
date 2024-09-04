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
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

sealed interface AnimeDestination {
    companion object {
        val typeMap = mapOf(
            typeOf<CharacterHeaderParams?>() to CustomNavTypes.SerializableType<CharacterHeaderParams>(),
            typeOf<ForumThreadSortOption?>() to CustomNavTypes.NullableEnumType(ForumThreadSortOption::valueOf),
            typeOf<MediaHeaderParams?>() to CustomNavTypes.SerializableType<MediaHeaderParams>(),
            typeOf<MediaListStatus?>() to CustomNavTypes.NullableEnumType(MediaListStatus::valueOf),
            typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType(MediaSeason::valueOf),
            typeOf<MediaSortOption?>() to CustomNavTypes.NullableEnumType(MediaSortOption::valueOf),
            typeOf<MediaType>() to CustomNavTypes.NullableEnumType(MediaType::valueOf),
            typeOf<MediaType?>() to CustomNavTypes.NullableEnumType(MediaType::valueOf),
            typeOf<Seasonal.Type>() to CustomNavTypes.NullableEnumType(Seasonal.Type::valueOf),
            typeOf<StaffHeaderParams?>() to CustomNavTypes.SerializableType<StaffHeaderParams>(),
            typeOf<UserHeaderParams?>() to CustomNavTypes.SerializableType<UserHeaderParams>(),
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
    data object AiringSchedule : AnimeDestination

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
    data object FeatureTiers : AnimeDestination

    @Serializable
    data object Forum : AnimeDestination

    @Serializable
    data class ForumSearch(
        val title: String? = null,
        val titleRes: Int? = null,
        val sort: ForumThreadSortOption? = null,
        val categoryId: String? = null,
        val mediaCategoryId: String? = null,
    ) : AnimeDestination

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
        val sharedTransitionKey: SharedTransitionKey? = null,
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
    data class MediaHistory(
        val mediaType: MediaType?,
    ) : AnimeDestination

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
        @Serializable
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
    ) : AnimeDestination

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
