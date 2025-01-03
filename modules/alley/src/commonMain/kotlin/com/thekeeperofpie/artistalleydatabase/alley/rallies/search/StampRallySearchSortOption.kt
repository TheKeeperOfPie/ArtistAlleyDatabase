package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_sort_booth
import artistalleydatabase.modules.alley.generated.resources.alley_sort_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_sort_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_sort_random
import artistalleydatabase.modules.alley.generated.resources.alley_sort_total_cost
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class StampRallySearchSortOption(override val textRes: StringResource) : SortOption {
    MAIN_TABLE(Res.string.alley_sort_booth),
    FANDOM(Res.string.alley_sort_fandom),
    RANDOM(Res.string.alley_sort_random),
    PRIZE_LIMIT(Res.string.alley_sort_prize_limit),
    TOTAL_COST(Res.string.alley_sort_total_cost),
}
