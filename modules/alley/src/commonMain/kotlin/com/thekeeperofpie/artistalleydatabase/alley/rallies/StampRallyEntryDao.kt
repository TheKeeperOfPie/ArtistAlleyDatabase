package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023Queries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024Queries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025Queries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntryAnimeExpo2026Queries
import com.thekeeperofpie.artistalleydatabase.alley.artist.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.getBooleanFixed
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2023.GetEntry as GetEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2024.GetEntry as GetEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry2025.GetEntry as GetEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntryAnimeExpo2026.GetEntry as GetEntryAnimeExpo2026

fun SqlCursor.toStampRallyWithUserData2023(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.ANIME_EXPO_2023,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            _tableMin = null,
            totalCost = null,
            prize = null,
            prizeLimit = null,
            series = emptyList(),
            notes = null,
            images = getString(5)!!.let(Json::decodeFromString),
            counter = getLong(6)!!,
            confirmed = true,
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBooleanFixed(7),
            ignored = getBooleanFixed(8),
        )
    )
}

fun SqlCursor.toStampRallyWithUserData2024(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.ANIME_EXPO_2024,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            _tableMin = getLong(5),
            totalCost = getLong(6),
            prize = null,
            prizeLimit = getLong(7),
            series = emptyList(),
            notes = getString(8),
            images = getString(9)!!.let(Json::decodeFromString),
            counter = getLong(10)!!,
            confirmed = true,
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBooleanFixed(11),
            ignored = getBooleanFixed(12),
        )
    )
}

fun SqlCursor.toStampRallyWithUserData2025(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.ANIME_EXPO_2025,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            _tableMin = getLong(5),
            totalCost = getLong(6),
            prize = getString(7),
            prizeLimit = getLong(8),
            series = getString(9)!!.let(Json::decodeFromString),
            notes = getString(10),
            images = getString(11)!!.let(Json::decodeFromString),
            counter = getLong(12)!!,
            confirmed = getBooleanFixed(13),
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBooleanFixed(14),
            ignored = getBooleanFixed(15),
        )
    )
}

fun SqlCursor.toStampRallyWithUserDataAnimeExpo2026(): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    return StampRallyWithUserData(
        stampRally = StampRallyEntry(
            year = DataYear.ANIME_EXPO_2026,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = getString(2)!!,
            tables = getString(3)!!.let(Json::decodeFromString),
            links = getString(4)!!.let(Json::decodeFromString),
            _tableMin = getLong(5),
            totalCost = getLong(6),
            prize = getString(7),
            prizeLimit = getLong(8),
            series = getString(9)!!.let(Json::decodeFromString),
            notes = getString(10),
            images = getString(11)!!.let(Json::decodeFromString),
            counter = getLong(12)!!,
            confirmed = getBooleanFixed(13),
        ),
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBooleanFixed(14),
            ignored = getBooleanFixed(15),
        )
    )
}

private fun GetEntry2023.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.ANIME_EXPO_2023,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        _tableMin = null,
        totalCost = null,
        prize = null,
        prizeLimit = null,
        series = emptyList(),
        notes = null,
        images = images,
        counter = counter,
        confirmed = true,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntry2024.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.ANIME_EXPO_2024,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        _tableMin = tableMin,
        totalCost = totalCost,
        prize = null,
        prizeLimit = prizeLimit,
        series = emptyList(),
        notes = notes,
        images = images,
        counter = counter,
        confirmed = true,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntry2025.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.ANIME_EXPO_2025,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        _tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        series = series,
        notes = notes,
        images = images,
        counter = counter,
        confirmed = confirmed,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

private fun GetEntryAnimeExpo2026.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyEntry(
        year = DataYear.ANIME_EXPO_2026,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        _tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        series = series,
        notes = notes,
        images = images,
        counter = counter,
        confirmed = confirmed,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

fun StampRallyEntry2023.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.ANIME_EXPO_2023,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    _tableMin = null,
    totalCost = null,
    prize = null,
    prizeLimit = null,
    series = emptyList(),
    notes = null,
    images = images,
    counter = counter,
    confirmed = true,
)

fun StampRallyEntry2024.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.ANIME_EXPO_2024,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    _tableMin = tableMin,
    totalCost = totalCost,
    prize = null,
    prizeLimit = prizeLimit,
    series = emptyList(),
    notes = notes,
    images = images,
    counter = counter,
    confirmed = true,
)

fun StampRallyEntry2025.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.ANIME_EXPO_2025,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    _tableMin = tableMin,
    totalCost = totalCost,
    prize = prize,
    prizeLimit = prizeLimit,
    series = series,
    notes = notes,
    images = images,
    counter = counter,
    confirmed = confirmed,
)

fun StampRallyEntryAnimeExpo2026.toStampRallyEntry() = StampRallyEntry(
    year = DataYear.ANIME_EXPO_2026,
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    links = links,
    _tableMin = tableMin,
    totalCost = totalCost,
    prize = prize,
    prizeLimit = prizeLimit,
    series = series,
    notes = notes,
    images = images,
    counter = counter,
    confirmed = confirmed,
)

class StampRallyEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao2023: suspend () -> StampRallyEntry2023Queries = { database().stampRallyEntry2023Queries },
    private val dao2024: suspend () -> StampRallyEntry2024Queries = { database().stampRallyEntry2024Queries },
    private val dao2025: suspend () -> StampRallyEntry2025Queries = { database().stampRallyEntry2025Queries },
    private val daoAnimeExpo2026: suspend () -> StampRallyEntryAnimeExpo2026Queries = { database().stampRallyEntryAnimeExpo2026Queries },
) {
    suspend fun getEntry(year: DataYear, stampRallyId: String) =
        when (year) {
            DataYear.ANIME_EXPO_2023 -> dao2023()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.ANIME_EXPO_2024 -> dao2024()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.ANIME_EXPO_2025 -> dao2025()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.ANIME_EXPO_2026 -> daoAnimeExpo2026()
                .getEntry(stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyWithUserData()
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> throw IllegalStateException("ANYC shouldn't have rallies")
        }

    suspend fun getEntryWithArtists(
        year: DataYear,
        stampRallyId: String,
    ): StampRallyWithArtistsEntry? =
        when (year) {
            DataYear.ANIME_EXPO_2023 -> dao2023().transactionWithResult {
                val stampRally =
                    getEntry(year, stampRallyId) ?: return@transactionWithResult null
                val artists = dao2023().getArtistEntries(stampRallyId).awaitAsList()
                    .map { it.toArtistEntry() }
                StampRallyWithArtistsEntry(stampRally, artists)
            }
            DataYear.ANIME_EXPO_2024 -> dao2024().transactionWithResult {
                val stampRally =
                    getEntry(year, stampRallyId) ?: return@transactionWithResult null
                val artists = dao2024().getArtistEntries(stampRallyId).awaitAsList()
                    .map { it.toArtistEntry() }
                StampRallyWithArtistsEntry(stampRally, artists)
            }
            DataYear.ANIME_EXPO_2025 -> dao2025().transactionWithResult {
                val stampRally =
                    getEntry(year, stampRallyId) ?: return@transactionWithResult null
                val artists =
                    dao2025().getArtistEntries(stampRallyId).awaitAsList()
                        .map { it.toArtistEntry() }
                StampRallyWithArtistsEntry(stampRally, artists)
            }
            DataYear.ANIME_EXPO_2026 -> daoAnimeExpo2026().transactionWithResult {
                val stampRally =
                    getEntry(year, stampRallyId) ?: return@transactionWithResult null
                val artists =
                    daoAnimeExpo2026().getArtistEntries(stampRallyId).awaitAsList()
                        .map { it.toArtistEntry() }
                StampRallyWithArtistsEntry(stampRally, artists)
            }
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> throw IllegalStateException("ANYC shouldn't have rallies")
        }

    fun search(
        year: DataYear,
        query: String,
        searchQuery: StampRallySearchQuery,
        onlyFavorites: Boolean = false,
    ): Pair<String, String>? {
        val tableName = year.stampRallyTableName ?: return null
        val filterParams = searchQuery.filterParams
        val andClauses = mutableListOf<String>().apply {
            if (onlyFavorites) this += "stampRallyUserEntry.favorite = 1"

            if (year.dates.year >= 2024) {
                val totalCost = filterParams.totalCost
                if (totalCost.isOnlyStart) {
                    this += "$tableName.totalCost = 0"
                } else {
                    val totalCostMin = totalCost.startInt
                    if (totalCostMin != null) {
                        this += "$tableName.totalCost IS NOT NULL"
                        this += "$tableName.totalCost >= $totalCostMin"
                    }

                    val totalCostMax = totalCost.endInt
                    if (totalCostMax != null) {
                        if (totalCostMin == null) {
                            this += "($tableName.totalCost IS NULL OR $tableName.totalCost <= $totalCostMax)"
                        } else {
                            this += "$tableName.totalCost <= $totalCostMax"
                        }
                    }
                }

                val prizeLimit = filterParams.prizeLimit
                val prizeLimitMin = prizeLimit.startInt
                if (prizeLimitMin != null) {
                    this += "$tableName.prizeLimit IS NOT NULL"
                    this += "$tableName.prizeLimit >= $prizeLimitMin"
                }

                val prizeLimitMax = prizeLimit.endInt
                if (prizeLimitMax != null) {
                    if (prizeLimitMin == null) {
                        this += "($tableName.prizeLimit IS NULL OR $tableName.prizeLimit <= $prizeLimitMax)"
                    } else {
                        this += "$tableName.prizeLimit <= $prizeLimitMax"
                    }
                }
            }

            if (year.dates.year >= 2025) {
                if (!filterParams.showUnconfirmed) {
                    this += "$tableName.confirmed = 1"
                }
            }

            // TODO: Locked series/merch doesn't enforce AND
            if (year.dates.year >= 2025 && filterParams.seriesIn.isNotEmpty()) {
                val seriesList = filterParams.seriesIn.joinToString(separator = ",") {
                    DatabaseUtils.sqlEscapeString(it)
                }

                this += "$tableName.id IN (SELECT stampRallyId from stampRallySeriesConnection " +
                        "WHERE stampRallySeriesConnection.seriesId IN ($seriesList))"
            }
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

        if (query.isEmpty()) {
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

            return countStatement to statement
        }

        val queries = query.split(Regex("\\s+"))
        val matchOrQuery = DaoUtils.makeMatchAndQuery(queries)
        val targetColumns = listOfNotNull(
            "fandom",
            "tables",
            "notes".takeIf { year != DataYear.ANIME_EXPO_2023 },
            "series".takeIf { year.dates.year >= 2025 },
            "prize".takeIf { year.dates.year >= 2025 },
        )
        val matchQuery = buildString {
            append("'")
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

        return countStatement to statement
    }

    suspend fun searchCount(
        year: DataYear,
        query: String,
        searchQuery: StampRallySearchQuery,
        onlyFavorites: Boolean = false,
    ): Flow<Int> {
        val statements = search(year, query, searchQuery, onlyFavorites)
            ?: return flowOf(0)
        val tableName = year.stampRallyTableNameOrThrow
        return DaoUtils.makeQuery(
            driver(),
            statement = statements.first,
            tableNames = listOf("${tableName}_fts", "stampRallyUserEntry"),
            mapper = { it.getLong(0)!!.toInt() },
        ).asFlow()
            .mapToOneOrDefault(0, PlatformDispatchers.IO)
    }

    fun searchPagingSource(
        year: DataYear,
        query: String,
        searchQuery: StampRallySearchQuery,
        onlyFavorites: Boolean = false,
    ): PagingSource<Int, StampRallyWithUserData> {
        val statements = search(year, query, searchQuery, onlyFavorites)
            ?: return object :
                PagingSource<Int, StampRallyWithUserData>() {
                override fun getRefreshKey(state: PagingState<Int, StampRallyWithUserData>) = null
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StampRallyWithUserData> {
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    return LoadResult.Page<Int, StampRallyWithUserData>(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null,
                    ) as LoadResult<Int, StampRallyWithUserData>
                }
            }

        val (countStatement, searchStatement) = statements
        val tableName = year.stampRallyTableNameOrThrow

        return DaoUtils.queryPagingSource(
            driver = driver,
            database = database,
            countStatement = countStatement,
            statement = searchStatement,
            tableNames = listOf("${tableName}_fts", "stampRallyUserEntry"),
            mapper = when (year) {
                DataYear.ANIME_EXPO_2023 -> SqlCursor::toStampRallyWithUserData2023
                DataYear.ANIME_EXPO_2024 -> SqlCursor::toStampRallyWithUserData2024
                DataYear.ANIME_EXPO_2025 -> SqlCursor::toStampRallyWithUserData2025
                DataYear.ANIME_EXPO_2026 -> SqlCursor::toStampRallyWithUserDataAnimeExpo2026
                DataYear.ANIME_NYC_2024,
                DataYear.ANIME_NYC_2025,
                    -> throw IllegalStateException("ANYC shouldn't have rallies")
            },
        )
    }
}
