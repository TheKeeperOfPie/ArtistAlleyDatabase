package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import java.io.File

class ArtEntryGridModel(
    val value: ArtEntry,
    val localImageFile: File?,
    val placeholderText: String,
) {
    companion object {
        fun buildFromEntry(
            application: Application,
            appJson: AppJson,
            entry: ArtEntry
        ): ArtEntryGridModel {
            val localImageFile = ArtEntryUtils.getImageFile(application, entry.id)
                .takeIf(File::exists)

            // Placeholder text is generally only useful without an image
            val placeholderText = if (localImageFile == null) {
                ArtEntryUtils.buildPlaceholderText(appJson.json, entry)
            } else ""

            return ArtEntryGridModel(
                value = entry,
                localImageFile = localImageFile,
                placeholderText = placeholderText,
            )
        }
    }
}