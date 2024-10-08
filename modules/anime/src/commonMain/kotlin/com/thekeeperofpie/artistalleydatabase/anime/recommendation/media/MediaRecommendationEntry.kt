package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData

data class MediaRecommendationEntry(
    val mediaId: String,
    val recommendation: MediaAndRecommendationsRecommendation,
    val userRating: RecommendationRating? = recommendation.userRating,
    val media: MediaPreviewEntry = MediaPreviewEntry(media = recommendation.mediaRecommendation),
) {
    val recommendationData = RecommendationData(
        mediaId = mediaId,
        recommendationMediaId = recommendation.mediaRecommendation.id.toString(),
        rating = recommendation.rating ?: 0,
        userRating = userRating ?: RecommendationRating.NO_RATING,
    )
}
