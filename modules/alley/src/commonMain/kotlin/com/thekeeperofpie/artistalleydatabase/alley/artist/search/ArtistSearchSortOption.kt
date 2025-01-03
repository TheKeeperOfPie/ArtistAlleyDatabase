package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_sort_artist
import artistalleydatabase.modules.alley.generated.resources.alley_sort_booth
import artistalleydatabase.modules.alley.generated.resources.alley_sort_random
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class ArtistSearchSortOption(override val textRes: StringResource) : SortOption {
    BOOTH(Res.string.alley_sort_booth),
    ARTIST(Res.string.alley_sort_artist),
    RANDOM(Res.string.alley_sort_random),
}
