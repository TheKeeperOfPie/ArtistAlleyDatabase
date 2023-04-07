package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

class TestSettings(cropUri: Uri? = null) : EntrySettings {
    override var cropDocumentUri = MutableStateFlow(cropUri)
}