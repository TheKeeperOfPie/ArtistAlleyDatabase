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
    // TODO: Should this be numerical?
    val minimumPerTable: String? = null,
    val favorite: Boolean = false,
    val ignored: Boolean = false,
    val notes: String? = null,
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
    val minimumPerTable: String?,
    val favorite: Boolean,
    val ignored: Boolean,
    val notes: String?,
)
