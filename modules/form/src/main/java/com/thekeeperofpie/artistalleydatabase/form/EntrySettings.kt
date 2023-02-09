package com.thekeeperofpie.artistalleydatabase.form

import android.net.Uri

interface EntrySettings {

    fun saveCropDocumentUri(uri: Uri?)

    fun loadCropDocumentUri(): Uri?
}