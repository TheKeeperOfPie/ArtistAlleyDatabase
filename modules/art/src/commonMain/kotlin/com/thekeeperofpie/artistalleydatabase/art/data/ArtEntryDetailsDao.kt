package com.thekeeperofpie.artistalleydatabase.art.data

import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import kotlin.time.Instant

@Dao
interface ArtEntryDetailsDao : ArtEntryDao {

    suspend fun queryArtists(
        query: String,
        limit: Int = 5,
        offset: Int = 0
    ) = DatabaseUtils.queryListStringColumn(
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
        artist = DatabaseUtils.wrapLikeQuery(artist),
        conventionName = DatabaseUtils.wrapLikeQuery(conventionName),
        conventionYear = DatabaseUtils.wrapLikeQuery(conventionYear.toString()),
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
    ) = DatabaseUtils.queryListStringColumn(
        query,
        { querySeriesViaMatch(it, limit, offset) },
        { querySeriesViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.seriesSerialized)
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
        SELECT DISTINCT (art_entries.seriesSerialized)
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
    ) = DatabaseUtils.queryListStringColumn(
        query,
        { queryCharactersViaMatch(it, limit, offset) },
        { queryCharactersViaLike(it, limit, offset) }
    )

    @Query(
        """
        SELECT DISTINCT (art_entries.charactersSerialized)
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
        SELECT DISTINCT (art_entries.charactersSerialized)
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
    ) = DatabaseUtils.queryListStringColumn(
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

    @Query("""SELECT * FROM art_entries WHERE id IN (:ids)""")
    suspend fun getEntries(ids: List<String>): List<ArtEntry>

    @Query(
        """
            SELECT COUNT(DISTINCT seriesSerialized)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountSeries(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT charactersSerialized)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountCharacters(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT sourceType)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountSourceType(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT sourceValue)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountSourceValue(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT artists)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountArtists(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT tags)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountTags(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT printWidth)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountPrintWidth(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT printHeight)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountPrintHeight(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT notes)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountNotes(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT artistsLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountArtistsLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT sourceLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountSourceLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT seriesLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountSeriesLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT charactersLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountCharactersLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT tagsLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountTagsLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT notesLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountNotesLocked(ids: List<String>): Int

    @Query(
        """
            SELECT COUNT(DISTINCT printSizeLocked)
            FROM art_entries
            WHERE id IN (:ids)
        """
    )
    suspend fun distinctCountPrintSizeLocked(ids: List<String>): Int

    @Query(
        """
            UPDATE art_entries
            SET seriesSerialized = :series, seriesSearchable = :seriesSearchable
            WHERE id IN (:ids)
        """
    )
    suspend fun updateSeries(
        ids: List<String>,
        series: List<String>,
        seriesSearchable: List<String>
    )

    @Query(
        """
            UPDATE art_entries
            SET charactersSerialized = :characters, charactersSearchable = :charactersSearchable
            WHERE id IN (:ids)
        """
    )
    suspend fun updateCharacters(
        ids: List<String>,
        characters: List<String>,
        charactersSearchable: List<String>
    )

    @Query(
        """
            UPDATE art_entries
            SET sourceType = :sourceType, sourceValue = :sourceValue
            WHERE id IN (:ids)
        """
    )
    suspend fun updateSource(ids: List<String>, sourceType: String, sourceValue: String)

    @Query(
        """
            UPDATE art_entries
            SET artists = :artists
            WHERE id IN (:ids)
        """
    )
    suspend fun updateArtists(ids: List<String>, artists: List<String>)

    @Query(
        """
            UPDATE art_entries
            SET tags = :tags
            WHERE id IN (:ids)
        """
    )
    suspend fun updateTags(ids: List<String>, tags: List<String>)

    @Query(
        """
            UPDATE art_entries
            SET printWidth = :printWidth, printHeight = :printHeight
            WHERE id IN (:ids)
        """
    )
    suspend fun updatePrintSize(ids: List<String>, printWidth: Int?, printHeight: Int?)

    @Query(
        """
            UPDATE art_entries
            SET notes = :notes
            WHERE id IN (:ids)
        """
    )
    suspend fun updateNotes(ids: List<String>, notes: String?)

    @Query(
        """
            UPDATE art_entries
            SET artistsLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateArtistsLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET sourceLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateSourceLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET seriesLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateSeriesLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET charactersLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateCharactersLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET tagsLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateTagsLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET notesLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updateNotesLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET printSizeLocked = :locked
            WHERE id IN (:ids)
        """
    )
    suspend fun updatePrintSizeLocked(ids: List<String>, locked: Boolean)

    @Query(
        """
            UPDATE art_entries
            SET lastEditTime = :lastEditTime
            WHERE id IN (:ids)
        """
    )
    suspend fun updateLastEditTime(ids: List<String>, lastEditTime: Instant?)
}
