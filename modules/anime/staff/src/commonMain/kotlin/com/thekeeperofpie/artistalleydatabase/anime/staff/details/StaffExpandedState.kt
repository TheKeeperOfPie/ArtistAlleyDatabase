package com.thekeeperofpie.artistalleydatabase.anime.staff.details

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class StaffExpandedState(
    description: Boolean = false,
) {
    var description by mutableStateOf(description)
}
