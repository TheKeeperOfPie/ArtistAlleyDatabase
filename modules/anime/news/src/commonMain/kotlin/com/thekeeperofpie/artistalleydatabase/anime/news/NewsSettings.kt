package com.thekeeperofpie.artistalleydatabase.anime.news

import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsCategory
import kotlinx.coroutines.flow.MutableStateFlow

interface NewsSettings {

    val animeNewsNetworkRegion: MutableStateFlow<AnimeNewsNetworkRegion>

    val animeNewsNetworkCategoriesIncluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val animeNewsNetworkCategoriesExcluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val crunchyrollNewsCategoriesIncluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
    val crunchyrollNewsCategoriesExcluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
}
