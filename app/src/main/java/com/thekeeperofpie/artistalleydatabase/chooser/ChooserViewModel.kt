package com.thekeeperofpie.artistalleydatabase.chooser

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChooserViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
) : SearchViewModel(application, artEntryDao) {

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

    fun getResult(entry: ArtEntryModel): Intent? {
        val appPackageName = application.packageName
        val (imageUri, mimeType) = getImageUriAndType(appPackageName, entry) ?: return null

        return Intent("$appPackageName.ACTION_RETURN_FILE").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, mimeType)
        }
    }

    private fun getImageUriAndType(
        appPackageName: String,
        entry: ArtEntryModel
    ): Pair<Uri, String>? {
        val file = ArtEntryUtils.getImageFile(application, entry.value.id)
        if (!file.exists()) {
            return null
        }

        return FileProvider.getUriForFile(application, "$appPackageName.fileprovider", file) to
                (ArtEntryUtils.getImageType(file) ?: "image/*")
    }
}