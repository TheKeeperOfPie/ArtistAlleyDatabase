package com.thekeeperofpie.artistalleydatabase.alley.artist

import app.cash.paging.PagingSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024Queries
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025Queries
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.toStampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.Json
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2023.GetEntry as GetEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2024.GetEntry as GetEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry2025.GetEntry as GetEntry2025

private fun SqlCursor.toArtistWithUserData2023(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.YEAR_2023,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(4),
            links = getString(5)!!.let(Json::decodeFromString),
            storeLinks = emptyList(),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            driveLink = getString(7),
            notes = null,
            seriesInferred = emptyList(),
            seriesConfirmed = emptyList(),
            merchInferred = emptyList(),
            merchConfirmed = emptyList(),
            counter = getLong(8)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.YEAR_2023,
            favorite = getBoolean(9) == true,
            ignored = getBoolean(10) == true,
        )
    )
}

private fun SqlCursor.toArtistWithUserData2024(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.YEAR_2024,
            id = artistId,
            booth = getString(1),
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
            dataYear = DataYear.YEAR_2024,
            favorite = getBoolean(14) == true,
            ignored = getBoolean(15) == true,
        )
    )
}

private fun SqlCursor.toArtistWithUserData2025(): ArtistWithUserData {
    val artistId = getString(0)!!
    return ArtistWithUserData(
        artist = ArtistEntry(
            year = DataYear.YEAR_2025,
            id = artistId,
            booth = getString(1),
            name = getString(2)!!,
            summary = getString(3),
            links = getString(4)!!.let(Json::decodeFromString),
            storeLinks = getString(5)!!.let(Json::decodeFromString),
            catalogLinks = getString(6)!!.let(Json::decodeFromString),
            driveLink = getString(7),
            notes = getString(8),
            commissions = getString(9)!!.let(Json::decodeFromString),
            seriesInferred = getString(10)!!.let(Json::decodeFromString),
            seriesConfirmed = getString(11)!!.let(Json::decodeFromString),
            merchInferred = getString(12)!!.let(Json::decodeFromString),
            merchConfirmed = getString(13)!!.let(Json::decodeFromString),
            counter = getLong(14)!!,
        ),
        userEntry = ArtistUserEntry(
            artistId = artistId,
            dataYear = DataYear.YEAR_2025,
            favorite = getBoolean(15) == true,
            ignored = getBoolean(16) == true,
        )
    )
}

private fun GetEntry2023.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.YEAR_2023,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = emptyList(),
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = null,
        seriesInferred = emptyList(),
        seriesConfirmed = emptyList(),
        merchInferred = emptyList(),
        merchConfirmed = emptyList(),
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.YEAR_2023,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

private fun GetEntry2024.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.YEAR_2024,
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
        dataYear = DataYear.YEAR_2024,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

private fun GetEntry2025.toArtistWithUserData() = ArtistWithUserData(
    artist = ArtistEntry(
        year = DataYear.YEAR_2025,
        id = id,
        booth = booth,
        name = name,
        summary = summary,
        links = links,
        storeLinks = storeLinks,
        catalogLinks = catalogLinks,
        driveLink = driveLink,
        notes = notes,
        commissions = commissions,
        seriesInferred = seriesInferred,
        seriesConfirmed = seriesConfirmed,
        merchInferred = merchInferred,
        merchConfirmed = merchConfirmed,
        counter = counter,
    ),
    userEntry = ArtistUserEntry(
        artistId = id,
        dataYear = DataYear.YEAR_2025,
        favorite = favorite == true,
        ignored = ignored == true,
    )
)

fun ArtistEntry2023.toArtistEntry() = ArtistEntry(
    year = DataYear.YEAR_2023,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = emptyList(),
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = null,
    seriesInferred = emptyList(),
    seriesConfirmed = emptyList(),
    merchInferred = emptyList(),
    merchConfirmed = emptyList(),
    counter = counter,
)

fun ArtistEntry2024.toArtistEntry() = ArtistEntry(
    year = DataYear.YEAR_2024,
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
)

fun ArtistEntry2025.toArtistEntry() = ArtistEntry(
    year = DataYear.YEAR_2025,
    id = id,
    booth = booth,
    name = name,
    summary = summary,
    links = links,
    storeLinks = storeLinks,
    catalogLinks = catalogLinks,
    driveLink = driveLink,
    notes = notes,
    commissions = commissions,
    seriesInferred = seriesInferred,
    seriesConfirmed = seriesConfirmed,
    merchInferred = merchInferred,
    merchConfirmed = merchConfirmed,
    counter = counter,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: ArtistAlleySettings,
    private val dao2023: suspend () -> ArtistEntry2023Queries = { database().artistEntry2023Queries },
    private val dao2024: suspend () -> ArtistEntry2024Queries = { database().artistEntry2024Queries },
    private val dao2025: suspend () -> ArtistEntry2025Queries = { database().artistEntry2025Queries },
) {
    suspend fun getEntry(year: DataYear, id: String) =
        when (year) {
            DataYear.YEAR_2023 -> dao2023()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.YEAR_2024 -> dao2024()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
            DataYear.YEAR_2025 -> dao2025()
                .getEntry(id)
                .awaitAsOneOrNull()
                ?.toArtistWithUserData()
        }

    fun getEntryFlow(id: String) = settings.dataYear
        .flatMapLatest {
            when (it) {
                DataYear.YEAR_2023 -> dao2023()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.YEAR_2024 -> dao2024()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
                DataYear.YEAR_2025 -> dao2025()
                    .getEntry(id)
                    .asFlow()
                    .mapToOne(PlatformDispatchers.IO)
                    .mapLatest { it.toArtistWithUserData() }
            }
        }

    suspend fun getEntryWithStampRallies(dataYear: DataYear, artistId: String) =
        when (dataYear) {
            DataYear.YEAR_2023 -> {
                dao2023().transactionWithResult {
                    val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                    val stampRallies = dao2023().getStampRallyEntries(artistId).awaitAsList()
                        .map { it.toStampRallyEntry() }
                    ArtistWithStampRalliesEntry(artist, stampRallies)
                }
            }
            DataYear.YEAR_2024 -> {
                dao2024().transactionWithResult {
                    val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                    val stampRallies = dao2024().getStampRallyEntries(artistId).awaitAsList()
                        .map { it.toStampRallyEntry() }
                    ArtistWithStampRalliesEntry(artist, stampRallies)
                }
            }
            DataYear.YEAR_2025 -> {
                dao2025().transactionWithResult {
                    val artist = getEntry(dataYear, artistId) ?: return@transactionWithResult null
                    val stampRallies = dao2025().getStampRallyEntries(artistId).awaitAsList()
                        .map { it.toStampRallyEntry() }
                    ArtistWithStampRalliesEntry(artist, stampRallies)
                }
            }
        }

    fun search(
        year: DataYear,
        query: String,
        searchQuery: ArtistSearchQuery,
        onlyFavorites: Boolean = false,
    ): PagingSource<Int, ArtistWithUserData> {
        val tableName = "artistEntry${year.year}"
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (onlyFavorites) this += "artistUserEntry.favorite = 1"

            // Search for "http" as a simplification of logic, since checking
            // not empty would require a separate query template
            if (filterParams.showOnlyWithCatalog) this += "$tableName.driveLink LIKE 'http%'"

            if (year == DataYear.YEAR_2025) {
                if (filterParams.showOnlyHasCommissions) this += "$tableName.commissions != '[]'"
            }

            if (searchQuery.lockedSeries != null) {
                val yearFilter = when (year) {
                    DataYear.YEAR_2023 -> ""
                    DataYear.YEAR_2024 -> "artistSeriesConnection.has2024 = 1 AND "
                    DataYear.YEAR_2025 -> "artistSeriesConnection.has2025 = 1 AND "
                }
                this += "$tableName.id IN (SELECT artistId from artistSeriesConnection WHERE " +
                        yearFilter +
                        (if (filterParams.showOnlyConfirmedTags) "artistSeriesConnection.confirmed = 1 AND " else "") +
                        " artistSeriesConnection.seriesId = " +
                        "${DatabaseUtils.sqlEscapeString(searchQuery.lockedSeries)})"
            } else if (searchQuery.lockedMerch != null) {
                val yearFilter = when (year) {
                    DataYear.YEAR_2023 -> ""
                    DataYear.YEAR_2024 -> "artistMerchConnection.has2024 = 1 AND "
                    DataYear.YEAR_2025 -> "artistMerchConnection.has2025 = 1 AND "
                }

                this += "$tableName.id IN (SELECT artistId from artistMerchConnection WHERE " +
                        yearFilter +
                        (if (filterParams.showOnlyConfirmedTags) "artistMerchConnection.confirmed = 1 AND " else "") +
                        "artistMerchConnection.merchId = " +
                        "${DatabaseUtils.sqlEscapeString(searchQuery.lockedMerch)})"
            }
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            ArtistSearchSortOption.BOOTH -> "ORDER BY $tableName.booth COLLATE NOCASE"
            ArtistSearchSortOption.ARTIST -> "ORDER BY $tableName.name COLLATE NOCASE"
            ArtistSearchSortOption.RANDOM -> "ORDER BY orderIndex"
        } + " $ascending" + " NULLS LAST"
        val randomSortSelectSuffix =
            (", substr(${tableName}_fts.counter * 0.${searchQuery.randomSeed}," +
                    " length(${tableName}_fts.counter) + 2) as orderIndex")
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM }
                .orEmpty()
        val selectSuffix = ", artistUserEntry.favorite, artistUserEntry.ignored"

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
        if (year != DataYear.YEAR_2023) {
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
        }

        if (query.isEmpty() && matchOptions.isEmpty()) {
            val andStatement = andClauses.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "WHERE ", separator = "\nAND ")
                .orEmpty()

            val joinStatement = """
                LEFT OUTER JOIN artistUserEntry
                ON $tableName.id = artistUserEntry.artistId
                AND '${year.serializedName}' = artistUserEntry.dataYear
            """.trimIndent()

            val countStatement = """
                SELECT COUNT(*)
                FROM $tableName
                $joinStatement
                $andStatement
            """.trimIndent()
            val statement = """
                SELECT $tableName.*$selectSuffix${randomSortSelectSuffix.replace("_fts", "")}
                FROM $tableName
                $joinStatement
                $andStatement
                ${sortSuffix.replace("_fts", "")}
                """.trimIndent()

            return DaoUtils.queryPagingSource(
                driver = driver,
                database = database,
                countStatement = countStatement,
                statement = statement,
                tableNames = listOf("${tableName}_fts", "artistUserEntry"),
                mapper = when (year) {
                    DataYear.YEAR_2023 -> SqlCursor::toArtistWithUserData2023
                    DataYear.YEAR_2024 -> SqlCursor::toArtistWithUserData2024
                    DataYear.YEAR_2025 -> SqlCursor::toArtistWithUserData2025
                },
            )
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "booth",
            "name",
            "summary",
            "notes",
        ).let {
            if (year == DataYear.YEAR_2023) {
                it
            } else {
                it + listOfNotNull(
                    "seriesInferred".takeUnless { filterParams.showOnlyConfirmedTags },
                    "seriesConfirmed",
                    "merchInferred".takeUnless { filterParams.showOnlyConfirmedTags },
                    "merchConfirmed",
                )
            }
        }
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
                LEFT OUTER JOIN artistUserEntry
                ON idAsKey = artistUserEntry.artistId
                AND '${year.serializedName}' = artistUserEntry.dataYear
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
                .takeIf { filterParams.sortOption == ArtistSearchSortOption.RANDOM },
            andStatement = andStatement,
        )

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = statement,
            tableNames = listOf("${tableName}_fts", "artistUserEntry"),
            mapper = when (year) {
                DataYear.YEAR_2023 -> SqlCursor::toArtistWithUserData2023
                DataYear.YEAR_2024 -> SqlCursor::toArtistWithUserData2024
                DataYear.YEAR_2025 -> SqlCursor::toArtistWithUserData2025
            },
        )
    }
}
