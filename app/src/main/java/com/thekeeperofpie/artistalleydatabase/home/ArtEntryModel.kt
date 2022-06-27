package com.thekeeperofpie.artistalleydatabase.home

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry

class ArtEntryModel(
    context: Context,
    val value: ArtEntry,
) {
    val localImageFile = context.filesDir.resolve("entry_images/${value.id}")
}