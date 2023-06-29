package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel

class ArtistEntryGridModel(
    val value: ArtistEntry,
    val images: List<Uri>,
    override val placeholderText: String,
) : EntryGridModel {

    override val id = EntryId("artist_entry", value.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    var favorite by mutableStateOf(value.favorite)

    private val artistNames = value.artistNames.map { it.split("\n").first() }

    val artistNamesText = artistNames.joinToString()
    val tableName = value.tableName ?: artistNamesText

    val showArtistNames = String.CASE_INSENSITIVE_ORDER
            .compare(tableName, artistNamesText) != 0
                && !tableName.startsWith(artistNamesText, ignoreCase = true)
                && artistNames != tableName.split("+", "&").map(String::trim)

    companion object {
        @WorkerThread
        fun buildFromEntry(application: Application, entry: ArtistEntry): ArtistEntryGridModel {
            return ArtistEntryGridModel(
                value = entry,
                images = ArtistAlleyUtils.getImages(application, entry.booth),
                placeholderText = entry.booth,
            )
        }
    }
}
