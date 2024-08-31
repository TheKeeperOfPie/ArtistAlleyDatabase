package com.thekeeperofpie.artistalleydatabase.entry

import com.benasher44.uuid.uuidFrom
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import kotlinx.io.Source
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeSourceToSequence
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
abstract class EntryImporter(
    private val appFileSystem: AppFileSystem,
) : Importer {

    abstract val scopedIdType: String

    override suspend fun readInnerFile(source: Source, fileName: String, dryRun: Boolean) {
        if (!dryRun) {
            val pathSegments = fileName.split(File.separator)
            val entryName = pathSegments.last().substringBefore(".")
            try {
                // Try to resolve the name directly as a UUID,
                // and if success, consider it a legacy name
                uuidFrom(entryName)

                val outputFolder = EntryUtils.getEntryImageFolder(
                    appFileSystem,
                    EntryId(scopedIdType, entryName),
                )
                SystemFileSystem.createDirectories(outputFolder)
                val outputPath = Path(outputFolder.toString() + SystemPathSeparator + "0-1-1")
                SystemFileSystem.sink(outputPath).use {
                    source.transferTo(it)
                }
                EntryUtils.fixImageName(appFileSystem, outputPath)
            } catch (e: Exception) {
                val entryId = pathSegments[pathSegments.size - 2]
                val folder =
                    EntryUtils.getEntryImageFolder(
                        appFileSystem,
                        EntryId(scopedIdType, entryId)
                    )
                SystemFileSystem.createDirectories(folder)
                val outputPath = Path(folder.toString() + SystemPathSeparator + entryName)
                SystemFileSystem.sink(outputPath).use {
                    source.transferTo(it)
                }
            }
        }
    }

    protected inline fun <reified T> Json.decodeSequenceIgnoreEndOfFile(source: Source): Sequence<T> {
        val decoded = decodeSourceToSequence<T>(source, DecodeSequenceMode.ARRAY_WRAPPED)
        return sequence<T> {
            val iterator = decoded.iterator()
            while (iterator.hasNextIgnoreEndOfFile()) {
                val next = iterator.next()
                yield(next)
            }
        }
    }

    fun Iterator<*>.hasNextIgnoreEndOfFile() = try {
        hasNext()
    } catch (throwable: Throwable) {
        // This is a bad hack to get around the fact the array is wrapped in another object
        // and the decoding mechanism can't handle that.
        if (throwable.message?.contains("Expected EOF") == true) {
            false
        } else {
            throw throwable
        }
    }
}
