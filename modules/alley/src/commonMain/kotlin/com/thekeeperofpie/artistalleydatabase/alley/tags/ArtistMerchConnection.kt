package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["artistId", "merchId"], tableName = "artist_merch_connections")
data class ArtistMerchConnection(
    val artistId: String,
    val merchId: String,
    @ColumnInfo(defaultValue = "0")
    val confirmed: Boolean = false,
)
