package com.thekeeperofpie.artistalleydatabase.alley.app.dao

import androidx.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.ArtistBoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.app.ArtistEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_merch_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_series_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.GetBoothsWithFavorites
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.app.ArtistAlleyAppDatabase
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.Json

fun Artist_entries.toArtistEntry(json: Json) = ArtistEntry(
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links.let(json::decodeFromString),
    storeLinks = storeLinks.let(json::decodeFromString),
    catalogLinks = catalogLinks.let(json::decodeFromString),
    driveLink = driveLink,
    favorite = favorite,
    ignored = ignored,
    notes = notes,
    seriesInferred = seriesInferred.let(json::decodeFromString),
    seriesConfirmed = seriesConfirmed.let(json::decodeFromString),
    merchInferred = merchInferred.let(json::decodeFromString),
    merchConfirmed = merchConfirmed.let(json::decodeFromString),
    counter = counter.toInt(),
)

fun ArtistEntry.toSqlObject(json: Json) = Artist_entries(
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links.let(json::encodeToString),
    storeLinks = storeLinks.let(json::encodeToString),
    catalogLinks = catalogLinks.let(json::encodeToString),
    driveLink = driveLink,
    favorite = favorite,
    ignored = ignored,
    notes = notes,
    seriesInferred = seriesInferred.let(json::encodeToString),
    seriesConfirmed = seriesConfirmed.let(json::encodeToString),
    merchInferred = merchInferred.let(json::encodeToString),
    merchConfirmed = merchConfirmed.let(json::encodeToString),
    counter = counter.toLong(),
)

fun ArtistSeriesConnection.toSqlObject() = Artist_series_connections(
    artistId = artistId,
    seriesId = seriesId,
    confirmed = confirmed,
)

fun ArtistMerchConnection.toSqlObject() = Artist_merch_connections(
    artistId = artistId,
    merchId = merchId,
    confirmed = confirmed,
)

fun GetBoothsWithFavorites.toArtistBoothWithFavorite() = ArtistBoothWithFavorite(
    booth = booth,
    favorite = favorite,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistEntryDaoImpl(
    private val driver: SqlDriver,
    private val database: suspend () -> ArtistAlleyAppDatabase,
    private val json: Json,
    private val dao: suspend () -> ArtistEntryQueries = { database().artistEntryQueries },
) : ArtistEntryDao {
    override suspend fun getEntry(id: String) =
        dao().getEntry(id)
            .awaitAsOneOrNull()
            ?.toArtistEntry(json)

    override fun getEntryFlow(id: String) =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getEntry(id).asFlow() }
            .mapToOne(PlatformDispatchers.IO)
            .mapLatest { it.toArtistEntry(json) }

    override suspend fun getEntryWithStampRallies(id: String) =
        dao().transactionWithResult {
            val artist = getEntry(id) ?: return@transactionWithResult null
            val stampRallies = dao().getStampRallyEntries(id).awaitAsList()
                .map { it.toStampRallyEntry(json) }
            ArtistWithStampRalliesEntry(artist, stampRallies)
        }

    override suspend fun getEntriesSize() = dao().getEntriesCount().awaitAsOne().toInt()

    override fun getBoothsWithFavorite() =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getBoothsWithFavorites().asFlow() }
            .mapLatest { it.awaitAsList().map { it.toArtistBoothWithFavorite() } }

    override suspend fun insertEntries(vararg entries: ArtistEntry) =
        insertEntries(entries.toList())

    override suspend fun insertEntries(entries: List<ArtistEntry>) =
        dao().transaction {
            entries.forEach {
                dao().insert(it.toSqlObject(json))
            }
        }

    override suspend fun insertSeriesConnections(entries: List<ArtistSeriesConnection>) =
        dao().transaction {
            entries.forEach {
                dao().insertSeriesConnection(it.toSqlObject())
            }
        }

    override suspend fun clearSeriesConnections() = dao().clearSeriesConnections()

    override suspend fun clearMerchConnections() = dao().clearMerchConnections()

    override suspend fun insertMerchConnections(entries: List<ArtistMerchConnection>) {
        dao().transaction {
            entries.forEach {
                dao().insertMerchConnection(it.toSqlObject())
            }
        }
    }

    override fun search(
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
        val basicSortSuffix = "\nORDER BY artist_entries_fts.FIELD COLLATE NOCASE $ascending"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> basicSortSuffix.replace("FIELD", "booth")
            ArtistSearchSortOption.ARTIST -> basicSortSuffix.replace("FIELD", "name")
            ArtistSearchSortOption.RANDOM -> "\nORDER BY orderIndex $ascending"
        }
        val selectSuffix = (", substr(artist_entries.counter * 0.${searchQuery.randomSeed}," +
                " length(artist_entries.counter) + 2) as orderIndex")
            .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
            .orEmpty()

        val lockedSuffix = if (searchQuery.lockedSeries != null) {
            "artist_entries.id IN (SELECT artistId from artist_series_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_series_connections.confirmed IS 1 AND" else "") +
                    " artist_series_connections.seriesId == " +
                    "${DatabaseUtils.sqlEscapeString(searchQuery.lockedSeries!!)})"
        } else if (searchQuery.lockedMerch != null) {
            "artist_entries.id IN (SELECT artistId from artist_merch_connections WHERE " +
                    (if (filterParams.showOnlyConfirmedTags) "artist_merch_connections.confirmed IS 1 AND" else "") +
                    " artist_merch_connections.merchId == " +
                    "${DatabaseUtils.sqlEscapeString(searchQuery.lockedMerch!!)})"
        } else {
            null
        }

        if (options.isEmpty() && filterParamsQueryPieces.isEmpty() && booleanOptions.isEmpty()) {
            val statement = """
                SELECT *$selectSuffix
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                ${if (lockedSuffix == null) "" else "WHERE $lockedSuffix"}
                """.trimIndent() + sortSuffix

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                statement = statement,
                tableNames = listOf("artist_entries"),
            ) {
                Artist_entries(
                    it.getString(0)!!,
                    it.getString(1)!!,
                    it.getString(2)!!,
                    it.getString(3),
                    it.getString(4)!!,
                    it.getString(5)!!,
                    it.getString(6)!!,
                    it.getString(7),
                    it.getBoolean(8)!!,
                    it.getBoolean(9)!!,
                    it.getString(10),
                    it.getString(11)!!,
                    it.getString(12)!!,
                    it.getString(13)!!,
                    it.getString(14)!!,
                    it.getLong(15)!!
                ).toArtistEntry(json)
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
                FROM artist_entries
                JOIN artist_entries_fts ON artist_entries.id = artist_entries_fts.id
                WHERE artist_entries_fts MATCH ?
                ${if (lockedSuffix == null) "" else "AND $lockedSuffix"}
                """.trimIndent()
            } + sortSuffix

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            statement = statement,
            tableNames = listOf("artist_entries", "artist_entries_fts"),
            parameters = bindArguments + filterParamsQueryPieces
        ) {
            Artist_entries(
                it.getString(0)!!,
                it.getString(1)!!,
                it.getString(2)!!,
                it.getString(3),
                it.getString(4)!!,
                it.getString(5)!!,
                it.getString(6)!!,
                it.getString(7),
                it.getBoolean(8)!!,
                it.getBoolean(9)!!,
                it.getString(10),
                it.getString(11)!!,
                it.getString(12)!!,
                it.getString(13)!!,
                it.getString(14)!!,
                it.getLong(15)!!
            ).toArtistEntry(json)
        }
    }

    override suspend fun insertUpdatedEntries(entries: Collection<Triple<ArtistEntry, List<ArtistSeriesConnection>, List<ArtistMerchConnection>>>) {
        dao().transaction {
            val mergedEntries = entries.map { (entry, _) ->
                val existingEntry = getEntry(entry.id)
                entry.copy(
                    favorite = existingEntry?.favorite == true,
                    ignored = existingEntry?.ignored == true,
                )
            }

            insertEntries(mergedEntries)
            insertSeriesConnections(entries.flatMap { it.second })
            insertMerchConnections(entries.flatMap { it.third })
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
