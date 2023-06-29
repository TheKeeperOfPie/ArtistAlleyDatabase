package com.thekeeperofpie.artistalleydatabase.alley

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
@Entity(tableName = "artist_entries")
data class ArtistEntry(
    @PrimaryKey
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val booth: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val tableName: String? = null,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val artistNames: List<String>,
    val region: String? = null,
    val description: String? = null,
    val contactLink: String? = null,
    val links: List<String> = emptyList(),
    val catalogLink: List<String> = emptyList(),
    val favorite: Boolean = false,
    val notes: String? = null,
)

@Entity(tableName = "artist_entries_fts")
@Fts4(contentEntity = ArtistEntry::class)
data class ArtistEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val booth: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val tableName: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val artistNames: List<String>,
    val region: String?,
    val description: String,
    val contactLink: String,
    val links: List<String>,
    val catalogLink: List<String>,
    val favorite: Boolean,
    val notes: String?,
)
