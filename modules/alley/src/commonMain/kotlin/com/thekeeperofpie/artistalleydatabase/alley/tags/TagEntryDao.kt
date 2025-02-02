package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.TagEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

fun SqlCursor.toSeriesEntry() = SeriesEntry(
    getString(0)!!,
    getString(1),
)

fun SqlCursor.toMerchEntry() = MerchEntry(
    getString(0)!!,
    getString(1),
)

@OptIn(ExperimentalCoroutinesApi::class)
class TagEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> TagEntryQueries = { database().tagEntryQueries },
) {
    fun getSeries(): PagingSource<Int, SeriesEntry> {
        val countStatement = "SELECT COUNT(*) FROM seriesEntry ORDER BY name COLLATE NOCASE"
        val statement = "SELECT * FROM seriesEntry ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("seriesEntry"),
            mapper = SqlCursor::toSeriesEntry,
        )
    }

    fun getSeriesSize() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getSeriesSize().asFlow() }
            .mapLatest { it.awaitAsOne().toInt() }

    fun getMerch(): PagingSource<Int, MerchEntry> {
        val countStatement = "SELECT COUNT(*) FROM merchEntry ORDER BY name COLLATE NOCASE"
        val statement = "SELECT * FROM merchEntry ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("merchEntry"),
            mapper = SqlCursor::toMerchEntry,
        )
    }

    fun getMerchSize() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getMerchSize().asFlow() }
            .mapLatest { it.awaitAsOne().toInt() }

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

    suspend fun getBooths(activeYearIs2025: Boolean, tagMapQuery: TagMapQuery): Set<String> =
        dao().run {
            val seriesId = tagMapQuery.series
            if (seriesId != null) {
                if (tagMapQuery.showOnlyConfirmedTags) {
                    if (activeYearIs2025) {
                        getBoothsBySeriesIdConfirmed2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    } else {
                        getBoothsBySeriesIdConfirmed2024(seriesId)
                            .awaitAsList()
                    }
                } else {
                    if (activeYearIs2025) {
                        getBoothsBySeriesId2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    } else {
                        getBoothsBySeriesId2024(seriesId)
                            .awaitAsList()
                    }
                }
            } else {
                val merchId = tagMapQuery.merch!!
                if (tagMapQuery.showOnlyConfirmedTags) {
                    if (activeYearIs2025) {
                        getBoothsByMerchIdConfirmed2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    } else {
                        getBoothsByMerchIdConfirmed2024(merchId)
                            .awaitAsList()
                    }
                } else {
                    if (activeYearIs2025) {
                        getBoothsByMerchId2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    } else {
                        getBoothsByMerchId2024(merchId)
                            .awaitAsList()
                    }
                }
            }
        }.filterNotNull().toSet()
}
