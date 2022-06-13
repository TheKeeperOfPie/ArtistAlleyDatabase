package com.thekeeperofpie.artistalleydatabase.art

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Entity(tableName = "art_entries")
data class ArtEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val artists: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val series: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val price: BigDecimal? = null,
    val date: Date? = null,
)

@Entity(tableName = "art_entries_fts")
@Fts4(contentEntity = ArtEntry::class)
data class ArtEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String = UUID.randomUUID().toString(),
    val artists: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val series: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val price: BigDecimal? = null,
    val date: Date? = null,
)