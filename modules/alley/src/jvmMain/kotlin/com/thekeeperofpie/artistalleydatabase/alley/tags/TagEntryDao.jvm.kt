package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import com.thekeeperofpie.artistalleydatabase.utils_room.RoomUtils
import kotlinx.coroutines.flow.Flow

@Dao
actual interface TagEntryDao {

    @Query("""SELECT * FROM series_entries ORDER BY name COLLATE NOCASE""")
    actual fun getSeries(): PagingSource<Int, SeriesEntry>

    @RawQuery([SeriesEntry::class])
    fun getSeries(query: RoomRawQuery): PagingSource<Int, SeriesEntry>

    @RawQuery([SeriesEntry::class])
    suspend fun getSeriesBooths(query: RoomRawQuery): List<String>

    actual fun searchSeries(query: String): PagingSource<Int, SeriesEntry> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY series_entries_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM series_entries
                JOIN series_entries_fts ON series_entries.name = series_entries_fts.name
                WHERE series_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return getSeries(RoomRawQuery(statement) {
            bindArguments.forEachIndexed { index, arg ->
                it.bindText(index + 1, arg)
            }
        })
    }

    @RawQuery([MerchEntry::class])
    fun getMerch(query: RoomRawQuery): PagingSource<Int, MerchEntry>

    @RawQuery([MerchEntry::class])
    suspend fun getMerchBooths(query: RoomRawQuery): List<String>

    @Query(
        """
        SELECT COUNT(*)
        FROM series_entries
        """
    )
    actual fun getSeriesSize(): Flow<Int>

    @Query("""SELECT * FROM merch_entries ORDER BY name COLLATE NOCASE""")
    actual fun getMerch(): PagingSource<Int, MerchEntry>

    actual fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY merch_entries_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM merch_entries
                JOIN merch_entries_fts ON merch_entries.name = merch_entries_fts.name
                WHERE merch_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix


        return getMerch(RoomRawQuery(statement) {
            bindArguments.forEachIndexed { index, arg ->
                it.bindText(index + 1, arg)
            }
        })
    }

    @Query(
        """
        SELECT COUNT(*)
        FROM merch_entries
        """
    )
    actual fun getMerchSize(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    actual suspend fun insertSeries(entries: List<SeriesEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    actual suspend fun insertMerch(entries: List<MerchEntry>)

    @Query("""DELETE FROM series_entries""")
    actual suspend fun clearSeries()

    @Query("""DELETE FROM merch_entries""")
    actual suspend fun clearMerch()

    actual suspend fun getBooths(tagMapQuery: TagMapQuery): Set<String> {
        val tagQuery = if (tagMapQuery.series != null) {
            """
            (SELECT artistId from artist_series_connections WHERE
            ${if (tagMapQuery.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND " else ""}
            artist_series_connections.seriesId == ${RoomUtils.sqlEscapeString(tagMapQuery.series)}
            )
            """
        } else {
            """
            (SELECT artistId from artist_merch_connections WHERE
            ${if (tagMapQuery.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND " else ""}
            artist_merch_connections.merchId == ${RoomUtils.sqlEscapeString(tagMapQuery.merch!!)}
            )
            """
        }
        val query = RoomRawQuery(
            """
            SELECT booth
            FROM artist_entries
            WHERE artist_entries.id IN $tagQuery
            """.trimIndent()
        )

        return if (tagMapQuery.series != null) {
            getSeriesBooths(query)
        } else {
            getMerchBooths(query)
        }.toSet()
    }
}
