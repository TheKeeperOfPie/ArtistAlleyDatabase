package com.thekeeperofpie.artistalleydatabase.art.persistence

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.EntryImporter
import okio.buffer
import okio.source
import java.io.InputStream

class ArtImporter(
    appContext: Context,
    private val artEntryDao: ArtEntryDao,
    moshi: Moshi,
) : EntryImporter(appContext) {

    override val zipEntryName = "art_entries"

    override val entryTypeId = ArtEntryUtils.TYPE_ID

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
                    val entry = artEntryAdapter.fromJson(reader) ?: continue
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