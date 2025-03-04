package com.thekeeperofpie.artistalleydatabase.cds.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.BigDecimalSerializer
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
@Entity(tableName = "cd_entries")
data class CdEntry(
    @PrimaryKey
    val id: String = Uuid.random().toString(),
    val catalogId: String? = null,
    val titles: List<String> = emptyList(),
    val performers: List<String> = emptyList(),
    val performersSearchable: List<String> = emptyList(),
    val composers: List<String> = emptyList(),
    val composersSearchable: List<String> = emptyList(),
    val seriesSerialized: List<String> = emptyList(),
    val seriesSearchable: List<String> = emptyList(),
    val charactersSerialized: List<String> = emptyList(),
    val charactersSearchable: List<String> = emptyList(),
    /** Encoded list of [DiscEntry] */
    val discs: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal? = null,
    val lastEditTime: Instant? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val notes: String? = null,
    @Embedded val locks: Locks = Locks.EMPTY,
) {
    @delegate:Transient
    val entryId by lazy { EntryId(CdEntryUtils.SCOPED_ID_TYPE, id) }

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
        val catalogIdLocked: Boolean? = false,
        val titlesLocked: Boolean? = false,
        val performersLocked: Boolean? = false,
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
    val performers: List<String> = emptyList(),
    val performersSearchable: List<String> = emptyList(),
    val composers: List<String> = emptyList(),
    val composersSearchable: List<String> = emptyList(),
    val seriesSerialized: List<String>,
    val seriesSearchable: List<String>,
    val charactersSerialized: List<String>,
    val charactersSearchable: List<String>,
    /** Encoded list of [DiscEntry] */
    val discs: List<String> = emptyList(),
    val tags: List<String>,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal?,
    val lastEditTime: Instant?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val notes: String?,
    @Embedded val locks: CdEntry.Locks = CdEntry.Locks.EMPTY,
)
