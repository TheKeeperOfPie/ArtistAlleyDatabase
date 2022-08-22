package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import okio.buffer
import okio.sink
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportUserReadableWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val artEntryDao: ArtEntryDao,
    private val artJson: ArtJson,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ExportUtils.KEY_OUTPUT_CONTENT_URI)
            ?: return Result.failure()

        val dateTime = ExportUtils.currentDateTimeFileName()
        val appFilesDir = appContext.filesDir
        val privateExportDir = appFilesDir.resolve("export").apply { mkdirs() }
        val tempJsonFile = privateExportDir.resolve("art_entries.json")
        val tempZipFile = privateExportDir.resolve("$dateTime.zip")

        try {
            tempZipFile.outputStream().use {
                ZipOutputStream(it).use { zip ->
                    val success = writeEntries(artEntryDao, tempJsonFile) {
                        val imageFile = ArtEntryUtils.getImageFile(appContext, it.id)
                        if (imageFile.exists()) {
                            val folderParts = it.series
                                .sorted()
                                .map {
                                    if (it.length > 120) {
                                        it.substring(0, 120)
                                    } else it
                                }
                                .toMutableList()

                            folderParts += when {
                                it.characters.isEmpty() -> {
                                    when {
                                        it.tags.isEmpty() -> "Unknown"
                                        else -> it.tags.joinToString("-")
                                    }
                                }
                                else -> if (it.characters.size == 1) {
                                    when {
                                        it.tags.isEmpty() -> it.characters.first()
                                        else -> {
                                            folderParts += it.characters.first()
                                            it.tags.joinToString("-")
                                        }
                                    }
                                } else {
                                    val characters = it.characters.joinToString("-")
                                    if (it.tags.isNotEmpty()) {
                                        characters + it.tags.joinToString(
                                            prefix = "-",
                                            separator = "-"
                                        )
                                    } else characters
                                }
                            }.take(120)

                            folderParts.fold(mutableListOf<String>()) { list, next ->
                                if ((list.sumOf { it.length } + list.count()) < 850) {
                                    list.apply { add(next.take(120)) }
                                } else list
                            }

                            val finalName = folderParts.joinToString(
                                separator = File.separator,
                                postfix = File.separator
                            ) + "${it.id}.jpg"

                            zip.putNextEntry(ZipEntry(finalName))
                            imageFile.inputStream().use { image ->
                                image.copyTo(zip)
                            }
                            zip.closeEntry()
                        }
                    }

                    if (!success) {
                        tempJsonFile.delete()
                        return Result.failure()
                    }

                    zip.putNextEntry(ZipEntry(tempJsonFile.name))
                    tempJsonFile.inputStream().use {
                        it.copyTo(zip)
                    }
                    zip.closeEntry()
                }
            }

            val outputUri = Uri.parse(uriString)
            appContext.contentResolver.openOutputStream(outputUri)?.use { output ->
                tempZipFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return Result.failure()

            return Result.success()
        } finally {
            tempJsonFile.delete()
            tempZipFile.delete()
        }
    }

    private fun CoroutineWorker.writeEntries(
        artEntryDao: ArtEntryDao,
        file: File,
        onEachEntry: (ArtEntry) -> Unit = {}
    ): Boolean {
        file.sink().use {
            it.buffer().use {
                JsonWriter.of(it).use { jsonWriter ->
                    jsonWriter.beginObject()
                        .name("art_entries")
                        .beginArray()
                    var stopped = false
                    var entriesSize = 0
                    artEntryDao.iterateEntries({ entriesSize = it }) { index, entry ->
                        if (isStopped) {
                            stopped = true
                            return@iterateEntries
                        }

                        onEachEntry(entry)

                        setProgressAsync(
                            Data.Builder()
                                .putFloat(
                                    "progress",
                                    index / entriesSize.coerceAtLeast(1).toFloat()
                                )
                                .build()
                        )

                        jsonWriter.jsonValue(artJson.json.encodeToString(entry))
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