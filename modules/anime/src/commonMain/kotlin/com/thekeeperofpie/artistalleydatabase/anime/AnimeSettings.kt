package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AnimeSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
    val showAdult: MutableStateFlow<Boolean>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val showLessImportantTags: MutableStateFlow<Boolean>
    val showSpoilerTags: MutableStateFlow<Boolean>

    val preferredMediaType: MutableStateFlow<MediaType>
    val mediaViewOption: MutableStateFlow<MediaViewOption>
    val rootNavDestination: MutableStateFlow<AnimeRootNavDestination>

    val mediaHistoryEnabled: MutableStateFlow<Boolean>
    val mediaHistoryMaxEntries: MutableStateFlow<Int>

    val mediaIgnoreEnabled: MutableStateFlow<Boolean>
    val mediaIgnoreHide: MutableStateFlow<Boolean>

    // Invert boolean and remove this
    val showIgnored: StateFlow<Boolean>

    val languageOptionMedia: MutableStateFlow<AniListLanguageOption>
    val languageOptionCharacters: MutableStateFlow<AniListLanguageOption>
    val languageOptionStaff: MutableStateFlow<AniListLanguageOption>
    val languageOptionVoiceActor: MutableStateFlow<VoiceActorLanguageOption>
    val showFallbackVoiceActor: MutableStateFlow<Boolean>

    val currentMediaListSizeAnime: MutableStateFlow<Int>
    val currentMediaListSizeManga: MutableStateFlow<Int>

    val lastCrash: MutableStateFlow<String>
    val lastCrashShown: MutableStateFlow<Boolean>
}
