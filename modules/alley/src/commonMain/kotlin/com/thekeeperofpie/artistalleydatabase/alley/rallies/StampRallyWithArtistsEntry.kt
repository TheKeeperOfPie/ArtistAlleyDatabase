package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry

data class StampRallyWithArtistsEntry(
    val stampRally: StampRallyEntry,
    val artists: List<ArtistEntry>,
)
