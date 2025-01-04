package com.thekeeperofpie.artistalleydatabase.entry

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils.Exporter
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import kotlinx.io.Source
import kotlinx.io.files.SystemPathSeparator

abstract class EntryExporter(private val appFileSystem: AppFileSystem) : Exporter {

    protected suspend fun writeImages(
        entryId: EntryId,
        writeEntry: suspend (String, () -> Source) -> Unit,
        vararg values: List<String>
    ) {
        EntryUtils.getImages(appFileSystem, entryId)
            .forEachIndexed { index, image ->
                if (image.uri != null) {
                    writeImage(
                        entryId = entryId,
                        uri = image.uri,
                        index = index,
                        width = image.width,
                        height = image.height,
                        label = image.label,
                        cropped = false,
                        writeEntry = writeEntry,
                        values = values,
                    )
                }

                if (image.croppedUri != null) {
                    writeImage(
                        entryId = entryId,
                        uri = image.croppedUri,
                        index = index,
                        width = image.croppedWidth ?: 1,
                        height = image.croppedHeight ?: 1,
                        label = image.label,
                        cropped = true,
                        writeEntry = writeEntry,
                        values = values,
                    )
                }
            }
    }

    private suspend fun writeImage(
        entryId: EntryId,
        uri: Uri,
        index: Int,
        width: Int,
        height: Int,
        label: String,
        cropped: Boolean,
        writeEntry: suspend (String, () -> Source) -> Unit,
        vararg values: List<String>,
    ) {
        val fileName = "$index-$width-$height-$label".let {
            if (cropped) "$it-cropped" else it
        }
        val filePath = buildEntryFilePath(
            lastPathSegments = "${entryId.valueId}$SystemPathSeparator$fileName",
            values = values
        )

        writeEntry(filePath) { appFileSystem.openUriSource(uri)!! }
    }

    private fun buildEntryFilePath(lastPathSegments: String, vararg values: List<String>) = values
        .asSequence()
        .map { it.filter { it.isNotBlank() } }
        .filter { it.isNotEmpty() }
        .map {
            it.joinToString(separator = "-") {
                it.replace("\\", "\u29F5")
                    .replace("/", "\u2215")
                    .replace(":", "\uA789")
                    .replace("*", "\u204E")
                    .replace("?", "\uFF1F")
                    .replace("\"", "\u201D")
                    .replace("<", "\uFF1C")
                    .replace(">", "\uFF1E")
                    .replace("|", "\u23D0")
            }
                .take(120)
        }
        .fold(mutableListOf<String>()) { list, next ->
            if ((list.sumOf { it.length } + list.count()) < 850) {
                list.apply { add(next.take(120)) }
            } else list
        }
        .joinToString(separator = "$SystemPathSeparator")
        .ifBlank { "Unknown" } + "$SystemPathSeparator$lastPathSegments.jpg"
}
