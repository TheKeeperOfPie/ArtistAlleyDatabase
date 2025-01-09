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
        val statement = "SELECT * FROM seriesEntry ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
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
        val statement = "SELECT * FROM merchEntry ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
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
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY seriesEntry_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM seriesEntry
                JOIN seriesEntry_fts ON seriesEntry.name = seriesEntry_fts.name
                WHERE seriesEntry_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("seriesEntry", "seriesEntry_fts"),
            parameters = bindArguments,
            mapper = SqlCursor::toSeriesEntry,
        )
    }

    fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY merchEntry_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM merchEntry
                JOIN merchEntry_fts ON merchEntry.name = merchEntry_fts.name
                WHERE merchEntry_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("merchEntry", "merchEntry_fts"),
            parameters = bindArguments,
            mapper = SqlCursor::toMerchEntry,
        )
    }

    suspend fun getBooths(tagMapQuery: TagMapQuery): Set<String> =
        dao().run {
            val seriesId = tagMapQuery.series
            if (seriesId != null) {
                if (tagMapQuery.showOnlyConfirmedTags) {
                    getBoothsBySeriesIdConfirmed(seriesId)
                } else {
                    getBoothsBySeriesId(seriesId)
                }
            } else {
                val merchId = tagMapQuery.merch!!
                if (tagMapQuery.showOnlyConfirmedTags) {
                    getBoothsByMerchIdConfirmed(merchId)
                } else {
                    getBoothsByMerchId(merchId)
                }
            }
        }.awaitAsList().toSet()
}
