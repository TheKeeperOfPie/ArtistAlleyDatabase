package com.thekeeperofpie.artistalleydatabase.anime.ignore.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IgnoreSettings {

    val mediaIgnoreEnabled: MutableStateFlow<Boolean>
    val mediaIgnoreHide: MutableStateFlow<Boolean>

    // TODO: Invert boolean and remove this
    val showIgnored: StateFlow<Boolean>
}
