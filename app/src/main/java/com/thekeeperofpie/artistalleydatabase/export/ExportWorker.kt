package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.utils.ImportExportWorker
import com.thekeeperofpie.artistalleydatabase.utils.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.utils.NotificationIds
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.internal.closeQuietly
import okio.buffer
import okio.sink
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val appMoshi: AppMoshi,
    private val exporters: Set<@JvmSuppressWildcards Exporter>,
) : ImportExportWorker(
    appContext = appContext,
    params = params,
    progressKey = ExportUtils.KEY_PROGRESS,
    notificationChannel = NotificationChannels.EXPORT,
    notificationIdOngoing = NotificationIds.EXPORT_ONGOING,
    notificationIdFinished = NotificationIds.EXPORT_FINISHED,
    ongoingTitle = R.string.notification_export_ongoing_title,
    finishedTitle = R.string.notification_export_finished_title,
    notificationClickDestination = NavDrawerItems.Export,
    pendingIntentRequestCode = PendingIntentRequestCodes.EXPORT_MAIN_ACTIVITY_OPEN,
) {

    override suspend fun doWork(): Result {
        val outputUri = params.inputData.getString(ExportUtils.KEY_OUTPUT_CONTENT_URI)
            ?.let(Uri::parse)
            ?: return Result.failure()

        // Immediately open the output URI to get permissions for it
        appContext.contentResolver.openOutputStream(outputUri)
            ?.closeQuietly()
            ?: return Result.failure()

        try {
            setForeground(getForegroundInfo())
        } catch (ignored: Exception) {
        }

        val dateTime = ExportUtils.currentDateTimeFileName()
        val appFilesDir = appContext.filesDir
        val privateExportDir = appFilesDir.resolve("export")
            .apply {
                mkdirs()
                deleteOnExit()
            }

        val tempZipFile = privateExportDir.resolve("$dateTime.zip")

        val entriesSizes = exporters.map { it.entriesSize() }
        val entriesSizeMax = entriesSizes.sum()

        try {
            tempZipFile.outputStream().use {
                ZipOutputStream(it).use { zip ->
                    exporters.forEachIndexed { index, exporter ->
                        val tempEntryDir = privateExportDir.resolve(exporter.zipEntryName)
                            .apply { mkdirs() }

                        val tempJsonFile = privateExportDir.resolve(exporter.zipEntryName + ".json")

                        val startingProgressIndex = entriesSizes.take(index).sum()
                        val success = tempJsonFile.sink().use {
                            it.buffer().use {
                                JsonWriter.of(it).use { jsonWriter ->
                                    jsonWriter.indent = "    "
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
                                    ) { progress, max ->
                                        setProgress(
                                            startingProgressIndex + progress,
                                            entriesSizeMax
                                        )
                                    }
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

            appContext.contentResolver.openOutputStream(outputUri)?.use { output ->
                tempZipFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return Result.failure()

            notifyComplete()

            return Result.success()
        } finally {
            privateExportDir.deleteRecursively()
        }
    }
}