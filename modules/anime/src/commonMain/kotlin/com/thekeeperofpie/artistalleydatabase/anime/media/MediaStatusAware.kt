package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Stable
import com.anilist.data.type.MediaListStatus

@Stable
interface MediaStatusAware {
    val mediaListStatus: MediaListStatus?
    val progress: Int?
    val progressVolumes: Int?
    val scoreRaw: Double?
    val ignored: Boolean
    val showLessImportantTags: Boolean
    val showSpoilerTags: Boolean
}
