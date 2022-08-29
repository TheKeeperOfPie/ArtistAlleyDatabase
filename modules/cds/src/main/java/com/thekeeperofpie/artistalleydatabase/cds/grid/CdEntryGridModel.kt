package com.thekeeperofpie.artistalleydatabase.cds.grid

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.cds.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

class CdEntryGridModel(
    val value: CdEntry,
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
            entry: CdEntry
        ): CdEntryGridModel {
            val localImageFile = CdEntryUtils.getImageFile(application, entry.id)
                .takeIf(File::exists)

            // Placeholder text is generally only useful without an image
            val placeholderText = if (localImageFile == null) {
                CdEntryUtils.buildPlaceholderText(appJson.json, entry)
            } else ""

            return CdEntryGridModel(
                value = entry,
                localImageFile = localImageFile,
                placeholderText = placeholderText,
            )
        }
    }
}