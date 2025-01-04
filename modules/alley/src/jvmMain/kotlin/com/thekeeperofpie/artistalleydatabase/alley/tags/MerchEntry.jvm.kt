package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "merch_entries")
actual data class MerchEntry actual constructor(
    @PrimaryKey
    actual val name: String,
    actual val notes: String?,
)

@Entity(tableName = "merch_entries_fts")
@Fts4(contentEntity = MerchEntry::class)
data class MerchEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val notes: String?,
)
