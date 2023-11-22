package com.thekeeperofpie.artistalleydatabase.art.data

import android.util.Log
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
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils.toBit
import com.thekeeperofpie.artistalleydatabase.art.search.ArtAdvancedSearchQuery
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.yield

@Dao
interface ArtEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM art_entries WHERE id = :id""")
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

    @Query(
        """
        SELECT *
        FROM art_entries
        LIMIT :limit
        OFFSET :offset
        """
    )
    suspend fun getEntries(limit: Int = 50, offset: Int = 0): List<ArtEntry>

    @Query(
        """
        SELECT COUNT(*)
        FROM art_entries
        """
    )
    fun getEntriesSize(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM art_entries
        """
    )
    fun getEntriesSizeFlow(): Flow<Int>

    @Transaction
    suspend fun iterateEntries(
        entriesSize: (Int) -> Unit,
        limit: Int = 50,
        block: suspend (index: Int, entry: ArtEntry) -> Unit,
    ) {
        var offset = 0
        var index = 0
        entriesSize(getEntriesSize())
        var entries = getEntries(limit = limit, offset = offset)
        while (entries.isNotEmpty()) {
            offset += entries.size
            entries.forEach {
                block(index++, it)
                yield()
            }
            entries = getEntries(limit = limit, offset = offset)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: Collection<ArtEntry>)

    suspend fun insertEntriesDeferred(
        dryRun: Boolean,
        replaceAll: Boolean,
        block: suspend (insertEntry: suspend (ArtEntry) -> Unit) -> Unit,
    ) {
        if (!dryRun && replaceAll) {
            deleteAll()
        }
        block { insertEntries(it) }
    }

    @Delete
    suspend fun delete(entry: ArtEntry) = delete(entry.id)

    @Delete
    suspend fun delete(entries: Collection<ArtEntry>)

    @Query("DELETE FROM art_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM art_entries")
    suspend fun deleteAll()

    @Transaction
    suspend fun transaction(block: suspend () -> Unit) = block()

    fun search(query: String, filterOptions: ArtAdvancedSearchQuery): PagingSource<Int, ArtEntry> {
        Log.d("SearchDebug", "search() called with: query = $filterOptions")
        val filterOptionsQueryPieces = filterOptionsQuery(filterOptions)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "artists:$it",
                    "sourceType:$it",
                    "sourceValue:$it",
                    "seriesSearchable:$it",
                    "charactersSearchable:$it",
                    "tags:$it",
                    "notes:$it",
                )
            }

        if (options.isEmpty() && filterOptionsQueryPieces.isEmpty()) {
            return getEntries()
        }

        val bindArguments = options.map { it.joinToString(separator = " OR ") }

        val statement = (bindArguments + filterOptionsQueryPieces).joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM art_entries
                JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
                WHERE art_entries_fts MATCH ?
                """.trimIndent()
        } + "\nORDER BY art_entries.lastEditTime DESC"

        return getEntries(
            SimpleSQLiteQuery(
                statement,
                bindArguments.toTypedArray() + filterOptionsQueryPieces.toTypedArray()
            )
        )
    }

    private fun filterOptionsQuery(filterOptions: ArtAdvancedSearchQuery): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        queryPieces += filterOptions.artists.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "artists:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "seriesSearchable:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.seriesById.map {
            "seriesSerialized:${
                RoomUtils.wrapMatchQuery(
                    it
                )
            }"
        }
        queryPieces += filterOptions.characters.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "charactersSearchable:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.charactersById
            .map { "charactersSerialized:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.tags.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "tags:${RoomUtils.wrapMatchQuery(it)}" }
        filterOptions.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "notes:${RoomUtils.wrapMatchQuery(it)}" }
        }
        when (val source = filterOptions.source) {
            is SourceType.Convention -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.name.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${RoomUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
                source.year
                    ?.let { "sourceValue:${RoomUtils.wrapMatchQuery(it.toString())}" }
                    ?.let { queryPieces += it }
                queryPieces += source.hall.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${RoomUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
                queryPieces += source.booth.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${RoomUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Custom -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.value.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${RoomUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Online -> TODO()
            SourceType.Different,
            SourceType.Unknown,
            null,
            -> {
                // Do nothing
            }
        }

        filterOptions.artistsLocked?.let { queryPieces += "artistsLocked:${it.toBit()}" }
        filterOptions.seriesLocked?.let { queryPieces += "seriesLocked:${it.toBit()}" }
        filterOptions.charactersLocked?.let { queryPieces += "charactersLocked:${it.toBit()}" }
        filterOptions.sourceLocked?.let { queryPieces += "sourceLocked:${it.toBit()}" }
        filterOptions.tagsLocked?.let { queryPieces += "tagsLocked:${it.toBit()}" }
        filterOptions.notesLocked?.let { queryPieces += "notesLocked:${it.toBit()}" }
        filterOptions.printSizeLocked?.let { queryPieces += "printSizeLocked:${it.toBit()}" }

        return queryPieces
    }
}
