package com.thekeeperofpie.artistalleydatabase.anime.recommendations.media

import com.anilist.data.fragment.MediaAndRecommendationsRecommendation
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationData

data class MediaRecommendationEntry<MediaEntry>(
    val mediaId: String,
    val recommendation: MediaAndRecommendationsRecommendation,
    val userRating: RecommendationRating? = recommendation.userRating,
    val media: MediaEntry,
) {
    val recommendationData = RecommendationData(
        mediaId = mediaId,
        recommendationMediaId = recommendation.mediaRecommendation.id.toString(),
        rating = recommendation.rating ?: 0,
        userRating = userRating ?: RecommendationRating.NO_RATING,
    )
}
