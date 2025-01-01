package com.thekeeperofpie.artistalleydatabase.anime.users.data.filter

data class UsersSortFilterParams(
    val sort: UserSortOption,
    val sortAscending: Boolean,
    val isModerator: Boolean?,
)
