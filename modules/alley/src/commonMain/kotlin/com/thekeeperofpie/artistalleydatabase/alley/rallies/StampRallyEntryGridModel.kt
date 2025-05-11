package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

class StampRallyEntryGridModel(
    val stampRally: StampRallyEntry,
    val userEntry: StampRallyUserEntry,
    override val images: List<CatalogImage>,
    override val placeholderText: String,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", stampRally.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = stampRally.hostTable

    companion object {
        fun buildFromEntry(entry: StampRallyWithUserData) = StampRallyEntryGridModel(
            stampRally = entry.stampRally,
            userEntry = entry.userEntry,
            images = AlleyDataUtils.getRallyImages(
                year = entry.stampRally.year,
                file = entry.stampRally.let { "${it.hostTable}${it.fandom}" },
            ),
            placeholderText = entry.stampRally.fandom,
        )
    }
}
