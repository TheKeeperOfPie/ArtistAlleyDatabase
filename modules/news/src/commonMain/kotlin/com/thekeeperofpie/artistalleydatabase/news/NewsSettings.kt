package com.thekeeperofpie.artistalleydatabase.news

import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.news.cr.CrunchyrollNewsCategory
import kotlinx.coroutines.flow.MutableStateFlow

interface NewsSettings {

    val animeNewsNetworkRegion: MutableStateFlow<AnimeNewsNetworkRegion>

    val animeNewsNetworkCategoriesIncluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val animeNewsNetworkCategoriesExcluded: MutableStateFlow<List<AnimeNewsNetworkCategory>>
    val crunchyrollNewsCategoriesIncluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
    val crunchyrollNewsCategoriesExcluded: MutableStateFlow<List<CrunchyrollNewsCategory>>
}
