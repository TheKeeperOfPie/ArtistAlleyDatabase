package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings : MediaDataSettings, IgnoreSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>

    val preferredMediaType: MutableStateFlow<MediaType>
    val mediaViewOption: MutableStateFlow<MediaViewOption>
    val rootNavDestination: MutableStateFlow<AnimeRootNavDestination>

    val mediaHistoryEnabled: MutableStateFlow<Boolean>
    val mediaHistoryMaxEntries: MutableStateFlow<Int>

    val languageOptionCharacters: MutableStateFlow<AniListLanguageOption>
    val languageOptionStaff: MutableStateFlow<AniListLanguageOption>
    val languageOptionVoiceActor: MutableStateFlow<VoiceActorLanguageOption>
    val showFallbackVoiceActor: MutableStateFlow<Boolean>

    val currentMediaListSizeAnime: MutableStateFlow<Int>
    val currentMediaListSizeManga: MutableStateFlow<Int>

    val lastCrash: MutableStateFlow<String>
    val lastCrashShown: MutableStateFlow<Boolean>
}
