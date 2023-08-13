package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.anilist.type.MediaListStatus
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware

data class RecommendationEntry(
    val mediaId: String,
    val recommendation: MediaAndRecommendationsRecommendation,
    override var mediaListStatus: MediaListStatus? =
        recommendation.mediaRecommendation.mediaListEntry?.status,
    override val progress: Int? = recommendation.mediaRecommendation.mediaListEntry?.progress,
    override val progressVolumes: Int? = recommendation.mediaRecommendation.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = recommendation.mediaRecommendation.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
    val userRating: RecommendationRating? = recommendation.userRating,
) : MediaStatusAware {
    val recommendationData = RecommendationData(
        mediaId = mediaId,
        recommendationMediaId = recommendation.mediaRecommendation.id.toString(),
        rating = recommendation.rating ?: 0,
        userRating = userRating ?: RecommendationRating.NO_RATING,
    )
    val entry = MediaPreviewEntry(
        media = recommendation.mediaRecommendation,
        mediaListStatus = mediaListStatus,
        progress = progress,
        progressVolumes = progressVolumes,
        scoreRaw = scoreRaw,
        ignored = ignored,
        showLessImportantTags = showLessImportantTags,
        showSpoilerTags = showSpoilerTags,
    )
}
