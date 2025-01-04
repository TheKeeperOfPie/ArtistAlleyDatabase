package com.thekeeperofpie.artistalleydatabase.alley.rallies

expect class StampRallyArtistConnection {
    val stampRallyId: String
    val artistId: String

    constructor(stampRallyId: String, artistId: String)
}
