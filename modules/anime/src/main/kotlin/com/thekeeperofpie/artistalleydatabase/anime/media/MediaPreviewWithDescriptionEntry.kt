package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils

data class MediaPreviewWithDescriptionEntry(
    override val media: MediaPreviewWithDescription,
    override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
    override val progress: Int? = media.mediaListEntry?.progress,
    override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = media.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
) : AnimeMediaListRow.Entry, MediaStatusAware, AnimeMediaLargeCard.Entry, MediaGridCard.Entry,
    AnimeMediaCompactListRow.Entry {
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val type = media.type
    override val maxProgress = MediaUtils.maxProgress(media)
    override val maxProgressVolumes = media.volumes
    override val averageScore = media.averageScore

    // So that enough meaningful text is shown, strip any double newlines
    override val description = media.description?.replace("<br><br />\n<br><br />\n", "\n")
    override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)
}
