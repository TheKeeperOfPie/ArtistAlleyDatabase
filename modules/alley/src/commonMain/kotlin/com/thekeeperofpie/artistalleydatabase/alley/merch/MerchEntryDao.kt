package com.thekeeperofpie.artistalleydatabase.alley.merch

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.MerchQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

fun SqlCursor.toMerchEntry() = MerchEntry(
    name = getString(0)!!,
    notes = getString(1),
    categories = getString(2),
    has2024 = getBoolean(3)!!,
    has2025 = getBoolean(4)!!,
)

class MerchEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val merchDao: suspend () -> MerchQueries = { database().merchQueries },
) {
    suspend fun getMerchById(id: String) = merchDao().getMerchById(id).awaitAsOneOrNull()

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

    suspend fun getMerchEntries(year: DataYear) = when (year) {
        DataYear.YEAR_2023 -> emptyList()
        DataYear.YEAR_2024 -> merchDao().getMerchEntries2024().awaitAsList()
        DataYear.YEAR_2025 -> merchDao().getMerchEntries2025().awaitAsList()
    }.filterNot { it.name.contains("Commissions") }

    fun searchMerch(year: DataYear, query: String): PagingSource<Int, MerchEntry> {
        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)

        val yearFilter = when (year) {
            DataYear.YEAR_2023 -> ""
            DataYear.YEAR_2024 -> "has2024 = 1 AND "
            DataYear.YEAR_2025 -> "has2025 = 1 AND "
        }
        val likeAndQuery = yearFilter +
                DaoUtils.makeLikeAndQuery("merchEntry_fts.name", queries)

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
}
