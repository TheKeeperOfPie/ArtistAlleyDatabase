package com.thekeeperofpie.artistalleydatabase.cds.grid

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

class CdEntryGridModel(
    val value: CdEntry,
    override val imageUri: Uri?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id get() = value.id
    override val imageWidth get() = value.imageWidth
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio

    companion object {
        fun buildFromEntry(
            application: Application,
            entry: CdEntry
        ): CdEntryGridModel {
            val imageUri = CdEntryUtils.getImageFile(application, entry.id)
                .takeIf(File::exists)
                ?.toUri()
                ?.buildUpon()
                ?.appendQueryParameter("width", entry.imageWidth.toString())
                ?.appendQueryParameter("width", entry.imageHeight.toString())
                ?.build()

            // Placeholder text is generally only useful without an image
            val placeholderText = if (imageUri == null) {
                CdEntryUtils.buildPlaceholderText(entry)
            } else ""

            return CdEntryGridModel(
                value = entry,
                imageUri = imageUri,
                placeholderText = placeholderText,
            )
        }
    }
}