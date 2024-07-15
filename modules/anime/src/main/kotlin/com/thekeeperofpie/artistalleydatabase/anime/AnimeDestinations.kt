package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.compose.navigation.CustomNavTypes
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

object AnimeDestinations {
    @Serializable
    data class MediaDetails(
        val mediaId: String,
        val title: String? = null,
        val coverImage: String? = null,
        val imageWidthToHeightRatio: Float? = null,
        // TODO: Corner radius animation
        val sharedElementKey: String? = null,
        val bannerImage: String? = null,
        val subtitleFormatRes: Int? = null,
        val subtitleStatusRes: Int? = null,
        val subtitleSeason: MediaSeason? = null,
        val subtitleSeasonYear: Int? = null,
        val nextEpisode: Int? = null,
        val nextEpisodeAiringAt: Int? = null,
        val colorArgb: Int? = null,
        val favorite: Boolean? = null,
        val type: MediaType? = null,
    ) {
        companion object {
            val typeMap = mapOf(
                typeOf<MediaSeason?>() to CustomNavTypes.NullableEnumType(MediaSeason::class.java),
                typeOf<MediaType?>() to CustomNavTypes.NullableEnumType(MediaType::class.java),
            )
        }
    }
}
