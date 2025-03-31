package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.MerchQueries
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesQueries
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun SqlCursor.toSeriesEntry(): SeriesEntry {
    val source = getString(4)
    return SeriesEntry(
        id = getString(0)!!,
        notes = getString(1),
        aniListId = getLong(2),
        aniListType = getString(3),
        source = SeriesSource.entries.find { it.name == source },
        titlePreferred = getString(5)!!,
        titleEnglish = getString(6)!!,
        titleRomaji = getString(7)!!,
        titleNative = getString(8)!!,
        link = getString(9),
        has2024 = getBoolean(10)!!,
        has2025 = getBoolean(11)!!,
    )
}

fun SqlCursor.toMerchEntry() = MerchEntry(
    getString(0)!!,
    getString(1),
    getBoolean(2)!!,
    getBoolean(3)!!,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TagEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val seriesDao: suspend () -> SeriesQueries = { database().seriesQueries },
    private val merchDao: suspend () -> MerchQueries = { database().merchQueries },
) {
    suspend fun getSeriesById(id: String): SeriesEntry =
        seriesDao().getSeriesById(id).awaitAsOneOrNull()
        // Some tags were adjusted between years, and the most recent list may not have all
        // of the prior tags. In those cases, mock a response.
            ?: SeriesEntry(
                id = id,
                notes = null,
                aniListId = null,
                aniListType = null,
                source = SeriesSource.NONE,
                titlePreferred = id,
                titleEnglish = id,
                titleRomaji = id,
                titleNative = id,
                link = null,
                has2024 = false,
                has2025 = false,
            )

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

    fun getMerch(year: DataYear): PagingSource<Int, MerchEntry> {
        val countStatement = "SELECT COUNT(*) FROM merchEntry WHERE has${year.year} = 1"
        val statement =
            "SELECT * FROM merchEntry WHERE has${year.year} = 1 ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry"),
            mapper = SqlCursor::toMerchEntry,
        )
    }

    fun searchSeries(
        languageOption: AniListLanguageOption,
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

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("seriesEntry_fts.$it", queries)})"
        }
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

    fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val likeAndQuery = DaoUtils.makeLikeAndQuery("merchEntry_fts.name", queries)

        val matchQuery = "'{ name } : $matchOrQuery'"
        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "merchEntry_fts",
            idField = "name",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "merchEntry",
            ftsTableName = "merchEntry_fts",
            idField = "name",
            likeOrderBy = "ORDER BY merchEntry_fts.name COLLATE NOCASE",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry_fts"),
            mapper = SqlCursor::toMerchEntry,
        )
    }

    suspend fun getBooths(year: DataYear, tagMapQuery: TagMapQuery): Set<String> =
        if (year == DataYear.YEAR_2023) {
            emptySet()
        } else {
            val seriesDao = seriesDao()
            val merchDao = merchDao()
            val seriesId = tagMapQuery.series
            if (seriesId != null) {
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> seriesDao.getBoothsBySeriesIdConfirmed2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> seriesDao.getBoothsBySeriesIdConfirmed2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> seriesDao.getBoothsBySeriesId2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> seriesDao.getBoothsBySeriesId2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            } else {
                val merchId = tagMapQuery.merch!!
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> merchDao.getBoothsByMerchIdConfirmed2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> merchDao.getBoothsByMerchIdConfirmed2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> merchDao.getBoothsByMerchId2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> merchDao.getBoothsByMerchId2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            }.filterNotNull().toSet()
        }
}
