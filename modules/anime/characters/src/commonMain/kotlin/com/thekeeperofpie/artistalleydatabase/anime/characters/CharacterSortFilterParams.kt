package com.thekeeperofpie.artistalleydatabase.anime.characters

import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterSortOption

data class CharacterSortFilterParams(
    val sort: CharacterSortOption,
    val sortAscending: Boolean,
    val isBirthday: Boolean,
)
