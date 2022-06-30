package com.thekeeperofpie.artistalleydatabase.search

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SearchOption(@StringRes val textRes: Int, enabled: Boolean = true) {
    var enabled by mutableStateOf(enabled)
}