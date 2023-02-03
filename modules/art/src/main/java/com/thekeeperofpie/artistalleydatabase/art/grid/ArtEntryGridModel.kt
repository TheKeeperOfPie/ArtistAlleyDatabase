package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import java.io.File

class ArtEntryGridModel(
    val value: ArtEntry,
    override val imageUri: Uri?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id get() = value.id
    override val imageWidth get() = value.imageWidth
    override val imageHeight get() = value.imageHeight
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio

    companion object {
        fun buildFromEntry(
            application: Application,
            appJson: AppJson,
            entry: ArtEntry
        ): ArtEntryGridModel {
            val imageUri = ArtEntryUtils.getImageFile(application, entry.id)
                .takeIf(File::exists)
                ?.toUri()
                ?.buildUpon()
                ?.appendQueryParameter("width", entry.imageWidth.toString())
                ?.appendQueryParameter("width", entry.imageHeight.toString())
                ?.build()

            // Placeholder text is generally only useful without an image
            val placeholderText = if (imageUri == null) {
                ArtEntryUtils.buildPlaceholderText(appJson, entry)
            } else ""

            return ArtEntryGridModel(
                value = entry,
                imageUri = imageUri,
                placeholderText = placeholderText,
            )
        }
    }
}