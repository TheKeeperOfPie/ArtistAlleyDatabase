package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchQuery
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils.toBit
import kotlinx.coroutines.yield

@Dao
interface ArtEntryDao {

    @Query("""SELECT * FROM art_entries WHERE id = :id""")
    suspend fun getEntry(id: String): ArtEntry

    @RawQuery([ArtEntry::class])
    fun getEntries(query: RoomRawQuery): PagingSource<Int, ArtEntry>

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
        block: suspend (insertEntry: suspend (Array<ArtEntry>) -> Unit) -> Unit,
    ) {
        if (!dryRun && replaceAll) {
            deleteAll()
        }
        block { insertEntries(*it) }
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

    fun search(query: String, filterOptions: ArtSearchQuery): PagingSource<Int, ArtEntry> {
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
            RoomRawQuery(statement) {
                (bindArguments + filterOptionsQueryPieces).forEachIndexed { index, arg ->
                    it.bindText(index + 1, arg)
                }
            }
        )
    }

    private fun filterOptionsQuery(filterOptions: ArtSearchQuery): List<String> {
        val queryPieces = mutableListOf<String>()

        queryPieces += filterOptions.artists.flatMap { it.split(DatabaseUtils.WHITESPACE_REGEX) }
            .map { "artists:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.series.flatMap { it.split(DatabaseUtils.WHITESPACE_REGEX) }
            .map { "seriesSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.seriesById.map {
            "seriesSerialized:${DatabaseUtils.wrapMatchQuery(it)}"
        }
        queryPieces += filterOptions.characters.flatMap { it.split(DatabaseUtils.WHITESPACE_REGEX) }
            .map { "charactersSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.charactersById
            .map { "charactersSerialized:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.tags.flatMap { it.split(DatabaseUtils.WHITESPACE_REGEX) }
            .map { "tags:${DatabaseUtils.wrapMatchQuery(it)}" }
        filterOptions.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "notes:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        when (val source = filterOptions.source) {
            is SourceType.Convention -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.name.takeIf(String::isNotBlank)
                    ?.split(DatabaseUtils.WHITESPACE_REGEX)
                    ?.map { "sourceValue:${DatabaseUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
                source.year
                    ?.let { "sourceValue:${DatabaseUtils.wrapMatchQuery(it.toString())}" }
                    ?.let { queryPieces += it }
                queryPieces += source.hall.takeIf(String::isNotBlank)
                    ?.split(DatabaseUtils.WHITESPACE_REGEX)
                    ?.map { "sourceValue:${DatabaseUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
                queryPieces += source.booth.takeIf(String::isNotBlank)
                    ?.split(DatabaseUtils.WHITESPACE_REGEX)
                    ?.map { "sourceValue:${DatabaseUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Custom -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.value.takeIf(String::isNotBlank)
                    ?.split(DatabaseUtils.WHITESPACE_REGEX)
                    ?.map { "sourceValue:${DatabaseUtils.wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Online -> TODO()
            SourceType.Unknown -> {
                queryPieces += "sourceType:${source.serializedType}"
            }
            SourceType.Different,
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
