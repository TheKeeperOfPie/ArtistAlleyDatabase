package com.thekeeperofpie.artistalleydatabase.alley

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.search.ArtistAlleySearchSortOption
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
            if (filterOptions.showOnlyWithCatalog) this += "catalogLink:*drive*"
        }

        val filterOptionsQueryPieces = filterOptionsQuery(filterOptions)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "booth:$it",
                    "artistNames:$it",
                    "description:$it",
                    // TODO
//                    "seriesSearchable:$it",
                )
            }

        val ascending = if (filterOptions.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY artist_entries_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (filterOptions.sortOption) {
            ArtistAlleySearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistAlleySearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "artist")
            ArtistAlleySearchSortOption.RANDOM -> {
                "\nORDER BY substr(hex(artist_entries.id) * 0.${filterOptions.randomSeed}," +
                        " length(hex(artist_entries.id)) + 2) $ascending"
            }
        }
        if (options.isEmpty() && filterOptionsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                """.trimIndent() + sortSuffix

            return getEntries(SimpleSQLiteQuery(statement))
        }

        val bindArguments = (options.ifEmpty { listOf(listOf("")) }).map {
            it.joinToString(separator = " OR ") + " " +
                    booleanOptions.joinToString(separator = " ")
        }
        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                WHERE artist_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        Log.d("DataDebug", "filterOptions = $filterOptions")
        Log.d("DataDebug", "sortSuffix = $sortSuffix")

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
                notes = existingEntry?.notes,
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
        filterOptions.description.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "description:${RoomUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterOptions.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "seriesSearchable:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.seriesById
            .map { "seriesSerialized:${RoomUtils.wrapMatchQuery(it)}" }

        return queryPieces
    }
}
