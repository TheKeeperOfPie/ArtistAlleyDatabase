package com.thekeeperofpie.artistalleydatabase.anilist

import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import kotlinx.coroutines.flow.MutableStateFlow

interface AniListSettings {
    val aniListViewer: MutableStateFlow<AniListViewer?>
}
