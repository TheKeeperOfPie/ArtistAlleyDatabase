package com.thekeeperofpie.artistalleydatabase.cds

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Serializable
@JsonClass(generateAdapter = true)
@Entity(tableName = "cd_entries")
data class CdEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val catalogId: String? = null,
    val titles: List<String> = emptyList(),
    val vocalists: List<String> = emptyList(),
    val vocalistsSearchable: List<String> = emptyList(),
    val composers: List<String> = emptyList(),
    val composersSearchable: List<String> = emptyList(),
    val series: List<String> = emptyList(),
    val seriesSearchable: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val charactersSearchable: List<String> = emptyList(),
    /** Encoded list of [DiscEntry] */
    val discs: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @Serializable(with = Converters.BigDecimalConverter::class)
    val price: BigDecimal? = null,
    @Serializable(with = Converters.DateConverter::class)
    val date: Date? = null,
    @Serializable(with = Converters.DateConverter::class)
    val lastEditTime: Date? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val notes: String? = null,
    @Embedded val locks: Locks = Locks.EMPTY,
) {
    @delegate:Transient
    val imageWidthToHeightRatio by lazy {
        (imageHeight?.toFloat() ?: 1f) /
                (imageWidth ?: 1).coerceAtLeast(1)
    }

    @Serializable
    @JsonClass(generateAdapter = true)
    data class Locks(
        val catalogIdLocked: Boolean? = false,
        val titlesLocked: Boolean? = false,
        val vocalistsLocked: Boolean? = false,
        val composersLocked: Boolean? = false,
        val seriesLocked: Boolean? = false,
        val charactersLocked: Boolean? = false,
        val discsLocked: Boolean? = false,
        val tagsLocked: Boolean? = false,
        val priceLocked: Boolean? = false,
        val notesLocked: Boolean? = false,
    ) {
        companion object {
            val EMPTY = Locks()
        }
    }
}

@Fts4(contentEntity = CdEntry::class)
@Entity(tableName = "cd_entries_fts")
data class CdEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String,
    val catalogId: String?,
    val titles: List<String>,
    val vocalists: List<String> = emptyList(),
    val vocalistsSearchable: List<String> = emptyList(),
    val composers: List<String> = emptyList(),
    val composersSearchable: List<String> = emptyList(),
    val series: List<String>,
    val seriesSearchable: List<String>,
    val characters: List<String>,
    val charactersSearchable: List<String>,
    /** Encoded list of [DiscEntry] */
    val discs: List<String> = emptyList(),
    val tags: List<String>,
    @Serializable(with = Converters.BigDecimalConverter::class)
    val price: BigDecimal?,
    @Serializable(with = Converters.DateConverter::class)
    val date: Date?,
    @Serializable(with = Converters.DateConverter::class)
    val lastEditTime: Date?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val notes: String?,
    @Embedded val locks: CdEntry.Locks = CdEntry.Locks.EMPTY,
)