package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import com.anilist.data.fragment.UserNavigationData

data class RecommendationEntry<MediaEntry>(
    val id: String,
    val user: UserNavigationData?,
    val media: MediaEntry,
    val mediaRecommendation: MediaEntry,
    val data: RecommendationData,
)
