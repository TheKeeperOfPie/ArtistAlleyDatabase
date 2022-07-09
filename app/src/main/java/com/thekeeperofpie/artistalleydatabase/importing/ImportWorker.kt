package com.thekeeperofpie.artistalleydatabase.importing

import android.content.Context
import android.net.Uri
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

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
            ?: return Result.failure()

        // Default to true to avoid accidentally overwrites
        val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
        val uri = Uri.parse(uriString)

        // First open only counts and inserts entries
        val entriesSize =
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

                            when (entry.name) {
                                "art_entries.json" -> count = readArtEntriesJson(
                                    entryInputStream,
                                    dryRun,
                                )
                            }
                            zipInput.closeEntry()
                            entry = zipInput.nextEntry
                        }
                        count
                    }
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
    private suspend fun readArtEntriesJson(input: InputStream, dryRun: Boolean): Int {
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

                val block: suspend (insert: suspend (ArtEntry) -> Unit) -> Unit = { insert ->
                    while (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        val entry = appMoshi.artEntryAdapter.fromJson(reader) ?: continue
                        count++
                        insert(entry)
                    }
                }

                if (dryRun) {
                    block {}
                } else {
                    artEntryDao.insertEntriesDeferred(block)
                }
                reader.endArray()
                reader.endObject()
            }
        }

        return count
    }
}