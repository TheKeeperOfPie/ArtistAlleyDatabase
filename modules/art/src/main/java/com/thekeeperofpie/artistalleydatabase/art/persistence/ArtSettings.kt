package com.thekeeperofpie.artistalleydatabase.art.persistence

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry

interface ArtSettings {

    fun saveArtEntryTemplate(entry: ArtEntry)

    fun loadArtEntryTemplate(): ArtEntry?

    fun saveCropDocumentUri(uri: Uri)

    fun loadCropDocumentUri(): Uri?
}