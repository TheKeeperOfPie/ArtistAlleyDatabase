package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.type.MediaListStatus

interface MediaStatusAware {
    val mediaListStatus: MediaListStatus?
    val progress: Int?
    val progressVolumes: Int?
    val ignored: Boolean
    val showLessImportantTags: Boolean
    val showSpoilerTags: Boolean
}
