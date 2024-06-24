package com.thekeeperofpie.artistalleydatabase.alley.artist

import android.database.DatabaseUtils
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    suspend fun getEntry(id: String): ArtistEntry?

    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    fun getEntryFlow(id: String): Flow<ArtistEntry>

    @Transaction
    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    suspend fun getEntryWithStampRallies(id: String): ArtistWithStampRalliesEntry?

    @Query(
        """
        SELECT *
        FROM artist_entries
        JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
        ORDER BY artist_entries_fts.booth
        """
    )
    fun getEntries(): PagingSource<Int, ArtistEntry>

    @RawQuery([ArtistEntry::class])
    fun getEntries(query: SupportSQLiteQuery): PagingSource<Int, ArtistEntry>

    @Query(
        """
        SELECT *
        FROM artist_entries
        JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
        WHERE artist_entries_fts MATCH :query
        ORDER BY artist_entries_fts.id
    """
    )
    fun getEntries(query: String): PagingSource<Int, ArtistEntry>

    @Query(
        """
        SELECT COUNT(*)
        FROM artist_entries 
        """
    )
    fun getEntriesSize(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM artist_entries
        """
    )
    fun getEntriesSizeFlow(): Flow<Int>

    @Query(
        """
        SELECT booth
        from artist_entries
        """
    )
    fun getBooths(): List<String>

    fun search(query: String, filterOptions: ArtistSearchQuery): PagingSource<Int, ArtistEntry> {
        val booleanOptions = mutableListOf<String>().apply {
            if (filterOptions.showOnlyFavorites) this += "favorite:1"

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterOptions.showOnlyWithCatalog) this += "driveLink:*http*"
        }

        val filterOptionsQueryPieces = filterOptionsQuery(filterOptions)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "booth:$it",
                    "name:$it",
                    "summary:$it",
                    "seriesInferredSearchable:$it",
                    "seriesConfirmedSearchable:$it",
                    "merchInferredSearchable:$it",
                    "merchConfirmedSearchable:$it",
                )
            }

        val ascending = if (filterOptions.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY artist_entries_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (filterOptions.sortOption) {
            ArtistSearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistSearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "name")
            ArtistSearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
        }
        val selectSuffix = (", substr(artist_entries.counter * 0.${filterOptions.randomSeed}," +
                " length(artist_entries.counter) + 2) as orderIndex")
            .takeIf { filterOptions.sortOption == ArtistSearchSortOption.RANDOM }
            .orEmpty()

        val lockedSuffix = if (filterOptions.lockedSeries != null) {
            "artist_entries.id IN (SELECT artistId from artist_series_connections WHERE " +
                    (if (filterOptions.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND" else "") +
                    " artist_series_connections.seriesId == " +
                    "${DatabaseUtils.sqlEscapeString(filterOptions.lockedSeries)})"
        } else if (filterOptions.lockedMerch != null) {
            "artist_entries.id IN (SELECT artistId from artist_merch_connections WHERE " +
                    (if (filterOptions.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND" else "") +
                    " artist_merch_connections.merchId == " +
                    "${DatabaseUtils.sqlEscapeString(filterOptions.lockedMerch)})"
        } else {
            null
        }

        if (options.isEmpty() && filterOptionsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                ${if (lockedSuffix == null) "" else "WHERE $lockedSuffix"}
                """.trimIndent() + sortSuffix

            return getEntries(SimpleSQLiteQuery(statement))
        }

        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val booleanArguments = booleanOptions.joinToString(separator = " ")
        val separator = " ".takeIf {
            optionsArguments.isNotEmpty() && booleanArguments.isNotEmpty()
        }
        val bindArguments = (optionsArguments + separator + booleanArguments)
            .filterNot { it.isNullOrEmpty() }

        val statement = (bindArguments + filterOptionsQueryPieces).joinToString("\nINTERSECT\n") {
            """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                WHERE artist_entries_fts MATCH ?
                ${if (lockedSuffix == null) "" else "AND $lockedSuffix"}
                """.trimIndent()
        } + sortSuffix

        return getEntries(
            SimpleSQLiteQuery(
                statement,
                bindArguments.toTypedArray() + filterOptionsQueryPieces.toTypedArray(),
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtistEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ArtistEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesConnections(entries: List<ArtistSeriesConnection>)

    @Query("""DELETE FROM artist_series_connections""")
    fun clearSeriesConnections()

    @Query("""DELETE FROM artist_merch_connections""")
    fun clearMerchConnections()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchConnections(entries: List<ArtistMerchConnection>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedEntries(entries: Collection<Triple<ArtistEntry, List<ArtistSeriesConnection>, List<ArtistMerchConnection>>>) {
        val mergedEntries = entries.map { (entry, _) ->
            val existingEntry = getEntry(entry.id)
            entry.copy(
                favorite = existingEntry?.favorite ?: false,
                ignored = existingEntry?.ignored ?: false,
            )
        }

        insertEntries(mergedEntries)
        insertSeriesConnections(entries.flatMap { it.second })
        insertMerchConnections(entries.flatMap { it.third })
    }

    private fun filterOptionsQuery(filterOptions: ArtistSearchQuery): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterOptions.artist.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "artistNames:${RoomUtils.wrapMatchQuery(it)}" }
        }
        filterOptions.booth.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "booth:${RoomUtils.wrapMatchQuery(it)}" }
        }
        filterOptions.summary.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "description:${RoomUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterOptions.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map {
                if (filterOptions.showOnlyConfirmedTags) {
                    "seriesConfirmedSearchable:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "seriesInferredSearchable:${RoomUtils.wrapMatchQuery(it)}" +
                            " OR seriesConfirmedSearchable:${RoomUtils.wrapMatchQuery(it)}"
                }
            }
        queryPieces += filterOptions.seriesById
            .map {
                if (filterOptions.showOnlyConfirmedTags) {
                    "seriesConfirmedSerialized:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "seriesInferredSerialized:${RoomUtils.wrapMatchQuery(it)}" +
                            "OR seriesConfirmedSerialized:${RoomUtils.wrapMatchQuery(it)}"
                }
            }
        queryPieces += filterOptions.merch
            .map {
                if (filterOptions.showOnlyConfirmedTags) {
                    "merchConfirmed:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "merchInferred:${RoomUtils.wrapMatchQuery(it)}" +
                            " OR merchConfirmed:${RoomUtils.wrapMatchQuery(it)}"
                }
            }

        return queryPieces
    }
}
