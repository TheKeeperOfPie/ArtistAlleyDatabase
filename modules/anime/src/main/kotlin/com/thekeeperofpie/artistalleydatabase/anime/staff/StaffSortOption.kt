package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import com.anilist.type.StaffSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class StaffSortOption(
    @StringRes override val textRes: Int,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(R.string.anime_staff_sort_search_match, supportsAscending = false),
    ID(R.string.anime_staff_sort_id),
    ROLE(R.string.anime_staff_sort_role),
    LANGUAGE(R.string.anime_staff_sort_language),
    FAVORITES(R.string.anime_staff_sort_favorites),
    RELEVANCE(R.string.anime_staff_sort_relevance)

    ;

    fun toApiValueForSearch(ascending: Boolean) = when (this) {
        SEARCH_MATCH -> listOf(
            StaffSort.SEARCH_MATCH,
            StaffSort.FAVOURITES_DESC,
            StaffSort.ID_DESC,
        )
        ID -> listOf(if (ascending) StaffSort.ID else StaffSort.ID_DESC)
        ROLE -> listOf(
            if (ascending) StaffSort.ROLE else StaffSort.ROLE_DESC,
            StaffSort.SEARCH_MATCH,
        )
        LANGUAGE -> listOf(
            if (ascending) StaffSort.LANGUAGE else StaffSort.LANGUAGE_DESC,
            StaffSort.SEARCH_MATCH,
        )
        FAVORITES -> listOf(
            if (ascending) StaffSort.FAVOURITES else StaffSort.FAVOURITES_DESC
        )
        RELEVANCE -> listOf(StaffSort.RELEVANCE, StaffSort.ROLE_DESC)
    }
}
