package com.thekeeperofpie.artistalleydatabase.alley.database

import androidx.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers

// TODO: On js target, "1" isn't coerced to true properly
fun SqlCursor.getBooleanFixed(index: Int) =
    (getBoolean(index) == true) || getString(index).toString() == "1"

object DaoUtils {

    fun createAlleySqlDatabase(driver: SqlDriver) = AlleySqlDatabase(
        driver = driver,
        artistEntry2023Adapter = ColumnAdapters.artistEntry2023Adapter,
        artistEntry2024Adapter = ColumnAdapters.artistEntry2024Adapter,
        artistEntry2025Adapter = ColumnAdapters.artistEntry2025Adapter,
        artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
        artistEntryAnimeNyc2024Adapter = ColumnAdapters.artistEntryAnimeNyc2024Adapter,
        artistEntryAnimeNyc2025Adapter = ColumnAdapters.artistEntryAnimeNyc2025Adapter,
        stampRallyEntry2023Adapter = ColumnAdapters.stampRallyEntry2023Adapter,
        stampRallyEntry2024Adapter = ColumnAdapters.stampRallyEntry2024Adapter,
        stampRallyEntry2025Adapter = ColumnAdapters.stampRallyEntry2025Adapter,
        stampRallyEntryAnimeExpo2026Adapter = ColumnAdapters.stampRallyEntryAnimeExpo2026Adapter,
        artistNotesAdapter = ArtistNotes.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
        ),
        artistUserEntryAdapter = ArtistUserEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter),
        seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
    )

    fun <T : Any> queryPagingSource(
        driver: suspend () -> SqlDriver,
        database: suspend () -> AlleySqlDatabase,
        countStatement: String,
        statement: String,
        tableNames: List<String>,
        parameters: List<String> = emptyList(),
        mapper: (SqlCursor, AlleySqlDatabase) -> T,
    ): PagingSource<Int, T> {
        return OffsetQueryPagingSource(
            queryProvider = { database, limit, offset ->
                val statement = "$statement LIMIT $limit OFFSET $offset"
                makeQuery(driver(), statement, tableNames, parameters, mapper = {
                    mapper(it, database)
                })
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
        additionalSelectStatement: String = "",
        additionalJoinStatement: String = "",
        andStatement: String = "",
    ) = """
        SELECT COUNT(*) FROM (
            SELECT * FROM (
                SELECT * FROM(
                    SELECT $ftsTableName.$idField as idAsKey$additionalSelectStatement
                    FROM $ftsTableName
                    WHERE $ftsTableName MATCH $matchQuery
                )
                UNION
                SELECT * FROM(
                    SELECT $ftsTableName.$idField$additionalSelectStatement
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
        additionalSelectStatement: String = "",
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
                    SELECT $ftsTableName.$idField as idAsKey$additionalSelectStatement, rank as rank$randomOrderIndex
                    FROM $ftsTableName
                    WHERE $ftsTableName MATCH $matchQuery
                    ORDER BY rank
                )
                UNION
                SELECT * FROM(
                    SELECT $ftsTableName.$idField$additionalSelectStatement, 999$randomOrderIndex
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

    // TODO: js target boolean equality broken
    @Suppress("EQUALITY_NOT_APPLICABLE_WARNING")
    fun coerceBooleanForJs(value: Boolean?) = value == true || value.toString() == "1"
}
