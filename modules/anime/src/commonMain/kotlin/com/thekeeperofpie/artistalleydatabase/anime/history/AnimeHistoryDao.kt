package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.anilist.type.MediaType

@Dao
interface AnimeHistoryDao {

    @Query("""SELECT * FROM anime_media_history WHERE type = :type ORDER BY viewedAt DESC""")
    fun getEntries(type: MediaType): PagingSource<Int, AnimeMediaHistoryEntry>

    @Query("""
        SELECT * FROM anime_media_history WHERE type = :type
        ORDER BY viewedAt DESC LIMIT :limit OFFSET :offset
        """)
    suspend fun getEntries(limit: Int, offset: Int, type: MediaType): List<AnimeMediaHistoryEntry>

    @Deprecated(message = "Use [insertEntry] instead", replaceWith = ReplaceWith("insertEntry"))
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: AnimeMediaHistoryEntry)

    @Query("""SELECT COUNT(*) FROM anime_media_history""")
    suspend fun getEntryCount(): Int

    @Query("""DELETE FROM anime_media_history WHERE id IN
        (SELECT id FROM anime_media_history ORDER BY viewedAt ASC LIMIT :limit)""")
    suspend fun deleteOldest(limit: Int)

    @Transaction
    @Suppress("DEPRECATION")
    suspend fun insertEntry(entry: AnimeMediaHistoryEntry, maxEntries: Int) {
        insertEntries(entry)
        val entryCount = getEntryCount()
        if (entryCount > maxEntries) {
            deleteOldest(entryCount - maxEntries)
        }
    }

    @Query("""SELECT * FROM anime_media_history ORDER BY viewedAt DESC LIMIT 1 OFFSET :index""")
    suspend fun getEntryAtIndex(index: Int): AnimeMediaHistoryEntry?

    @Query("""DELETE FROM anime_media_history""")
    suspend fun deleteAll()
}
