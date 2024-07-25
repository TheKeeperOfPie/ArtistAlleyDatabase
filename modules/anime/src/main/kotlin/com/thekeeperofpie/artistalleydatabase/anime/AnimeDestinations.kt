package com.thekeeperofpie.artistalleydatabase.anime

import android.util.Log
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.compose.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

object AnimeDestinations {
    val typeMap = mapOf(
        typeOf<CharacterHeaderParams?>() to CustomNavTypes.SerializableParcelableType<CharacterHeaderParams>(),
        typeOf<MediaHeaderParams?>() to CustomNavTypes.SerializableParcelableType<MediaHeaderParams>(),
        typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType<MediaSeason>(),
        typeOf<MediaType?>() to CustomNavTypes.NullableEnumType<MediaType>(),
        typeOf<StaffHeaderParams?>() to CustomNavTypes.SerializableParcelableType<StaffHeaderParams>(),
    )

    @Serializable
    data class CharacterDetails(
        val characterId: String,
        val sharedTransitionKey: SharedTransitionKey?,
        val headerParams: CharacterHeaderParams? = null,
    ) {
        init {
            Log.d("SharedDebug", "CharacterDetails sharedTransitionKey = $sharedTransitionKey")
        }
    }

    @Serializable
    data class CharacterMedias(
        val characterId: String,
        val sharedTransitionKey: SharedTransitionKey?,
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
        val coverImage: ImageState? = null,
        // TODO: Corner radius animation
        val headerParams: MediaHeaderParams? = null,
        val sharedTransitionKey: SharedTransitionKey?,
    ) {
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
    data class ReviewDetails(
        val reviewId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    )

    @Serializable
    data class StaffCharacters(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey?,
        val headerParams: StaffHeaderParams? = null,
    )

    @Serializable
    data class StaffDetails(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey?,
        val headerParams: StaffHeaderParams? = null,
    )
}
