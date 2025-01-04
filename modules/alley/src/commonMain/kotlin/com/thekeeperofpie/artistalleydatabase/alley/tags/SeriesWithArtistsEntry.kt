package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

expect class SeriesWithArtistsEntry {
    val series: SeriesEntry
    val artists: List<ArtistEntry>
}
