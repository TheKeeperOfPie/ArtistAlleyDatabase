package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

class ArtistEntryGridModel(
    val data: ArtistWithUserData,
    val series: List<String>,
    val merch: List<String>,
) {

    val artist get() = data.artist
    val userEntry get() = data.userEntry

    val id = EntryId("artist_entry", artist.id)

    var favorite by mutableStateOf(userEntry.favorite)
    var ignored by mutableStateOf(userEntry.ignored)

    val booth get() = artist.booth
    val title get() = artist.name

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
