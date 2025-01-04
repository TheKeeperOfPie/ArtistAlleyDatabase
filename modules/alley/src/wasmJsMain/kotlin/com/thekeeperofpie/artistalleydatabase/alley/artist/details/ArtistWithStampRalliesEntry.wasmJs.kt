package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry

actual data class ArtistWithStampRalliesEntry(
    actual val artist: ArtistEntry,
    actual val stampRallies: List<StampRallyEntry>,
)
