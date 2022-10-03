package com.thekeeperofpie.artistalleydatabase.cds.utils

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import kotlinx.serialization.json.Json

object CdEntryUtils {

    fun getImageFile(context: Context, id: String) = context.filesDir
        .resolve("cd_entry_images/${id}")

    fun buildPlaceholderText(json: Json, entry: CdEntry) = entry.run {
        titles.joinToString()
    }
}