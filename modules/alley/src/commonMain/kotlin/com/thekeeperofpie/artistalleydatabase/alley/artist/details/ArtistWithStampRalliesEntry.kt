package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry

expect class ArtistWithStampRalliesEntry {
    val artist: ArtistEntry
    val stampRallies: List<StampRallyEntry>
}
