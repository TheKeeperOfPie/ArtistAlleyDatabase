package com.thekeeperofpie.artistalleydatabase.anime

import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import kotlinx.coroutines.flow.MutableStateFlow

interface AnimeSettings {

    val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
    val showAdult: MutableStateFlow<Boolean>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val showIgnored: MutableStateFlow<Boolean>
    val showLessImportantTags: MutableStateFlow<Boolean>
    val showSpoilerTags: MutableStateFlow<Boolean>

    // TODO: Better database to store ignored IDs
    val ignoredAniListMediaIds: MutableStateFlow<Set<Int>>

    val animeNewsNetworkRegion: MutableStateFlow<AnimeNewsNetworkRegion>

    val animeNewsNetworkCategoriesIncluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val animeNewsNetworkCategoriesExcluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val crunchyrollNewsCategoriesIncluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
    val crunchyrollNewsCategoriesExcluded: MutableStateFlow<List<CrunchyrollNewsCategory>>

    val preferredMediaType: MutableStateFlow<MediaType>
}
