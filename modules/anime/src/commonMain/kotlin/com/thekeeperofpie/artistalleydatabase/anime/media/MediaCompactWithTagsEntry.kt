package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.fragment.MediaCompactWithTags
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.data.toNextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow

data class MediaCompactWithTagsEntry(
    override val media: MediaCompactWithTags,
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
) : AnimeMediaCompactListRow.Entry {
    override val tags = MediaUtils.buildTags(
        media = media,
        showLessImportantTags = mediaFilterable.showLessImportantTags,
        showSpoilerTags = mediaFilterable.showSpoilerTags,
    )
    override val ignored: Boolean get() = mediaFilterable.ignored
    override val mediaListStatus get() = mediaFilterable.mediaListStatus
    override val progress get() = mediaFilterable.progress
    override val progressVolumes get() = mediaFilterable.progressVolumes
    override val scoreRaw get() = mediaFilterable.scoreRaw
    override val type get() = media.type
    override val chapters get() = media.chapters
    override val episodes get() = media.episodes
    override val volumes get() = media.volumes
    override val nextAiringEpisode = media.nextAiringEpisode?.toNextAiringEpisode()
}
