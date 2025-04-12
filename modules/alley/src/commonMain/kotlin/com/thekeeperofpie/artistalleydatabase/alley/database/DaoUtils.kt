package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.paging.PagingSource
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.serialization.json.Json

object DaoUtils {

    val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) = Json.decodeFromString<List<String>>(databaseValue)
        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
        override fun decode(databaseValue: String) =
            DataYear.entries.first { it.serializedName == databaseValue }

        override fun encode(value: DataYear) = value.serializedName
    }

    private val countReplaceRegex = Regex("(\\QSELECT\\E )(.*?)(,|\\n|\\Q FROM\\E)")

    fun <T : Any> queryPagingSource(
        driver: suspend () -> SqlDriver,
        database: suspend () -> SuspendingTransacter,
        countStatement: String,
        statement: String,
        tableNames: List<String>,
        parameters: List<String> = emptyList(),
        mapper: (SqlCursor) -> T,
    ): PagingSource<Int, T> {
        return OffsetQueryPagingSource(
            queryProvider = { limit, offset ->
                val statement = "$statement LIMIT $limit OFFSET $offset"
                makeQuery(driver(), statement, tableNames, parameters, mapper)
            },
            countQuery = {
                makeQuery(
                    driver = driver(),
                    statement = countStatement,
                    tableNames = tableNames,
                    mapper = { it.getLong(0)!!.toInt() },
                )
            },
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

    fun makeMatchAndQuery(query: List<String>) = query.map { it.replace('"', ' ') }
        .filter(String::isNotBlank)
        .joinToString(separator = " AND ") { "\"${it}*\"" }

    fun makeLikeAndQuery(field: String, query: List<String>) = query.map { it.replace('"', ' ') }
        .filter(String::isNotBlank)
        .joinToString(separator = " AND ") {
            "$field LIKE '%$it%'"
        }

    fun buildSearchCountStatement(
        ftsTableName: String,
        idField: String,
        matchQuery: String,
        likeStatement: String,
        additionalJoinStatement: String = "",
        andStatement: String = "",
    ) = """
        SELECT COUNT(*) FROM (
            SELECT * FROM (
                SELECT * FROM(
                    SELECT $ftsTableName.$idField as idAsKey
                    FROM $ftsTableName
                    WHERE $ftsTableName MATCH $matchQuery
                )
                UNION
                SELECT * FROM(
                    SELECT $ftsTableName.$idField
                    FROM $ftsTableName
                    WHERE (
                        $likeStatement
                    )
                )
            )
            JOIN $ftsTableName
            ON idAsKey = $ftsTableName.$idField
            $additionalJoinStatement
            $andStatement
            GROUP BY idAsKey
        )
        """.trimIndent()

    fun buildSearchStatement(
        tableName: String,
        ftsTableName: String,
        idField: String,
        likeOrderBy: String,
        matchQuery: String,
        likeStatement: String,
        select: String = "$tableName.*",
        additionalJoinStatement: String = "",
        orderBy: String = "ORDER BY rank",
        randomSeed: Int? = null,
        andStatement: String = "",
    ): String {
        val randomOrderIndex = randomSeed?.let {
            (", substr($ftsTableName.counter * 0.$randomSeed," +
                    " length($ftsTableName.counter) + 2) as orderIndex")
        }.orEmpty()
        return """
            SELECT $select FROM (
                SELECT * FROM(
                    SELECT $ftsTableName.$idField as idAsKey, rank as rank$randomOrderIndex
                    FROM $ftsTableName
                    WHERE $ftsTableName MATCH $matchQuery
                    ORDER BY rank
                )
                UNION
                SELECT * FROM(
                    SELECT $ftsTableName.$idField, 999$randomOrderIndex
                    FROM $ftsTableName
                    WHERE (
                        $likeStatement
                    )
                    $likeOrderBy
                )
            )
            JOIN $tableName
            ON idAsKey = $tableName.$idField
            $additionalJoinStatement
            $andStatement
            GROUP BY idAsKey
            $orderBy
            """.trimIndent()
    }
}
