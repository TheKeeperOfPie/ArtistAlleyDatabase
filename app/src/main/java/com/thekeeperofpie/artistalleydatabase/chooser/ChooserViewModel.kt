package com.thekeeperofpie.artistalleydatabase.chooser

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.AppJson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChooserViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
    appJson: AppJson,
) : SearchViewModel(application, artEntryDao, appJson) {

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

        return Intent("$appPackageName.ACTION_RETURN_FILE").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, mimeType)
        }
    }

    private fun getImageUriAndType(
        appPackageName: String,
        entry: ArtEntryGridModel
    ): Pair<Uri, String>? {
        val file = ArtEntryUtils.getImageFile(application, entry.value.id)
        if (!file.exists()) {
            return null
        }

        return FileProvider.getUriForFile(application, "$appPackageName.fileprovider", file) to
                (ArtEntryUtils.getImageType(file) ?: "image/*")
    }
}