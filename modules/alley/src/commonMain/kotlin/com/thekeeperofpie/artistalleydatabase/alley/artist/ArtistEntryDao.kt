package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import com.thekeeperofpie.artistalleydatabase.alley.ArtistBoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.utils_room.RoomUtils
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
    fun getEntries(query: RoomRawQuery): PagingSource<Int, ArtistEntry>

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
    suspend fun getEntriesSize(): Int

    @Query(
        """
        SELECT booth, favorite
        from artist_entries
        """
    )
    fun getBoothsWithFavorite(): Flow<List<ArtistBoothWithFavorite>>

    fun search(query: String, searchQuery: ArtistSearchQuery): PagingSource<Int, ArtistEntry> {
        val filterParams = searchQuery.filterParams
        val booleanOptions = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "favorite:1"

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterParams.showOnlyWithCatalog) this += "driveLink:*http*"
        }

        val filterParamsQueryPieces = filterParamsQuery(filterParams)
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

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY artist_entries_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistSearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "name")
            ArtistSearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
        }
        val selectSuffix = (", substr(artist_entries.counter * 0.${searchQuery.randomSeed}," +
                " length(artist_entries.counter) + 2) as orderIndex")
            .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
            .orEmpty()

        val lockedSuffix = if (searchQuery.lockedSeries != null) {
            "artist_entries.id IN (SELECT artistId from artist_series_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND" else "") +
                    " artist_series_connections.seriesId == " +
                    "${RoomUtils.sqlEscapeString(searchQuery.lockedSeries)})"
        } else if (searchQuery.lockedMerch != null) {
            "artist_entries.id IN (SELECT artistId from artist_merch_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND" else "") +
                    " artist_merch_connections.merchId == " +
                    "${RoomUtils.sqlEscapeString(searchQuery.lockedMerch)})"
        } else {
            null
        }

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                ${if (lockedSuffix == null) "" else "WHERE $lockedSuffix"}
                """.trimIndent() + sortSuffix

            return getEntries(RoomRawQuery(statement))
        }

        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val booleanArguments = booleanOptions.joinToString(separator = " ")
        val separator = " ".takeIf {
            optionsArguments.isNotEmpty() && booleanArguments.isNotEmpty()
        }
        val bindArguments = (optionsArguments + separator + booleanArguments)
            .filterNotNull()
            .filter { it.isNotEmpty() }

        val statement = (bindArguments + filterParamsQueryPieces).joinToString("\nINTERSECT\n") {
            """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                WHERE artist_entries_fts MATCH ?
                ${if (lockedSuffix == null) "" else "AND $lockedSuffix"}
                """.trimIndent()
        } + sortSuffix

        return getEntries(RoomRawQuery(statement) {
            (bindArguments + filterParamsQueryPieces)
                .forEachIndexed { index, arg ->
                    it.bindText(index + 1, arg)
                }
        })
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtistEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ArtistEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesConnections(entries: List<ArtistSeriesConnection>)

    @Query("""DELETE FROM artist_series_connections""")
    suspend fun clearSeriesConnections()

    @Query("""DELETE FROM artist_merch_connections""")
    suspend fun clearMerchConnections()

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

    private fun filterParamsQuery(
        filterParams: ArtistSortFilterViewModel.FilterParams,
    ): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterParams.artist.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "artistNames:${RoomUtils.wrapMatchQuery(it)}" }
        }
        filterParams.booth.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "booth:${RoomUtils.wrapMatchQuery(it)}" }
        }
        filterParams.summary.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "description:${RoomUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterParams.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map {
                if (filterParams.showOnlyConfirmedTags) {
                    "seriesConfirmedSearchable:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "seriesInferredSearchable:${RoomUtils.wrapMatchQuery(it)}" +
                            " OR seriesConfirmedSearchable:${RoomUtils.wrapMatchQuery(it)}"
                }
            }
        queryPieces += filterParams.seriesById
            .map {
                if (filterParams.showOnlyConfirmedTags) {
                    "seriesConfirmedSerialized:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "seriesInferredSerialized:${RoomUtils.wrapMatchQuery(it)}" +
                            "OR seriesConfirmedSerialized:${RoomUtils.wrapMatchQuery(it)}"
                }
            }
        queryPieces += filterParams.merch
            .map {
                if (filterParams.showOnlyConfirmedTags) {
                    "merchConfirmed:${RoomUtils.wrapMatchQuery(it)}"
                } else {
                    "merchInferred:${RoomUtils.wrapMatchQuery(it)}" +
                            " OR merchConfirmed:${RoomUtils.wrapMatchQuery(it)}"
                }
            }

        return queryPieces
    }
}
