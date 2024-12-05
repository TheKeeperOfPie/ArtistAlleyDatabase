package com.thekeeperofpie.artistalleydatabase.anime.characters

import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_sort_favorites
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_sort_id
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_sort_relevance
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_sort_role
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_sort_search_match
import com.anilist.data.type.CharacterSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class CharacterSortOption(
    override val textRes: StringResource,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(Res.string.anime_character_sort_search_match, supportsAscending = false),
    ID(Res.string.anime_character_sort_id),
    ROLE(Res.string.anime_character_sort_role),
    FAVORITES(Res.string.anime_character_sort_favorites),
    RELEVANCE(Res.string.anime_character_sort_relevance, supportsAscending = false),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> listOf(if (ascending) CharacterSort.ID else CharacterSort.ID_DESC)
        ROLE -> listOf(
            // Role sort is reversed from the API definition because it's more intuitive
            if (ascending) CharacterSort.ROLE_DESC else CharacterSort.ROLE,
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
        RELEVANCE -> listOf(CharacterSort.RELEVANCE, CharacterSort.ROLE)
    }

    fun toApiValueForSearch(ascending: Boolean) = when (this) {
        ID -> listOf(if (ascending) CharacterSort.ID else CharacterSort.ID_DESC)
        ROLE -> listOf(
            // Role sort is reversed from the API definition because it's more intuitive
            if (ascending) CharacterSort.ROLE_DESC else CharacterSort.ROLE,
            CharacterSort.SEARCH_MATCH,
        )
        SEARCH_MATCH -> listOf(
            CharacterSort.SEARCH_MATCH,
            CharacterSort.FAVOURITES_DESC,
            CharacterSort.ID_DESC,
        )
        FAVORITES -> listOf(
            if (ascending) CharacterSort.FAVOURITES else CharacterSort.FAVOURITES_DESC,
        )
        RELEVANCE -> listOf(CharacterSort.RELEVANCE, CharacterSort.ROLE)
    }
}
