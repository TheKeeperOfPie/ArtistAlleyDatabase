package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.io.Sink
import kotlinx.io.Source

interface Exporter {

    val zipEntryName: String

    suspend fun entriesSize(): Int

    suspend fun writeEntries(
        sink: Sink,
        writeEntry: suspend (String, () -> Source) -> Unit,
        updateProgress: suspend (progress: Int, progressMax: Int) -> Unit,
    ): Boolean
}
