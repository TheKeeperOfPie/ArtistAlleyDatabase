package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData

data class ArtistWithStampRalliesEntry(
    val artist: ArtistWithUserData,
    val stampRallies: List<StampRallyEntry>,
)
