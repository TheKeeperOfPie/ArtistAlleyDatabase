package com.thekeeperofpie.artistalleydatabase.cds.persistence

import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryImporter
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.decodeSequenceIgnoreEndOfFile
import kotlinx.io.Source
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.indexOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class CdImporter(
    appFileSystem: AppFileSystem,
    private val cdEntryDao: CdEntryDao,
    private val json: Json,
) : EntryImporter(appFileSystem) {

    override val zipEntryName = "cd_entries"

    override val scopedIdType = CdEntryUtils.SCOPED_ID_TYPE

    override suspend fun readEntries(
        source: Source,
        dryRun: Boolean,
        replaceAll: Boolean
    ): Int {
        var count = 0
        cdEntryDao.insertEntriesDeferred(dryRun, replaceAll) { insertEntries ->
            count = if (dryRun) {
                readCdEntriesJson(source) {}
            } else {
                readCdEntriesJson(source, insertEntries)
            }
        }
        return count
    }

    /**
     * @return number of valid entries found
     */
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun readCdEntriesJson(
        source: Source,
        insertEntries: suspend (Array<CdEntry>) -> Unit,
    ): Int {
        var count = 0
        val arrayStartIndex = source.indexOf("[".encodeToByteString())
        source.skip(arrayStartIndex)
        json.decodeSequenceIgnoreEndOfFile<CdEntry>(source)
            .chunked(10)
            .map {
                it.map {
                    var entry = it

                    if (entry.performersSearchable.isEmpty()) {
                        entry = entry.copy(performersSearchable = entry.performers)
                    }

                    if (entry.composersSearchable.isEmpty()) {
                        entry = entry.copy(composersSearchable = entry.composers)
                    }

                    if (entry.seriesSearchable.isEmpty()) {
                        entry = entry.copy(seriesSearchable = entry.seriesSerialized)
                    }

                    if (entry.charactersSearchable.isEmpty()) {
                        entry = entry.copy(charactersSearchable = entry.charactersSerialized)
                    }

                    entry
                }
            }
            .forEach {
                count += it.size
                insertEntries(it.toTypedArray())
            }

        return count
    }
}
