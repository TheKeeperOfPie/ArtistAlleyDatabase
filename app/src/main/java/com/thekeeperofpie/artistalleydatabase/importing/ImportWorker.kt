package com.thekeeperofpie.artistalleydatabase.importing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonReader
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.buffer
import okio.source
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.nameWithoutExtension

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val artEntryDao: ArtEntryDao,
    private val appMoshi: AppMoshi,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "ImportWorker"
    }

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
            ?: return Result.failure()

        // Default to safe values to avoid accidentally overwrites
        val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
        val replaceAll = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, false)
        val uri = Uri.parse(uriString)

        // TODO: Wrap the entire delete -> insert -> success cycle in a transaction

        // First open only counts and inserts entries
        var entriesSize = 0
        try {
            artEntryDao.insertEntriesDeferred(dryRun, replaceAll) { insertEntry ->
                appContext.contentResolver.openInputStream(uri).use { fileInput ->
                    ZipInputStream(fileInput).use { zipInput ->
                        var entry = zipInput.nextEntry
                        while (entry != null) {
                            val entryInputStream = object : FilterInputStream(zipInput) {
                                override fun close() {
                                    // Do nothing
                                }
                            }

                            when (entry.name) {
                                "art_entries.json" ->
                                    entriesSize = if (dryRun) {
                                        readArtEntriesJson(entryInputStream) {}
                                    } else {
                                        readArtEntriesJson(entryInputStream, insertEntry)
                                    }
                            }
                            zipInput.closeEntry()
                            entry = zipInput.nextEntry
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failure inserting entries", e)
            return Result.failure()
        }

        // Second pass uses previous count to determine progress and does image copying
        (appContext.contentResolver.openInputStream(uri) ?: return Result.failure())
            .use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    var count = 0
                    var entry = zipInput.nextEntry
                    while (entry != null) {
                        val entryInputStream = object : FilterInputStream(zipInput) {
                            override fun close() {
                                // Do nothing
                            }
                        }

                        if (entry.name != "art_entries.json") {
                            if (!dryRun) {
                                ArtEntryUtils.getImageFile(
                                    appContext,
                                    Paths.get(entry.name).nameWithoutExtension
                                )
                                    .outputStream()
                                    .use { entryInputStream.copyTo(it) }
                            }
                            count++
                            setProgressAsync(
                                Data.Builder().putFloat(
                                    ImportUtils.KEY_PROGRESS,
                                    (count / entriesSize.toFloat()).coerceIn(0f, 1f)
                                ).build()
                            )
                        }
                        zipInput.closeEntry()
                        entry = zipInput.nextEntry
                    }
                }
            }

        return Result.success()
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
                    var entry = appMoshi.artEntryAdapter.fromJson(reader) ?: continue

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