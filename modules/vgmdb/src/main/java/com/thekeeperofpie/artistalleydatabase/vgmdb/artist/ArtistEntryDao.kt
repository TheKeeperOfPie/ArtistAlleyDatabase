package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistEntryDao {

    @Query("""SELECT * FROM artist_entries WHERE id = :id""")
    fun getEntry(id: String): Flow<ArtistEntry?>

    @Query("""SELECT * FROM artist_entries WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<ArtistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: ArtistEntry)

    @Update
    suspend fun updateEntry(entry: ArtistEntry)

    @Query("DELETE FROM artist_entries")
    suspend fun deleteAll()
}