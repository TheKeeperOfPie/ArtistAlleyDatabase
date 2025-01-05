package com.thekeeperofpie.artistalleydatabase.alley.app.dao

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.app.Merch_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Series_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.TagEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import com.thekeeperofpie.artistalleydatabase.app.ArtistAlleyAppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

fun SqlCursor.toSeriesEntry() = Series_entries(
    getString(0)!!,
    getString(1),
).toSeriesEntry()

fun Series_entries.toSeriesEntry() = SeriesEntry(name = name, notes = notes)
fun SeriesEntry.toSqlObject() = Series_entries(name = name, notes = notes)

fun SqlCursor.toMerchEntry() = Merch_entries(
    getString(0)!!,
    getString(1),
).toMerchEntry()

fun Merch_entries.toMerchEntry() = MerchEntry(name = name, notes = notes)
fun MerchEntry.toSqlObject() = Merch_entries(name = name, notes = notes)

@OptIn(ExperimentalCoroutinesApi::class)
class TagEntryDaoImpl(
    private val driver: SqlDriver,
    private val database: suspend () -> ArtistAlleyAppDatabase,
    private val dao: suspend () -> TagEntryQueries = { database().tagEntryQueries },
) : TagEntryDao {
    override fun getSeries(): PagingSource<Int, SeriesEntry> {
        val statement = "SELECT * FROM series_entries ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("series_entries"),
            mapper = SqlCursor::toSeriesEntry,
        )
    }

    override fun getSeriesSize() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getSeriesSize().asFlow() }
            .mapLatest { it.awaitAsOne().toInt() }

    override fun getMerch(): PagingSource<Int, MerchEntry> {
        val statement = "SELECT * FROM merch_entries ORDER BY name COLLATE NOCASE"
        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("merch_entries"),
            mapper = SqlCursor::toMerchEntry,
        )
    }

    override fun getMerchSize() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getMerchSize().asFlow() }
            .mapLatest { it.awaitAsOne().toInt() }

    override suspend fun insertSeries(entries: List<SeriesEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insertSeries(it.toSqlObject())
            }
        }

    override suspend fun insertMerch(entries: List<MerchEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insertMerch(it.toSqlObject())
            }
        }

    override suspend fun clearSeries() = dao().clearSeries()

    override suspend fun clearMerch() = dao().clearMerch()

    override fun searchSeries(query: String): PagingSource<Int, SeriesEntry> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY series_entries_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM series_entries
                JOIN series_entries_fts ON series_entries.name = series_entries_fts.name
                WHERE series_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("series_entries", "series_entries_fts"),
            parameters = bindArguments,
            mapper = SqlCursor::toSeriesEntry,
        )
    }

    override fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "name:$it",
                )
            }

        val sortSuffix = "\nORDER BY merch_entries_fts.name COLLATE NOCASE"
        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val bindArguments = optionsArguments.filterNot { it.isEmpty() }

        val statement = bindArguments.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM merch_entries
                JOIN merch_entries_fts ON merch_entries.name = merch_entries_fts.name
                WHERE merch_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("merch_entries", "merch_entries_fts"),
            parameters = bindArguments,
            mapper = SqlCursor::toMerchEntry,
        )
    }

    override suspend fun getBooths(tagMapQuery: TagMapQuery): Set<String> =
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
