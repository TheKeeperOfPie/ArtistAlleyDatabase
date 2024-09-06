package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import org.jetbrains.compose.resources.StringResource

interface SortOption {
    val textRes: StringResource

    val supportsAscending: Boolean
        get() = true
}
