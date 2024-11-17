package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.data.type.RecommendationRating

data class RecommendationData(
    val mediaId: String,
    val recommendationMediaId: String,
    val rating: Int,
    val userRating: RecommendationRating,
)
