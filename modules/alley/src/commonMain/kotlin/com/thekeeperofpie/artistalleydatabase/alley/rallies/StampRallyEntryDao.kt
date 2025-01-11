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
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry.GetEntry
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import kotlinx.serialization.json.Json

fun SqlCursor.toStampRallyEntry(json: Json): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(json::decodeFromString),
            links = getString(4)!!.let(json::decodeFromString),
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
    private val json: Json,
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
        val booleanOptions = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "stampRallyUserEntry.favorite:1"
        }

        val filterParamsQueryPieces = filterParamsQuery(filterParams)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "fandom:$it",
                    "tables:$it",
                )
            }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY stampRallyEntry.FIELD $ascending"
        val sortSuffix = when (filterParams.sortOption) {
            StampRallySearchSortOption.MAIN_TABLE -> basicSortSuffix.replace(
                "FIELD",
                "hostTable COLLATE NOCASE"
            )
            StampRallySearchSortOption.FANDOM -> basicSortSuffix.replace(
                "FIELD",
                "fandom COLLATE NOCASE"
            )
            StampRallySearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
            StampRallySearchSortOption.PRIZE_LIMIT -> basicSortSuffix.replace(
                "FIELD",
                "prizeLimit"
            ) + " NULLS LAST"
            StampRallySearchSortOption.TOTAL_COST -> basicSortSuffix.replace(
                "FIELD",
                "totalCost"
            ) + " NULLS LAST"
        }
        val randomSortSuffix = (", substr(stampRallyEntry.counter * 0.${searchQuery.randomSeed}," +
                " length(stampRallyEntry.counter) + 2) as orderIndex")
            .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
            .orEmpty()
        val selectSuffix = ", stampRallyUserEntry.favorite, stampRallyUserEntry.ignored, " +
                "stampRallyUserEntry.notes$randomSortSuffix"

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM stampRallyEntry
                LEFT OUTER JOIN stampRallyUserEntry
                ON stampRallyEntry.id = stampRallyUserEntry.stampRallyId
                """.trimIndent() + sortSuffix

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                statement = statement,
                tableNames = listOf("stampRallyEntry"),
                mapper = { it.toStampRallyEntry(json) },
            )
        }

        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val booleanArguments = booleanOptions.joinToString(separator = " ")
        val separator = " ".takeIf {
            optionsArguments.isNotEmpty() && booleanArguments.isNotEmpty()
        }
        val bindArguments = (optionsArguments + separator + booleanArguments)
            .filterNotNull()
            .filter { it.isNotEmpty() }
        val statement = (bindArguments + filterParamsQueryPieces).joinToString("\nINTERSECT\n") {
            """
                SELECT *$selectSuffix
                FROM stampRallyEntry
                LEFT OUTER JOIN stampRallyUserEntry
                ON stampRallyEntry.id = stampRallyUserEntry.stampRallyId
                WHERE stampRallyEntry MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("stampRallyEntry", "stampRallyEntry_fts"),
            parameters = bindArguments + filterParamsQueryPieces,
            mapper = { it.toStampRallyEntry(json) },
        )
    }

    private fun filterParamsQuery(
        filterParams: StampRallySortFilterViewModel.FilterParams,
    ): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterParams.fandom.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "fandom:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        filterParams.tables.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "tables:${DatabaseUtils.wrapMatchQuery(it)}" }
        }

        return queryPieces
    }
}
