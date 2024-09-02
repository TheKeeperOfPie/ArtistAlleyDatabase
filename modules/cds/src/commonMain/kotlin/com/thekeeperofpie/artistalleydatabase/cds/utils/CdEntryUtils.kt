package com.thekeeperofpie.artistalleydatabase.cds.utils

import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry

object CdEntryUtils {

    const val SCOPED_ID_TYPE = "cd_entry"

    fun buildPlaceholderText(entry: CdEntry) = entry.run {
        "$catalogId ${titles.joinToString()}"
    }
}