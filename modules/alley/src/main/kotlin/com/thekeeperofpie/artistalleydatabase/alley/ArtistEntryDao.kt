package com.thekeeperofpie.artistalleydatabase.alley

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.alley.search.ArtistAlleySearchSortOption
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistEntryDao {

    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    suspend fun getEntry(id: String): ArtistEntry?

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

    fun getEntries(
        query: ArtistSearchQuery,
        sort: ArtistAlleySearchSortOption,
        sortAscending: Boolean,
        showOnlyFavorites: Boolean,
        showOnlyWithCatalog: Boolean,
        randomSeed: Int,
    ): PagingSource<Int, ArtistEntry> {
        val includeAll = query.includeAll
        val options = query.query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map { queryValue ->
                mutableListOf<String>().apply {
                    if (includeAll || query.includeBooth) this += "booth:$queryValue"
                    if (includeAll || query.includeTableName) this += "tableName:$queryValue"
                    if (includeAll || query.includeArtistNames) this += "artistNames:$queryValue"
                    if (includeAll || query.includeRegion) this += "region:$queryValue"
                    if (includeAll || query.includeDescription) this += "description:$queryValue"
                }
            }

        val booleanOptions = mutableListOf<String>().apply {
            if (showOnlyFavorites) this += "favorite:1"
            if (showOnlyWithCatalog) this += "catalogLink:*drive*"
        }

        val ascending = if (sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY artist_entries_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (sort) {
            ArtistAlleySearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistAlleySearchSortOption.TABLE -> basicSortSuffix.replace("FIELD", "tableName")
            ArtistAlleySearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "artistNames")
            ArtistAlleySearchSortOption.RANDOM -> {
                "\nORDER BY substr(hex(artist_entries.id) * 0.$randomSeed, length(hex(artist_entries.id)) + 2) $ascending"
            }
        }
        if (options.isEmpty() && booleanOptions.isEmpty()) {
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

        return getEntries(SimpleSQLiteQuery(statement, bindArguments.toTypedArray()))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtistEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: Collection<ArtistEntry>)
}
