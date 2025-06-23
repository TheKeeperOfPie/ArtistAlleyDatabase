package com.thekeeperofpie.artistalleydatabase.alley.series

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_sort_name
import artistalleydatabase.modules.alley.generated.resources.alley_series_sort_popularity
import artistalleydatabase.modules.alley.generated.resources.alley_series_sort_random
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class SeriesSearchSortOption(override val textRes: StringResource) : SortOption {
    RANDOM(Res.string.alley_series_sort_random),
    NAME(Res.string.alley_series_sort_name),
    POPULARITY(Res.string.alley_series_sort_popularity),
}
