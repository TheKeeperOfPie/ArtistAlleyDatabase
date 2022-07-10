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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: MediaEntry)
}