package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.anilist.type.MediaListStatus
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow

data class RecommendationEntry(
    val mediaId: String,
    val recommendation: MediaAndRecommendationsRecommendation,
    override var mediaListStatus: MediaListStatus? =
        recommendation.mediaRecommendation.mediaListEntry?.status,
    override val progress: Int? = null,
    override val progressVolumes: Int? = null,
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
    val entry = recommendation.mediaRecommendation?.let {
        AnimeMediaListRow.Entry(
            media = it,
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags,
        )
    }
}
