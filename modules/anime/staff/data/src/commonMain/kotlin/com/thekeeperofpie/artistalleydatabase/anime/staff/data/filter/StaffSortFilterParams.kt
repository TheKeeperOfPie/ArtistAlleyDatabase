package com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter

data class StaffSortFilterParams(
    val sort: StaffSortOption,
    val sortAscending: Boolean,
    val isBirthday: Boolean,
)
