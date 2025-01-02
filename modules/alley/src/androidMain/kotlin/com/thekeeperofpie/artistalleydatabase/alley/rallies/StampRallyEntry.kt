package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "stamp_rally_entries")
data class StampRallyEntry(
    @PrimaryKey
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val fandom: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val hostTable: String,
    val tables: List<String> = emptyList(),
    val links: List<String> = emptyList(),
    val tableMin: Int? = null,
    val totalCost: Int? = null,
    val prizeLimit: Int? = null,
    val favorite: Boolean = false,
    val ignored: Boolean = false,
    val notes: String? = null,
    // Used fo random ordering while maintaining a stable key
    @ColumnInfo(defaultValue = "1")
    val counter: Int = 1,
)

@Entity(tableName = "stamp_rally_entries_fts")
@Fts4(contentEntity = StampRallyEntry::class)
data class StampRallyEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val fandom: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val hostTable: String,
    val tables: List<String>,
    val links: List<String>,
    val tableMin: Int?,
    val totalCost: Int?,
    val prizeLimit: Int?,
    val favorite: Boolean,
    val ignored: Boolean,
    val notes: String?,
)
