package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils

data class MediaPreviewEntry(
    override val media: MediaPreview,
    override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
    override val progress: Int? = media.mediaListEntry?.progress,
    override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = media.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
) : AnimeMediaListRow.Entry, AnimeMediaCompactListRow.Entry {
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)
}
