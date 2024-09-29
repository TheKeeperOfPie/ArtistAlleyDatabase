package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class EntrySearchOption(val textRes: Int, enabled: Boolean = false) {
    var enabled by mutableStateOf(enabled)
}
