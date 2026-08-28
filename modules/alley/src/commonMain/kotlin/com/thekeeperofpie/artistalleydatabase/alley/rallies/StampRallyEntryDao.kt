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
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyChangelogEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.artist.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.getBooleanFixed
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.stampRallyEntry.GetEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Serializable
private data class BoothAndProfileImage(
    val booth: String? = null,
    val profileImage: String? = null,
)

fun SqlCursor.toStampRallyWithUserData(dataYear: DataYear): StampRallyWithUserData {
    val stampRallyId = getString(0)!!
    val tables: List<String> = getString(2)!!.let(Json::decodeFromString)
    val links: List<String> = getString(5)!!.let(Json::decodeFromString)
    return StampRallyWithUserData(
        stampRally = StampRallyDatabaseEntry(
            year = dataYear,
            id = stampRallyId,
            fandom = getString(1)!!,
            hostTable = tables.firstOrNull().orEmpty(),
            tables = tables,
            startTables = getString(3)?.let { Json.decodeFromString<Set<String>>(it) }.orEmpty(),
            endTables = getString(4)?.let { Json.decodeFromString<Set<String>>(it) }.orEmpty(),
            links = links,
            tableMin = getLong(6)?.toInt()?.let(TableMin::parseFromValue),
            totalCost = getLong(7),
            prize = getString(8),
            prizeLimit = getLong(9),
            prizeMerch = getString(10)?.let { Json.decodeFromString<List<String>>(it) }.orEmpty(),
            series = getString(11)!!.let(Json::decodeFromString),
            merch = getString(12)!!.let(Json::decodeFromString),
            notes = getString(13),
            images = getString(14)!!.let(Json::decodeFromString),
            confirmed = links.isNotEmpty(),
            editorNotes = null,
            lastEditor = null,
            lastEditTime = null,
        ),
        seriesImageInfo = Json.decodeFromString<List<SeriesImageInfo>>(getString(15)!!),
        artistBoothToProfileImages = Json.decodeFromString<List<BoothAndProfileImage>>(getString(16)!!)
            .associate {
                it.booth.orEmpty() to it.profileImage?.let(ColumnAdapters.databaseImageAdapter::decode)
            },
        userEntry = StampRallyUserEntry(
            stampRallyId = stampRallyId,
            favorite = getBooleanFixed(17),
            ignored = getBooleanFixed(18),
        )
    )
}

private fun GetEntry.toStampRallyWithUserData() = StampRallyWithUserData(
    stampRally = StampRallyDatabaseEntry(
        year = dataYear,
        id = id,
        fandom = fandom,
        hostTable = tables.firstOrNull().orEmpty(),
        tables = tables,
        startTables = startTables.orEmpty(),
        endTables = endTables.orEmpty(),
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        prizeMerch = prizeMerch.orEmpty(),
        series = series,
        merch = merch,
        notes = notes,
        images = images,
        confirmed = links.isNotEmpty() || images.isNotEmpty(),
        editorNotes = null,
        lastEditor = null,
        lastEditTime = null,
    ),
    userEntry = StampRallyUserEntry(
        stampRallyId = id,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        ignored = DaoUtils.coerceBooleanForJs(ignored),
    )
)

fun StampRallyEntry.toStampRallyEntry(dataYear: DataYear) = StampRallyDatabaseEntry(
    year = dataYear,
    id = id,
    fandom = fandom,
    hostTable = tables.firstOrNull().orEmpty(),
    tables = tables,
    startTables = emptySet(),
    endTables = emptySet(),
    links = links,
    tableMin = tableMin,
    totalCost = totalCost,
    prize = prize,
    prizeLimit = prizeLimit,
    prizeMerch = prizeMerch.orEmpty(),
    series = series,
    merch = merch,
    notes = notes,
    images = images,
    confirmed = links.isNotEmpty() || images.isNotEmpty(),
    editorNotes = null,
    lastEditor = null,
    lastEditTime = null,
)

@SingleIn(AppScope::class)
class StampRallyEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> StampRallyEntryQueries = { database().stampRallyEntryQueries },
    private val daoChangelog: suspend () -> StampRallyChangelogEntryQueries = { database().stampRallyChangelogEntryQueries },
) {
    @Inject
    constructor(database: ArtistAlleyDatabase) : this(
        driver = database::driver,
        database = database::database,
    )

    suspend fun getEntry(year: DataYear, stampRallyId: String) =
        dao().getEntry(stampRallyId)
            .awaitAsOneOrNull()
            ?.toStampRallyWithUserData()

    suspend fun getEntryWithArtists(
        year: DataYear,
        stampRallyId: String,
    ): StampRallyWithArtistsEntry? =
        dao().transactionWithResult {
            val stampRally =
                getEntry(year, stampRallyId) ?: return@transactionWithResult null
            val artists = dao().getArtistEntries(stampRallyId).awaitAsList()
                .map { it.toArtistEntry(year) }
            StampRallyWithArtistsEntry(stampRally, artists)
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
            this += "$tableName.dataYear = '${year.serializedName}'"
            if (onlyFavorites) this += "stampRallyUserEntry.favorite = 1"

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

            // TODO: Locked series/merch doesn't enforce AND
            if (filterParams.seriesIn.isNotEmpty()) {
                val seriesList = filterParams.seriesIn.joinToString(separator = ",") {
                    DatabaseUtils.sqlEscapeString(it)
                }

                this += "$tableName.rowid IN (SELECT stampRallyRowId FROM stampRallySeriesConnection " +
                        "WHERE stampRallySeriesConnection.seriesId IN ($seriesList) " +
                        "AND stampRallySeriesConnection.dataYear = '${year.serializedName}')"
            }

            if (year.dates.year >= 2026) {
                if (filterParams.merchIdIn.isNotEmpty()) {
                    val merchList = filterParams.merchIdIn.joinToString(separator = ",") {
                        DatabaseUtils.sqlEscapeString(it)
                    }

                    this += "$tableName.rowid IN (SELECT stampRallyRowId FROM stampRallyMerchConnection " +
                            "WHERE stampRallyMerchConnection.merchId IN ($merchList) " +
                            "AND stampRallyMerchConnection.dataYear = '${year.serializedName}')"
                }
                if (filterParams.prizeMerchIdIn.isNotEmpty()) {
                    val prizeMerchList = filterParams.prizeMerchIdIn.joinToString(separator = ",") {
                        DatabaseUtils.sqlEscapeString(it)
                    }

                    this += "$tableName.rowid IN (SELECT stampRallyRowId FROM stampRallyPrizeMerchConnection " +
                            "WHERE stampRallyPrizeMerchConnection.merchId IN ($prizeMerchList) " +
                            "AND stampRallyPrizeMerchConnection.dataYear = '${year.serializedName}')"
                }
            }
        }

        val ascending = if (filterParams.sortAscending) "ASC" else "DESC"
        val sortSuffix = when (filterParams.sortOption) {
            StampRallySearchSortOption.FANDOM ->
                "ORDER BY $tableName.fandom COLLATE NOCASE $ascending"
            StampRallySearchSortOption.RANDOM ->
                "ORDER BY SIN($tableName.rowid + ${searchQuery.randomSeed}) $ascending"
            StampRallySearchSortOption.PRIZE_LIMIT ->
                "ORDER BY $tableName.prizeLimit $ascending NULLS LAST"
            StampRallySearchSortOption.TOTAL_COST ->
                "ORDER BY $tableName.totalCost $ascending NULLS LAST"
        }
        val selectSuffix = ", stampRallyUserEntry.favorite, stampRallyUserEntry.ignored"
        val imageSubquery = StampRallyUtils.imageSubquery("$tableName.rowid", year)
        val selectFields = when (year) {
            DataYear.ANIME_NYC_2026,
                -> throw IllegalStateException("Cannot load rallies for $year")
            else -> listOf(
                "$tableName.id",
                "$tableName.fandom",
                "$tableName.tables",
                "$tableName.startTables",
                "$tableName.endTables",
                "$tableName.links",
                "$tableName.tableMin",
                "$tableName.totalCost",
                "$tableName.prize",
                "$tableName.prizeLimit",
                "$tableName.prizeMerch",
                "$tableName.series",
                "$tableName.merch",
                "$tableName.notes",
                "$tableName.images",
                imageSubquery,
                """(
                    SELECT
                        json_group_array (
                            json_object ('booth', artistEntry.booth, 'profileImage', artistEntry.profileImage)
                        )
                    FROM
                        artistEntry
                        JOIN stampRallyArtistConnection ON stampRallyArtistConnection.artistRowId = artistEntry.rowid
                    WHERE
                        stampRallyArtistConnection.stampRallyRowId = $tableName.rowid
                 )""".trimMargin(),
            )
        }.joinToString()

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
                SELECT $selectFields$selectSuffix
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
            "notes",
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
            tableName = tableName,
            ftsTableName = "${tableName}_fts",
            idField = "id",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            andStatement = andStatement,
        )
        val statement = DaoUtils.buildSearchStatement(
            tableName = tableName,
            ftsTableName = "${tableName}_fts",
            select = "$selectFields$selectSuffix",
            idField = "id",
            likeOrderBy = "",
            matchQuery = matchQuery,
            likeStatement = likeStatement,
            additionalJoinStatement = joinStatement,
            orderBy = sortSuffix,
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
            mapper = { cursor, _ -> cursor.toStampRallyWithUserData(year) },
        )
    }

    suspend fun getAllEntries(year: DataYear) =
        dao().getAllEntries(year).awaitAsList()
            .map {
                StampRallySummary(
                    id = it.id,
                    fandom = it.fandom,
                    hostTable = it.tables.firstOrNull().orEmpty(),
                    tables = it.tables,
                    series = it.series,
                )
            }

    suspend fun getAllEntriesForChangelog(year: DataYear) =
        dao().getAllEntries(year).awaitAsList()
            .associateBy { Uuid.parse(it.id) }

    // TODO: Split by DataYear
    suspend fun getChangelog(year: DataYear): List<StampRallyChangelogEntry> =
        daoChangelog().getAllEntries(year).awaitAsList()
}
