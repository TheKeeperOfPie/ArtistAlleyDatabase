package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

class ArtistEntryGridModel(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
    override val images: List<CatalogImage>,
    override val placeholderText: String,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", artist.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = artist.booth

    companion object {
        fun buildFromEntry(entry: ArtistWithUserData): ArtistEntryGridModel {
            return ArtistEntryGridModel(
                artist = entry.artist,
                userEntry = entry.userEntry,
                images = AlleyDataUtils.getImages(
                    folder = AlleyDataUtils.Folder.CATALOGS,
                    file = entry.artist.booth,
                ),
                placeholderText = entry.artist.booth,
            )
        }
    }
}
