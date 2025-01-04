package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["stampRallyId", "artistId"], tableName = "stamp_rally_artist_connections")
actual data class StampRallyArtistConnection actual constructor(
    actual val stampRallyId: String,
    @ColumnInfo(index = true)
    actual val artistId: String,
)
