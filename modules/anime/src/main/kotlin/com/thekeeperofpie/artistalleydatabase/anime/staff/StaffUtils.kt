package com.thekeeperofpie.artistalleydatabase.anime.staff

object StaffUtils {
    fun subtitleName(userPreferred: String?, native: String?, full: String?) =
        if (native != userPreferred) {
            native
        } else if (full != userPreferred) {
            full
        } else {
            null
        }
}
