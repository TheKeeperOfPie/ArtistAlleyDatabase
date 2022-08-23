package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.buffer
import okio.sink
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportUserReadableWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val appMoshi: AppMoshi,
    private val exporters: Set<@JvmSuppressWildcards Exporter>,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ExportUtils.KEY_OUTPUT_CONTENT_URI)
            ?: return Result.failure()

        val dateTime = ExportUtils.currentDateTimeFileName()
        val appFilesDir = appContext.filesDir
        val privateExportDir = appFilesDir.resolve("export")
            .apply {
                mkdirs()
                deleteOnExit()
            }

        val tempZipFile = privateExportDir.resolve("$dateTime.zip")

        try {
            tempZipFile.outputStream().use {
                ZipOutputStream(it).use { zip ->
                    exporters.forEach { exporter ->
                        val tempEntryDir = privateExportDir.resolve(exporter.zipEntryName)
                            .apply { mkdirs() }

                        val tempJsonFile = privateExportDir.resolve(exporter.zipEntryName + ".json")

                        val success = tempJsonFile.sink().use {
                            it.buffer().use {
                                JsonWriter.of(it).use { jsonWriter ->
                                    exporter.writeEntries(
                                        this,
                                        jsonWriter,
                                        jsonElementConverter =
                                        appMoshi.jsonElementAdapter::toJsonValue,
                                        writeEntry =
                                        @Suppress("BlockingMethodInNonBlockingContext")
                                        { name, input ->
                                            zip.putNextEntry(
                                                ZipEntry(
                                                    tempEntryDir.resolve(name)
                                                        .relativeTo(privateExportDir)
                                                        .path
                                                )
                                            )
                                            input.use { it.copyTo(zip) }
                                            zip.closeEntry()
                                        }
                                    )
                                }
                            }
                        }

                        zip.putNextEntry(ZipEntry(tempJsonFile.relativeTo(privateExportDir).path))
                        tempJsonFile.inputStream().use {
                            it.copyTo(zip)
                        }
                        zip.closeEntry()

                        if (!success) {
                            tempJsonFile.delete()
                            return Result.failure()
                        }
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
        } finally {
            privateExportDir.deleteRecursively()
        }
    }
}