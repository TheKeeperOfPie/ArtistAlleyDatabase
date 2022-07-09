package com.thekeeperofpie.artistalleydatabase.art

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@JsonClass(generateAdapter = true)
@Entity(tableName = "art_entries")
data class ArtEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val artists: List<String> = emptyList(),
    val sourceType: String? = null,
    val sourceValue: String? = null,
    val series: List<String> = emptyList(),
    val seriesSearchable: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val charactersSearchable: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val price: BigDecimal? = null,
    val date: Date? = null,
    val lastEditTime: Date? = null,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String? = null,
    @Embedded val locks: Locks = Locks.EMPTY,
) {
    val imageWidthToHeightRatio by lazy {
        (imageHeight?.toFloat() ?: 1f) /
                (imageWidth ?: 1).coerceAtLeast(1)
    }

    @JsonClass(generateAdapter = true)
    data class Locks(
        val artistsLocked: Boolean = false,
        val sourceLocked: Boolean = false,
        val seriesLocked: Boolean = false,
        val charactersLocked: Boolean = false,
        val tagsLocked: Boolean = false,
        val notesLocked: Boolean = false,
        val printSizeLocked: Boolean = false,
    ) {
        companion object {
            val EMPTY = Locks()
        }
    }
}

@Entity(tableName = "art_entries_fts")
@Fts4(contentEntity = ArtEntry::class)
data class ArtEntryFts(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int? = null,
    val id: String,
    val artists: List<String>,
    val sourceType: String?,
    val sourceValue: String?,
    val series: List<String>,
    val characters: List<String>,
    val tags: List<String>,
    val price: BigDecimal?,
    val date: Date?,
    val lastEditTime: Date?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String?,
    @Embedded val locks: ArtEntry.Locks = ArtEntry.Locks.EMPTY,
)