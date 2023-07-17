package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware

data class RecommendationEntry(
    val recommendation: MediaAndRecommendationsRecommendation,
    override var mediaListStatus: MediaListStatus? =
        recommendation.mediaRecommendation?.mediaListEntry?.status,
    override val progress: Int? = null,
    override val progressVolumes: Int? = null,
    override val ignored: Boolean = false,
) : MediaStatusAware {
    val entry = recommendation.mediaRecommendation?.let {
        AnimeMediaListRow.Entry(
            media = it,
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            ignored = ignored,
        )
    }
}
