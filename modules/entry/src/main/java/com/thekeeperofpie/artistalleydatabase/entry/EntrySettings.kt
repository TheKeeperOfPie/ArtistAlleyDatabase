package com.thekeeperofpie.artistalleydatabase.entry

import kotlinx.coroutines.flow.MutableStateFlow

interface EntrySettings {

    val cropImageUri: MutableStateFlow<String?>
}
