package com.thekeeperofpie.artistalleydatabase.chooser

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils_compose.ShareHandler
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class ChooserViewModel(
    private val application: Application,
    appFileSystem: AppFileSystem,
    artEntryDao: ArtEntryDetailsDao,
    aniListAutocompleter: AniListAutocompleter,
    json: Json,
) : ArtSearchViewModel(
    appFileSystem = appFileSystem,
    artEntryDao = artEntryDao,
    aniListAutocompleter = aniListAutocompleter,
    json = json,
) {
    fun getResults(): Intent? {
        val imageUriAndTypes = synchronized(selectedEntries) {
            selectedEntries.mapNotNull { getImageUriAndMimeType(it.value) }
        }

        if (imageUriAndTypes.isEmpty()) return null

        return Intent("${application.packageName}.ACTION_RETURN_FILE").apply {
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
        val (imageUri, mimeType) = getImageUriAndMimeType(entry)
        imageUri ?: return null

        return Intent().apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, mimeType)
        }
    }

    private fun getImageUriAndMimeType(entry: ArtEntryGridModel) =
        ShareHandler.getShareUriAndMimeTypeForPath(
            application = application,
            appFileSystem = appFileSystem,
            path = EntryUtils.getImagePath(appFileSystem, entry.value.entryId),
            uri = entry.imageUri,
        )
}
