package com.thekeeperofpie.artistalleydatabase.anime.staff

import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import kotlinx.coroutines.flow.MutableStateFlow

interface StaffSettings : MediaDataSettings {
    val languageOptionStaff: MutableStateFlow<AniListLanguageOption>
    val languageOptionCharacters: MutableStateFlow<AniListLanguageOption>
}
