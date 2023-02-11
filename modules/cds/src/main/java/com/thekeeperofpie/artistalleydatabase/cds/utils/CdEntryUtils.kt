package com.thekeeperofpie.artistalleydatabase.cds.utils

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils

object CdEntryUtils {

    const val SCOPED_ID_TYPE = "cd_entry"

    fun getImageFile(context: Context, entryId: EntryId) = EntryUtils.getImageFile(context, entryId)

    fun buildPlaceholderText(entry: CdEntry) = entry.run {
        "$catalogId ${titles.joinToString()}"
    }
}