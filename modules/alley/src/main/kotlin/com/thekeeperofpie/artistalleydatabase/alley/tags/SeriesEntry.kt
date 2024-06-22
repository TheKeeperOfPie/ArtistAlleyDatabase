package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "series_entries")
data class SeriesEntry(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val notes: String?,
)

@Entity(tableName = "series_entries_fts")
@Fts4(contentEntity = SeriesEntry::class)
data class SeriesEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val notes: String?,
)
