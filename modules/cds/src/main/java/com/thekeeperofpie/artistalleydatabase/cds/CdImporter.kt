package com.thekeeperofpie.artistalleydatabase.cds

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import okio.buffer
import okio.source
import java.io.InputStream

class CdImporter(
    private val appContext: Context,
    private val cdEntryDao: CdEntryDao,
    moshi: Moshi,
) : Importer {

    override val zipEntryName = "cd_entries"

    private val cdEntryAdapter = moshi.adapter(CdEntry::class.java)!!

    override suspend fun readEntries(
        input: InputStream,
        dryRun: Boolean,
        replaceAll: Boolean
    ): Int {
        var count = 0
        cdEntryDao.insertEntriesDeferred(dryRun, replaceAll) { insertEntry ->
            count = if (dryRun) {
                readCdEntriesJson(input) {}
            } else {
                readCdEntriesJson(input, insertEntry)
            }
        }
        return count
    }

    override suspend fun readInnerFile(input: InputStream, fileName: String, dryRun: Boolean) {
        if (!dryRun) {
            CdEntryUtils.getImageFile(appContext, fileName)
                .outputStream()
                .use { input.copyTo(it) }
        }
    }

    /**
     * @return number of valid entries found
     */
    private suspend fun readCdEntriesJson(
        input: InputStream,
        insertEntry: suspend (CdEntry) -> Unit,
    ): Int {
        var count = 0
        input.source().use {
            it.buffer().use {
                val reader = JsonReader.of(it)
                reader.isLenient = true
                reader.beginObject()
                val rootName = reader.nextName()
                if (rootName != "cd_entries") {
                    reader.skipValue()
                }

                reader.beginArray()

                while (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                    var entry = cdEntryAdapter.fromJson(reader) ?: continue

                    if (entry.performersSearchable.isEmpty()) {
                        entry = entry.copy(performersSearchable = entry.performers)
                    }

                    if (entry.composersSearchable.isEmpty()) {
                        entry = entry.copy(composersSearchable = entry.composers)
                    }

                    if (entry.seriesSearchable.isEmpty()) {
                        entry = entry.copy(seriesSearchable = entry.series)
                    }

                    if (entry.charactersSearchable.isEmpty()) {
                        entry = entry.copy(charactersSearchable = entry.characters)
                    }

                    count++
                    insertEntry(entry)
                }

                reader.endArray()
                reader.endObject()
            }
        }

        return count
    }
}