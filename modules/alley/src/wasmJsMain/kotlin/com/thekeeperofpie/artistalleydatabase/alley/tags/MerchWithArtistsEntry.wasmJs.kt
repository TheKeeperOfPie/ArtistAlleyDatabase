package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

actual data class MerchWithArtistsEntry(
    actual val merch: MerchEntry,
    actual val artists: List<ArtistEntry>,
)
