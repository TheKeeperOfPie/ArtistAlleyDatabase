package com.thekeeperofpie.artistalleydatabase.cds.persistence

import android.content.Context
import androidx.work.CoroutineWorker
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.ExportUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.Exporter
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.io.InputStream

class CdExporter(
    private val appContext: Context,
    private val cdEntryDao: CdEntryDao,
    private val dataConverter: DataConverter,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val appJson: AppJson,
) : Exporter {

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

            val imageFile = CdEntryUtils.getImageFile(appContext, entry.id)
            if (imageFile.exists()) {
                writeImage(entry, imageFile, writeEntry)
            }

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

    private suspend fun writeImage(
        entry: CdEntry,
        imageFile: File,
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

        val entryFileName = ExportUtils.buildEntryFilePath(
            entry.id,
            series,
            characters,
            artists,
            catalogId,
            tags
        )
        writeEntry(entryFileName, imageFile.inputStream())
    }
}