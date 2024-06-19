package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series_entries")
data class SeriesEntry(
    @PrimaryKey
    val name: String,
    val notes: String?,
)
