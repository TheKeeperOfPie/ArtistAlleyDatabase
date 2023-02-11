package com.thekeeperofpie.artistalleydatabase.art.persistence

import android.content.Context
import androidx.work.CoroutineWorker
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntryExporter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.InputStream

class ArtExporter(
    appContext: Context,
    private val artEntryDao: ArtEntryDao,
    private val dataConverter: DataConverter,
    private val appJson: AppJson,
) : EntryExporter(appContext) {

    override val zipEntryName = "art_entries"

    override suspend fun entriesSize() = artEntryDao.getEntriesSize()

    override suspend fun writeEntries(
        worker: CoroutineWorker,
        jsonWriter: JsonWriter,
        jsonElementConverter: (JsonElement) -> Any?,
        writeEntry: suspend (String, InputStream) -> Unit,
        updateProgress: suspend (progress: Int, max: Int) -> Unit,
    ): Boolean {
        jsonWriter.beginObject()
            .name(zipEntryName)
            .beginArray()
        var stopped = false
        var entriesSize = 0
        artEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
            if (worker.isStopped) {
                stopped = true
                return@iterateEntries
            }

            writeImages(entry, writeEntry)

            updateProgress(index, entriesSize)

            val jsonValue = jsonElementConverter(appJson.json.encodeToJsonElement(entry))
            jsonWriter.jsonValue(jsonValue)
        }
        if (stopped) {
            return false
        }
        jsonWriter.endArray()
            .endObject()

        return true
    }

    private suspend fun writeImages(
        entry: ArtEntry,
        writeEntry: suspend (String, InputStream) -> Unit,
    ) {
        val series = dataConverter.seriesEntries(entry.series(appJson))
            .map { it.text }
            .sorted()

        val characters = dataConverter.characterEntries(entry.characters(appJson))
            .map { it.text }
            .sorted()

        val tags = entry.tags

        writeImages(entryId = entry.entryId, writeEntry = writeEntry, series, characters, tags)
    }
}