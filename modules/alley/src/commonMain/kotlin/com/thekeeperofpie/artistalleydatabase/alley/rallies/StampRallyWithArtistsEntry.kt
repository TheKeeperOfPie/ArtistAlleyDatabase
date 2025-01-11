package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry

data class StampRallyWithArtistsEntry(
    val stampRally: StampRallyWithUserData,
    val artists: List<ArtistEntry>,
)
