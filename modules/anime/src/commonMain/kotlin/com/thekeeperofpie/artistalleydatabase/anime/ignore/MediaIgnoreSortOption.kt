package com.thekeeperofpie.artistalleydatabase.anime.ignore

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_sort_id
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class MediaIgnoreSortOption(override val textRes: StringResource) : SortOption {
    ID(Res.string.anime_media_sort_id),
}
