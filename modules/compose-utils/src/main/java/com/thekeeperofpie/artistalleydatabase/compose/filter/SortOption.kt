package com.thekeeperofpie.artistalleydatabase.compose.filter

import androidx.annotation.StringRes

interface SortOption {
    @get:StringRes
    val textRes: Int

    val supportsAscending: Boolean
        get() = true
}
