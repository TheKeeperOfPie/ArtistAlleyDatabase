package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merch_entries")
data class MerchEntry(
    @PrimaryKey
    val name: String,
    val notes: String?,
)
