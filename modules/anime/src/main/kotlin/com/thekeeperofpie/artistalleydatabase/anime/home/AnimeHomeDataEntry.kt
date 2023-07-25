package com.thekeeperofpie.artistalleydatabase.anime.home

import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard

data class AnimeHomeDataEntry(
    val lists: List<RowData>?,
) {
    data class MediaEntry(
        override val media: MediaPreviewWithDescription,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = null,
        override val progressVolumes: Int? = null,
        override val ignored: Boolean = false,
    ) : AnimeMediaLargeCard.Entry

    data class RowData(
        val id: String,
        val titleRes: Int,
        val entries: List<MediaEntry>?,
        val viewAllRoute: String?,
    )
}
