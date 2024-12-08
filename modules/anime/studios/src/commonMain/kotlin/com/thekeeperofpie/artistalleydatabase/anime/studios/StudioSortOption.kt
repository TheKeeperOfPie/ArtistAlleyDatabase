package com.thekeeperofpie.artistalleydatabase.anime.studios

import artistalleydatabase.modules.anime.studios.generated.resources.Res
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_sort_favorites
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_sort_id
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_sort_name
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_sort_search_match
import com.anilist.data.type.StudioSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class StudioSortOption(
    override val textRes: StringResource,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(Res.string.anime_studio_sort_search_match, supportsAscending = false),
    ID(Res.string.anime_studio_sort_id),
    NAME(Res.string.anime_studio_sort_name),
    FAVORITES(Res.string.anime_studio_sort_favorites),
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
