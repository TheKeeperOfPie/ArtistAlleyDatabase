package com.thekeeperofpie.artistalleydatabase.art.persistence

import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntryExporter
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.encodeToSink
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalSerializationApi::class)
@SingletonScope
@Inject
class ArtExporter(
    appFileSystem: AppFileSystem,
    private val artEntryDao: ArtEntryDao,
    private val dataConverter: DataConverter,
    private val json: Json,
) : EntryExporter(appFileSystem) {

    override val zipEntryName = "art_entries"

    override suspend fun entriesSize() = artEntryDao.getEntriesSize()

    override suspend fun writeEntries(
        sink: Sink,
        writeEntry: suspend (String, () -> Source) -> Unit,
        updateProgress: suspend (progress: Int, progressMax: Int) -> Unit,
    ): Boolean {
        sink.writeString("""{ "$zipEntryName": [""")
        var stopped = false
        var entriesSize = 0
        var writtenFirst = false
        artEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
            if (!currentCoroutineContext().isActive) {
                stopped = true
                return@iterateEntries
            }
            if (writtenFirst) {
                sink.writeString(",\n")
            } else {
                writtenFirst = true
            }

            writeImages(entry, writeEntry)

            updateProgress(index, entriesSize)

            json.encodeToSink(entry, sink)
        }
        if (stopped) {
            return false
        }
        sink.writeString("""]}""")

        return true
    }

    private suspend fun writeImages(
        entry: ArtEntry,
        writeEntry: suspend (String, () -> Source) -> Unit,
    ) {
        val series = dataConverter.seriesEntries(entry.series(json))
            .map { it.text }
            .sorted()

        val characters = dataConverter.characterEntries(entry.characters(json))
            .map { it.text }
            .sorted()

        val tags = entry.tags

        writeImages(entryId = entry.entryId, writeEntry = writeEntry, series, characters, tags)
    }
}
