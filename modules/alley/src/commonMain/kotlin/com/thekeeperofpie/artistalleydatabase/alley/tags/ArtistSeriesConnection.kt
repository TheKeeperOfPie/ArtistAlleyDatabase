package com.thekeeperofpie.artistalleydatabase.alley.tags

expect class ArtistSeriesConnection {
    val artistId: String
    val seriesId: String
    val confirmed: Boolean

    constructor(artistId: String, seriesId: String, confirmed: Boolean = false)
}
