package com.thekeeperofpie.artistalleydatabase.alley.rallies

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import kotlinx.serialization.json.Json

fun SqlCursor.toStampRallyEntry(json: Json) = StampRallyEntry(
    id = getString(0)!!,
    fandom = getString(1)!!,
    hostTable = getString(2)!!,
    tables = getString(3)!!.let(json::decodeFromString),
    links = getString(4)!!.let(json::decodeFromString),
    tableMin = getLong(5),
    totalCost = getLong(6),
    prizeLimit = getLong(7),
    favorite = getBoolean(8)!!,
    ignored = getBoolean(9)!!,
    notes = getString(10),
    counter = getLong(11)!!,
)

class StampRallyEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val json: Json,
    private val dao: suspend () -> StampRallyEntryQueries = { database().stampRallyEntryQueries },
) {
    suspend fun getEntry(id: String) = dao().getEntry(id).awaitAsOneOrNull()

    suspend fun getEntryWithArtists(id: String) =
        dao().transactionWithResult {
            val stampRally = getEntry(id) ?: return@transactionWithResult null
            val artists = dao().getArtistEntries(id).awaitAsList()
            StampRallyWithArtistsEntry(stampRally, artists)
        }

    suspend fun insertEntries(vararg entries: StampRallyEntry) =
        insertEntries(entries.toList())

    suspend fun insertEntries(entries: List<StampRallyEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insert(it)
            }
        }

    fun search(
        query: String,
        searchQuery: StampRallySearchQuery,
    ): PagingSource<Int, StampRallyEntry> {
        val filterParams = searchQuery.filterParams
        val booleanOptions = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "favorite:1"
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
        val selectSuffix =
            (", substr(stampRallyEntry.counter * 0.${searchQuery.randomSeed}," +
                    " length(stampRallyEntry.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
                .orEmpty()

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM stampRallyEntry
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
                JOIN stampRallyEntry_fts ON stampRallyEntry.id = stampRallyEntry_fts.id
                WHERE stampRallyEntry_fts MATCH ?
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
