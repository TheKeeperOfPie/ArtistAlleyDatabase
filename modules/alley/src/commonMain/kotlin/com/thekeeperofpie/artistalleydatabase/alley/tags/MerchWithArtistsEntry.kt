package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class MerchWithArtistsEntry(
    val merch: MerchEntry,
    val artists: List<ArtistEntry>,
)
