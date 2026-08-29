package com.thekeeperofpie.artistalleydatabase.alley.metrics

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MetricsQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
class MetricsDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val daoMetrics: suspend () -> MetricsQueries = { database().metricsQueries },
) {
    @Inject
    constructor(database: ArtistAlleyDatabase) : this(
        driver = database::driver,
        database = database::database,
    )

    suspend fun getArtistsWithMostSeries(
        year: DataYear,
        includeInferred: Boolean,
    ): List<ArtistData> {
        val flags = tagFlags(year, includeInferred)
        val statement = """
            SELECT artistEntry.id, artistEntry.booth, artistEntry.name, COUNT(*) AS count
            FROM artistSeriesConnection
            INNER JOIN artistEntry ON artistSeriesConnection.artistRowId = artistEntry.rowid
            WHERE (yearFlags & $flags) != 0
            GROUP BY artistRowId
            ORDER BY count DESC
            LIMIT 10
        """.trimIndent()

        return DaoUtils.makeQuery(
            driver = driver(),
            statement = statement,
            tableNames = listOf("artistSeriesConnection", "artistEntry"),
            mapper = { it.toArtistData(year) },
        ).awaitAsList()
    }

    suspend fun getArtistsWithMostMerch(
        year: DataYear,
        includeInferred: Boolean,
    ): List<ArtistData> {
        val flags = tagFlags(year, includeInferred)
        val statement = """
            SELECT artistEntry.id, artistEntry.booth, artistEntry.name, COUNT(*) AS count
            FROM artistMerchConnection
            INNER JOIN artistEntry ON artistMerchConnection.artistRowId = artistEntry.rowid
            WHERE (yearFlags & $flags) != 0
            GROUP BY artistRowId
            ORDER BY count DESC
            LIMIT 10
        """.trimIndent()

        return DaoUtils.makeQuery(
            driver = driver(),
            statement = statement,
            tableNames = listOf("artistMerchConnection", "artistEntry"),
            mapper = { it.toArtistData(year) },
        ).awaitAsList()
    }

    suspend fun getArtistsWithMostRallies(year: DataYear): List<ArtistData> {
        val statement = """
            SELECT artistEntry.id, artistEntry.booth, artistEntry.name, COUNT(*) AS count
            FROM stampRallyArtistConnection
            INNER JOIN stampRallyEntry ON stampRallyArtistConnection.stampRallyRowId = stampRallyEntry.rowid
            INNER JOIN artistEntry ON stampRallyArtistConnection.artistRowId = artistEntry.rowid
            GROUP BY artistRowId
            ORDER BY count DESC
            LIMIT 10
        """.trimIndent()

        return DaoUtils.makeQuery(
            driver = driver(),
            statement = statement,
            tableNames = listOf("stampRallyArtistConnection", "stampRallyEntry", "artistEntry"),
            mapper = { it.toArtistData(year) }
        ).awaitAsList()
    }

    suspend fun getPopularSeries(
        year: DataYear,
        includeInferred: Boolean,
    ): List<SeriesData> =
        daoMetrics()
            .getPopularSeries(tagFlags(year, includeInferred))
            .awaitAsList()
            .map { SeriesData(year, it.id, it.count) }

    suspend fun getPopularMerch(
        year: DataYear,
        includeInferred: Boolean,
    ): List<MerchData> =
        daoMetrics()
            .getPopularMerch(tagFlags(year, includeInferred))
            .awaitAsList()
            .map { MerchData(year, it.merchId, it.count) }

    private fun tagFlags(year: DataYear, includeInferred: Boolean): Long {
        var flags = TagYearFlag.getFlag(year, confirmed = true)
        if (includeInferred) {
            flags = flags or TagYearFlag.getFlag(year, confirmed = false)
        }
        return flags
    }

    private fun SqlCursor.toArtistData(year: DataYear) = ArtistData(
        year = year,
        id = Uuid.parse(getString(0)!!),
        booth = getString(1),
        name = getString(2)!!,
        count = getLong(3)!!,
    )

    data class ArtistData(
        val year: DataYear,
        val id: Uuid,
        val booth: String?,
        val name: String,
        val count: Long,
    )

    data class SeriesData(
        val year: DataYear,
        val id: String,
        val count: Long,
    )

    data class MerchData(
        val year: DataYear,
        val id: String,
        val count: Long,
    )
}
