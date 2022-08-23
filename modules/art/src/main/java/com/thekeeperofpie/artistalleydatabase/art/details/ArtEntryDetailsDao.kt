package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.toUpperCase
import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao.Companion.wrapLikeQuery
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao.Companion.wrapMatchQuery

@Dao
interface ArtEntryDetailsDao : ArtEntryDao {

    private suspend fun queryListStringColumn(
        query: String,
        matchFunction: suspend (String) -> List<String>,
        likeFunction: suspend (String) -> List<String>,
    ): List<String> {
        // TODO: Filter results so that only individual entries with the query are returned
        val matchQuery = wrapMatchQuery(query)
        val likeQuery = wrapLikeQuery(query)
        return matchFunction(matchQuery)
            .plus(matchFunction(matchQuery.toLowerCase(Locale.current)))
            .plus(matchFunction(matchQuery.toUpperCase(Locale.current)))
            .plus(likeFunction(likeQuery))
            .plus(likeFunction(likeQuery.toLowerCase(Locale.current)))
            .plus(likeFunction(likeQuery.toUpperCase(Locale.current)))
            .flatMap(JsonUtils::readStringList)
            .distinct()
    }

    suspend fun queryArtists(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryArtistsViaMatch(it, limit, offset) },
        { queryArtistsViaLike(it, limit, offset) }
    )

    @Query(
        """
            SELECT sourceValue
            FROM art_entries
            WHERE artists LIKE :artist
            AND sourceType IS 'convention'
            AND sourceValue LIKE :conventionName
            AND sourceValue LIKE :conventionYear
            LIMIT 1
        """
    )
    suspend fun queryArtistForHallBoothInternal(
        artist: String,
        conventionName: String,
        conventionYear: String,
    ): String?

    suspend fun queryArtistForHallBooth(
        artist: String,
        conventionName: String,
        conventionYear: Int,
    ) = queryArtistForHallBoothInternal(
        artist = wrapLikeQuery(artist),
        conventionName = wrapLikeQuery(conventionName),
        conventionYear = wrapLikeQuery(conventionYear.toString()),
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.artists)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.artists MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryArtistsViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.artists)
        FROM art_entries
        WHERE art_entries.artists LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryArtistsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun querySeries(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { querySeriesViaMatch(it, limit, offset) },
        { querySeriesViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.series)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.seriesSearchable MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun querySeriesViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.series)
        FROM art_entries
        WHERE art_entries.seriesSearchable LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun querySeriesViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryCharacters(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryCharactersViaMatch(it, limit, offset) },
        { queryCharactersViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.characters)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.charactersSearchable MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryCharactersViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.characters)
        FROM art_entries
        WHERE art_entries.charactersSearchable LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryCharactersViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryTags(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = queryListStringColumn(
        query,
        { queryTagsViaMatch(it, limit, offset) },
        { queryTagsViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.tags)
        FROM art_entries
        JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
        WHERE art_entries_fts.tags MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryTagsViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (art_entries.tags)
        FROM art_entries
        WHERE art_entries.tags LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryTagsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>
}