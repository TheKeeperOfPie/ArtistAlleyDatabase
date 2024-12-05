package com.thekeeperofpie.artistalleydatabase.anime.staff.data

import com.anilist.data.fragment.StaffNameLanguageFragment
import com.anilist.data.fragment.StaffNavigationData

data class StaffDetails(
    val id: String,
    val name: StaffNameLanguageFragment?,
    val image: String?,
    val role: String?,
    val staff: StaffNavigationData,
) {
    val idWithRole = "$id-$role"
}
