package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.annotation.Discouraged
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.BigDecimalSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@kotlin.OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Serializable
@Entity(tableName = "art_entries")
data class ArtEntry(
    @Discouraged("Prefer entryId")
    @PrimaryKey
    val id: String = Uuid.random().toString(),
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
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal? = null,
    val lastEditTime: Instant? = null,
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

    fun series(json: Json): List<Series> {
        if (!::_series.isInitialized) {
            _series = Series.parse(json, seriesSerialized)
        }

        return _series
    }

    fun characters(json: Json): List<Character> {
        if (!::_characters.isInitialized) {
            _characters = Character.parse(json, charactersSerialized)
        }

        return _characters
    }

    @Serializable
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
    val lastEditTime: Instant?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String?,
    @Embedded val locks: ArtEntry.Locks = ArtEntry.Locks.EMPTY,
)
