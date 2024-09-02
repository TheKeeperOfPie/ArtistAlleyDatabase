package com.thekeeperofpie.artistalleydatabase.musical_artists

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicalArtistDao {

    @Suppress("SpellCheckingInspection")
    @Query("""SELECT * FROM musical_artists ORDER BY name COLLATE NOCASE ASC""")
    fun getEntries(): PagingSource<Int, MusicalArtist>

    @Query("DELETE FROM musical_artists")
    suspend fun deleteAll()

    @Query("DELETE FROM musical_artists WHERE id = :id")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: Collection<MusicalArtist>)
}