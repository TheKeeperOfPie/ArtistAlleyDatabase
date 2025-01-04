package com.thekeeperofpie.artistalleydatabase.alley.tags

expect class ArtistMerchConnection {
    val artistId: String
    val merchId: String
    val confirmed: Boolean

    constructor(artistId: String, merchId: String, confirmed: Boolean = false)
}
