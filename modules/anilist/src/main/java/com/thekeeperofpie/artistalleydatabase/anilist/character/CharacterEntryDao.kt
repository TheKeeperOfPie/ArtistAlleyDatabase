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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(vararg entries: CharacterEntry)
}