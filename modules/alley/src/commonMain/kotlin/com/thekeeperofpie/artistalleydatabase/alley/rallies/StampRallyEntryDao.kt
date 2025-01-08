package com.thekeeperofpie.artistalleydatabase.alley.rallies

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.Stamp_rally_artist_connections
import com.thekeeperofpie.artistalleydatabase.alley.Stamp_rally_entries
import com.thekeeperofpie.artistalleydatabase.alley.artist.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.dao.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import kotlinx.serialization.json.Json

fun Stamp_rally_entries.toStampRallyEntry(json: Json) = StampRallyEntry(
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables.let(json::decodeFromString),
    links = links.let(json::decodeFromString),
    tableMin = tableMin?.toInt(),
    totalCost = totalCost?.toInt(),
    prizeLimit = prizeLimit?.toInt(),
    favorite = favorite,
    ignored = ignored,
    notes = notes,
    counter = counter.toInt(),
)

fun StampRallyEntry.toSqlObject(json: Json) = Stamp_rally_entries(
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables.let(json::encodeToString),
    links = links.let(json::encodeToString),
    tableMin = tableMin?.toLong(),
    totalCost = totalCost?.toLong(),
    prizeLimit = prizeLimit?.toLong(),
    favorite = favorite,
    ignored = ignored,
    notes = notes,
    counter = counter.toLong(),
)

fun StampRallyArtistConnection.toSqlObject() = Stamp_rally_artist_connections(
    stampRallyId = stampRallyId,
    artistId = artistId,
)

fun SqlCursor.toStampRallyEntry(json: Json) = Stamp_rally_entries(
    id = getString(0)!!,
    fandom = getString(1)!!,
    hostTable = getString(2)!!,
    tables = getString(3)!!,
    links = getString(4)!!,
    tableMin = getLong(5),
    totalCost = getLong(6),
    prizeLimit = getLong(7),
    favorite = getBoolean(8)!!,
    ignored = getBoolean(9)!!,
    notes = getString(10),
    counter = getLong(11)!!,
).toStampRallyEntry(json)

class StampRallyEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val json: Json,
    private val dao: suspend () -> StampRallyEntryQueries = { database().stampRallyEntryQueries },
) {
    suspend fun getEntry(id: String) =
        dao().getEntry(id)
            .awaitAsOneOrNull()
            ?.toStampRallyEntry(json)

    suspend fun getEntryWithArtists(id: String) =
        dao().transactionWithResult {
            val stampRally = getEntry(id) ?: return@transactionWithResult null
            val artists = dao().getArtistEntries(id).awaitAsList()
                .map { it.toArtistEntry(json) }
            StampRallyWithArtistsEntry(stampRally, artists)
        }

    suspend fun insertEntries(vararg entries: StampRallyEntry) =
        insertEntries(entries.toList())

    suspend fun insertEntries(entries: List<StampRallyEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insert(it.toSqlObject(json))
            }
        }

    suspend fun clearEntries() = dao().clear()

    suspend fun clearConnections() = dao().clearArtistConnections()

    suspend fun insertConnections(entries: List<StampRallyArtistConnection>) =
        dao().transaction {
            entries.forEach {
                dao().insertArtistConnection(it.toSqlObject())
            }
        }

    suspend fun retainIds(ids: List<String>) =
        dao().retainIds(ids.joinToString(separator = ","))

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
        val basicSortSuffix = "\nORDER BY stamp_rally_entries.FIELD $ascending"
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
            (", substr(stamp_rally_entries.counter * 0.${searchQuery.randomSeed}," +
                    " length(stamp_rally_entries.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == StampRallySearchSortOption.RANDOM }
                .orEmpty()

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM stamp_rally_entries
                """.trimIndent() + sortSuffix

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                statement = statement,
                tableNames = listOf("stamp_rally_entries"),
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
                FROM stamp_rally_entries
                JOIN stamp_rally_entries_fts ON stamp_rally_entries.id = stamp_rally_entries_fts.id
                WHERE stamp_rally_entries_fts MATCH ?
                """.trimIndent()
        } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("stamp_rally_entries", "stamp_rally_entries_fts"),
            parameters = bindArguments + filterParamsQueryPieces,
            mapper = { it.toStampRallyEntry(json) },
        )
    }

    suspend fun insertUpdatedEntries(entries: Collection<Pair<StampRallyEntry, List<StampRallyArtistConnection>>>) {
        dao().transaction {
            val mergedEntries = entries.map { (entry, _) ->
                val existingEntry = getEntry(entry.id)
                entry.copy(
                    favorite = existingEntry?.favorite == true,
                    ignored = existingEntry?.ignored == true,
                )
            }

            insertEntries(mergedEntries)
            insertConnections(entries.flatMap { it.second })
        }
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
