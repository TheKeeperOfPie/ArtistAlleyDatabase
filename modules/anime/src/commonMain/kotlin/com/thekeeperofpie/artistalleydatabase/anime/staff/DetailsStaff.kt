package com.thekeeperofpie.artistalleydatabase.anime.staff

import com.anilist.fragment.StaffNameLanguageFragment
import com.anilist.fragment.StaffNavigationData

data class DetailsStaff(
    val id: String,
    val name: StaffNameLanguageFragment?,
    val image: String?,
    val role: String?,
    val staff: StaffNavigationData,
) {
    val idWithRole = "$id-$role"
}
