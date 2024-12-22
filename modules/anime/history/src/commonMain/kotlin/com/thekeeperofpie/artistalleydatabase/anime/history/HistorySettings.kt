package com.thekeeperofpie.artistalleydatabase.anime.history

import kotlinx.coroutines.flow.MutableStateFlow

interface HistorySettings {
    val mediaHistoryEnabled: MutableStateFlow<Boolean>
    val mediaHistoryMaxEntries: MutableStateFlow<Int>
}
