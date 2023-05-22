package com.thekeeperofpie.artistalleydatabase.anime.staff

import com.anilist.fragment.StaffNavigationData

data class DetailsStaff(
    val id: String,
    val name: String?,
    val image: String?,
    val role: String?,
    val staff: StaffNavigationData,
)
