package com.thekeeperofpie.artistalleydatabase.art

import android.content.Context

class ArtEntryModel(
    context: Context,
    val value: ArtEntry,
) {
    val localImageFile = context.filesDir.resolve("entry_images/${value.id}")
}