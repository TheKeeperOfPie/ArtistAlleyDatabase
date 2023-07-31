package com.thekeeperofpie.artistalleydatabase.anime.home

import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils

data class AnimeHomeDataEntry(
    val lists: List<RowData>?,
) {
    data class MediaEntry(
        override val media: MediaPreviewWithDescription,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = null,
        override val progressVolumes: Int? = null,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : AnimeMediaLargeCard.Entry {
        override val tags = media.tags?.filterNotNull()
            ?.filter {
                showLessImportantTags
                        || it.category !in MediaUtils.LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
            }
            ?.filter {
                showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
            }
            ?.map(::AnimeMediaTagEntry)
            ?.distinctBy { it.id }
            .orEmpty()
    }

    data class RowData(
        val id: String,
        val titleRes: Int,
        val entries: List<MediaEntry>?,
        val viewAllRoute: String?,
    )
}
