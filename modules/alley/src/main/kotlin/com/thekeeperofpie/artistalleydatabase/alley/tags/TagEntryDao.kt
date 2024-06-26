package com.thekeeperofpie.artistalleydatabase.alley.tags

import android.database.DatabaseUtils
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface TagEntryDao {

    @Query("""SELECT * FROM series_entries ORDER BY name COLLATE NOCASE""")
    fun getSeries(): PagingSource<Int, SeriesEntry>

    @RawQuery([SeriesEntry::class])
    fun getSeries(query: SupportSQLiteQuery): PagingSource<Int, SeriesEntry>

    @RawQuery([SeriesEntry::class])
    fun getSeriesBooths(query: SupportSQLiteQuery): List<String>

    fun searchSeries(query: String): PagingSource<Int, SeriesEntry> {
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

        return getSeries(SimpleSQLiteQuery(statement, bindArguments.toTypedArray()))
    }

    @RawQuery([MerchEntry::class])
    fun getMerch(query: SupportSQLiteQuery): PagingSource<Int, MerchEntry>

    @RawQuery([MerchEntry::class])
    fun getMerchBooths(query: SupportSQLiteQuery): List<String>

    @Query(
        """
        SELECT COUNT(*)
        FROM series_entries
        """
    )
    fun getSeriesSize(): Flow<Int>

    @Query("""SELECT * FROM merch_entries ORDER BY name COLLATE NOCASE""")
    fun getMerch(): PagingSource<Int, MerchEntry>

    fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
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

        return getMerch(SimpleSQLiteQuery(statement, bindArguments.toTypedArray()))
    }

    @Query(
        """
        SELECT COUNT(*)
        FROM merch_entries
        """
    )
    fun getMerchSize(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(entries: List<SeriesEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerch(entries: List<MerchEntry>)

    @Query("""DELETE FROM series_entries""")
    fun clearSeries()

    @Query("""DELETE FROM merch_entries""")
    fun clearMerch()

    fun getBooths(tagMapQuery: TagMapQuery): Set<String> {
        val tagQuery = if (tagMapQuery.series != null) {
            """
            (SELECT artistId from artist_series_connections WHERE
            ${if (tagMapQuery.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND " else ""}
            artist_series_connections.seriesId == ${DatabaseUtils.sqlEscapeString(tagMapQuery.series)}
            )
            """
        } else {
            """
            (SELECT artistId from artist_merch_connections WHERE
            ${if (tagMapQuery.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND " else ""}
            artist_merch_connections.merchId == ${DatabaseUtils.sqlEscapeString(tagMapQuery.merch)}
            )
            """
        }
        val query = SimpleSQLiteQuery(
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
