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
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchQuery
import kotlinx.coroutines.yield

@Dao
interface CdEntryDao {

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

    fun getEntries(query: CdSearchQuery): PagingSource<Int, CdEntry> {
        val includeAll = query.includeAll
        val lockedValue = when {
            includeAll -> null
            query.locked && query.unlocked -> null
            query.locked -> "1"
            query.unlocked -> "0"
            else -> null
        }

        val options = query.query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map { queryValue ->
                mutableListOf<String>().apply {
                    if (includeAll || query.includeCatalogIds) this += "catalogId:$queryValue"
                    if (includeAll || query.includeTitles) this += "titles:$queryValue"
                    if (includeAll || query.includePerformers) this += "performersSearchable:$queryValue"
                    if (includeAll || query.includeComposers) this += "composersSearchable:$queryValue"
                    if (includeAll || query.includeSeries) this += "seriesSearchable:$queryValue"
                    if (includeAll || query.includeCharacters) this += "charactersSearchable:$queryValue"
                    if (includeAll || query.includeDiscs) this += "discs:$queryValue"
                    if (includeAll || query.includeTags) this += "tags:$queryValue"
                    if (includeAll || query.includeNotes) this += "notes:$queryValue"
                }
            }

        if (options.isEmpty() && lockedValue == null) {
            return getEntries()
        }

        val lockOptions = if (lockedValue == null) emptyList() else {
            mutableListOf<String>().apply {
                if (query.includeCatalogIds) this += "catalogIdLocked:$lockedValue"
                if (query.includeTitles) this += "titlesLocked:$lockedValue"
                if (query.includePerformers) this += "performersLocked:$lockedValue"
                if (query.includeComposers) this += "composersLocked:$lockedValue"
                if (query.includeSeries) this += "seriesLocked:$lockedValue"
                if (query.includeCharacters) this += "charactersLocked:$lockedValue"
                if (query.includeDiscs) this += "discsLocked:$lockedValue"
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
                FROM cd_entries
                JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
                WHERE cd_entries_fts MATCH ?
                """.trimIndent()
        } + "\nORDER BY cd_entries.lastEditTime DESC"

        return getEntries(SimpleSQLiteQuery(statement, bindArguments.toTypedArray()))
    }

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
}