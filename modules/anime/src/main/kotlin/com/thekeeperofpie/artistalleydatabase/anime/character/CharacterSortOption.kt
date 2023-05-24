package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.annotation.StringRes
import com.anilist.type.CharacterSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController

enum class CharacterSortOption(
    @StringRes override val textRes: Int
) : AnimeMediaFilterController.Data.SortOption {

    // Omissions: SEARCH_MATCH is used as a default
    ID(R.string.anime_character_sort_id),
    ROLE(R.string.anime_character_sort_role),
    FAVORITES(R.string.anime_character_sort_favorites),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> if (ascending) CharacterSort.ID else CharacterSort.ID_DESC
        ROLE -> if (ascending) CharacterSort.ROLE else CharacterSort.ROLE_DESC
        FAVORITES -> if (ascending) CharacterSort.FAVOURITES else CharacterSort.FAVOURITES_DESC
    }
}
