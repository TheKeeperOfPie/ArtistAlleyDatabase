package com.thekeeperofpie.artistalleydatabase.art.export

import android.content.Context
import androidx.work.CoroutineWorker
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.io.InputStream

class ArtExporter(
    private val appContext: Context,
    private val artEntryDao: ArtEntryDao,
    private val artEntryDataConverter: ArtEntryDataConverter,
    private val appJson: AppJson,
) : Exporter {

    override val zipEntryName = "art_entries"

    override suspend fun writeEntries(
        worker: CoroutineWorker,
        jsonWriter: JsonWriter,
        jsonElementConverter: (JsonElement) -> Any?,
        writeEntry: suspend (String, InputStream) -> Unit,
        updateProgress: suspend (progress: Int, max: Int) -> Unit,
    ): Boolean {
        jsonWriter.beginObject()
            .name("art_entries")
            .beginArray()
        var stopped = false
        var entriesSize = 0
        artEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
            if (worker.isStopped) {
                stopped = true
                return@iterateEntries
            }

            val imageFile = ArtEntryUtils.getImageFile(appContext, entry.id)
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
        entry: ArtEntry,
        imageFile: File,
        writeEntry: suspend (String, InputStream) -> Unit,
    ) {
        val folderParts = entry.series
            .asSequence()
            .map(artEntryDataConverter::databaseToSeriesEntry)
            .map { it.text }
            .sorted()
            .map {
                if (it.length > 120) {
                    it.substring(0, 120)
                } else it
            }
            .toMutableList()

        val characters = entry.characters
            .map(artEntryDataConverter::databaseToCharacterEntry)
            .map { it.text }

        folderParts += when {
            characters.isEmpty() -> {
                when {
                    entry.tags.isEmpty() -> "Unknown"
                    else -> entry.tags.joinToString("-")
                }
            }
            else -> if (characters.size == 1) {
                when {
                    entry.tags.isEmpty() -> characters.first()
                    else -> {
                        folderParts += characters.first()
                        entry.tags.joinToString("-")
                    }
                }
            } else {
                val charactersCombined = characters.joinToString("-")
                if (entry.tags.isNotEmpty()) {
                    charactersCombined + entry.tags.joinToString(
                        prefix = "-",
                        separator = "-"
                    )
                } else charactersCombined
            }
        }.take(120)

        folderParts.fold(mutableListOf<String>()) { list, next ->
            if ((list.sumOf { it.length } + list.count()) < 850) {
                list.apply { add(next.take(120)) }
            } else list
        }

        val finalName = folderParts.joinToString(
            separator = File.separator,
            postfix = File.separator
        ) + "${entry.id}.jpg"

        writeEntry(finalName, imageFile.inputStream())
    }
}