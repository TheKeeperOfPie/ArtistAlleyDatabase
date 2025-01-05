package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtEntryBrowseDao : ArtEntryDao {

    @Query(
        """
            SELECT DISTINCT (art_entries.artists)
            FROM art_entries
            LIMIT :limit OFFSET :offset
        """
    )
    fun getArtists(limit: Int = Int.MAX_VALUE, offset: Int = 0): Flow<List<String>>

    @Query(
        """
            SELECT sourceType
            FROM art_entries
        """
    )
    fun getSourceTypes(): Flow<List<String>>

    @Query(
        """
            SELECT sourceValue
            FROM art_entries
        """
    )
    fun getSourceValues(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.seriesSerialized)
            FROM art_entries
            LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getSeries(limit: Int = Int.MAX_VALUE, offset: Int = 0): List<String>

    @Query(
        """
            SELECT DISTINCT (art_entries.charactersSerialized)
            FROM art_entries
            LIMIT :limit OFFSET :offset
        """
    )
    fun getCharacters(limit: Int = Int.MAX_VALUE, offset: Int = 0): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.tags)
            FROM art_entries
            LIMIT :limit OFFSET :offset
        """
    )
    fun getTags(limit: Int = Int.MAX_VALUE, offset: Int = 0): Flow<List<String>>

    fun getArtist(query: String) = getArtistInternal(DatabaseUtils.wrapLikeQuery(query))

    fun getArtistFlow(query: String, limit: Int = 1) =
        getArtistFlowInternal(query = DatabaseUtils.wrapLikeQuery(query), limit = limit)

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE artists LIKE :query
        """
    )
    fun getArtistInternal(query: String): PagingSource<Int, ArtEntry>

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE artists LIKE :query
            LIMIT :limit
        """
    )
    fun getArtistFlowInternal(query: String, limit: Int): Flow<List<ArtEntry>>

    fun getSeries(query: String) = getSeriesInternal(DatabaseUtils.wrapLikeQuery(query))

    fun getSeriesFlow(query: String, limit: Int = 1) =
        getSeriesFlowInternal(query = DatabaseUtils.wrapLikeQuery(query), limit = limit)

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE seriesSerialized LIKE :query
        """
    )
    fun getSeriesInternal(query: String): PagingSource<Int, ArtEntry>

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE seriesSerialized LIKE :query
            LIMIT :limit
        """
    )
    fun getSeriesFlowInternal(query: String, limit: Int): Flow<List<ArtEntry>>

    fun getCharacter(query: String) = getCharacterInternal(DatabaseUtils.wrapLikeQuery(query))

    fun getCharacterFlow(query: String, limit: Int = 1) =
        getCharacterFlowInternal(query = DatabaseUtils.wrapLikeQuery(query), limit = limit)

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE charactersSerialized LIKE :query
        """
    )
    fun getCharacterInternal(query: String): PagingSource<Int, ArtEntry>

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE charactersSerialized LIKE :query
            LIMIT :limit
        """
    )
    fun getCharacterFlowInternal(query: String, limit: Int): Flow<List<ArtEntry>>

    fun getTag(query: String) = getTagInternal(DatabaseUtils.wrapLikeQuery(query))

    fun getTagFlow(query: String, limit: Int = 1) =
        getTagFlowInternal(query = DatabaseUtils.wrapLikeQuery(query), limit = limit)

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE tags LIKE :query
        """
    )
    fun getTagInternal(query: String): PagingSource<Int, ArtEntry>

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE tags LIKE :query
            LIMIT :limit
        """
    )
    fun getTagFlowInternal(query: String, limit: Int): Flow<List<ArtEntry>>
}
