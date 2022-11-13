package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

class ArtEntryGridModel(
    val value: ArtEntry,
    override val localImageFile: File?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id get() = value.id
    override val imageWidth get() = value.imageWidth
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio

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
                ArtEntryUtils.buildPlaceholderText(appJson, entry)
            } else ""

            return ArtEntryGridModel(
                value = entry,
                localImageFile = localImageFile,
                placeholderText = placeholderText,
            )
        }
    }
}