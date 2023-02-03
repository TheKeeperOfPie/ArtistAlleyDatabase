package com.thekeeperofpie.artistalleydatabase.art.persistence

import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry

interface ArtSettings {

    fun saveArtEntryTemplate(entry: ArtEntry)

    fun loadArtEntryTemplate(): ArtEntry?
}