package com.thekeeperofpie.artistalleydatabase.cds

import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils

@Dao
interface CdEntryDetailsDao : CdEntryDao {

    suspend fun queryVocalists(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = RoomUtils.queryListStringColumn(
        query,
        { queryVocalistsViaMatch(it, limit, offset) },
        { queryVocalistsViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (cd_entries.vocalists)
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.vocalistsSearchable MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryVocalistsViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (cd_entries.vocalists)
        FROM cd_entries
        WHERE cd_entries.vocalistsSearchable LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryVocalistsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun queryComposers(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = RoomUtils.queryListStringColumn(
        query,
        { queryComposersViaMatch(it, limit, offset) },
        { queryComposersViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (cd_entries.composers)
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.composersSearchable MATCH :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryComposersViaMatch(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    @Query(
        """
        SELECT DISTINCT (cd_entries.composers)
        FROM cd_entries
        WHERE cd_entries.composersSearchable LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryComposersViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>

    suspend fun querySeries(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = RoomUtils.queryListStringColumn(
        query,
        { querySeriesViaMatch(it, limit, offset) },
        { querySeriesViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (cd_entries.series)
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.seriesSearchable MATCH :query
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
        SELECT DISTINCT (cd_entries.series)
        FROM cd_entries
        WHERE cd_entries.seriesSearchable LIKE :query
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
    ) = RoomUtils.queryListStringColumn(
        query,
        { queryCharactersViaMatch(it, limit, offset) },
        { queryCharactersViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (cd_entries.characters)
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.charactersSearchable MATCH :query
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
        SELECT DISTINCT (cd_entries.characters)
        FROM cd_entries
        WHERE cd_entries.charactersSearchable LIKE :query
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
    ) = RoomUtils.queryListStringColumn(
        query,
        { queryTagsViaMatch(it, limit, offset) },
        { queryTagsViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (cd_entries.tags)
        FROM cd_entries
        JOIN cd_entries_fts ON cd_entries.id = cd_entries_fts.id
        WHERE cd_entries_fts.tags MATCH :query
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
        SELECT DISTINCT (cd_entries.tags)
        FROM cd_entries
        WHERE cd_entries.tags LIKE :query
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun queryTagsViaLike(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ): List<String>
}