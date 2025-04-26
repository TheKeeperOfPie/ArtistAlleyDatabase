package com.thekeeperofpie.artistalleydatabase.alley.rallies

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023Queries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024Queries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025Queries
import com.thekeeperofpie.artistalleydatabase.alley.artist.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.json.Json
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2023.GetEntry as GetEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2024.GetEntry as GetEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2025.GetEntry as GetEntry2025

fun SqlCursor.toStampRallyWithUserData2023(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.YEAR_2023,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            tableMin = null,
            totalCost = null,
            prizeLimit = null,
            notes = null,
            counter = getLong(5)!!,
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBoolean(6) == true,
            ignored = getBoolean(7) == true,
        )
    )
}

fun SqlCursor.toStampRallyWithUserData2024(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.YEAR_2024,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            tableMin = getLong(5),
            totalCost = getLong(6),
            prizeLimit = getLong(7),
            notes = getString(8),
            counter = getLong(9)!!,
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBoolean(10) == true,
            ignored = getBoolean(11) == true,
        )
    )
}

fun SqlCursor.toStampRallyWithUserData2025(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.YEAR_2025,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            tableMin = getLong(5),
            totalCost = getLong(6),
            prizeLimit = getLong(7),
            notes = getString(8),
            counter = getLong(9)!!,
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBoolean(10) == true,
            ignored = getBoolean(11) == true,
        )
    )
}

private fun GetEntry2023.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.YEAR_2023,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = null,
        totalCost = null,
        prizeLimit = null,
        notes = null,
        counter = counter,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

private fun GetEntry2024.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.YEAR_2024,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prizeLimit = prizeLimit,
        notes = notes,
        counter = counter,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

private fun GetEntry2025.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.YEAR_2025,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prizeLimit = prizeLimit,
        notes = notes,
        counter = counter,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

fun StampRallyEntry2023.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.YEAR_2023,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    tableMin = null,
    totalCost = null,
    prizeLimit = null,
    notes = null,
    counter = counter,
)

fun StampRallyEntry2024.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.YEAR_2024,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    tableMin = tableMin,
    totalCost = totalCost,
    prizeLimit = prizeLimit,
    notes = notes,
    counter = counter,
)

fun StampRallyEntry2025.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.YEAR_2025,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    tableMin = tableMin,
    totalCost = totalCost,
    prizeLimit = prizeLimit,
    notes = notes,
    counter = counter,
)

class StampRallyEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao2023: suspend () -> StampRallyEntry2023Queries = { database().stampRallyEntry2023Queries },
    private val dao2024: suspend () -> StampRallyEntry2024Queries = { database().stampRallyEntry2024Queries },
    private val dao2025: suspend () -> StampRallyEntry2025Queries = { database().stampRallyEntry2025Queries },
) {
    suspend fun getEntry(year: DataYear, stampRallyId: String) =
        when (year) {
            DataYear.YEAR_2023 -> dao2023()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.YEAR_2024 -> dao2024()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.YEAR_2025 -> dao2025()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
        }

    suspend fun getEntryWithArtists(year: DataYear, stampRallyId: String) =
        when (year) {
            DataYear.YEAR_2023 -> {
                dao2023().transactionWithResult {
                    val stampRally =
                        getEntry(year, stampRallyId) ?: return@transactionWithResult null
                    val artists = dao2023().getArtistEntries(stampRallyId).awaitAsList()
                        .map { it.toArtistEntry() }
                    StampRallyWithArtistsEntry(stampRally, artists)
                }
            }
            DataYear.YEAR_2024 -> {
                dao2024().transactionWithResult {
                    val stampRally =
                        getEntry(year, stampRallyId) ?: return@transactionWithResult null
                    val artists = dao2024().getArtistEntries(stampRallyId).awaitAsList()
                        .map { it.toArtistEntry() }
                    StampRallyWithArtistsEntry(stampRally, artists)
                }
            }
            DataYear.YEAR_2025 -> {
                dao2025().transactionWithResult {
                    val stampRally =
                        getEntry(year, stampRallyId) ?: return@transactionWithResult null
                    val artists =
                        dao2025().getArtistEntries(stampRallyId).awaitAsList()
                            .map { it.toArtistEntry() }
                    StampRallyWithArtistsEntry(stampRally, artists)
                }
            }
        }

    fun search(
        year: DataYear,
        query: String,
        searchQuery: StampRallySearchQuery,
        onlyFavorites: Boolean = false,
    ): PagingSource<Int, StampRallyWithUserData> {
        val tableName = "stampRallyEntry${year.year}"
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (onlyFavorites) this += "stampRallyUserEntry.favorite = 1"
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            StampRallySearchSortOption.MAIN_TABLE ->
                "ORDER BY $tableName.hostTable COLLATE NOCASE $ascending"
            StampRallySearchSortOption.FANDOM ->
                "ORDER BY $tableName.fandom COLLATE NOCASE $ascending"
            StampRallySearchSortOption.RANDOM -> "ORDER BY orderIndex $ascending"
            StampRallySearchSortOption.PRIZE_LIMIT ->
                "ORDER BY $tableName.prizeLimit $ascending NULLS LAST"
            StampRallySearchSortOption.TOTAL_COST ->
                "ORDER BY $tableName.totalCost $ascending NULLS LAST"
        }
        val randomSortSelectSuffix =
            (", substr(${tableName}_fts.counter * 0.${searchQuery.randomSeed}," +
                    " length(${tableName}_fts.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
                .orEmpty()
        val selectSuffix = ", stampRallyUserEntry.favorite, stampRallyUserEntry.ignored"

        val matchOptions = mutableListOf<String>()
        filterParams.fandom.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(fandom : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }
        filterParams.tables.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(tables : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }

        if (query.isEmpty() && matchOptions.isEmpty()) {
            val andStatement = andClauses.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "WHERE ", separator = "\nAND ")
                .orEmpty()
            val countStatement = """
                SELECT COUNT(*)
                FROM $tableName
                LEFT OUTER JOIN stampRallyUserEntry
                ON $tableName.id = stampRallyUserEntry.stampRallyId
                $andStatement
                """.trimIndent()
            val statement = """
                SELECT $tableName.*$selectSuffix${randomSortSelectSuffix.replace("_fts", "")}
                FROM $tableName
                LEFT OUTER JOIN stampRallyUserEntry
                ON $tableName.id = stampRallyUserEntry.stampRallyId
                $andStatement
                ${sortSuffix.replace("_fts", "")}
                """.trimIndent()

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                countStatement = countStatement,
                statement = statement,
                tableNames = listOf("${tableName}_fts"),
                mapper = when (year) {
                    DataYear.YEAR_2023 -> SqlCursor::toStampRallyWithUserData2023
                    DataYear.YEAR_2024 -> SqlCursor::toStampRallyWithUserData2024
                    DataYear.YEAR_2025 -> SqlCursor::toStampRallyWithUserData2025
                },
            )
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "fandom",
            "tables",
            "notes".takeIf { year != DataYear.YEAR_2023 },
        )
        val matchQuery = buildString {
            append("'")
            append(matchOptions.joinToString(separator = " ", postfix = " "))
            append("{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("${tableName}_fts.$it", queries)})"
        }

        val andStatement = andClauses.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "WHERE ", separator = "\nAND ").orEmpty()

        val joinStatement = """
            LEFT OUTER JOIN stampRallyUserEntry
            ON idAsKey = stampRallyUserEntry.stampRallyId
        """.trimIndent()

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "${tableName}_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            andStatement = andStatement.replace(tableName, "${tableName}_fts"),
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = tableName,
            ftsTableName = "${tableName}_fts",
            select = "$tableName.*$selectSuffix",
            idField = "id",
            likeOrderBy = "",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            orderBy = sortSuffix,
            randomSeed = searchQuery.randomSeed
                .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM },
            andStatement = andStatement,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("${tableName}_fts"),
            mapper = when (year) {
                DataYear.YEAR_2023 -> SqlCursor::toStampRallyWithUserData2023
                DataYear.YEAR_2024 -> SqlCursor::toStampRallyWithUserData2024
                DataYear.YEAR_2025 -> SqlCursor::toStampRallyWithUserData2025
            },
        )
    }
}
