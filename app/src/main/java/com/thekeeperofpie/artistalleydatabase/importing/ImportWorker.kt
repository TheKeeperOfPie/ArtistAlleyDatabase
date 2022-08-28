package com.thekeeperofpie.artistalleydatabase.importing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.utils.ImportExportWorker
import com.thekeeperofpie.artistalleydatabase.utils.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.utils.NotificationIds
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.FilterInputStream
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.nameWithoutExtension

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val importers: Set<@JvmSuppressWildcards Importer>,
) : ImportExportWorker(
    appContext = appContext,
    params = params,
    progressKey = ImportUtils.KEY_PROGRESS,
    notificationChannel = NotificationChannels.IMPORT,
    notificationIdOngoing = NotificationIds.IMPORT_ONGOING,
    notificationIdFinished = NotificationIds.IMPORT_FINISHED,
    ongoingTitle = R.string.notification_import_ongoing_title,
    finishedTitle = R.string.notification_import_finished_title,
    notificationClickDestination = NavDrawerItems.Import,
    pendingIntentRequestCode = PendingIntentRequestCodes.IMPORT_MAIN_ACTIVITY_OPEN,
) {

    companion object {
        private const val TAG = "ImportWorker"
    }

    override suspend fun doWork(): Result {
        val inputUri = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
            ?.let(Uri::parse)
            ?: return Result.failure()

        // Default to safe values to avoid accidentally overwrites
        val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
        val replaceAll = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, false)

        // Wraps the entire delete -> insert -> success cycle in a transaction
        // by nesting transactions started from each module's DAO
        // TODO: Verify this nesting actually works as expected
        val initialBlock: suspend (block: suspend () -> Result) -> Result = { it() }
        val runBlock: suspend (block: suspend () -> Result) -> Result =
            importers.fold(initialBlock) { block, importer ->
                {
                    block {
                        importer.transaction(it)
                    }
                }
            }

        return runBlock {
            var entriesSize = 0
            // First open only counts and inserts entries
            val firstPass = appContext.contentResolver.openInputStream(inputUri)
                ?: throw IllegalArgumentException("Cannot open input URI $inputUri")
            try {
                firstPass.use { fileInput ->
                    ZipInputStream(fileInput).use { zipInput ->
                        var entry = zipInput.nextEntry
                        while (entry != null) {
                            val entryInputStream = object : FilterInputStream(zipInput) {
                                override fun close() {
                                    // Do nothing
                                }
                            }

                            entriesSize += importers.find { "${it.zipEntryName}.json" == entry.name }
                                ?.readEntries(entryInputStream, dryRun, replaceAll) ?: 0

                            zipInput.closeEntry()
                            entry = zipInput.nextEntry
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Failure inserting entries", e)
                throw RuntimeException(e)
            }

            // Second pass uses previous count to determine progress and does image copying
            val secondPass = appContext.contentResolver.openInputStream(inputUri)
                ?: throw IllegalArgumentException("Cannot open input URI $inputUri")
            secondPass.use { fileInput ->
                ZipInputStream(fileInput).use { zipInput ->
                    var count = 0
                    var entry = zipInput.nextEntry
                    while (entry != null) {
                        val entryInputStream = object : FilterInputStream(zipInput) {
                            override fun close() {
                                // Do nothing
                            }
                        }

                        val fileName = Paths.get(entry.name).nameWithoutExtension
                        importers.find { entry.name.startsWith(it.zipEntryName + "/") }
                            ?.readInnerFile(entryInputStream, fileName, dryRun)
                            ?.also {
                                count++
                                setProgress(count, entriesSize)
                            }

                        zipInput.closeEntry()
                        entry = zipInput.nextEntry
                    }
                }
            }

            notifyComplete()

            return@runBlock Result.success()
        }
    }
}