package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.Exporter
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.utils.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.utils.NotificationIds
import com.thekeeperofpie.artistalleydatabase.utils.NotificationProgressWorker
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import okhttp3.internal.closeQuietly
import okio.buffer
import okio.sink
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipMethod
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val appJson: AppJson,
    private val appMoshi: AppMoshi,
    private val settingsProvider: SettingsProvider,
    private val exporters: Set<@JvmSuppressWildcards Exporter>,
) : NotificationProgressWorker(
    appContext = appContext,
    params = params,
    progressKey = ExportUtils.KEY_PROGRESS,
    notificationChannel = NotificationChannels.EXPORT,
    notificationIdOngoing = NotificationIds.EXPORT_ONGOING,
    notificationIdFinished = NotificationIds.EXPORT_FINISHED,
    smallIcon = R.drawable.baseline_import_export_24,
    ongoingTitle = R.string.notification_export_ongoing_title,
    successTitle = R.string.notification_export_finished_title,
    failureTitle = R.string.notification_export_failed_title,
    notificationClickDestination = NavDrawerItems.Export,
    pendingIntentRequestCode = PendingIntentRequestCodes.EXPORT_MAIN_ACTIVITY_OPEN,
) {

    companion object {
        private const val TAG = "ExportWorker"
        private const val PARALLELISM = 8
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
    override suspend fun doWorkInternal(): Result {
        val dispatcher = Dispatchers.IO.limitedParallelism(PARALLELISM)
        return withContext(dispatcher) {
            val outputUri = params.inputData.getString(ExportUtils.KEY_OUTPUT_CONTENT_URI)
                ?.let(Uri::parse)
                ?: return@withContext Result.failure()

            // Immediately open the output URI to get permissions for it
            // noinspection Recycle Lint check doesn't handle closeQuietly
            appContext.contentResolver.openOutputStream(outputUri)
                ?.closeQuietly()
                ?: return@withContext Result.failure()

            val appFilesDir = appContext.filesDir
            val privateExportDir = appFilesDir.resolve("export")
                .apply {
                    mkdirs()
                    deleteOnExit()
                }

            val entriesSizes = exporters.map { it.entriesSize() }
            val entriesSizeMax = entriesSizes.sum()

            try {
                val executor = dispatcher.asExecutor()
                val zipCreator = ParallelScatterZipCreator(object : AbstractExecutorService() {
                    override fun execute(command: Runnable) = executor.execute(command)
                    override fun shutdown() = Unit
                    override fun shutdownNow() = mutableListOf<Runnable>()
                    override fun isShutdown() = false
                    override fun isTerminated() = false
                    override fun awaitTermination(length: Long, unit: TimeUnit) = false
                })

                zipCreator.addArchiveEntry(ZipArchiveEntry(SettingsProvider.EXPORT_FILE_NAME).apply {
                    method = ZipMethod.DEFLATED.code
                }) {
                    ByteArrayOutputStream().let {
                        it.use {
                            appJson.json.encodeToStream(
                                settingsProvider.serializer,
                                settingsProvider.settingsData,
                                it
                            )
                        }
                        it.toByteArray()
                    }.let(::ByteArrayInputStream)
                }

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
                                    this@ExportWorker,
                                    jsonWriter,
                                    jsonElementConverter =
                                    appMoshi.jsonElementAdapter::toJsonValue,
                                    writeEntry =
                                    { name, input ->
                                        zipCreator.addArchiveEntry(
                                            ZipArchiveEntry(
                                                tempEntryDir.resolve(
                                                    name
                                                ).relativeTo(privateExportDir).path
                                            ).apply { method = ZipMethod.DEFLATED.code },
                                            input
                                        )
                                    }
                                ) { progress, _ ->
                                    setProgress(
                                        startingProgressIndex + progress,
                                        entriesSizeMax
                                    )
                                }
                            }
                        }
                    }

                    zipCreator.addArchiveEntry(
                        ZipArchiveEntry(
                            tempJsonFile.relativeTo(
                                privateExportDir
                            ).path
                        ).apply { method = ZipMethod.DEFLATED.code }
                    ) { tempJsonFile.inputStream() }

                    if (!success) {
                        tempJsonFile.delete()
                        return@withContext Result.failure()
                    }
                }

                appContext.contentResolver.openOutputStream(outputUri)?.use { output ->
                    ZipArchiveOutputStream(output).use(zipCreator::writeTo)
                } ?: return@withContext Result.failure()

                return@withContext Result.success()
            } catch (e: Exception) {
                Log.d(TAG, "Error exporting", e)
                throw e
            } finally {
                privateExportDir.deleteRecursively()
            }
        }
    }
}