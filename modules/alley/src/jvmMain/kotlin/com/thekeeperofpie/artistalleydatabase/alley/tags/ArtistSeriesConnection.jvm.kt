package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["artistId", "seriesId"], tableName = "artist_series_connections")
actual data class ArtistSeriesConnection actual constructor(
    actual val artistId: String,
    actual val seriesId: String,
    @ColumnInfo(defaultValue = "0")
    actual val confirmed: Boolean,
)
