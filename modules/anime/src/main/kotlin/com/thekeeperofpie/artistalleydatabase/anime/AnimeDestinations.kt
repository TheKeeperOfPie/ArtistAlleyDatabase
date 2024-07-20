package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.compose.navigation.CustomNavTypes
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

object AnimeDestinations {
    val typeMap = mapOf(
        typeOf<CharacterHeaderParams?>() to CustomNavTypes.ParcelableType<CharacterHeaderParams>(),
        typeOf<MediaHeaderParams?>() to CustomNavTypes.ParcelableType<MediaHeaderParams>(),
        typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType<MediaSeason>(),
        typeOf<MediaType?>() to CustomNavTypes.NullableEnumType<MediaType>(),
        typeOf<StaffHeaderParams?>() to CustomNavTypes.ParcelableType<CharacterHeaderParams>(),
    )

    @Serializable
    data class CharacterDetails(
        val characterId: String,
        val sharedElementKey: String? = null,
        val headerParams: CharacterHeaderParams? = null,
    )

    @Serializable
    data class CharacterMedias(
        val characterId: String,
        val sharedElementKey: String? = null,
        val headerParams: CharacterHeaderParams? = null,
    )

    @Serializable
    data class MediaCharacters(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class MediaReviews(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class MediaRecommendations(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class MediaActivities(
        val mediaId: String,
        val showFollowing: Boolean,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class MediaDetails(
        val mediaId: String,
        val title: String? = null,
        val coverImage: String? = null,
        val sharedElementKey: String? = null,
        // TODO: Corner radius animation
        val headerParams: MediaHeaderParams? = null,
    ) {
        constructor(
            mediaNavigationData: MediaNavigationData,
            coverImageWidthToHeightRatio: Float?,
            languageOptionMedia: AniListLanguageOption,
        ) : this(
            mediaId = mediaNavigationData.id.toString(),
            title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
            coverImage = mediaNavigationData.coverImage?.extraLarge,
            sharedElementKey = null,
            headerParams = MediaHeaderParams(
                coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                title = mediaNavigationData.title?.primaryTitle(languageOptionMedia),
                coverImage = mediaNavigationData.coverImage?.extraLarge,
            ),
        )
    }

    @Serializable
    data class ReviewDetails(
        val reviewId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class StaffCharacters(
        val staffId: String,
        val sharedElementKey: String? = null,
        val headerParams: StaffHeaderParams? = null,
    )

    @Serializable
    data class StaffDetails(
        val staffId: String,
        val sharedElementKey: String? = null,
        val headerParams: StaffHeaderParams? = null,
    )
}
