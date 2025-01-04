package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

expect class MerchWithArtistsEntry {
    val merch: MerchEntry
    val artists: List<ArtistEntry>
}
