package com.thekeeperofpie.artistalleydatabase.alley.series

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun SqlCursor.toSeriesEntry(): SeriesEntry {
    val source = getString(5)
    return SeriesEntry(
        id = getString(0)!!,
        notes = getString(1),
        aniListId = getLong(2),
        aniListType = getString(3),
        wikipediaId = getLong(4),
        source = SeriesSource.entries.find { it.name == source },
        titlePreferred = getString(6)!!,
        titleEnglish = getString(7)!!,
        titleRomaji = getString(8)!!,
        titleNative = getString(9)!!,
        link = getString(10),
        has2024 = getBoolean(11)!!,
        has2025 = getBoolean(12)!!,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val seriesDao: suspend () -> SeriesQueries = { database().seriesQueries },
) {
    suspend fun getSeriesIds() = seriesDao().getSeriesAndImageIds().awaitAsList()

    suspend fun getSeriesById(id: String): SeriesEntry =
        seriesDao().getSeriesById(id).awaitAsOneOrNull() ?: fallbackSeriesEntry(id)

    suspend fun getSeriesByIds(ids: List<String>): List<SeriesEntry> {
        if (ids.isEmpty()) return emptyList()
        val series = seriesDao().getSeriesByIds(ids).awaitAsList().associateBy { it.id }
        return ids.map { series[it] ?: fallbackSeriesEntry(it) }
    }

    fun getSeries(
        languageOption: AniListLanguageOption,
        year: DataYear,
        seriesFilterState: List<Pair<SeriesFilterOption, Boolean>>,
    ): PagingSource<Int, SeriesEntry> {
        val filteredSources = seriesFilterState
            .filter { (_, enabled) -> enabled }
            .flatMap { (option) ->
                when (option) {
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
        var where = "WHERE has${year.year} = 1"
        if (filteredSources.isNotEmpty()) {
            where += " AND ("
            filteredSources.forEachIndexed { index, source ->
                if (index != 0) {
                    where += " OR "
                }
                where += "source = '${source.name}'"
            }
            if (filteredSources.contains(SeriesSource.ANIME)) {
                where += " OR aniListType = 'ANIME'"
            }
            if (filteredSources.contains(SeriesSource.MANGA)) {
                where += " OR aniListType = 'MANGA'"
            }
            where += ")"
        }

        val countStatement = "SELECT COUNT(*) FROM seriesEntry $where"
        val orderBy = when (languageOption) {
            AniListLanguageOption.DEFAULT -> "titlePreferred"
            AniListLanguageOption.ENGLISH -> "titleEnglish"
            AniListLanguageOption.NATIVE -> "titleNative"
            AniListLanguageOption.ROMAJI -> "titleRomaji"
        }
        val statement =
            "SELECT * FROM seriesEntry $where ORDER BY $orderBy COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("seriesEntry"),
            mapper = SqlCursor::toSeriesEntry,
        )
    }

    fun searchSeries(
        languageOption: AniListLanguageOption,
        year: DataYear,
        query: String,
    ): PagingSource<Int, SeriesEntry> {
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

        val yearFilter = when (year) {
            DataYear.YEAR_2023 -> ""
            DataYear.YEAR_2024 -> "has2024 IS 1 AND ("
            DataYear.YEAR_2025 -> "has2025 IS 1 AND ("
        }
        val yearFilterSuffix = if (yearFilter.isEmpty()) "" else ")"
        val likeStatement = yearFilter + targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("seriesEntry_fts.$it", queries)})"
        } + yearFilterSuffix
        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "seriesEntry_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
        )
        val orderBy = when (languageOption) {
            AniListLanguageOption.DEFAULT -> "titlePreferred"
            AniListLanguageOption.ENGLISH -> "titleEnglish"
            AniListLanguageOption.NATIVE -> "titleNative"
            AniListLanguageOption.ROMAJI -> "titleRomaji"
        }
        val statement = DaoUtils.buildSearchStatement(
            tableName = "seriesEntry",
            ftsTableName = "seriesEntry_fts",
            idField = "id",
            likeOrderBy = "ORDER BY seriesEntry_fts.$orderBy COLLATE NOCASE",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("seriesEntry_fts"),
            mapper = SqlCursor::toSeriesEntry,
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

    // Some tags were adjusted between years, and the most recent list may not have all
    // of the prior tags. In those cases, mock a response.
    private fun fallbackSeriesEntry(id: String) = SeriesEntry(
        id = id,
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
        has2024 = false,
        has2025 = false,
    )
}
