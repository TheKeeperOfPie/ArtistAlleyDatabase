package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.data.fragment.MediaWithListStatus
import com.thekeeperofpie.artistalleydatabase.anime.data.toNextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils

data class MediaWithListStatusEntry(
    override val media: MediaWithListStatus,
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
) : MediaGridCard.Entry {
    override val type
        get() = media.type
    override val chapters
        get() = media.chapters
    override val episodes
        get() = media.episodes
    override val volumes
        get() = media.volumes
    override val nextAiringEpisode = media.nextAiringEpisode?.toNextAiringEpisode()
    override val mediaListStatus
        get() = mediaFilterable.mediaListStatus
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val averageScore
        get() = media.averageScore
    override val ignored
        get() = mediaFilterable.ignored
    override val progress
        get() = mediaFilterable.progress
    override val progressVolumes
        get() = mediaFilterable.progressVolumes
    override val scoreRaw
        get() = mediaFilterable.scoreRaw

    object Provider : MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry> {
        override fun mediaEntry(media: MediaWithListStatus) = MediaWithListStatusEntry(media)
        override fun mediaFilterable(entry: MediaWithListStatusEntry) = entry.mediaFilterable
        override fun copyMediaEntry(entry: MediaWithListStatusEntry, data: MediaFilterableData) =
            entry.copy(mediaFilterable = data)
        override fun id(entry: MediaWithListStatusEntry) = entry.mediaFilterable.mediaId
    }
}
