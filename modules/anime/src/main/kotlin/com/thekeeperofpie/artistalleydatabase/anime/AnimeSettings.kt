package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
    val showAdult: MutableStateFlow<Boolean>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val showIgnored: MutableStateFlow<Boolean>

    // TODO: Better database to store ignored IDs
    val ignoredAniListMediaIds: MutableStateFlow<Set<Int>>
}
