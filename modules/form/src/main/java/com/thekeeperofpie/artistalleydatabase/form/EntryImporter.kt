package com.thekeeperofpie.artistalleydatabase.form

import android.content.Context
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.Importer
import java.io.File
import java.io.InputStream
import java.util.UUID

abstract class EntryImporter(protected val appContext: Context) : Importer {

    abstract val scopedIdType: String

    override suspend fun readInnerFile(input: InputStream, fileName: String, dryRun: Boolean) {
        if (!dryRun) {
            val pathSegments = fileName.split(File.separator)
            val entryName = pathSegments.last().substringBefore(".")
            try {
                // Try to resolve the name directly as a UUID,
                // and if success, consider it a legacy name
                UUID.fromString(entryName)

                val outputFile = EntryUtils.getEntryImageFolder(
                    appContext,
                    EntryId(scopedIdType, entryName),
                ).apply(File::mkdirs)
                    .resolve("0-1-1")
                outputFile.outputStream().use(input::copyTo)
                EntryUtils.fixImageName(appContext, outputFile)
            } catch (e: Exception) {
                val entryId = pathSegments[pathSegments.size - 2]
                EntryUtils.getEntryImageFolder(appContext, EntryId(scopedIdType, entryId))
                    .apply(File::mkdirs)
                    .resolve(entryName)
                    .outputStream()
                    .use(input::copyTo)
            }
        }
    }
}