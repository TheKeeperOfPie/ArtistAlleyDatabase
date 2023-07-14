package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.annotation.StringRes
import com.anilist.type.CharacterSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class CharacterSortOption(
    @StringRes override val textRes: Int,
    val supportsAscending: Boolean = true,
) : SortOption {

    ID(R.string.anime_character_sort_id),
    ROLE(R.string.anime_character_sort_role),
    SEARCH_MATCH(R.string.anime_character_sort_search_match, supportsAscending = false),
    FAVORITES(R.string.anime_character_sort_favorites),
    RELEVANCE(R.string.anime_character_sort_relevance, supportsAscending = false),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> listOf(if (ascending) CharacterSort.ID else CharacterSort.ID_DESC)
        ROLE -> listOf(
            if (ascending) CharacterSort.ROLE else CharacterSort.ROLE_DESC,
            CharacterSort.RELEVANCE,
        )
        SEARCH_MATCH -> listOf(
            CharacterSort.SEARCH_MATCH,
            CharacterSort.RELEVANCE,
            CharacterSort.ROLE_DESC,
        )
        FAVORITES -> listOf(
            if (ascending) CharacterSort.FAVOURITES else CharacterSort.FAVOURITES_DESC,
            CharacterSort.RELEVANCE,
            CharacterSort.ROLE_DESC,
        )
        RELEVANCE -> listOf(CharacterSort.RELEVANCE, CharacterSort.ROLE_DESC)
    }
}
