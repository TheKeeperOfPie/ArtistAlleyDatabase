package com.thekeeperofpie.artistalleydatabase.anime.characters

import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import kotlinx.coroutines.flow.MutableStateFlow

interface CharacterSettings : MediaDataSettings {
    val languageOptionCharacters: MutableStateFlow<AniListLanguageOption>
}
