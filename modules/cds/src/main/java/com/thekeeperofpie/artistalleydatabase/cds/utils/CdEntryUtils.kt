package com.thekeeperofpie.artistalleydatabase.cds.utils

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.form.EntryUtils

object CdEntryUtils {

    const val TYPE_ID = "cd_entry_images"

    fun getImageFile(context: Context, entryId: String) =
        EntryUtils.getImageFile(context, TYPE_ID, entryId)

    fun buildPlaceholderText(entry: CdEntry) = entry.run {
        "$catalogId ${titles.joinToString()}"
    }
}