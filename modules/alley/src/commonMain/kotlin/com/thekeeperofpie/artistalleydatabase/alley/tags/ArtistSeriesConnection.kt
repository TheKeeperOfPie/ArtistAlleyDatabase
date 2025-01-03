package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["artistId", "seriesId"], tableName = "artist_series_connections")
data class ArtistSeriesConnection(
    val artistId: String,
    val seriesId: String,
    @ColumnInfo(defaultValue = "0")
    val confirmed: Boolean = false,
)
