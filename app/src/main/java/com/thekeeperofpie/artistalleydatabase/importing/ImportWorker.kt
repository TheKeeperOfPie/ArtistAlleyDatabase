package com.thekeeperofpie.artistalleydatabase.importing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
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
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "ImportWorker"
    }

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ImportUtils.KEY_INPUT_CONTENT_URI)
            ?: return Result.failure()

        // Default to safe values to avoid accidentally overwrites
        val dryRun = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, true)
        val replaceAll = params.inputData.getBoolean(ImportUtils.KEY_DRY_RUN, false)
        val uri = Uri.parse(uriString)

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
            val firstPass = appContext.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open input URI $uri")
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
            val secondPass = appContext.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open input URI $uri")
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

            return@runBlock Result.success()
        }
    }
}