package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSettings
import com.thekeeperofpie.artistalleydatabase.anime.history.HistorySettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings : CharacterSettings, MediaDataSettings, HistorySettings, IgnoreSettings,
    StaffSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>

    val rootNavDestination: MutableStateFlow<AnimeRootNavDestination>

    val languageOptionVoiceActor: MutableStateFlow<VoiceActorLanguageOption>
    val showFallbackVoiceActor: MutableStateFlow<Boolean>

    val currentMediaListSizeAnime: MutableStateFlow<Int>
    val currentMediaListSizeManga: MutableStateFlow<Int>

    val lastCrash: MutableStateFlow<String>
    val lastCrashShown: MutableStateFlow<Boolean>
}
