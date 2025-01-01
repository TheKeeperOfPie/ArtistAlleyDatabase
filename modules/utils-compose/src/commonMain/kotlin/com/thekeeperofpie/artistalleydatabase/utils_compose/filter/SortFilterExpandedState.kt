package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf

@Stable
class SortFilterExpandedState {
    val expandedState = mutableStateMapOf<String, Boolean>()
}
