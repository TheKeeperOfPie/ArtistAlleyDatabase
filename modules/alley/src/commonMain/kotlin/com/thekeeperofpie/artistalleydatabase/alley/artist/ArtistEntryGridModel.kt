package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

class ArtistEntryGridModel(
    val data: ArtistWithUserData,
    val series: List<String>,
    val merch: List<String>,
) : SearchScreen.SearchEntryModel {

    val artist get() = data.artist
    val userEntry get() = data.userEntry
    override val images get() = data.images

    override val id = EntryId("artist_entry", artist.id)

    override val fallbackImages = data.fallbackImages
    override val fallbackYear: DataYear? get() = artist.fallbackImageYear
    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = artist.booth
    override val title get() = artist.name

    override val hasCatalog = artist.images.isNotEmpty()

    companion object {

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
        ): ArtistEntryGridModel {
            val artist = entry.artist
            val merch = TagUtils.combineForDisplay(
                inferred = artist.merchInferred,
                confirmed = artist.merchConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            val series = TagUtils.combineForDisplay(
                inferred = artist.seriesInferred,
                confirmed = artist.seriesConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            return ArtistEntryGridModel(
                data = ArtistWithUserData(artist, entry.userEntry),
                series = series,
                merch = merch,
            )
        }
    }
}
