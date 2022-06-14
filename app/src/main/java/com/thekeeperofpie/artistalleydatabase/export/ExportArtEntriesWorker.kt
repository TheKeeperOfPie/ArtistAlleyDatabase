package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.sink
import java.io.File
import java.text.DateFormat
import java.time.Instant
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.reflect.full.memberProperties

@HiltWorker
class ExportArtEntriesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val artEntryDao: ArtEntryDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_OUTPUT_CONTENT_URI = "output_content_uri"
    }

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(KEY_OUTPUT_CONTENT_URI)
            ?: return Result.failure()

        val dateTime = DateFormat.getDateInstance().format(Date.from(Instant.now()))
        val appFilesDir = appContext.filesDir
        val privateExportDir = appFilesDir.resolve("export").apply { mkdirs() }
        val tempJsonFile = privateExportDir.resolve("$dateTime.json")

        if (!writeEntries(tempJsonFile)) {
            tempJsonFile.delete()
            return Result.failure()
        }

        val artEntryImagesDir = appFilesDir.resolve("entry_images")
        val tempZipFile = privateExportDir.resolve("$dateTime.zip")
        tempZipFile.outputStream().use {
            ZipOutputStream(it).use { zip ->
                zip.putNextEntry(ZipEntry(tempJsonFile.name))
                tempJsonFile.inputStream().use {
                    it.copyTo(zip)
                }
                zip.closeEntry()

                artEntryImagesDir.listFiles()?.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.inputStream().use {
                        it.copyTo(zip)
                    }
                    zip.closeEntry()
                }
            }
        }

        val outputUri = Uri.parse(uriString)
        appContext.contentResolver.openOutputStream(outputUri)?.use { output ->
            tempZipFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: return Result.Failure()

        return Result.Success()
    }

    private fun writeEntries(file: File): Boolean {
        file.sink().use {
            it.buffer().use {
                JsonWriter.of(it).use { jsonWriter ->
                    jsonWriter.beginObject()
                        .name("art_entries")
                        .beginArray()
                    var stopped = false
                    artEntryDao.iterateEntries { _, entry ->
                        if (isStopped) {
                            stopped = true
                            return@iterateEntries
                        }
                        jsonWriter.beginObject()
                        for (property in ArtEntry::class.memberProperties) {
                            jsonWriter.name(property.name)
                            jsonWriter.value(Json.Default.encodeToString(property.get(entry)))
                        }
                        jsonWriter.endObject()
                    }
                    if (stopped) {
                        return false
                    }
                    jsonWriter.endArray()
                        .endObject()
                }
            }
        }

        return true
    }
}