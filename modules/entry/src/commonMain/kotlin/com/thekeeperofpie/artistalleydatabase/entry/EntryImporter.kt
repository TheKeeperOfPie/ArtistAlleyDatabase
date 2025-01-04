package com.thekeeperofpie.artistalleydatabase.entry

import com.benasher44.uuid.uuidFrom
import com.thekeeperofpie.artistalleydatabase.utils.Importer
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.io.Source
import kotlinx.io.files.Path
import kotlinx.io.files.SystemPathSeparator
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
abstract class EntryImporter(
    private val appFileSystem: AppFileSystem,
) : Importer {

    abstract val scopedIdType: String

    override suspend fun readInnerFile(source: Source, fileName: String, dryRun: Boolean) {
        if (!dryRun) {
            val pathSegments = fileName.split(SystemPathSeparator)
            val entryName = pathSegments.last().substringBefore(".")
            try {
                // Try to resolve the name directly as a UUID,
                // and if success, consider it a legacy name
                uuidFrom(entryName)

                val outputFolder = EntryUtils.getEntryImageFolder(
                    appFileSystem,
                    EntryId(scopedIdType, entryName),
                )
                appFileSystem.createDirectories(outputFolder)
                val outputPath = Path(outputFolder.toString() + SystemPathSeparator + "0-1-1")
                appFileSystem.sink(outputPath).use {
                    source.transferTo(it)
                }
                EntryUtils.fixImageName(appFileSystem, outputPath)
            } catch (_: Exception) {
                val entryId = pathSegments[pathSegments.size - 2]
                val folder =
                    EntryUtils.getEntryImageFolder(
                        appFileSystem,
                        EntryId(scopedIdType, entryId)
                    )
                appFileSystem.createDirectories(folder)
                val outputPath = Path(folder.toString() + SystemPathSeparator + entryName)
                appFileSystem.sink(outputPath).use {
                    source.transferTo(it)
                }
            }
        }
    }
}
