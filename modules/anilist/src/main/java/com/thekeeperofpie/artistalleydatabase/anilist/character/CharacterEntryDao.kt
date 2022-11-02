package com.thekeeperofpie.artistalleydatabase.anilist.character

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterEntryDao {

    @Query("""SELECT * FROM character_entries WHERE id = :id""")
    fun getEntry(id: Int): Flow<CharacterEntry?>

    @Query("""SELECT * FROM character_entries WHERE id in (:ids)""")
    fun getEntries(ids: Collection<String>): Flow<List<CharacterEntry>>

    @Query("""SELECT DISTINCT (id) FROM character_entries WHERE id in (:ids)""")
    suspend fun getEntriesById(ids: Collection<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: CharacterEntry)

    @Query("DELETE FROM character_entries")
    suspend fun deleteAll()
}