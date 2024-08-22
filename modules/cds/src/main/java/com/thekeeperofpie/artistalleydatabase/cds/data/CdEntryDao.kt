package com.thekeeperofpie.artistalleydatabase.cds.data

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
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchQuery
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.yield

@Dao
interface CdEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    @Query("""SELECT * FROM cd_entries WHERE id = :id""")
    suspend fun getEntry(id: String): CdEntry

    @RawQuery([CdEntry::class])
    fun getEntries(query: SupportSQLiteQuery): PagingSource<Int, CdEntry>

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
    fun getEntries(limit: Int = 50, offset: Int = 0): List<CdEntry>

    @Query(
        """
        SELECT COUNT(*)
        FROM cd_entries
        """
    )
    fun getEntriesSize(): Int

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
        block: suspend (insertEntry: suspend (CdEntry) -> Unit) -> Unit
    ) {
        if (!dryRun && replaceAll) {
            deleteAll()
        }
        block { insertEntries(it) }
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

    suspend fun searchSeriesByMediaId(appJson: AppJson, mediaId: String) =
        (searchSeriesViaMatch(mediaId) + searchSeriesViaLike(mediaId))
            .filter {
                it.series(appJson)
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
            SimpleSQLiteQuery(
                statement,
                bindArguments.toTypedArray() + filterOptionsQueryPieces.toTypedArray()
            )
        )
    }

    private fun filterOptionsQuery(filterOptions: CdSearchQuery): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterOptions.catalogId.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "catalogId:${RoomUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterOptions.titles.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "titles:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.performers.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "performersSearchable:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.performersById
            .map { "performers:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.composers.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "composersSearchable:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.composersById
            .map { "composers:${RoomUtils.wrapMatchQuery(it)}" }
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
        // TODO: Search by discs is hard to implement
//        queryPieces += filterOptions.discs.flatMap { it.split(WHITESPACE_REGEX) }
//            .map { "discs:${RoomUtils.wrapMatchQuery(it)}" }
        queryPieces += filterOptions.tags.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "tags:${RoomUtils.wrapMatchQuery(it)}" }
        filterOptions.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX)
                .map { "notes:${RoomUtils.wrapMatchQuery(it)}" }
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
