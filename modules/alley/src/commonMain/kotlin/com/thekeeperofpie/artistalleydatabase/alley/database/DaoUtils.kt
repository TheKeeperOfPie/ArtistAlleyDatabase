package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers

object DaoUtils {

    private val countReplaceRegex = Regex("(\\QSELECT\\E )(.*?)(,|\\n|\\Q FROM\\E)")

    fun <T : Any> queryPagingSource(
        driver: SqlDriver,
        database: suspend () -> SuspendingTransacter,
        statement: String,
        tableNames: List<String>,
        parameters: List<String> = emptyList(),
        mapper: (SqlCursor) -> T,
    ): PagingSource<Int, T> {
        val countStatement = statement.replace(countReplaceRegex) {
            "${it.groupValues[1]}COUNT (*)${it.groupValues[3]}"
        }
        val countQuery = makeQuery(
            driver = driver,
            statement = countStatement,
            tableNames = tableNames,
            mapper = { it.getLong(0)!!.toInt() },
        )
        return OffsetQueryPagingSource(
            queryProvider = { limit, offset ->
                val statement = "$statement LIMIT $limit OFFSET $offset"
                makeQuery(driver, statement, tableNames, parameters, mapper)
            },
            countQuery = countQuery,
            transacter = database,
            context = PlatformDispatchers.IO,
            initialOffset = 0,
        )
    }

    fun <T : Any> makeQuery(
        driver: SqlDriver,
        statement: String,
        tableNames: List<String>,
        parameters: List<String> = emptyList(),
        mapper: (SqlCursor) -> T,
    ) = object : Query<T>(mapper = mapper) {
        override fun addListener(listener: Listener) {
            tableNames.forEach {
                driver.addListener(it, listener = listener)
            }
        }

        override fun removeListener(listener: Listener) {
            tableNames.forEach {
                driver.removeListener(it, listener = listener)
            }
        }

        override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>) =
            driver.executeQuery(
                null,
                statement,
                mapper = mapper,
                parameters = parameters.size,
                binders = {
                    parameters.forEachIndexed { index, arg ->
                        bindString(index, arg)
                    }
                },
            )
    }
}
