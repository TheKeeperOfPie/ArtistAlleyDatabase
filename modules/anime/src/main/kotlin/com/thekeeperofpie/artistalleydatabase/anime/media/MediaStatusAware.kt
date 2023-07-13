package com.thekeeperofpie.artistalleydatabase.anime.media

import com.anilist.type.MediaListStatus

interface MediaStatusAware {
    val mediaListStatus: MediaListStatus?
    val ignored: Boolean
}
