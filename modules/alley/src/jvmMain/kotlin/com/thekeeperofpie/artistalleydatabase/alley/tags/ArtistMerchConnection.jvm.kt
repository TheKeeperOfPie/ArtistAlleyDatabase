package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["artistId", "merchId"], tableName = "artist_merch_connections")
actual data class ArtistMerchConnection actual constructor(
    actual val artistId: String,
    actual val merchId: String,
    @ColumnInfo(defaultValue = "0")
    actual val confirmed: Boolean,
)
