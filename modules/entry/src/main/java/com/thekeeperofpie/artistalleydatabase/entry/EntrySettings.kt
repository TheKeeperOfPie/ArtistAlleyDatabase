package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri

interface EntrySettings {

    fun saveCropDocumentUri(uri: Uri?)

    fun loadCropDocumentUri(): Uri?
}