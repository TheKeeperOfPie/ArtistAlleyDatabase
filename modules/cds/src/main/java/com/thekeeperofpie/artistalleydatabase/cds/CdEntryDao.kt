package com.thekeeperofpie.artistalleydatabase.cds

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchQuery

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: CdEntry)

    @Transaction
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
}