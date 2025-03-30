package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
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

fun SqlCursor.toSeriesEntry() = SeriesEntry(
    id = getString(0)!!,
    notes = getString(1),
    titleEnglish = getString(2)!!,
    titleRomaji = getString(3)!!,
    titleNative = getString(4)!!,
    has2024 = getBoolean(5)!!,
    has2025 = getBoolean(6)!!,
)

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
    fun getSeries(languageOption: AniListLanguageOption, year: DataYear): PagingSource<Int, SeriesEntry> {
        val countStatement = "SELECT COUNT(*) FROM seriesEntry WHERE has${year.year} = 1"
        val orderBy = when (languageOption) {
            AniListLanguageOption.DEFAULT,
            AniListLanguageOption.ROMAJI -> "titleRomaji"
            AniListLanguageOption.ENGLISH -> "titleEnglish"
            AniListLanguageOption.NATIVE -> "titleNative"
        }
        val statement =
            "SELECT * FROM seriesEntry WHERE has${year.year} = 1 ORDER BY $orderBy COLLATE NOCASE"
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

    fun searchSeries(languageOption: AniListLanguageOption, query: String): PagingSource<Int, SeriesEntry> {
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
            AniListLanguageOption.DEFAULT,
            AniListLanguageOption.ROMAJI -> "titleRomaji"
            AniListLanguageOption.ENGLISH -> "titleEnglish"
            AniListLanguageOption.NATIVE -> "titleNative"
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
