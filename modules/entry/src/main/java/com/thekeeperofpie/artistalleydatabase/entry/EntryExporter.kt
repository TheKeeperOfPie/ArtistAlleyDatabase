package com.thekeeperofpie.artistalleydatabase.entry

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.ExportUtils
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import kotlinx.io.Source
import java.io.File

abstract class EntryExporter(private val appFileSystem: AppFileSystem) : Exporter {

    protected suspend fun writeImages(
        entryId: EntryId,
        writeEntry: suspend (String, () -> Source) -> Unit,
        vararg values: List<String>
    ) {
        EntryUtils.getImages(appFileSystem, entryId, 0)
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
        val filePath = ExportUtils.buildEntryFilePath(
            lastPathSegments = "${entryId.valueId}${File.separator}$fileName",
            values = values
        )

        writeEntry(filePath) { appFileSystem.openUri(uri)!! }
    }
}
