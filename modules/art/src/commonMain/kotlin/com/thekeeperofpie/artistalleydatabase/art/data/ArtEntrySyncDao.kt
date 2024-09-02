package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ArtEntrySyncDao : ArtEntryDao {

    @Query(
        """
            SELECT DISTINCT art_entries.charactersSerialized, art_entries.seriesSerialized
            FROM art_entries
            LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getCharactersAndSeries(
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<CharactersAndSeries>

    data class CharactersAndSeries(
        val charactersSerialized: String,
        val seriesSerialized: String,
    )
}