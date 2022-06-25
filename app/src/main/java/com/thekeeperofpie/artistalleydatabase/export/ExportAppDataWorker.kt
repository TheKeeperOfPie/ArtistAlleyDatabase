package com.thekeeperofpie.artistalleydatabase.export

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.export.ExportUtils.writeEntries
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportAppDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val artEntryDao: ArtEntryDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uriString = params.inputData.getString(ExportUtils.KEY_OUTPUT_CONTENT_URI)
            ?: return Result.failure()

        val dateTime = ExportUtils.currentDateTimeFileName()
        val appFilesDir = appContext.filesDir
        val privateExportDir = appFilesDir.resolve("export").apply { mkdirs() }
        val tempJsonFile = privateExportDir.resolve("art_entries.json")
        if (!writeEntries(artEntryDao, tempJsonFile)) {
            tempJsonFile.delete()
            return Result.failure()
        }

        val artEntryImagesDir = appFilesDir.resolve("entry_images")
        val tempZipFile = privateExportDir.resolve("$dateTime.zip")
        try {
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
        } finally {
            tempJsonFile.delete()
            tempZipFile.delete()
        }
    }
}