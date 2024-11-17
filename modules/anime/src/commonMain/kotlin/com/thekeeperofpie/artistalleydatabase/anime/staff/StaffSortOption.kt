package com.thekeeperofpie.artistalleydatabase.anime.staff

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_favorites
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_id
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_language
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_relevance
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_role
import artistalleydatabase.modules.anime.generated.resources.anime_staff_sort_search_match
import com.anilist.data.type.StaffSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class StaffSortOption(
    override val textRes: StringResource,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(Res.string.anime_staff_sort_search_match, supportsAscending = false),
    ID(Res.string.anime_staff_sort_id),
    ROLE(Res.string.anime_staff_sort_role),
    LANGUAGE(Res.string.anime_staff_sort_language),
    FAVORITES(Res.string.anime_staff_sort_favorites),
    RELEVANCE(Res.string.anime_staff_sort_relevance)

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
