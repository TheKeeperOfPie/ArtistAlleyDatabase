package com.thekeeperofpie.artistalleydatabase.alley.artist

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry.GetEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.Json

private fun SqlCursor.toArtistWithUserData(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            id = artistId,
            booth = getString(1)!!,
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            driveLink = getString(7),
            notes = getString(8),
            seriesInferred = getString(9)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(10)!!.let(Json::decodeFromString),
            merchInferred = getString(11)!!.let(Json::decodeFromString),
            merchConfirmed = getString(12)!!.let(Json::decodeFromString),
            counter = getLong(13)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            favorite = getBoolean(14) == true,
            ignored = getBoolean(15) == true,
            notes = getString(16),
        )
    )
}

private fun GetEntry.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        favorite = favorite == true,
        ignored = ignored == true,
        notes = userNotes,
    )
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistEntryDao(
    private val driver: SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> ArtistEntryQueries = { database().artistEntryQueries },
) {
    suspend fun getEntry(id: String) = dao()
        .getEntry(id)
        .awaitAsOneOrNull()
        ?.toArtistWithUserData()

    fun getEntryFlow(id: String) =
        flowFromSuspend { dao() }
            .flatMapLatest { it.getEntry(id).asFlow() }
            .mapToOne(PlatformDispatchers.IO)
            .mapLatest { it.toArtistWithUserData() }

    suspend fun getEntryWithStampRallies(id: String) =
        dao().transactionWithResult {
            val artist = getEntry(id) ?: return@transactionWithResult null
            val stampRallies = dao().getStampRallyEntries(id).awaitAsList()
            ArtistWithStampRalliesEntry(artist, stampRallies)
        }

    fun search(
        query: String,
        searchQuery: ArtistSearchQuery,
    ): PagingSource<Int, ArtistWithUserData> {
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (filterParams.showOnlyFavorites) this += "artistUserEntry.favorite = 1"

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterParams.showOnlyWithCatalog) this += "artistEntry.driveLink LIKE 'http%'"

            if (searchQuery.lockedSeries != null) {
                this += "artistEntry.id IN (SELECT artistId from artistSeriesConnection WHERE " +
                        (if (filterParams.showOnlyConfirmedTags) "artistSeriesConnection.confirmed IS 1 AND" else "") +
                        " artistSeriesConnection.seriesId == " +
                        "${DatabaseUtils.sqlEscapeString(searchQuery.lockedSeries)})"
            } else if (searchQuery.lockedMerch != null) {
                this += "artistEntry.id IN (SELECT artistId from artistMerchConnection WHERE " +
                        (if (filterParams.showOnlyConfirmedTags) "artistMerchConnection.confirmed IS 1 AND" else "") +
                        "artistMerchConnection.merchId == " +
                        "${DatabaseUtils.sqlEscapeString(searchQuery.lockedMerch)})"
            }
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> "ORDER BY artistEntry_fts.booth COLLATE NOCASE"
            ArtistSearchSortOption.ARTIST -> "ORDER BY artistEntry_fts.name COLLATE NOCASE"
            ArtistSearchSortOption.RANDOM -> "ORDER BY orderIndex"
        } + " $ascending"
        val randomSortSelectSuffix =
            (", substr(artistEntry_fts.counter * 0.${searchQuery.randomSeed}," +
                    " length(artistEntry_fts.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
                .orEmpty()
        val selectSuffix =
            ", artistUserEntry.favorite, artistUserEntry.ignored, artistUserEntry.notes"

        val matchOptions = mutableListOf<String>()
        filterParams.artist.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(name : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }
        filterParams.booth.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(booth : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }
        filterParams.summary.takeUnless(String?::isNullOrBlank)?.let {
            matchOptions += "(summary : ${DaoUtils.makeMatchAndQuery(listOf(it))})"
        }
        filterParams.series.takeUnless { it.isEmpty() }?.let {
            if (filterParams.showOnlyConfirmedTags) {
                "(seriesConfirmed : ${DaoUtils.makeMatchAndQuery(it)})"
            } else {
                "({seriesInferred seriesConfirmed} : ${DaoUtils.makeMatchAndQuery(it)})"
            }
        }
        filterParams.merch.takeUnless { it.isEmpty() }?.let {
            if (filterParams.showOnlyConfirmedTags) {
                "(merchConfirmed : ${DaoUtils.makeMatchAndQuery(it)})"
            } else {
                "({merchInferred merchConfirmed} : ${DaoUtils.makeMatchAndQuery(it)})"
            }
        }

        if (query.isEmpty() && matchOptions.isEmpty()) {
            val andStatement = andClauses.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "WHERE ", separator = "\nAND ")
                .orEmpty()
            val countStatement = """
                SELECT COUNT(*)
                FROM artistEntry
                LEFT OUTER JOIN artistUserEntry
                ON artistEntry.id = artistUserEntry.artistId
                $andStatement
            """.trimIndent()
            val statement = """
                SELECT artistEntry.*$selectSuffix${randomSortSelectSuffix.replace("_fts", "")}
                FROM artistEntry
                LEFT OUTER JOIN artistUserEntry
                ON artistEntry.id = artistUserEntry.artistId
                $andStatement
                ${sortSuffix.replace("_fts", "")}
                """.trimIndent()

            return DaoUtils.queryPagingSource<ArtistWithUserData>(
                driver = driver,
                database = database,
                countStatement = countStatement,
                statement = statement,
                tableNames = listOf("artistEntry_fts", "artistUserEntry"),
                mapper = SqlCursor::toArtistWithUserData,
            )
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "booth",
            "name",
            "summary",
            "seriesInferred".takeUnless { filterParams.showOnlyConfirmedTags },
            "seriesConfirmed",
            "merchInferred".takeUnless { filterParams.showOnlyConfirmedTags },
            "merchConfirmed",
        )
        val matchQuery = buildString {
            append("'")
            append(matchOptions.joinToString(separator = " ", postfix = " "))
            append("{ ${targetColumns.joinToString(separator = " ")} } : $matchOrQuery'")
        }

        val likeStatement = targetColumns.joinToString(separator = "\nOR ") {
            "(${DaoUtils.makeLikeAndQuery("artistEntry_fts.$it", queries)})"
        }

        val andStatement = andClauses.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "WHERE ", separator = "\nAND ").orEmpty()

        val countStatement = DaoUtils.buildSearchCountStatement(
            ftsTableName = "artistEntry_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = "artistEntry",
            ftsTableName = "artistEntry_fts",
            select = "artistEntry.*$selectSuffix",
            idField = "id",
            likeOrderBy = "",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = """
                LEFT OUTER JOIN artistUserEntry
                ON idAsKey = artistUserEntry.artistId
                """.trimIndent(),
            orderBy = sortSuffix,
            randomSeed = searchQuery.randomSeed
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM },
            andStatement = andStatement,
        )

        return DaoUtils.queryPagingSource<ArtistWithUserData>(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("artistEntry_fts", "artistUserEntry"),
            mapper = SqlCursor::toArtistWithUserData,
        )
    }
}
