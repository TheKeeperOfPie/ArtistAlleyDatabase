package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class AnimeNewsSortOption(@StringRes override val textRes: Int) : SortOption {
    DATETIME(R.string.anime_news_sort_datetime),
    TITLE(R.string.anime_news_sort_title),
    SOURCE(R.string.anime_news_sort_source),
}
