package com.thekeeperofpie.artistalleydatabase.anime.home

import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard

data class AnimeHomeDataEntry(
    val lists: List<RowData>?,
) {
    data class MediaEntry(
        override val media: MediaPreviewWithDescription,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = media.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : AnimeMediaLargeCard.Entry {
        // So that enough meaningful text is shown, strip any double newlines
        override val description = media.description?.replace("<br><br />\n<br><br />\n", "\n")
        override val tags = media.tags?.asSequence()
            ?.filterNotNull()
            ?.filter {
                showLessImportantTags
                        || it.category !in MediaUtils.LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
            }
            ?.filter {
                showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
            }
            ?.map(::AnimeMediaTagEntry)
            ?.distinctBy { it.id }?.toList()
            .orEmpty()
    }

    data class RowData(
        val id: String,
        val titleRes: Int,
        val entries: List<MediaEntry>?,
        val viewAllRoute: String?,
    )
}
