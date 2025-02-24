package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.TagEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun SqlCursor.toSeriesEntry() = SeriesEntry(
    getString(0)!!,
    getString(1),
    getBoolean(2)!!,
    getBoolean(3)!!,
)

fun SqlCursor.toMerchEntry() = MerchEntry(
    getString(0)!!,
    getString(1),
    getBoolean(2)!!,
    getBoolean(3)!!,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TagEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> TagEntryQueries = { database().tagEntryQueries },
) {
    fun getSeries(year: DataYear): PagingSource<Int, SeriesEntry> {
        val countStatement = "SELECT COUNT(*) FROM seriesEntry WHERE has${year.year} = 1"
        val statement = "SELECT * FROM seriesEntry WHERE has${year.year} = 1 ORDER BY name COLLATE NOCASE"
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
        val statement = "SELECT * FROM merchEntry WHERE has${year.year} = 1 ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry"),
            mapper = SqlCursor::toMerchEntry,
        )
    }

    fun searchSeries(query: String): PagingSource<Int, SeriesEntry> {
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val likeAndQuery = DaoUtils.makeLikeAndQuery("seriesEntry_fts.name", queries)

        val matchQuery = "'{ name } : $matchOrQuery'"
        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "seriesEntry_fts",
            idField = "name",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "seriesEntry",
            ftsTableName = "seriesEntry_fts",
            idField = "name",
            likeOrderBy = "ORDER BY seriesEntry_fts.name COLLATE NOCASE",
            matchQuery = matchQuery,
            likeStatement = likeAndQuery,
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
        dao().run {
            val seriesId = tagMapQuery.series
            if (seriesId != null) {
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> getBoothsBySeriesIdConfirmed2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> getBoothsBySeriesIdConfirmed2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> getBoothsBySeriesId2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> getBoothsBySeriesId2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            } else {
                val merchId = tagMapQuery.merch!!
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> getBoothsByMerchIdConfirmed2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> getBoothsByMerchIdConfirmed2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> getBoothsByMerchId2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> getBoothsByMerchId2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            }
        }.filterNotNull().toSet()
}
