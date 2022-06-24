package com.thekeeperofpie.artistalleydatabase.art

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel

@Dao
interface ArtEntryDao {

    @Query("""SELECT * FROM art_entries WHERE art_entries.id = :id LIMIT 1""")
    suspend fun getEntry(id: String): ArtEntry

    @RawQuery([ArtEntry::class])
    fun getEntries(query: SupportSQLiteQuery): PagingSource<Int, ArtEntry>

    @Query(
        """
        SELECT *
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts MATCH :query
    """
    )
    fun getEntries(query: String): PagingSource<Int, ArtEntry>

    @Query(
        """
        SELECT *
        FROM art_entries
        ORDER BY lastEditTime DESC
        """
    )
    fun getEntries(): PagingSource<Int, ArtEntry>

    fun getEntries(query: SearchViewModel.QueryWrapper): PagingSource<Int, ArtEntry> {
        val options = mutableListOf<String>()

        val lockedValue = when {
            query.locked && query.unlocked -> null
            query.locked -> "1"
            query.unlocked -> "0"
            else -> null
        }

        val optionsSuffix = if (lockedValue == null) "" else {
            val lockOptions = mutableListOf<String>()
            if (query.includeArtists) lockOptions += "artistsLocked:$lockedValue"
            if (query.includeSources) lockOptions += "sourceLocked:$lockedValue"
            if (query.includeSeries) lockOptions += "seriesLocked:$lockedValue"
            if (query.includeCharacters) lockOptions += "charactersLocked:$lockedValue"
            if (query.includeTags) lockOptions += "tagsLocked:$lockedValue"
            if (query.includeNotes) lockOptions += "notesLocked:$lockedValue"
            lockOptions.joinToString(prefix = " ", separator = " ")
        }

        val queryValue = "*${query.value}*"
        if (query.includeArtists) options += "artists:$queryValue"
        if (query.includeSources) options += "sourceType:$queryValue"
        if (query.includeSources) options += "sourceValue:$queryValue"
        if (query.includeSeries) options += "series:$queryValue"
        if (query.includeCharacters) options += "characters:$queryValue"
        if (query.includeTags) options += "tags:$queryValue"
        if (query.includeNotes) options += "notes:$queryValue"

        val finalQuery = options.joinToString(separator = " OR ") + optionsSuffix
        val statement = """
            SELECT *
            FROM art_entries
            JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
            WHERE art_entries_fts MATCH '$finalQuery'
        """.trimIndent()
        return getEntries(SimpleSQLiteQuery(statement))
    }

    @Query(
        """
        SELECT *
        FROM art_entries
        LIMIT :limit
        OFFSET :offset
        """
    )
    fun getEntries(limit: Int = 50, offset: Int = 0): List<ArtEntry>

    @Transaction
    fun iterateEntries(limit: Int = 50, block: (index: Int, entry: ArtEntry) -> Unit) {
        var offset = 0
        var index = 0
        var entries = getEntries(limit = limit, offset = offset)
        while (entries.isNotEmpty()) {
            offset += entries.size
            entries.forEach {
                block(index++, it)
            }
            entries = getEntries(limit = limit, offset = offset)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtEntry)

    @Transaction
    suspend fun insertEntriesDeferred(block: suspend (insert: suspend (ArtEntry) -> Unit) -> Unit) {
        block { insertEntries(it) }
    }

    @Delete
    suspend fun delete(entry: ArtEntry) = delete(entry.id)

    @Delete
    suspend fun delete(entries: Collection<ArtEntry>)

    @Query("DELETE FROM art_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        SELECT DISTINCT (art_entries.artists)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.artists MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryArtistsViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.artists)
        FROM art_entries
        WHERE art_entries.artists LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryArtistsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryArtists(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryArtistsViaMatch("'*$query*'", limit, offset)
        .plus(queryArtistsViaLike("'%$query%'", limit, offset))
        .distinct()

    @Query(
        """
        SELECT DISTINCT (art_entries.series)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.series MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun querySeriesViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.series)
        FROM art_entries
        WHERE art_entries.series LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun querySeriesViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun querySeries(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = querySeriesViaMatch("'*$query*'", limit, offset)
        .plus(querySeriesViaLike("'%$query%'", limit, offset))
        .distinct()

    @Query(
        """
        SELECT DISTINCT (art_entries.characters)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.characters MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryCharactersViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.characters)
        FROM art_entries
        WHERE art_entries.characters LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryCharactersViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryCharacters(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryCharactersViaMatch("'*$query*'", limit, offset)
        .plus(queryCharactersViaLike("'%$query%'", limit, offset))
        .distinct()

    @Query(
        """
        SELECT DISTINCT (art_entries.tags)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.tags MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryTagsViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.tags)
        FROM art_entries
        WHERE art_entries.tags LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryTagsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryTags(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryTagsViaMatch("'*$query*'", limit, offset)
        .plus(queryTagsViaLike("'%$query%'", limit, offset))
        .distinct()
}