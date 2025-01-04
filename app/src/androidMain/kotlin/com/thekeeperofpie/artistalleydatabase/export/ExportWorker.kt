package com.thekeeperofpie.artistalleydatabase.export

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.WorkerParameters
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.notification_export_failed_title
import artistalleydatabase.app.generated.resources.notification_export_finished_title
import artistalleydatabase.app.generated.resources.notification_export_ongoing_title
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.MainActivity
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.notification.NotificationIds
import com.thekeeperofpie.artistalleydatabase.notification.NotificationProgressWorker
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.Exporter
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import okhttp3.internal.closeQuietly
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipMethod
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

@Inject
class ExportWorker(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val appFileSystem: AppFileSystem,
    private val json: Json,
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
    ongoingTitle = Res.string.notification_export_ongoing_title,
    successTitle = Res.string.notification_export_finished_title,
    failureTitle = Res.string.notification_export_failed_title,
    notificationContentIntent = {
        PendingIntent.getActivity(
            appContext,
            PendingIntentRequestCodes.EXPORT_MAIN_ACTIVITY_OPEN.code,
            Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setClass(appContext, MainActivity::class.java)
                putExtra(
                    MainActivity.STARTING_NAV_DESTINATION,
                    NavDrawerItems.EXPORT.id,
                )
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    },
) {

    companion object {
        private const val TAG = "ExportWorker"
        private const val PARALLELISM = 8
    }

    @OptIn(ExperimentalSerializationApi::class)
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
                            json.encodeToStream(settingsProvider.settingsData, it)
                        }
                        it.toByteArray()
                    }.let(::ByteArrayInputStream)
                }

                exporters.forEachIndexed { index, exporter ->
                    val tempEntryDir = privateExportDir.resolve(exporter.zipEntryName)
                        .apply { mkdirs() }

                    val tempJsonFile = privateExportDir.resolve(exporter.zipEntryName + ".json")

                    val startingProgressIndex = entriesSizes.take(index).sum()
                    val success = appFileSystem.sink(Path(tempJsonFile.path)).buffered().use {
                        exporter.writeEntries(
                            it,
                            writeEntry =
                            { name, input ->
                                zipCreator.addArchiveEntry(
                                    ZipArchiveEntry(
                                        tempEntryDir.resolve(
                                            name
                                        ).relativeTo(privateExportDir).path
                                    ).apply { method = ZipMethod.DEFLATED.code },
                                    { input().asInputStream() }
                                )
                            }
                        ) { progress, _ ->
                            setProgress(
                                startingProgressIndex + progress,
                                entriesSizeMax
                            )
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
