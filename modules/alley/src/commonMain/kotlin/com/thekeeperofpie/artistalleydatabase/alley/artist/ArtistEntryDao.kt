package com.thekeeperofpie.artistalleydatabase.alley.artist

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistBoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.Json

fun GetBoothsWithFavorites.toArtistBoothWithFavorite() = ArtistBoothWithFavorite(
    booth = booth,
    favorite = favorite,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> ArtistEntryQueries = { database().artistEntryQueries },
) {
    suspend fun getEntry(id: String) = dao().getEntry(id).awaitAsOneOrNull()

    fun getEntryFlow(id: String) =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getEntry(id).asFlow() }
            .mapToOne(PlatformDispatchers.IO)

    suspend fun getEntryWithStampRallies(id: String) =
        dao().transactionWithResult {
            val artist = getEntry(id) ?: return@transactionWithResult null
            val stampRallies = dao().getStampRallyEntries(id).awaitAsList()
            ArtistWithStampRalliesEntry(artist, stampRallies)
        }

    fun getBoothsWithFavorite() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getBoothsWithFavorites().asFlow() }
            .mapLatest { it.awaitAsList().map { it.toArtistBoothWithFavorite() } }

    suspend fun insertEntries(vararg entries: ArtistEntry) =
        insertEntries(entries.toList())

    suspend fun insertEntries(entries: List<ArtistEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insert(it)
            }
        }

    fun search(
        query: String,
        searchQuery: ArtistSearchQuery,
    ): PagingSource<Int, ArtistEntry> {
        val filterParams = searchQuery.filterParams
        val booleanOptions = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "favorite:1"

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterParams.showOnlyWithCatalog) this += "driveLink:*http*"
        }

        val filterParamsQueryPieces = filterParamsQuery(filterParams)
        val options = query.split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .map { "*$it*" }
            .map {
                listOf(
                    "booth:$it",
                    "name:$it",
                    "summary:$it",
                    "seriesInferredSearchable:$it",
                    "seriesConfirmedSearchable:$it",
                    "merchInferredSearchable:$it",
                    "merchConfirmedSearchable:$it",
                )
            }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val basicSortSuffix = "\nORDER BY artistEntry_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistSearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "name")
            ArtistSearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
        }
        val selectSuffix = (", substr(artistEntry.counter * 0.${searchQuery.randomSeed}," +
                " length(artistEntry.counter) + 2) as orderIndex")
            .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
            .orEmpty()

        val lockedSuffix = if (searchQuery.lockedSeries != null) {
            "artistEntry.id IN (SELECT artistId from artist_series_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND" else "") +
                    " artist_series_connections.seriesId == " +
                    "${DatabaseUtils.sqlEscapeString(searchQuery.lockedSeries)})"
        } else if (searchQuery.lockedMerch != null) {
            "artistEntry.id IN (SELECT artistId from artist_merch_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND" else "") +
                    " artist_merch_connections.merchId == " +
                    "${DatabaseUtils.sqlEscapeString(searchQuery.lockedMerch)})"
        } else {
            null
        }

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM artistEntry
                JOIN artistEntry_fts ON artistEntry.id = artistEntry_fts.id
                ${if (lockedSuffix == null) "" else "WHERE $lockedSuffix"}
                """.trimIndent() + sortSuffix

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                statement = statement,
                tableNames = listOf("artistEntry"),
            ) {
                ArtistEntry(
                    it.getString(0)!!,
                    it.getString(1)!!,
                    it.getString(2)!!,
                    it.getString(3),
                    it.getString(4)!!.let(Json::decodeFromString),
                    it.getString(5)!!.let(Json::decodeFromString),
                    it.getString(6)!!.let(Json::decodeFromString),
                    it.getString(7),
                    it.getBoolean(8)!!,
                    it.getBoolean(9)!!,
                    it.getString(10),
                    it.getString(11)!!.let(Json::decodeFromString),
                    it.getString(12)!!.let(Json::decodeFromString),
                    it.getString(13)!!.let(Json::decodeFromString),
                    it.getString(14)!!.let(Json::decodeFromString),
                    it.getLong(15)!!
                )
            }
        }

        val optionsArguments = options.map { it.joinToString(separator = " OR ") }
        val booleanArguments = booleanOptions.joinToString(separator = " ")
        val separator = " ".takeIf {
            optionsArguments.isNotEmpty() && booleanArguments.isNotEmpty()
        }
        val bindArguments = (optionsArguments + separator + booleanArguments)
            .filterNotNull()
            .filter { it.isNotEmpty() }

        val statement =
            (bindArguments + filterParamsQueryPieces).joinToString("\nINTERSECT\n") {
                """
                SELECT *$selectSuffix
                FROM artistEntry
                JOIN artistEntry_fts ON artistEntry.id = artistEntry_fts.id
                WHERE artistEntry_fts MATCH ?
                ${if (lockedSuffix == null) "" else "AND $lockedSuffix"}
                """.trimIndent()
            } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("artistEntry", "artistEntry_fts"),
            parameters = bindArguments + filterParamsQueryPieces
        ) {
            ArtistEntry(
                it.getString(0)!!,
                it.getString(1)!!,
                it.getString(2)!!,
                it.getString(3),
                it.getString(4)!!.let(Json::decodeFromString),
                it.getString(5)!!.let(Json::decodeFromString),
                it.getString(6)!!.let(Json::decodeFromString),
                it.getString(7),
                it.getBoolean(8)!!,
                it.getBoolean(9)!!,
                it.getString(10),
                it.getString(11)!!.let(Json::decodeFromString),
                it.getString(12)!!.let(Json::decodeFromString),
                it.getString(13)!!.let(Json::decodeFromString),
                it.getString(14)!!.let(Json::decodeFromString),
                it.getLong(15)!!
            )
        }
    }

    private fun filterParamsQuery(
        filterParams: ArtistSortFilterViewModel.FilterParams,
    ): MutableList<String> {
        val queryPieces = mutableListOf<String>()

        filterParams.artist.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "artistNames:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        filterParams.booth.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "booth:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        filterParams.summary.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(DatabaseUtils.WHITESPACE_REGEX)
                .map { "description:${DatabaseUtils.wrapMatchQuery(it)}" }
        }
        queryPieces += filterParams.series.flatMap { it.split(DatabaseUtils.WHITESPACE_REGEX) }
            .map {
                if (filterParams.showOnlyConfirmedTags) {
                    "seriesConfirmed:${DatabaseUtils.wrapMatchQuery(it)}"
                } else {
                    "seriesInferred:${DatabaseUtils.wrapMatchQuery(it)}" +
                            " OR seriesConfirmed:${DatabaseUtils.wrapMatchQuery(it)}"
                }
            }
        queryPieces += filterParams.merch
            .map {
                if (filterParams.showOnlyConfirmedTags) {
                    "merchConfirmed:${DatabaseUtils.wrapMatchQuery(it)}"
                } else {
                    "merchInferred:${DatabaseUtils.wrapMatchQuery(it)}" +
                            " OR merchConfirmed:${DatabaseUtils.wrapMatchQuery(it)}"
                }
            }

        return queryPieces
    }
}
