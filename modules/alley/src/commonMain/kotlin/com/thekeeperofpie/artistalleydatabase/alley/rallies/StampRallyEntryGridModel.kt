package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

class StampRallyEntryGridModel(
    val stampRallyWithUserData: StampRallyWithUserData,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", stampRallyWithUserData.stampRally.id)

    override var favorite by mutableStateOf(stampRallyWithUserData.userEntry.favorite)
    override var ignored by mutableStateOf(stampRallyWithUserData.userEntry.ignored)

    override val booth get() = stampRallyWithUserData.stampRally.hostTable

    override val hasCatalog get() = images.isNotEmpty()
    override val fallbackImages get() = emptyList<CatalogImage>()
    override val fallbackYear: DataYear? get() = null
    override val title get() = "${stampRallyWithUserData.stampRally.hostTable}-${stampRallyWithUserData.stampRally.fandom}"

    override val images get() = stampRallyWithUserData.images

    companion object {
        fun buildFromEntry(entry: StampRallyWithUserData): StampRallyEntryGridModel =
            StampRallyEntryGridModel(
                stampRallyWithUserData = entry,
            )
    }
}
