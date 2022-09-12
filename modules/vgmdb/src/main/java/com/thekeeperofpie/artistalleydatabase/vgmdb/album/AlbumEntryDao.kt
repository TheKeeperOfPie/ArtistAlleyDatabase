package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumEntryDao {

    @Query("""SELECT * FROM album_entries WHERE id = :id""")
    fun getEntry(id: String): Flow<AlbumEntry?>

    @Query("""SELECT * FROM album_entries WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<AlbumEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: AlbumEntry)

    @Update
    suspend fun updateEntry(entry: AlbumEntry)
}