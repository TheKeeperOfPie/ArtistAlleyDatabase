package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

actual data class StampRallyWithArtistsEntry(
    actual val stampRally: StampRallyEntry,
    actual val artists: List<ArtistEntry>,
)
