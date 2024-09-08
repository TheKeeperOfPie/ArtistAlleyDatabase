package com.thekeeperofpie.artistalleydatabase.importing

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.WorkerParameters
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.MainActivity
import com.thekeeperofpie.artistalleydatabase.export.ExportUtils
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.notification.NotificationIds
import com.thekeeperofpie.artistalleydatabase.notification.NotificationProgressWorker
import com.thekeeperofpie.artistalleydatabase.settings.SettingsData
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import okio.use
import org.apache.commons.compress.archivers.zip.ZipFile
import java.nio.channels.FileChannel

@Inject
class ImportWorker(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val json: Json,
    private val settingsProvider: SettingsProvider,
    private val importers: Set<@JvmSuppressWildcards Importer>,
) : NotificationProgressWorker(
    appContext = appContext,
    params = params,
    progressKey = ImportUtils.KEY_PROGRESS,
    notificationChannel = NotificationChannels.IMPORT,
    notificationIdOngoing = NotificationIds.IMPORT_ONGOING,
    notificationIdFinished = NotificationIds.IMPORT_FINISHED,
    smallIcon = R.drawable.baseline_import_export_24,
    ongoingTitle = R.string.notification_import_ongoing_title,
    successTitle = R.string.notification_import_finished_title,
    failureTitle = R.string.notification_import_failed_title,
    notificationContentIntent = {
        PendingIntent.getActivity(
            appContext,
            PendingIntentRequestCodes.IMPORT_MAIN_ACTIVITY_OPEN.code,
            Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setClass(appContext, MainActivity::class.java)
                putExtra(
                    MainActivity.STARTING_NAV_DESTINATION,
                    NavDrawerItems.IMPORT.id,
                )
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    },
) {

    companion object {
        private const val TAG = "ImportWorker"
        private const val PARALLELISM = 8
    }

    override suspend fun doWorkInternal() =
        withContext(Dispatchers.IO.limitedParallelism(PARALLELISM)) {
            val inputUri = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
                ?.let(Uri::parse)
                ?: return@withContext Result.failure()

            // Default to safe values to avoid accidentally overwrites
            val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
            val replaceAll = params.inputData.getBoolean(ImportUtils.KEY_REPLACE_ALL, false)

            val dateTime = ExportUtils.currentDateTimeFileName()
            val appFilesDir = appContext.filesDir
            val privateImportDir = appFilesDir.resolve("import")
                .apply {
                    mkdirs()
                    deleteOnExit()
                }

            val tempZipFile = privateImportDir.resolve("$dateTime.zip")
            appContext.contentResolver.openInputStream(inputUri)?.use { input ->
                tempZipFile.outputStream().use(input::copyTo)
            } ?: throw IllegalArgumentException("Cannot open input URI $inputUri")

            try {
                // Warning doesn't handle limitedParallelism
                @Suppress("BlockingMethodInNonBlockingContext")
                FileChannel.open(tempZipFile.toPath()).use {
                    ZipFile(it).use { zipFile ->
                        val settingsEntry = zipFile.getEntry(SettingsProvider.EXPORT_FILE_NAME)
                        if (settingsEntry != null) {
                            try {
                                @OptIn(ExperimentalSerializationApi::class)
                                val settingsData = zipFile.getInputStream(settingsEntry).use {
                                    json.decodeFromStream<SettingsData>(it)
                                }
                                if (!dryRun) {
                                    settingsProvider.overwrite(settingsData)
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Failure restoring settings", e)
                            }
                        }

                        val entriesSize = try {
                            importers.sumOf {
                                val entry =
                                    zipFile.getEntry("${it.zipEntryName}.json") ?: return@sumOf 0
                                it.readEntries(
                                    zipFile.getInputStream(entry).asSource().buffered(),
                                    dryRun,
                                    replaceAll,
                                )
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Failure inserting entries", e)
                            throw RuntimeException(e)
                        }

                        var count = 0
                        zipFile.entries.asSequence()
                            .chunked(PARALLELISM)
                            .forEach { entries ->
                                entries.map { entry ->
                                    async {
                                        importers.find { entry.name.startsWith(it.zipEntryName + "/") }
                                            ?.readInnerFile(
                                                zipFile.getInputStream(entry).asSource().buffered(),
                                                entry.name,
                                                dryRun,
                                            )
                                    }
                                }.awaitAll()
                                count += entries.size
                                setProgress(count, entriesSize)
                            }
                    }
                }
            } finally {
                privateImportDir.deleteRecursively()
            }

            return@withContext Result.success()
        }
}
