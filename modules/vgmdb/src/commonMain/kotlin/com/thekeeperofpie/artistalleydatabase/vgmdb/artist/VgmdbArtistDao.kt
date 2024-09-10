package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VgmdbArtistDao {

    @Query("""SELECT * FROM vgmdb_artists WHERE id = :id""")
    suspend fun getEntry(id: String): VgmdbArtist?

    @Query("""SELECT * FROM vgmdb_artists WHERE id = :id""")
    fun getEntryFlow(id: String): Flow<VgmdbArtist?>

    @Query("""SELECT * FROM vgmdb_artists WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<VgmdbArtist>>

    @Query("""SELECT DISTINCT (id) FROM vgmdb_artists WHERE id in (:ids)""")
    suspend fun getEntriesById(ids: Collection<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: VgmdbArtist)

    @Update
    suspend fun updateEntry(entry: VgmdbArtist)

    @Query("DELETE FROM vgmdb_artists")
    suspend fun deleteAll()

    @Query("DELETE FROM vgmdb_artists WHERE id = :id")
    suspend fun delete(id: String)
}
