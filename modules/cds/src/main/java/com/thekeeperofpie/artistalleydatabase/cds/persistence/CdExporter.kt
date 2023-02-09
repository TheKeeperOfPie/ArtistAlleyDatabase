package com.thekeeperofpie.artistalleydatabase.cds.persistence

import android.content.Context
import androidx.work.CoroutineWorker
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.form.EntryExporter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.InputStream

class CdExporter(
    appContext: Context,
    private val cdEntryDao: CdEntryDao,
    private val dataConverter: DataConverter,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val appJson: AppJson,
) : EntryExporter(appContext) {

    override val zipEntryName = "cd_entries"

    override suspend fun entriesSize() = cdEntryDao.getEntriesSize()

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
        cdEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
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
        entry: CdEntry,
        writeEntry: suspend (String, InputStream) -> Unit,
    ) {
        val series = dataConverter.seriesEntries(entry.series(appJson))
            .map { it.text }
            .sorted()

        val characters = dataConverter.characterEntries(entry.characters(appJson))
            .map { it.text }
            .sorted()

        val performers = entry.performers.map(vgmdbDataConverter::databaseToArtistEntry)
        val composers = entry.composers.map(vgmdbDataConverter::databaseToArtistEntry)

        val artists = (performers + composers).map { it.text }
        val catalogId = listOfNotNull(entry.catalogId)
            .map(vgmdbDataConverter::databaseToCatalogIdEntry)
            .map { it.text }

        val tags = entry.tags

        writeImages(
            entryId = entry.entryId,
            writeEntry = writeEntry,
            series,
            characters,
            artists,
            catalogId,
            tags
        )
    }
}
