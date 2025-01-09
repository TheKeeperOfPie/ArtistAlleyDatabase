package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry

data class ArtistWithStampRalliesEntry(
    val artist: ArtistEntry,
    val stampRallies: List<StampRallyEntry>,
)
