package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.Converters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.buffer
import okio.sink
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

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

        val dateTime = ExportUtils.currentDateTimeFileName()
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
        } ?: return Result.failure()

        return Result.success()
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
                            val value = property.get(entry)
                            val kType = property.returnType
                            val kClass = kType.jvmErasure

                            @Suppress("UNCHECKED_CAST")
                            val serializer = Converters.KSERIALIZERS[kClass] as? KSerializer<Any?>
                                ?: Json.Default.serializersModule.serializer(kType)

                            jsonWriter.value(Json.Default.encodeToString(serializer, value))
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