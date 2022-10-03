package com.thekeeperofpie.artistalleydatabase.cds.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchQuery
import kotlinx.coroutines.yield

@Dao
interface CdEntryDao {

    @Query("""SELECT * FROM cd_entries WHERE id = :id""")
    suspend fun getEntry(id: String): CdEntry

    @Query(
        """
        SELECT *
        FROM cd_entries
        ORDER BY lastEditTime DESC
        """
    )
    fun getEntries(): PagingSource<Int, CdEntry>

    fun getEntries(query: CdSearchQuery): PagingSource<Int, CdEntry> {
        return getEntries()
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
        entriesSize: (Int) -> Unit,
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