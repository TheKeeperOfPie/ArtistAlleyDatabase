package com.thekeeperofpie.artistalleydatabase.anime.ignore.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anilist.data.type.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeIgnoreDao {

    @Query("""SELECT * FROM anime_media_ignore WHERE type = :type ORDER BY viewedAt DESC""")
    fun getEntries(type: MediaType): PagingSource<Int, AnimeMediaIgnoreEntry>

    @Query("""
        SELECT * FROM anime_media_ignore WHERE type = :type
        ORDER BY viewedAt DESC LIMIT :limit OFFSET :offset
        """)
    suspend fun getEntries(limit: Int, offset: Int, type: MediaType): List<AnimeMediaIgnoreEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: AnimeMediaIgnoreEntry)

    @Query("""SELECT COUNT(*) FROM anime_media_ignore""")
    suspend fun getEntryCount(): Int

    @Query("""SELECT COUNT(*) FROM anime_media_ignore""")
    fun entryCountFlow(): Flow<Int>

    @Query("""SELECT * FROM anime_media_ignore WHERE type = :type ORDER BY viewedAt DESC LIMIT 1 OFFSET :index""")
    suspend fun getEntryAtIndex(index: Int, type: MediaType): AnimeMediaIgnoreEntry?

    @Query("""SELECT EXISTS(SELECT id FROM anime_media_ignore WHERE id = :id)""")
    suspend fun exists(id : String) : Boolean

    @Query("DELETE FROM anime_media_ignore WHERE id = :id")
    suspend fun delete(id: String)

    @Query("""DELETE FROM anime_media_ignore""")
    suspend fun deleteAll()
}
