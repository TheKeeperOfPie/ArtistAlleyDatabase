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
    val showOutdatedCatalogs: Boolean,
) : SearchScreen.SearchEntryModel {

    val artist get() = data.artist
    val userEntry get() = data.userEntry
    override val images get() = data.images

    override val id = EntryId("artist_entry", artist.id)

    override val fallbackImages = data.fallbackImages.takeIf { showOutdatedCatalogs }.orEmpty()
    override val fallbackYear: DataYear?
        get() = artist.fallbackImageYear?.takeIf { showOutdatedCatalogs }
    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = artist.booth
    override val title get() = artist.name

    override val hasCatalog = artist.images.isNotEmpty()
    val showingFallback = !hasCatalog && fallbackImages.isNotEmpty()
    val displayImages get() = if (showingFallback) fallbackImages else images

    companion object {

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            showOutdatedCatalogs: Boolean, // TODO: Move this to UI layer?
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
                showOutdatedCatalogs = showOutdatedCatalogs,
            )
        }
    }
}
