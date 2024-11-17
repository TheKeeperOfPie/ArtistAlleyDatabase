package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.data.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry

data class RecommendationEntry(
    val id: String,
    val user: UserNavigationData?,
    val media: MediaCompactWithTagsEntry,
    val mediaRecommendation: MediaCompactWithTagsEntry,
    val data: RecommendationData,
)
