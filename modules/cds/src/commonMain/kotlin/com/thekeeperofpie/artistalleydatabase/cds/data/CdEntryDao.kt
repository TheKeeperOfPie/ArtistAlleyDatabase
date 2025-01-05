package com.thekeeperofpie.artistalleydatabase.cds.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchQuery
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils.toBit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json

@Dao
interface CdEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM cd_entries WHERE id = :id""")
    suspend fun getEntry(id: String): CdEntry

    @RawQuery([CdEntry::class])
    fun getEntries(query: RoomRawQuery): PagingSource<Int, CdEntry>

    @Query(
        """
        SELECT *
        FROM cd_entries
        ORDER BY lastEditTime DESC
        """
    )
    fun getEntries(): PagingSource<Int, CdEntry>

    @Query(
        """
        SELECT *
        FROM cd_entries
        LIMIT :limit
        OFFSET :offset
        """
    )
    suspend fun getEntries(limit: Int = 50, offset: Int = 0): List<CdEntry>

    @Query(
        """
        SELECT COUNT(*)
        FROM cd_entries
        """
    )
    suspend fun getEntriesSize(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM cd_entries
        """
    )
    fun getEntriesSizeFlow(): Flow<Int>

    @Transaction
    suspend fun iterateEntries(
        entriesSize: (Int) -> Unit = {},
        limit: Int = 50,
        block: suspend (index: Int, entry: CdEntry) -> Unit,
    ) {
        iterateEntriesNoTransaction(entriesSize, limit, block)
    }

    suspend fun iterateEntriesNoTransaction(
        entriesSize: (Int) -> Unit = {},
        limit: Int = 50,
        block: suspend (index: Int, entry: CdEntry) -> Unit,
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
    suspend fun insertEntries(vararg entries: CdEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: Collection<CdEntry>)

    suspend fun insertEntriesDeferred(
        dryRun: Boolean,
        replaceAll: Boolean,
        block: suspend (insertEntry: suspend (Array<CdEntry>) -> Unit) -> Unit
    ) {
        if (!dryRun && replaceAll) {
            deleteAll()
        }
        block { insertEntries(*it) }
    }

    @Delete
    suspend fun delete(entry: CdEntry) = delete(entry.id)

    @Delete
    suspend fun delete(entries: Collection<CdEntry>)

    @Query("DELETE FROM cd_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM cd_entries")
    suspend fun deleteAll()

    @Transaction
    suspend fun transaction(block: suspend () -> Unit) = block()

    suspend fun searchSeriesByMediaId(json: Json, mediaId: String) =
        (searchSeriesViaMatch(mediaId) + searchSeriesViaLike(mediaId))
            .filter {
                it.series(json)
                    .filterIsInstance<Series.AniList>()
                    .any { it.id == mediaId }
            }

    @Query(
        """
        SELECT *
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.seriesSerialized MATCH :query
    """
    )
    suspend fun searchSeriesViaMatch(query: String): List<CdEntry>

    @Query(
        """
        SELECT *
        FROM cd_entries
        WHERE cd_entries.seriesSerialized LIKE :query
    """
    )
    suspend fun searchSeriesViaLike(query: String): List<CdEntry>


    fun search(query: String, filterOptions: CdSearchQuery): PagingSource<Int, CdEntry> {
        val filterOptionsQueryPieces = filterOptionsQuery(filterOptions)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "catalogId:$it",
                    "titles:$it",
                    "performersSearchable:$it",
                    "composersSearchable:$it",
                    "seriesSearchable:$it",
                    "charactersSearchable:$it",
                    "discs:$it",
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
                FROM cd_entries
                JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
                WHERE cd_entries_fts MATCH ?
                """.trimIndent()
        } + "\nORDER BY cd_entries.lastEditTime DESC"

        return getEntries(
            RoomRawQuery(statement) {
                (bindArguments + filterOptionsQueryPieces).forEachIndexed { index, arg ->
                    it.bindText(index + 1, arg)
                }
            }
        )
    }

    private fun filterOptionsQuery(filterOptions: CdSearchQuery): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterOptions.catalogId.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "catalogId:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterOptions.titles.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "titles:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.performers.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "performersSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.performersById
            .map { "performers:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.composers.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "composersSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.composersById
            .map { "composers:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "seriesSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.seriesById.map {
            "seriesSerialized:${DatabaseUtils.wrapMatchQuery(it)}"
        }
        queryPieces += filterOptions.characters.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "charactersSearchable:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.charactersById
            .map { "charactersSerialized:${DatabaseUtils.wrapMatchQuery(it)}" }
        // TODO: Search by discs is hard to implement
//        queryPieces += filterOptions.discs.flatMap { it.split(WHITESPACE_REGEX) }
//            .map { "discs:${DatabaseUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.tags.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "tags:${DatabaseUtils.wrapMatchQuery(it)}" }
        filterOptions.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "notes:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        filterOptions.catalogIdLocked?.let { queryPieces += "catalogIdLocked:${it.toBit()}" }
        filterOptions.titlesLocked?.let { queryPieces += "titlesLocked:${it.toBit()}" }
        filterOptions.performersLocked?.let { queryPieces += "performersLocked:${it.toBit()}" }
        filterOptions.composersLocked?.let { queryPieces += "composersLocked:${it.toBit()}" }
        filterOptions.seriesLocked?.let { queryPieces += "seriesLocked:${it.toBit()}" }
        filterOptions.charactersLocked?.let { queryPieces += "charactersLocked:${it.toBit()}" }
        filterOptions.discsLocked?.let { queryPieces += "discsLocked:${it.toBit()}" }
        filterOptions.tagsLocked?.let { queryPieces += "tagsLocked:${it.toBit()}" }
        filterOptions.notesLocked?.let { queryPieces += "notesLocked:${it.toBit()}" }

        return queryPieces
    }
}
