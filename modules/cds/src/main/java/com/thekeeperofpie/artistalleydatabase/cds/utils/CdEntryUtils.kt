package com.thekeeperofpie.artistalleydatabase.cds.utils

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry

object CdEntryUtils {

    fun getImageFile(context: Context, id: String) = context.filesDir
        .resolve("cd_entry_images/${id}")

    fun buildPlaceholderText(entry: CdEntry) = entry.run {
        "$catalogId ${titles.joinToString()}"
    }
}