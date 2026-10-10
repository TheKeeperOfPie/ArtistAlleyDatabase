package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry

data class ArtistWithStampRalliesEntry(
    val artistWithUserData: ArtistWithUserData,
    val stampRallies: List<StampRallyDatabaseEntry>,
)
