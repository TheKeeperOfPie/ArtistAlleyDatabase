package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class StampRallyWithArtistsEntry(
    val stampRallyWithUserData: StampRallyWithUserData,
    val artists: List<ArtistEntry>,
)
