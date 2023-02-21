package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.annotation.Discouraged
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Serializable
@JsonClass(generateAdapter = true)
@Entity(tableName = "art_entries")
data class ArtEntry(
    @Discouraged("Prefer entryId")
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val artists: List<String> = emptyList(),
    val sourceType: String? = null,
    val sourceValue: String? = null,
    @Discouraged("Use #series(AppJson) instead")
    val seriesSerialized: List<String> = emptyList(),
    val seriesSearchable: List<String> = emptyList(),
    @Discouraged("Use #characters(AppJson) instead")
    val charactersSerialized: List<String> = emptyList(),
    val charactersSearchable: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @Serializable(with = Converters.BigDecimalConverter::class)
    val price: BigDecimal? = null,
    @Serializable(with = Converters.DateConverter::class)
    val date: Date? = null,
    @Serializable(with = Converters.DateConverter::class)
    val lastEditTime: Date? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val printWidth: Int? = null,
    val printHeight: Int? = null,
    val notes: String? = null,
    @Embedded val locks: Locks = Locks.EMPTY,
) {
    @delegate:Transient
    val entryId by lazy { EntryId(ArtEntryUtils.SCOPED_ID_TYPE, id) }

    @delegate:Transient
    val imageWidthToHeightRatio by lazy {
        (imageHeight?.toFloat() ?: 1f) /
                (imageWidth ?: 1).coerceAtLeast(1)
    }

    @Ignore
    @Transient
    @kotlinx.serialization.Transient
    private lateinit var _series: List<Series>

    @Ignore
    @Transient
    @kotlinx.serialization.Transient
    private lateinit var _characters: List<Character>

    fun series(appJson: AppJson): List<Series> {
        if (!::_series.isInitialized) {
            _series = Series.parse(appJson, seriesSerialized)
        }

        return _series
    }

    fun characters(appJson: AppJson): List<Character> {
        if (!::_characters.isInitialized) {
            _characters = Character.parse(appJson, charactersSerialized)
        }

        return _characters
    }

    @Serializable
    @JsonClass(generateAdapter = true)
    data class Locks(
        val artistsLocked: Boolean? = false,
        val sourceLocked: Boolean? = false,
        val seriesLocked: Boolean? = false,
        val charactersLocked: Boolean? = false,
        val tagsLocked: Boolean? = false,
        val notesLocked: Boolean? = false,
        val printSizeLocked: Boolean? = false,
    ) {
        companion object {
            val EMPTY = Locks()
        }

        constructor(locked: Boolean?) : this(
            artistsLocked = locked,
            sourceLocked = locked,
            seriesLocked = locked,
            charactersLocked = locked,
            tagsLocked = locked,
            notesLocked = locked,
            printSizeLocked = locked,
        )
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
    val seriesSerialized: List<String>,
    val seriesSearchable: List<String>,
    val charactersSerialized: List<String>,
    val charactersSearchable: List<String>,
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