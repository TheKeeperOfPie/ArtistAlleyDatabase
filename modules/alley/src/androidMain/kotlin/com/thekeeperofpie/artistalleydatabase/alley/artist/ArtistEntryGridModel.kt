package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem

class ArtistEntryGridModel(
    val value: ArtistEntry,
    override val images: List<CatalogImage>,
    override val placeholderText: String,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", value.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    override var favorite by mutableStateOf(value.favorite)
    override var ignored by mutableStateOf(value.ignored)

    override val booth get() = value.booth

    companion object {
        @WorkerThread
        fun buildFromEntry(
            appFileSystem: AppFileSystem,
            entry: ArtistEntry,
        ): ArtistEntryGridModel {
            return ArtistEntryGridModel(
                value = entry,
                images = ArtistAlleyUtils.getImages(
                    appFileSystem,
                    "catalogs",
                    entry.booth,
                ),
                placeholderText = entry.booth,
            )
        }
    }
}
