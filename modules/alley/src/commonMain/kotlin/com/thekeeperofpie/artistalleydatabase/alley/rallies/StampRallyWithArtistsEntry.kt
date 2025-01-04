package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

expect class StampRallyWithArtistsEntry {
    val stampRally: StampRallyEntry
    val artists: List<ArtistEntry>
}
