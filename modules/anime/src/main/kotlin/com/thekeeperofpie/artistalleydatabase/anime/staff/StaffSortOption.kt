package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import com.anilist.type.StaffSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController

enum class StaffSortOption(
    @StringRes override val textRes: Int
) : AnimeMediaFilterController.Data.SortOption {

    // Omissions: SEARCH_MATCH is used as a default, RELEVANCE not useful for search
    ID(R.string.anime_staff_sort_id),
    ROLE(R.string.anime_staff_sort_role),
    LANGUAGE(R.string.anime_staff_sort_language),
    FAVORITES(R.string.anime_staff_sort_favorites),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> if (ascending) StaffSort.ID else StaffSort.ID_DESC
        ROLE -> if (ascending) StaffSort.ROLE else StaffSort.ROLE_DESC
        LANGUAGE -> if (ascending) StaffSort.LANGUAGE else StaffSort.LANGUAGE_DESC
        FAVORITES -> if (ascending) StaffSort.FAVOURITES else StaffSort.FAVOURITES_DESC
    }
}
