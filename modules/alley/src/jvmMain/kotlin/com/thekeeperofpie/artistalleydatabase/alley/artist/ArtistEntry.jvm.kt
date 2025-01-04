package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "artist_entries")
actual data class ArtistEntry actual constructor(
    @PrimaryKey
    actual val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    actual val booth: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    actual val name: String,
    actual val summary: String?,
    actual val links: List<String>,
    @ColumnInfo(defaultValue = "")
    actual val storeLinks: List<String>,
    @ColumnInfo(defaultValue = "")
    actual val catalogLinks: List<String>,
    actual val driveLink: String?,
    actual val favorite: Boolean,
    actual val ignored: Boolean,
    actual val notes: String?,
    actual val seriesInferred: List<String>,
    actual val seriesConfirmed: List<String>,
    actual val merchInferred: List<String>,
    actual val merchConfirmed: List<String>,
    // Used fo random ordering while maintaining a stable key
    @ColumnInfo(defaultValue = "1")
    actual val counter: Int,
) {
    actual fun copy(
        id: String,
        booth: String,
        name: String,
        summary: String?,
        links: List<String>,
        storeLinks: List<String>,
        catalogLinks: List<String>,
        driveLink: String?,
        favorite: Boolean,
        ignored: Boolean,
        notes: String?,
        seriesInferred: List<String>,
        seriesConfirmed: List<String>,
        merchInferred: List<String>,
        merchConfirmed: List<String>,
    ) = copy(
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        favorite = favorite,
        ignored = ignored,
        notes = notes,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        counter = this.counter,
    )
}

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
    val name: String,
    val summary: String?,
    val links: List<String>,
    val storeLinks: List<String>,
    val catalogLinks: List<String>,
    val driveLink: String?,
    val favorite: Boolean,
    val ignored: Boolean,
    val notes: String?,
    val seriesInferred: List<String>,
    val seriesConfirmed: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
)
