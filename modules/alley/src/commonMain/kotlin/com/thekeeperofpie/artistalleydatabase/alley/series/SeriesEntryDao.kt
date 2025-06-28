package com.thekeeperofpie.artistalleydatabase.alley.series

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesById
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesByIds
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.getBooleanFixed
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

fun SqlCursor.toSeriesEntry(): SeriesEntry {
    val source = getString(6)
    return SeriesEntry(
        id = getString(0)!!,
        uuid = getString(1)!!,
        notes = getString(2),
        aniListId = getLong(3),
        aniListType = getString(4),
        wikipediaId = getLong(5),
        source = SeriesSource.entries.find { it.name == source },
        titlePreferred = getString(7)!!,
        titleEnglish = getString(8)!!,
        titleRomaji = getString(9)!!,
        titleNative = getString(10)!!,
        link = getString(11),
        inferred2024 = getLong(12)!!,
        inferred2025 = getLong(13)!!,
        confirmed2024 = getLong(14)!!,
        confirmed2025 = getLong(15)!!,
        counter = getLong(16)!!,
    )
}

fun SqlCursor.toSeriesWithUserData(): SeriesWithUserData {
    val uuid = getString(1)!!
    val source = getString(6)
    return SeriesWithUserData(
        SeriesEntry(
            id = getString(0)!!,
            uuid = uuid,
            notes = getString(2),
            aniListId = getLong(3),
            aniListType = getString(4),
            wikipediaId = getLong(5),
            source = SeriesSource.entries.find { it.name == source },
            titlePreferred = getString(7)!!,
            titleEnglish = getString(8)!!,
            titleRomaji = getString(9)!!,
            titleNative = getString(10)!!,
            link = getString(11),
            inferred2024 = getLong(12)!!,
            inferred2025 = getLong(13)!!,
            confirmed2024 = getLong(14)!!,
            confirmed2025 = getLong(15)!!,
            counter = getLong(16)!!,
        ),
        userEntry = SeriesUserEntry(
            seriesId = uuid,
            favorite = getBooleanFixed(17),
        )
    )
}

fun GetSeriesById.toSeriesWithUserData() = SeriesWithUserData(
    series = SeriesEntry(
        id = id,
        uuid = uuid,
        notes = notes,
        aniListId = aniListId,
        aniListType = aniListType,
        wikipediaId = wikipediaId,
        source = source,
        titlePreferred = titlePreferred,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji,
        titleNative = titleNative,
        link = link,
        inferred2024 = inferred2024,
        inferred2025 = inferred2025,
        confirmed2024 = confirmed2024,
        confirmed2025 = confirmed2025,
        counter = counter,
    ),
    userEntry = SeriesUserEntry(
        seriesId = uuid,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )
)

fun GetSeriesByIds.toSeriesWithUserData() = SeriesWithUserData(
    series = SeriesEntry(
        id = id,
        uuid = uuid,
        notes = notes,
        aniListId = aniListId,
        aniListType = aniListType,
        wikipediaId = wikipediaId,
        source = source,
        titlePreferred = titlePreferred,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji,
        titleNative = titleNative,
        link = link,
        inferred2024 = inferred2024,
        inferred2025 = inferred2025,
        confirmed2024 = confirmed2024,
        confirmed2025 = confirmed2025,
        counter = counter,
    ),
    userEntry = SeriesUserEntry(
        seriesId = uuid,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )
)

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val seriesDao: suspend () -> SeriesQueries = { database().seriesQueries },
) {
    suspend fun getSeriesIds() = seriesDao().getSeriesAndImageIds().awaitAsList()

    fun getSeriesById(id: String): Flow<SeriesWithUserData> =
        flowFromSuspend { seriesDao() }
            .flatMapLatest { it.getSeriesById(id).asFlow().mapToOneOrNull(PlatformDispatchers.IO) }
            .mapLatest { it?.toSeriesWithUserData() ?: fallbackSeriesWithUserData(id) }

    suspend fun getSeriesByIds(ids: List<String>): List<SeriesWithUserData> {
        if (ids.isEmpty()) return emptyList()
        val series = seriesDao().getSeriesByIds(ids).awaitAsList().associateBy { it.id }
        return ids.map { series[it]?.toSeriesWithUserData() ?: fallbackSeriesWithUserData(it) }
    }

    suspend fun observeSeriesByIds(ids: List<String>): Flow<List<SeriesWithUserData>> {
        if (ids.isEmpty()) return flowOf(emptyList())
        return seriesDao().getSeriesByIds(ids).asFlow().mapToList(PlatformDispatchers.IO)
            .mapLatest { it.associateBy { it.id } }
            .mapLatest { series ->
                ids.map { series[it]?.toSeriesWithUserData() ?: fallbackSeriesWithUserData(it) }
            }
    }

    fun searchSeries(
        languageOption: AniListLanguageOption,
        year: DataYear,
        query: String,
        randomSeed: Int,
        seriesFilterParams: SeriesSortFilterController.FilterParams,
        favoriteOnly: Boolean = false,
    ): PagingSource<Int, SeriesWithUserData> {
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "id",
            "titleEnglish",
            "titleRomaji",
            "titleNative",
        )
        val matchQuery = buildString {
            append("'{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val favoritesStatement = "seriesUserEntry.favorite = 1"
            .takeIf { favoriteOnly }.orEmpty()
        val filteredSourcesStatement = getFilteredSourcesStatement(
            seriesFilterParams = seriesFilterParams,
            sourceKey = if (query.isEmpty()) "source" else "sourceAsKey",
            aniListTypeKey = if (query.isEmpty()) "aniListType" else "aniListTypeAsKey",
        )

        val popularityColumn = when (year) {
            DataYear.YEAR_2023 -> ""
            DataYear.YEAR_2024 -> if (seriesFilterParams.showOnlyConfirmedTags) {
                "confirmed2024"
            } else {
                "inferred2024"
            }
            DataYear.YEAR_2025 -> if (seriesFilterParams.showOnlyConfirmedTags) {
                "confirmed2025"
            } else {
                "inferred2025"
            }
        }
        var whereStatement = ""
        if (favoritesStatement.isNotEmpty() ||
            filteredSourcesStatement.isNotEmpty() ||
            year != DataYear.YEAR_2023
        ) {
            whereStatement += "WHERE "
            if (year != DataYear.YEAR_2023) {
                whereStatement += "$popularityColumn > 0"
                if (favoritesStatement.isNotEmpty() || filteredSourcesStatement.isNotEmpty()) {
                    whereStatement += " AND "
                }
            }
            whereStatement += favoritesStatement
            if (favoritesStatement.isNotEmpty() && filteredSourcesStatement.isNotEmpty()) {
                whereStatement += " AND"
            }
            whereStatement += filteredSourcesStatement
        }

        val additionalSelectStatement =
            ", seriesEntry_fts.aniListType as aniListTypeAsKey, seriesEntry_fts.source as sourceAsKey, seriesEntry_fts.uuid as uuidAsKey"

        val nameOrderBy = when (languageOption) {
            AniListLanguageOption.DEFAULT -> "titlePreferred"
            AniListLanguageOption.ENGLISH -> "titleEnglish"
            AniListLanguageOption.NATIVE -> "titleNative"
            AniListLanguageOption.ROMAJI -> "titleRomaji"
        }
        val ascending = if (seriesFilterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (seriesFilterParams.sortOption) {
            SeriesSearchSortOption.RANDOM -> "ORDER BY orderIndex"
            SeriesSearchSortOption.NAME -> "ORDER BY seriesEntry.$nameOrderBy COLLATE NOCASE"
            SeriesSearchSortOption.POPULARITY -> "ORDER BY seriesEntry." +
                    "${popularityColumn.ifEmpty { "inferred2025" }} COLLATE NOCASE"
        } + " $ascending" + " NULLS LAST"
        val randomSortSelectSuffix =
            (", substr(seriesEntry_fts.counter * 0.$randomSeed," +
                    " length(seriesEntry_fts.counter) + 2) as orderIndex")
                .takeIf { seriesFilterParams.sortOption == SeriesSearchSortOption.RANDOM }
                .orEmpty()

        if (query.isEmpty()) {
            val joinStatement = """
                LEFT OUTER JOIN seriesUserEntry
                ON seriesEntry.uuid = seriesUserEntry.seriesId
            """.trimIndent()

            val countStatement = """
                SELECT COUNT(*) FROM seriesEntry
                $joinStatement
                $whereStatement
            """.trimIndent()
            val statement = """
                SELECT seriesEntry.*, seriesUserEntry.favorite${
                randomSortSelectSuffix.replace(
                    "_fts",
                    ""
                )
            }
                FROM seriesEntry
                $joinStatement
                $whereStatement
                $sortSuffix
            """.trimIndent()

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                countStatement = countStatement,
                statement = statement,
                tableNames = listOf("seriesEntry", "seriesUserEntry"),
                mapper = SqlCursor::toSeriesWithUserData,
            )
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("seriesEntry_fts.$it", queries)})"
        }

        val joinStatement = """
            LEFT OUTER JOIN seriesUserEntry
            ON uuidAsKey = seriesUserEntry.seriesId
        """.trimIndent()

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "seriesEntry_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalSelectStatement = additionalSelectStatement,
            additionalJoinStatement = joinStatement,
            andStatement = whereStatement,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "seriesEntry",
            ftsTableName = "seriesEntry_fts",
            select = "seriesEntry.*, seriesUserEntry.favorite",
            idField = "id",
            likeOrderBy = "",
            orderBy = sortSuffix,
            randomSeed = randomSeed
                .takeIf { seriesFilterParams.sortOption == SeriesSearchSortOption.RANDOM },
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalSelectStatement = additionalSelectStatement,
            additionalJoinStatement = joinStatement,
            andStatement = whereStatement,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("seriesEntry_fts", "seriesUserEntry"),
            mapper = SqlCursor::toSeriesWithUserData,
        )
    }

    suspend fun searchSeriesForAutocomplete(query: String): List<SeriesEntry> {
        if (query.isBlank()) return emptyList()
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "titleEnglish",
            "titleRomaji",
            "titleNative",
        )
        val matchQuery = buildString {
            append("'{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("seriesEntry_fts.$it", queries)})"
        }
        val statement = DaoUtils.buildSearchStatement(
            tableName = "seriesEntry",
            ftsTableName = "seriesEntry_fts",
            idField = "id",
            likeOrderBy = "ORDER BY rank",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
        ) + " LIMIT 10"

        return DaoUtils.makeQuery(
            driver = driver(),
            statement = statement,
            tableNames = listOf("seriesEntry", "seriesEntry_fts"), mapper = SqlCursor::toSeriesEntry
        ).awaitAsList()
    }

    suspend fun hasRallies(series: String) = seriesDao().getRallyCount(series)
        .awaitAsOne() > 0

    private fun getFilteredSourcesStatement(
        seriesFilterParams: SeriesSortFilterController.FilterParams,
        sourceKey: String = "source",
        aniListTypeKey: String = "aniListType",
    ): String {
        val filteredSources = seriesFilterParams.sourceIn
            .flatMap {
                when (it) {
                    SeriesFilterOption.ALL -> SeriesSource.entries
                    SeriesFilterOption.ANIME_MANGA -> listOf(
                        SeriesSource.ANIME,
                        SeriesSource.MANGA,
                    )
                    SeriesFilterOption.GAMES -> listOf(SeriesSource.GAME, SeriesSource.VIDEO_GAME)
                    SeriesFilterOption.TV -> listOf(SeriesSource.TV)
                    SeriesFilterOption.MOVIES -> listOf(SeriesSource.MOVIE)
                    SeriesFilterOption.BOOKS -> listOf(
                        SeriesSource.BOOK,
                        SeriesSource.LIGHT_NOVEL,
                        SeriesSource.NOVEL
                    )
                    SeriesFilterOption.WEB_SERIES -> listOf(
                        SeriesSource.WEB_NOVEL,
                        SeriesSource.WEB_SERIES,
                        SeriesSource.WEBTOON,
                    )
                    SeriesFilterOption.VISUAL_NOVELS -> listOf(SeriesSource.VISUAL_NOVEL)
                    SeriesFilterOption.MUSIC -> listOf(SeriesSource.MUSIC)
                    SeriesFilterOption.MULTIMEDIA -> listOf(SeriesSource.MULTIMEDIA_PROJECT)
                    SeriesFilterOption.OTHER -> listOf(SeriesSource.COMIC, SeriesSource.OTHER)
                }
            }

        if (filteredSources.isEmpty()) return ""

        var statement = "("
        filteredSources.forEachIndexed { index, source ->
            if (index != 0) {
                statement += " OR "
            }
            statement += "$sourceKey = '${source.name}'"
        }
        if (filteredSources.contains(SeriesSource.ANIME)) {
            statement += " OR $aniListTypeKey = 'ANIME'"
        }
        if (filteredSources.contains(SeriesSource.MANGA)) {
            statement += " OR $aniListTypeKey = 'MANGA'"
        }
        statement += ")"
        return statement
    }

    // Some tags were adjusted between years, and the most recent list may not have all
    // of the prior tags. In those cases, mock a response.
    private fun fallbackSeriesWithUserData(id: String) = SeriesWithUserData(
        series = SeriesEntry(
            id = id,
            uuid = id,
            notes = null,
            aniListId = null,
            aniListType = null,
            wikipediaId = null,
            source = SeriesSource.NONE,
            titlePreferred = id,
            titleEnglish = id,
            titleRomaji = id,
            titleNative = id,
            link = null,
            inferred2024 = 1,
            inferred2025 = 1,
            confirmed2024 = 0,
            confirmed2025 = 0,
            counter = 1,
        ),
        userEntry = SeriesUserEntry(
            seriesId = id,
            favorite = false,
        )
    )
}
