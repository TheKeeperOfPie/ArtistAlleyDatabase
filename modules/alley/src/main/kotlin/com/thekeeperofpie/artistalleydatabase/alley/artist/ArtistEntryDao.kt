package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    suspend fun getEntry(id: String): ArtistEntry?

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
        val selectSuffix = (", substr(artist_entries.rowid * 0.${filterOptions.randomSeed}," +
                " length(artist_entries.rowid) + 2) as orderIndex")
            .takeIf { filterOptions.sortOption == ArtistSearchSortOption.RANDOM }
            .orEmpty()

        if (options.isEmpty() && filterOptionsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
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

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedEntries(entries: Collection<ArtistEntry>) {
        insertEntries(entries.map {
            val existingEntry = getEntry(it.id)
            it.copy(
                favorite = existingEntry?.favorite ?: false,
                ignored = existingEntry?.ignored ?: false,
            )
        })
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
                "seriesInferredSearchable:${RoomUtils.wrapMatchQuery(it)} OR "
                    .takeUnless { filterOptions.showOnlyWithCatalog } +
                        "seriesConfirmedSearchable:${RoomUtils.wrapMatchQuery(it)}"
            }
        queryPieces += filterOptions.seriesById
            .map {
                "seriesInferredSerialized:${RoomUtils.wrapMatchQuery(it)} OR "
                    .takeUnless { filterOptions.showOnlyWithCatalog } +
                        "seriesConfirmedSerialized:${RoomUtils.wrapMatchQuery(it)}"
            }
        queryPieces += filterOptions.merch
            .map {
                "merchInferred:${RoomUtils.wrapMatchQuery(it)} OR "
                    .takeUnless { filterOptions.showOnlyWithCatalog } +
                        "merchConfirmed:${RoomUtils.wrapMatchQuery(it)}"
            }

        return queryPieces
    }
}
