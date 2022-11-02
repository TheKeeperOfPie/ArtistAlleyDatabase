package com.thekeeperofpie.artistalleydatabase.anilist.media

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaEntryDao {

    @Query("""SELECT * FROM media_entries WHERE id = :id""")
    fun getEntry(id: Int): Flow<MediaEntry?>

    @Query("""SELECT * FROM media_entries WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<MediaEntry>>

    @Query("""SELECT DISTINCT (id) FROM media_entries WHERE id in (:ids)""")
    suspend fun getEntriesById(ids: Collection<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: MediaEntry)

    @Query("DELETE FROM media_entries")
    suspend fun deleteAll()
}