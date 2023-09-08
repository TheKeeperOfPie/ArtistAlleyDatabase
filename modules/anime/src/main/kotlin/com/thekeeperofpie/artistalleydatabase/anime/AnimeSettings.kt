package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
    val showAdult: MutableStateFlow<Boolean>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val showLessImportantTags: MutableStateFlow<Boolean>
    val showSpoilerTags: MutableStateFlow<Boolean>

    val animeNewsNetworkRegion: MutableStateFlow<AnimeNewsNetworkRegion>

    val animeNewsNetworkCategoriesIncluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val animeNewsNetworkCategoriesExcluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val crunchyrollNewsCategoriesIncluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
    val crunchyrollNewsCategoriesExcluded: MutableStateFlow<List<CrunchyrollNewsCategory>>

    val preferredMediaType: MutableStateFlow<MediaType>
    val mediaViewOption: MutableStateFlow<MediaViewOption>
    val rootNavDestination: MutableStateFlow<AnimeRootNavDestination>

    val mediaHistoryEnabled: MutableStateFlow<Boolean>
    val mediaHistoryMaxEntries: MutableStateFlow<Int>

    val mediaIgnoreEnabled: MutableStateFlow<Boolean>
    val mediaIgnoreHide: MutableStateFlow<Boolean>

    // Invert boolean and remove this
    val showIgnored: Flow<Boolean>

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
