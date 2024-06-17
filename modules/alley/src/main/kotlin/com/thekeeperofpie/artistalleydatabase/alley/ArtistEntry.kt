package com.thekeeperofpie.artistalleydatabase.alley

import androidx.annotation.Discouraged
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.data.Series
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
    val name: String,
    val summary: String? = null,
    val links: List<String> = emptyList(),
    val store: String? = null,
    val catalog: String? = null,
    val favorite: Boolean = false,
    val ignored: Boolean = false,
    val notes: String? = null,
    @Discouraged("Use #seriesInferred(AppJson) instead")
    val seriesInferredSerialized: List<String> = emptyList(),
    val seriesInferredSearchable: List<String> = emptyList(),
    @Discouraged("Use #seriesConfirmed(AppJson) instead")
    val seriesConfirmedSerialized: List<String> = emptyList(),
    val seriesConfirmedSearchable: List<String> = emptyList(),
    val merchInferred: List<String> = emptyList(),
    val merchConfirmed: List<String> = emptyList(),
) {

    @Ignore
    @Transient
    @kotlinx.serialization.Transient
    private lateinit var _seriesInferred: List<Series>

    @Ignore
    @Transient
    @kotlinx.serialization.Transient
    private lateinit var _seriesConfirmed: List<Series>

    fun seriesInferred(appJson: AppJson): List<Series> {
        if (!::_seriesInferred.isInitialized) {
            _seriesInferred = Series.parse(appJson, seriesInferredSerialized)
        }

        return _seriesInferred
    }

    fun seriesConfirmed(appJson: AppJson): List<Series> {
        if (!::_seriesConfirmed.isInitialized) {
            _seriesConfirmed = Series.parse(appJson, seriesConfirmedSerialized)
        }

        return _seriesConfirmed
    }
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
    val store: String?,
    val catalog: String?,
    val favorite: Boolean,
    val ignored: Boolean,
    val notes: String?,
    val seriesInferredSerialized: List<String>,
    val seriesInferredSearchable: List<String>,
    val seriesConfirmedSerialized: List<String>,
    val seriesConfirmedSearchable: List<String>,
    val merchInferred: List<String>,
    val merchConfirmed: List<String>,
)
