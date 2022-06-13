package com.thekeeperofpie.artistalleydatabase.art

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery


@Dao
interface ArtEntryDao {

    @RawQuery([ArtEntry::class])
    fun getEntries(query: SupportSQLiteQuery): PagingSource<Int, ArtEntry>

//    @Query("""
//        SELECT *
//        FROM art_entries
//        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
//        WHERE art_entries_fts MATCH :query
//    """)
//    fun getEntries(
//        query: String = "*"
//    ): PagingSource<Int, ArtEntry>

    @Query("""
        SELECT *
        FROM art_entries
    """)
    fun getEntries(): PagingSource<Int, ArtEntry>

    suspend fun getEntries(
        orderBy: String = "date",
        ascending: Boolean = false,
        query: String = ""
    ): PagingSource<Int, ArtEntry> {
        val orderDirection = if (ascending) "ASC" else "DESC"
        val statement =
            "SELECT * FROM art_entries ORDER BY :orderBy $orderDirection WHERE * MATCH ':query'"
        return getEntries(SimpleSQLiteQuery(statement, arrayOf(orderBy, query)))
    }

    @Insert
    suspend fun insertEntries(vararg entries: ArtEntry)

    @Delete
    suspend fun delete(entry: ArtEntry) = delete(entry.id)

    @Query("DELETE FROM art_entries WHERE id = :id")
    suspend fun delete(id: String)
}