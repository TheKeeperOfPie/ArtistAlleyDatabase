package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.toUpperCase
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
import com.thekeeperofpie.artistalleydatabase.JsonUtils
import com.thekeeperofpie.artistalleydatabase.search.SearchQueryWrapper
import kotlinx.coroutines.flow.Flow

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

    fun getEntries(query: SearchQueryWrapper): PagingSource<Int, ArtEntry> {
        val lockedValue = when {
            query.locked && query.unlocked -> null
            query.locked -> "1"
            query.unlocked -> "0"
            else -> null
        }

        val options = query.value.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map { queryValue ->
                mutableListOf<String>().apply {
                    if (query.includeArtists) this += "artists:$queryValue"
                    if (query.includeSources) this += "sourceType:$queryValue"
                    if (query.includeSources) this += "sourceValue:$queryValue"
                    if (query.includeSeries) this += "series:$queryValue"
                    if (query.includeCharacters) this += "characters:$queryValue"
                    if (query.includeTags) this += "tags:$queryValue"
                    if (query.includeNotes) this += "notes:$queryValue"
                }
            }

        if (options.isEmpty() && lockedValue == null) {
            return getEntries()
        }

        val lockOptions = if (lockedValue == null) emptyList() else {
            mutableListOf<String>().apply {
                if (query.includeArtists) this += "artistsLocked:$lockedValue"
                if (query.includeSources) this += "sourceLocked:$lockedValue"
                if (query.includeSeries) this += "seriesLocked:$lockedValue"
                if (query.includeCharacters) this += "charactersLocked:$lockedValue"
                if (query.includeTags) this += "tagsLocked:$lockedValue"
                if (query.includeNotes) this += "notesLocked:$lockedValue"
            }
        }

        val bindArguments = (options.ifEmpty { listOf(listOf("")) }).map {
            it.joinToString(separator = " OR ") + " " +
                    lockOptions.joinToString(separator = " ")
        }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM art_entries
                JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
                WHERE art_entries_fts MATCH ?
                """.trimIndent()
        }

        return getEntries(SimpleSQLiteQuery(statement, bindArguments.toTypedArray()))
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

    @Query(
        """
        SELECT COUNT(*)
        FROM art_entries
        """
    )
    fun getEntriesSize(): Int

    @Transaction
    fun iterateEntries(
        entriesSize: (Int) -> Unit,
        limit: Int = 50,
        block: (index: Int, entry: ArtEntry) -> Unit,
    ) {
        var offset = 0
        var index = 0
        entriesSize(getEntriesSize())
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

    private suspend fun queryListStringColumn(
        query: String,
        matchFunction: suspend (String) -> List<String>,
        likeFunction: suspend (String) -> List<String>,
    ): List<String> {
        val matchQuery = "'*$query*'"
        val likeQuery = wrapLikeQuery(query)
        return matchFunction(matchQuery)
            .plus(matchFunction(matchQuery.toLowerCase(Locale.current)))
            .plus(matchFunction(matchQuery.toUpperCase(Locale.current)))
            .plus(likeFunction(likeQuery))
            .plus(likeFunction(likeQuery.toLowerCase(Locale.current)))
            .plus(likeFunction(likeQuery.toUpperCase(Locale.current)))
            .flatMap(JsonUtils::readStringList)
            .distinct()
            .filter { it.toLowerCase(Locale.current).contains(query.toLowerCase(Locale.current)) }
    }

    suspend fun queryArtists(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryArtistsViaMatch(it, limit, offset) },
        { queryArtistsViaLike(it, limit, offset) }
    )

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

    suspend fun querySeries(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { querySeriesViaMatch(it, limit, offset) },
        { querySeriesViaLike(it, limit, offset) }
    )

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

    suspend fun queryCharacters(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryCharactersViaMatch(it, limit, offset) },
        { queryCharactersViaLike(it, limit, offset) }
    )

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

    suspend fun queryTags(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryTagsViaMatch(it, limit, offset) },
        { queryTagsViaLike(it, limit, offset) }
    )

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

    @Query(
        """
            SELECT DISTINCT (art_entries.artists)
            FROM art_entries
        """
    )
    fun getArtists(): Flow<List<String>>

    @Query(
        """
            SELECT sourceType
            FROM art_entries
        """
    )
    fun getSourceTypes(): Flow<List<String>>

    @Query(
        """
            SELECT sourceValue
            FROM art_entries
        """
    )
    fun getSourceValues(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.series)
            FROM art_entries
        """
    )
    fun getSeries(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.characters)
            FROM art_entries
        """
    )
    fun getCharacters(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.tags)
            FROM art_entries
        """
    )
    fun getTags(): Flow<List<String>>

    fun getArtist(query: String) = getArtistInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE artists LIKE :query
        """
    )
    fun getArtistInternal(query: String): PagingSource<Int, ArtEntry>

    fun getSeries(query: String) = getSeriesInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE series LIKE :query
        """
    )
    fun getSeriesInternal(query: String): PagingSource<Int, ArtEntry>

    fun getCharacter(query: String) = getCharacterInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE characters LIKE :query
        """
    )
    fun getCharacterInternal(query: String): PagingSource<Int, ArtEntry>

    fun getTag(query: String) = getTagInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE tags LIKE :query
        """
    )
    fun getTagInternal(query: String): PagingSource<Int, ArtEntry>

    private fun wrapLikeQuery(query: String) = "%${query.replace(Regex("\\s+"), "%")}%"
}