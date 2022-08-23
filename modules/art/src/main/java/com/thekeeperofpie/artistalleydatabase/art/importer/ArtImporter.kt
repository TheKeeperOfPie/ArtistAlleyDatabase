package com.thekeeperofpie.artistalleydatabase.art.importer

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import okio.buffer
import okio.source
import java.io.InputStream

class ArtImporter(
    private val appContext: Context,
    private val artEntryDao: ArtEntryDao,
    moshi: Moshi,
) : Importer {

    override val zipEntryName = "art_entries"

    private val artEntryAdapter = moshi.adapter(ArtEntry::class.java)!!

    override suspend fun readEntries(
        input: InputStream,
        dryRun: Boolean,
        replaceAll: Boolean
    ): Int {
        var count = 0
        artEntryDao.insertEntriesDeferred(dryRun, replaceAll) { insertEntry ->
            count = if (dryRun) {
                readArtEntriesJson(input) {}
            } else {
                readArtEntriesJson(input, insertEntry)
            }
        }
        return count
    }

    override suspend fun readInnerFile(input: InputStream, fileName: String, dryRun: Boolean) {
        if (!dryRun) {
            ArtEntryUtils.getImageFile(appContext, fileName)
                .outputStream()
                .use { input.copyTo(it) }
        }
    }

    override suspend fun <T> transaction(block: suspend () -> T): T {
        var value: T? = null
        artEntryDao.transaction {
            value = block()
        }
        return value!!
    }

    /**
     * @return number of valid entries found
     */
    private suspend fun readArtEntriesJson(
        input: InputStream,
        insertEntry: suspend (ArtEntry) -> Unit,
    ): Int {
        var count = 0
        input.source().use {
            it.buffer().use {
                val reader = JsonReader.of(it)
                reader.isLenient = true
                reader.beginObject()
                val rootName = reader.nextName()
                if (rootName != "art_entries") {
                    reader.skipValue()
                }

                reader.beginArray()

                while (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                    var entry = artEntryAdapter.fromJson(reader) ?: continue

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