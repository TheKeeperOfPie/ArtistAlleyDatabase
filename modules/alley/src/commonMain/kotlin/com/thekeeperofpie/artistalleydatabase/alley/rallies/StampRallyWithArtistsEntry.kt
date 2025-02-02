package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class StampRallyWithArtistsEntry(
    val stampRally: StampRallyWithUserData,
    val artists: List<ArtistEntry>,
)
