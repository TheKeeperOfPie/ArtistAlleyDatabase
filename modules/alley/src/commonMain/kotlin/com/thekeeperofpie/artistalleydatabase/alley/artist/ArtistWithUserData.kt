package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry

data class ArtistWithUserData(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
)
