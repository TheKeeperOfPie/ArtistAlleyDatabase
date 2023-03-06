package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri

class TestSettings(cropUri: Uri? = null) : EntrySettings {
    override var cropDocumentUri = cropUri
}