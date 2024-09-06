package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaListStatus

data class MediaWithListStatusEntry(
    val media: MediaWithListStatus,
    override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
    override val progress: Int? = media.mediaListEntry?.progress,
    override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = media.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
) : MediaStatusAware
