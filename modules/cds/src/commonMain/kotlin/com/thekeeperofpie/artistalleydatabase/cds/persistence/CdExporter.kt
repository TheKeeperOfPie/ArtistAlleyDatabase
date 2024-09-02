package com.thekeeperofpie.artistalleydatabase.cds.persistence

import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntryExporter
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
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
class CdExporter(
    appFileSystem: AppFileSystem,
    private val cdEntryDao: CdEntryDao,
    private val dataConverter: DataConverter,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val json: Json,
) : EntryExporter(appFileSystem) {

    override val zipEntryName = "cd_entries"

    override suspend fun entriesSize() = cdEntryDao.getEntriesSize()

    override suspend fun writeEntries(
        sink: Sink,
        writeEntry: suspend (String, () -> Source) -> Unit,
        updateProgress: suspend (progress: Int, progressMax: Int) -> Unit,
    ): Boolean {
        sink.writeString("""{ "$zipEntryName": [""")
        var stopped = false
        var entriesSize = 0
        var writtenFirst = false
        cdEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
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
        entry: CdEntry,
        writeEntry: suspend (String, () -> Source) -> Unit,
    ) {
        val series = dataConverter.seriesEntries(entry.series(json))
            .map { it.text }
            .sorted()

        val characters = dataConverter.characterEntries(entry.characters(json))
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
