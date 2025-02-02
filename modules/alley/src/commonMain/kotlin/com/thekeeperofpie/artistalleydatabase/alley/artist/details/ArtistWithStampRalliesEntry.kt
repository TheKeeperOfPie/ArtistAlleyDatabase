package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry

data class ArtistWithStampRalliesEntry(
    val artist: ArtistWithUserData,
    val stampRallies: List<StampRallyEntry>,
)
