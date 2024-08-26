package com.thekeeperofpie.artistalleydatabase.art.persistence

import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryImporter
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import kotlinx.io.Source
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.indexOf
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class ArtImporter(
    appFileSystem: AppFileSystem,
    private val artEntryDao: ArtEntryDao,
    private val appJson: AppJson,
) : EntryImporter(appFileSystem) {

    override val zipEntryName = "art_entries"

    override val scopedIdType = ArtEntryUtils.SCOPED_ID_TYPE

    override suspend fun readEntries(source: Source, dryRun: Boolean, replaceAll: Boolean): Int {
        var count = 0
        artEntryDao.insertEntriesDeferred(dryRun, replaceAll) { insertEntry ->
            count = if (dryRun) {
                readArtEntriesJson(source) {}
            } else {
                readArtEntriesJson(source, insertEntry)
            }
        }
        return count
    }

    /**
     * @return number of valid entries found
     */
    private suspend fun readArtEntriesJson(
        source: Source,
        insertEntries: suspend (Array<ArtEntry>) -> Unit,
    ): Int {
        var count = 0
        val arrayStartIndex = source.indexOf("[".encodeToByteString())
        source.skip(arrayStartIndex)
        appJson.json.decodeSequenceIgnoreEndOfFile<ArtEntry>(source)
            .chunked(10)
            .forEach {
                count += it.size
                insertEntries(it.toTypedArray())
            }

        return count
    }
}
