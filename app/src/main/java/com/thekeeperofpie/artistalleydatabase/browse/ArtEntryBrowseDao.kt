package com.thekeeperofpie.artistalleydatabase.browse

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils.wrapLikeQuery
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtEntryBrowseDao : ArtEntryDao {

    @Query(
        """
            SELECT DISTINCT (art_entries.artists)
            FROM art_entries
        """
    )
    fun getArtists(): Flow<List<String>>

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
            SELECT DISTINCT (art_entries.series)
            FROM art_entries
        """
    )
    fun getSeries(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.characters)
            FROM art_entries
        """
    )
    fun getCharacters(): Flow<List<String>>

    @Query(
        """
            SELECT DISTINCT (art_entries.tags)
            FROM art_entries
        """
    )
    fun getTags(): Flow<List<String>>

    fun getArtist(query: String) = getArtistInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE artists LIKE :query
        """
    )
    fun getArtistInternal(query: String): PagingSource<Int, ArtEntry>

    fun getSeries(query: String) = getSeriesInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE series LIKE :query
        """
    )
    fun getSeriesInternal(query: String): PagingSource<Int, ArtEntry>

    fun getCharacter(query: String) = getCharacterInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE characters LIKE :query
        """
    )
    fun getCharacterInternal(query: String): PagingSource<Int, ArtEntry>

    fun getTag(query: String) = getTagInternal(wrapLikeQuery(query))

    @Query(
        """
            SELECT *
            FROM art_entries
            WHERE tags LIKE :query
        """
    )
    fun getTagInternal(query: String): PagingSource<Int, ArtEntry>
}