package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val showAdult: MutableStateFlow<Boolean>
}