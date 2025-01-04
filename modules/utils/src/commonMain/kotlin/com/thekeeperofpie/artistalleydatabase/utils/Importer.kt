package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.io.Source

interface Importer {

    val zipEntryName: String

    suspend fun readEntries(source: Source, dryRun: Boolean, replaceAll: Boolean): Int

    suspend fun readInnerFile(
        source: Source,
        fileName: String,
        dryRun: Boolean
    )
}
