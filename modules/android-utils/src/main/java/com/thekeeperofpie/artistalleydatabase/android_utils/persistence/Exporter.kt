package com.thekeeperofpie.artistalleydatabase.android_utils.persistence

import com.squareup.moshi.JsonWriter
import kotlinx.serialization.json.JsonElement
import java.io.InputStream

interface Exporter {

    val zipEntryName: String

    suspend fun entriesSize(): Int

    suspend fun writeEntries(
        jsonWriter: JsonWriter,
        jsonElementConverter: (JsonElement) -> Any?,
        writeEntry: suspend (String, () -> InputStream) -> Unit,
        updateProgress: suspend (progress: Int, progressMax: Int) -> Unit,
    ): Boolean
}
