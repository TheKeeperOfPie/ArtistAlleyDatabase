package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.annotation.StringRes
import com.anilist.type.StudioSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class StudioSortOption(
    @StringRes override val textRes: Int,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(R.string.anime_studio_sort_search_match, supportsAscending = false),
    ID(R.string.anime_studio_sort_id),
    NAME(R.string.anime_studio_sort_name),
    FAVORITES(R.string.anime_studio_sort_favorites),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        SEARCH_MATCH -> listOf(
            StudioSort.SEARCH_MATCH,
            StudioSort.FAVOURITES_DESC,
            StudioSort.ID_DESC,
        )
        ID -> listOf(if (ascending) StudioSort.ID else StudioSort.ID_DESC)
        NAME -> listOf(if (ascending) StudioSort.NAME else StudioSort.NAME_DESC)
        FAVORITES -> listOf(if (ascending) StudioSort.FAVOURITES else StudioSort.FAVOURITES_DESC)
    }
}
