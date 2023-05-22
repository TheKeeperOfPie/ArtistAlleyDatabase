package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class StaffTab(@StringRes val textRes: Int) {
    OVERVIEW(R.string.anime_staff_overview_label),
    MEDIA(R.string.anime_staff_media_label),
    STAFF(R.string.anime_staff_staff_label),
}
