package com.thekeeperofpie.artistalleydatabase.alley.rallies

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry.GetEntry
import kotlinx.serialization.json.Json

fun SqlCursor.toStampRallyWithUserData(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
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
            notes = getString(12),
        )
    )
}

private fun GetEntry.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
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
        notes = userNotes,
    )
)

class StampRallyEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> StampRallyEntryQueries = { database().stampRallyEntryQueries },
) {
    suspend fun getEntry(id: String) = dao()
        .getEntry(id)
        .awaitAsOneOrNull()
        ?.toStampRallyWithUserData()

    suspend fun getEntryWithArtists(id: String) =
        dao().transactionWithResult {
            val stampRally = getEntry(id) ?: return@transactionWithResult null
            val artists = dao().getArtistEntries(id).awaitAsList()
            StampRallyWithArtistsEntry(stampRally, artists)
        }

    fun search(
        query: String,
        searchQuery: StampRallySearchQuery,
    ): PagingSource<Int, StampRallyWithUserData> {
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "stampRallyUserEntry.favorite = 1"
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            StampRallySearchSortOption.MAIN_TABLE ->
                "ORDER BY stampRallyEntry_fts.hostTable COLLATE NOCASE $ascending"
            StampRallySearchSortOption.FANDOM ->
                "ORDER BY stampRallyEntry_fts.fandom COLLATE NOCASE $ascending"
            StampRallySearchSortOption.RANDOM -> "ORDER BY orderIndex $ascending"
            StampRallySearchSortOption.PRIZE_LIMIT ->
                "ORDER BY stampRallyEntry_fts.prizeLimit $ascending NULLS LAST"
            StampRallySearchSortOption.TOTAL_COST ->
                "ORDER BY stampRallyEntry_fts.totalCost $ascending NULLS LAST"
        }
        val randomSortSelectSuffix =
            (", substr(stampRallyEntry_fts.counter * 0.${searchQuery.randomSeed}," +
                " length(stampRallyEntry_fts.counter) + 2) as orderIndex")
            .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
            .orEmpty()
        val selectSuffix = ", stampRallyUserEntry.favorite, stampRallyUserEntry.ignored, " +
                "stampRallyUserEntry.notes"

        val matchOptions = mutableListOf<String>()
        filterParams.fandom.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(fandom : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }
        filterParams.tables.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(tables : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }

        if (query.isEmpty() && matchOptions.isEmpty()) {
            val andStatement = andClauses.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "WHERE ", separator = "\nAND ").orEmpty()
            val countStatement = """
                SELECT COUNT(*)
                FROM stampRallyEntry_fts
                LEFT OUTER JOIN stampRallyUserEntry
                ON stampRallyEntry_fts.id = stampRallyUserEntry.stampRallyId
                $andStatement
                """.trimIndent()
            val statement = """
                SELECT stampRallyEntry_fts.*$selectSuffix$randomSortSelectSuffix
                FROM stampRallyEntry_fts
                LEFT OUTER JOIN stampRallyUserEntry
                ON stampRallyEntry_fts.id = stampRallyUserEntry.stampRallyId
                $andStatement
                $sortSuffix
                """.trimIndent()

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                countStatement = countStatement,
                statement = statement,
                tableNames = listOf("stampRallyEntry_fts"),
                mapper = SqlCursor::toStampRallyWithUserData,
            )
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "fandom",
            "tables",
        )
        val matchQuery = buildString {
            append("'")
            append(matchOptions.joinToString(separator = " ", postfix = " "))
            append("{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("stampRallyEntry_fts.$it", queries)})"
        }

        val andStatement = andClauses.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "WHERE ", separator = "\nAND ").orEmpty()

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "stampRallyEntry_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "stampRallyEntry",
            ftsTableName = "stampRallyEntry_fts",
            select = "stampRallyEntry.*$selectSuffix",
            idField = "id",
            likeOrderBy = "",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = """
                LEFT OUTER JOIN stampRallyUserEntry
                ON idAsKey = stampRallyUserEntry.stampRallyId
                """.trimIndent(),
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
            tableNames = listOf("stampRallyEntry_fts"),
            mapper = SqlCursor::toStampRallyWithUserData,
        )
    }
}
