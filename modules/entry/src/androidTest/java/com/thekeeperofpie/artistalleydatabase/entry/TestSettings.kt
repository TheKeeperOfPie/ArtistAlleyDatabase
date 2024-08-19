package com.thekeeperofpie.artistalleydatabase.entry

import kotlinx.coroutines.flow.MutableStateFlow

class TestSettings(cropUri: String? = null) : EntrySettings {
    override var cropImageUri = MutableStateFlow(cropUri)
}
