package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils_room.RoomUtils

@Dao
interface StampRallyEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM stamp_rally_entries WHERE id = :id""")
    suspend fun getEntry(id: String): StampRallyEntry?

    @Transaction
    @Query("""SELECT * FROM stamp_rally_entries WHERE id = :id""")
    suspend fun getEntryWithArtists(id: String): StampRallyWithArtistsEntry?

    @Query(
        """
        SELECT *
        FROM stamp_rally_entries
        JOIN stamp_rally_entries_fts ON stamp_rally_entries.id = stamp_rally_entries_fts.id
        ORDER BY stamp_rally_entries_fts.hostTable
        """
    )
    fun getEntries(): PagingSource<Int, StampRallyEntry>

    @RawQuery([StampRallyEntry::class])
    fun getEntries(query: SupportSQLiteQuery): PagingSource<Int, StampRallyEntry>

    @Query(
        """
        SELECT *
        FROM stamp_rally_entries
        JOIN stamp_rally_entries_fts ON stamp_rally_entries.id = stamp_rally_entries_fts.id
        WHERE stamp_rally_entries_fts MATCH :query
        ORDER BY stamp_rally_entries_fts.id
    """
    )
    fun getEntries(query: String): PagingSource<Int, StampRallyEntry>

    @Query(
        """
        SELECT COUNT(*)
        FROM stamp_rally_entries 
        """
    )
    fun getEntriesSize(): Int

    fun search(
        query: String,
        searchQuery: StampRallySearchQuery,
    ): PagingSource<Int, StampRallyEntry> {
        val filterParams = searchQuery.filterParams
        val booleanOptions = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "favorite:1"
        }

        val filterParamsQueryPieces = filterParamsQuery(filterParams)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "fandom:$it",
                    "tables:$it",
                )
            }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY stamp_rally_entries_fts.FIELD $ascending"
        val sortSuffix = when (filterParams.sortOption) {
            StampRallySearchSortOption.MAIN_TABLE -> basicSortSuffix.replace("FIELD", "hostTable COLLATE NOCASE")
            StampRallySearchSortOption.FANDOM -> basicSortSuffix.replace("FIELD", "fandom COLLATE NOCASE")
            StampRallySearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
            StampRallySearchSortOption.PRIZE_LIMIT -> basicSortSuffix.replace("FIELD", "prizeLimit") + " NULLS LAST"
            StampRallySearchSortOption.TOTAL_COST -> basicSortSuffix.replace("FIELD", "totalCost") + " NULLS LAST"
        }
        val selectSuffix =
            (", substr(stamp_rally_entries.counter * 0.${searchQuery.randomSeed}," +
                    " length(stamp_rally_entries.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
                .orEmpty()

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM stamp_rally_entries
                JOIN stamp_rally_entries_fts ON stamp_rally_entries.id = stamp_rally_entries_fts.id
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
        val statement = (bindArguments + filterParamsQueryPieces).joinToString("\nINTERSECT\n") {
            """
                SELECT *$selectSuffix
                FROM stamp_rally_entries
                JOIN stamp_rally_entries_fts ON stamp_rally_entries.id = stamp_rally_entries_fts.id
                WHERE stamp_rally_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return getEntries(
            SimpleSQLiteQuery(
                statement,
                bindArguments.toTypedArray() + filterParamsQueryPieces.toTypedArray(),
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: StampRallyEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<StampRallyEntry>)

    @Query("DELETE FROM stamp_rally_entries")
    suspend fun clearEntries()

    @Query("DELETE FROM stamp_rally_artist_connections")
    suspend fun clearConnections()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(entries: List<StampRallyArtistConnection>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedEntries(entries: Collection<Pair<StampRallyEntry, List<StampRallyArtistConnection>>>) {
        val mergedEntries = entries.map { (entry, _) ->
            val existingEntry = getEntry(entry.id)
            entry.copy(
                favorite = existingEntry?.favorite ?: false,
                ignored = existingEntry?.ignored ?: false,
            )
        }

        insertEntries(mergedEntries)
        insertConnections(entries.flatMap { it.second })
    }

    @Query("""DELETE FROM stamp_rally_entries WHERE stamp_rally_entries.id NOT IN (:ids)""")
    suspend fun retainIds(ids: List<String>)

    private fun filterParamsQuery(
        filterParams: StampRallySortFilterViewModel.FilterParams,
    ): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterParams.fandom.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "fandom:${RoomUtils.wrapMatchQuery(it)}" }
        }
        filterParams.tables.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "tables:${RoomUtils.wrapMatchQuery(it)}" }
        }

        return queryPieces
    }
}
