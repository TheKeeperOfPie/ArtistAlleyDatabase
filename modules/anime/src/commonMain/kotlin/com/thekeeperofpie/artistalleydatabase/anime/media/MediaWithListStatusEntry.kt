package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.data.fragment.MediaWithListStatus
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus

data class MediaWithListStatusEntry(
    val media: MediaWithListStatus,
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
)
