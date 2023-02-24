package com.thekeeperofpie.artistalleydatabase.chooser

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChooserViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
    appJson: AppJson,
) : ArtSearchViewModel(application, artEntryDao, appJson) {

    fun getResults(): Intent? {
        val appPackageName = application.packageName
        val imageUriAndTypes = synchronized(selectedEntries) {
            selectedEntries.mapNotNull { getImageUriAndType(appPackageName, it.value) }
        }

        if (imageUriAndTypes.isEmpty()) return null

        return Intent("$appPackageName.ACTION_RETURN_FILE").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(null, "image/*")
            val mimeTypes = imageUriAndTypes
                .map { it.second }
                .distinct()
                .toTypedArray()
            val description = ClipDescription("images", mimeTypes)
            clipData = ClipData(description, ClipData.Item(imageUriAndTypes.first().first)).apply {
                imageUriAndTypes.drop(1).forEach {
                    addItem(ClipData.Item(it.first))
                }
            }
        }
    }

    fun getResult(entry: ArtEntryGridModel): Intent? {
        val appPackageName = application.packageName
        val (imageUri, mimeType) = getImageUriAndType(appPackageName, entry) ?: return null

        return Intent().apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, mimeType)
        }
    }

    private fun getImageUriAndType(
        appPackageName: String,
        entry: ArtEntryGridModel
    ): Pair<Uri, String>? {
        val file = EntryUtils.getImageFile(application, entry.value.entryId)
        if (!file.exists()) {
            return null
        }

        // Some consumers require a file extension to parse the image correctly.
        val mimeType = ImageUtils.getImageType(file)
        val extension = when (mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> ""
        }

        // TODO: Find a better solution for the file extension problem
        // TODO: Offer an option to compress before export in case the caller has a size limitation
        val externalFile = application.filesDir.resolve("external/external.$extension")
        file.copyTo(externalFile)

        return FileProvider.getUriForFile(
            application,
            "$appPackageName.fileprovider",
            externalFile
        ) to (mimeType ?: "image/*")
    }
}