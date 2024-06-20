package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Entity

@Entity(primaryKeys = ["artistId", "merchId"], tableName = "artist_merch_connections")
data class ArtistMerchConnection(
    val artistId: String,
    val merchId: String,
)
