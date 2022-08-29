package com.thekeeperofpie.artistalleydatabase.cds

import android.content.Context
import kotlinx.serialization.json.Json

object CdEntryUtils {

    fun getImageFile(context: Context, id: String) = context.filesDir
        .resolve("cd_entry_images/${id}")

    fun buildPlaceholderText(json: Json, entry: CdEntry) = entry.run {
        entry.titles.joinToString()
    }
}