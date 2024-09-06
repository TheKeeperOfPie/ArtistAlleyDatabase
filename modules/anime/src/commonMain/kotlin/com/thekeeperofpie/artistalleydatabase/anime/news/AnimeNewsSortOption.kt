package com.thekeeperofpie.artistalleydatabase.anime.news

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_news_sort_datetime
import artistalleydatabase.modules.anime.generated.resources.anime_news_sort_source
import artistalleydatabase.modules.anime.generated.resources.anime_news_sort_title
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class AnimeNewsSortOption(override val textRes: StringResource) : SortOption {
    DATETIME(Res.string.anime_news_sort_datetime),
    TITLE(Res.string.anime_news_sort_title),
    SOURCE(Res.string.anime_news_sort_source),
}
