package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.fragment.MediaCompactWithTags
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow

data class MediaCompactWithTagsEntry(
    override val media: MediaCompactWithTags,
    override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
    override val progress: Int? = media.mediaListEntry?.progress,
    override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = media.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
) : AnimeMediaCompactListRow.Entry {

    override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)
}
