package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["stampRallyId", "artistId"], tableName = "stamp_rally_artist_connections")
data class StampRallyArtistConnection(
    val stampRallyId: String,
    @ColumnInfo(index = true)
    val artistId: String,
)
