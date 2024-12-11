package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anime.data.toNextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils

data class MediaPreviewEntry(
    override val media: MediaPreview,
    val mediaFilterable: MediaFilterableData = MediaFilterableData(
        mediaId = media.id.toString(),
        isAdult = media.isAdult,
        mediaListStatus = media.mediaListEntry?.status?.toMediaListStatus(),
        progress = media.mediaListEntry?.progress,
        progressVolumes = media.mediaListEntry?.progressVolumes,
        scoreRaw = media.mediaListEntry?.score,
        ignored = false,
        showLessImportantTags = false,
        showSpoilerTags = false,
    ),
) : AnimeMediaListRow.Entry, AnimeMediaCompactListRow.Entry {
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val tags = MediaUtils.buildTags(
        media = media,
        showLessImportantTags = mediaFilterable.showLessImportantTags,
        showSpoilerTags = mediaFilterable.showSpoilerTags,
    )
    override val type get() = media.type
    override val chapters get() = media.chapters
    override val episodes get() = media.episodes
    override val volumes get() = media.volumes
    override val nextAiringEpisode get() = media.nextAiringEpisode?.toNextAiringEpisode()

    override val mediaListStatus get() = mediaFilterable.mediaListStatus
    override val progress get() = mediaFilterable.progress
    override val progressVolumes get() = mediaFilterable.progressVolumes
    override val scoreRaw get() = mediaFilterable.scoreRaw
    override val ignored get() = mediaFilterable.ignored

    object Provider : MediaEntryProvider<MediaPreview, MediaPreviewEntry> {
        override fun mediaEntry(media: MediaPreview) = MediaPreviewEntry(media)
        override fun mediaFilterable(entry: MediaPreviewEntry) = entry.mediaFilterable
        override fun copyMediaEntry(entry: MediaPreviewEntry, data: MediaFilterableData) =
            entry.copy(mediaFilterable = data)
        override fun id(entry: MediaPreviewEntry) = entry.mediaFilterable.mediaId
    }
}
