package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "stamp_rally_entries")
actual data class StampRallyEntry actual constructor(
    @PrimaryKey
    actual val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    actual val fandom: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    actual val hostTable: String,
    actual val tables: List<String>,
    actual val links: List<String>,
    actual val tableMin: Int?,
    actual val totalCost: Int?,
    actual val prizeLimit: Int?,
    actual val favorite: Boolean,
    actual val ignored: Boolean,
    actual val notes: String?,
    // Used fo random ordering while maintaining a stable key
    @ColumnInfo(defaultValue = "1")
    actual val counter: Int,
) {
    actual fun copy(
        id: String,
        fandom: String,
        hostTable: String,
        tables: List<String>,
        links: List<String>,
        tableMin: Int?,
        totalCost: Int?,
        prizeLimit: Int?,
        favorite: Boolean,
        ignored: Boolean,
        notes: String?,
    ) = copy(
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prizeLimit = prizeLimit,
        favorite = favorite,
        ignored = ignored,
        notes = notes,
        counter = this.counter,
    )
}

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
