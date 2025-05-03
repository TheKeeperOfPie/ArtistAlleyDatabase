package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.TestQueries
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readString
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TEST_COUNT_ARTISTS = 600
private const val TEST_COUNT_RALLIES = 150

@OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class)
class AlleyExporterTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun exportPartial() = runTest {
        val buffer = Buffer()
        exportPartial(buffer)

        val output = buffer.copy().readString()
        println("Export length ${output.length}: $output")
        val separatorChar = AlleyExporter.SEPARATOR.first()
        println("Separator count: ${output.count { it == separatorChar }}")

        val importResult = import(buffer)
        assertTrue(importResult.success, importResult.error?.message?.leftOrNull())

        val database = importResult.result!!
        assertDataPartial(database)
    }

    @Test
    fun exportFull() = runTest {
        val tempFile = temporaryFolder.newFile()
        tempFile.outputStream().use {
            it.asSink().buffered().use { exportFull(it) }
        }

        println("Export size ${tempFile.length()} at ${tempFile.absolutePath}: ${tempFile.readText()}")

        val importResult = tempFile.inputStream().use {
            it.asSource().buffered().use { import(it) }
        }
        assertTrue(importResult.success, importResult.error?.message?.leftOrNull())

        val database = importResult.result!!
        assertDataPartial(database)
        assertNotes(database)
    }

    private suspend fun assertDataPartial(database: AlleySqlDatabase) {
        assertData(
            database = database,
            values = artists2023().take(TEST_COUNT_ARTISTS).toList(),
            id = { it.id },
            resultQuery = { getArtistUserData2023().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
        assertData(
            database = database,
            values = artists2024().take(TEST_COUNT_ARTISTS).toList(),
            id = { it.id },
            resultQuery = { getArtistUserData2024().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
        assertData(
            database = database,
            values = artists2025().take(TEST_COUNT_ARTISTS).toList(),
            id = { it.id },
            resultQuery = { getArtistUserData2025().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )

        assertData(
            database = database,
            values = rallies2023().take(TEST_COUNT_RALLIES).toList(),
            id = { it.id },
            resultQuery = { getStampRallyUserData2023().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
        assertData(
            database = database,
            values = rallies2024().take(TEST_COUNT_RALLIES).toList(),
            id = { it.id },
            resultQuery = { getStampRallyUserData2024().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
        assertData(
            database = database,
            values = rallies2025().take(TEST_COUNT_RALLIES).toList(),
            id = { it.id },
            resultQuery = { getStampRallyUserData2025().awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
    }

    private suspend fun <Entry, TestEntry> assertData(
        database: AlleySqlDatabase,
        values: List<Entry>,
        id: (Entry) -> String,
        resultQuery: suspend TestQueries.() -> List<TestEntry>,
        testFavoriteAndIgnored: (TestEntry) -> Pair<Boolean?, Boolean?>,
        testId: (TestEntry) -> String,
    ) {
        val expectedFavorites = values
            .filterIndexed { index, _ -> index % 3 == 0 }
            .map(id)
            .sorted()
            .toList()
        val expectedIgnored = values
            .filterIndexed { index, _ -> index % 3 == 1 }
            .map(id)
            .sorted()
            .toList()
        val actualEntries = database.testQueries.resultQuery()
        val actualFavorites = actualEntries
            .filter { testFavoriteAndIgnored(it).first == true }
            .map(testId)
            .sorted()
        val actualIgnored = actualEntries
            .filter { testFavoriteAndIgnored(it).second == true }
            .map(testId)
            .sorted()

        assertEquals(expectedFavorites, actualFavorites)
        assertEquals(expectedIgnored, actualIgnored)
    }

    private suspend fun assertNotes(database: AlleySqlDatabase) {
        assertArtistNotes(database)
        assertStampRallyNotes(database)
    }

    private suspend fun assertArtistNotes(database: AlleySqlDatabase) {
        val artists2023 = artists2023()
            .take(TEST_COUNT_ARTISTS)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { Triple(it.id, DataYear.YEAR_2023, "notes${it.id.hashCode()}") }
        val artists2024 = artists2024()
            .take(TEST_COUNT_ARTISTS)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { Triple(it.id, DataYear.YEAR_2024, "notes${it.id.hashCode()}") }
        val artists2025 = artists2025()
            .take(TEST_COUNT_ARTISTS)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { Triple(it.id, DataYear.YEAR_2025, "notes${it.id.hashCode()}") }

        val expected = (artists2023 + artists2024 + artists2025)
            .sortedWith(compareBy({ it.first }, { it.second }))
            .toList()

        val actual = database.testQueries
            .getArtistNotes()
            .awaitAsList()
            .map { Triple(it.artistId, it.dataYear, it.notes) }

        assertEquals(expected, actual)
    }

    private suspend fun assertStampRallyNotes(database: AlleySqlDatabase) {
        val rallies2023 = rallies2023()
            .take(TEST_COUNT_RALLIES)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { it.id to "notes${it.id.hashCode()}" }
        val rallies2024 = rallies2024()
            .take(TEST_COUNT_RALLIES)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { it.id to "notes${it.id.hashCode()}" }
        val rallies2025 = rallies2025()
            .take(TEST_COUNT_RALLIES)
            .filterIndexed { index, _ -> index % 3 == 2 }
            .map { it.id to "notes${it.id.hashCode()}" }
        val expected = (rallies2023 + rallies2024 + rallies2025)
            .sortedBy { it.first }
            .toList()

        val actual = database.testQueries
            .getStampRallyNotes()
            .awaitAsList()
            .map { it.stampRallyId to it.notes }

        assertEquals(expected, actual)
    }

    private suspend fun exportPartial(sink: Sink) {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = true)

        val importExportDao = ImportExportDao { database }
        val exporter = AlleyExporter(importExportDao)
        exporter.exportPartial(sink)
    }

    private suspend fun exportFull(sink: Sink) {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = true)

        val importExportDao = ImportExportDao { database }
        val exporter = AlleyExporter(importExportDao)
        exporter.exportFull(sink)
    }

    private suspend fun import(source: Source): LoadingResult<AlleySqlDatabase> {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = false)

        val importExportDao = ImportExportDao { database }
        val exporter = AlleyExporter(importExportDao)
        return exporter.import(source).transformResult { database }
    }

    private suspend fun makeDriver() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { AlleySqlDatabase.Schema.awaitCreate(it) }

    private fun makeDatabase(driver: SqlDriver): AlleySqlDatabase = AlleySqlDatabase(
        driver = driver,
        artistEntry2023Adapter = ArtistEntry2023.Adapter(
            artistNamesAdapter = DaoUtils.listStringAdapter,
            linksAdapter = DaoUtils.listStringAdapter,
            catalogLinksAdapter = DaoUtils.listStringAdapter,
        ),
        artistEntry2024Adapter = ArtistEntry2024.Adapter(
            linksAdapter = DaoUtils.listStringAdapter,
            storeLinksAdapter = DaoUtils.listStringAdapter,
            catalogLinksAdapter = DaoUtils.listStringAdapter,
            seriesInferredAdapter = DaoUtils.listStringAdapter,
            seriesConfirmedAdapter = DaoUtils.listStringAdapter,
            merchInferredAdapter = DaoUtils.listStringAdapter,
            merchConfirmedAdapter = DaoUtils.listStringAdapter,
        ),
        artistEntry2025Adapter = ArtistEntry2025.Adapter(
            linksAdapter = DaoUtils.listStringAdapter,
            storeLinksAdapter = DaoUtils.listStringAdapter,
            catalogLinksAdapter = DaoUtils.listStringAdapter,
            seriesInferredAdapter = DaoUtils.listStringAdapter,
            seriesConfirmedAdapter = DaoUtils.listStringAdapter,
            merchInferredAdapter = DaoUtils.listStringAdapter,
            merchConfirmedAdapter = DaoUtils.listStringAdapter,
            commissionsAdapter = DaoUtils.listStringAdapter,
        ),
        stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
            tablesAdapter = DaoUtils.listStringAdapter,
            linksAdapter = DaoUtils.listStringAdapter,
        ),
        stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
            tablesAdapter = DaoUtils.listStringAdapter,
            linksAdapter = DaoUtils.listStringAdapter,
        ),
        stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
            tablesAdapter = DaoUtils.listStringAdapter,
            linksAdapter = DaoUtils.listStringAdapter,
        ),
        artistNotesAdapter = ArtistNotes.Adapter(
            dataYearAdapter = DaoUtils.dataYearAdapter,
        ),
        artistUserEntryAdapter = ArtistUserEntry.Adapter(
            dataYearAdapter = DaoUtils.dataYearAdapter,
        ),
        seriesEntryAdapter = SeriesEntry.Adapter(
            sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
                override fun decode(databaseValue: String) =
                    SeriesSource.entries.find { it.name == databaseValue }
                        ?: SeriesSource.NONE

                override fun encode(value: SeriesSource) = value.name
            },
        )
    )

    private suspend fun addData(database: AlleySqlDatabase, insertUserData: Boolean = false) {
        insertArtists(
            database = database,
            source = artists2023(),
            dataYear = DataYear.YEAR_2023,
            id = { it.id },
            insert = TestQueries::insertArtist2023,
            insertUserData = insertUserData,
        )

        insertArtists(
            database = database,
            source = artists2024(),
            dataYear = DataYear.YEAR_2024,
            id = { it.id },
            insert = TestQueries::insertArtist2024,
            insertUserData = insertUserData,
        )

        insertArtists(
            database = database,
            source = artists2025(),
            dataYear = DataYear.YEAR_2025,
            id = { it.id },
            insert = TestQueries::insertArtist2025,
            insertUserData = insertUserData,
        )

        insertRallies(
            database = database,
            source = rallies2023(),
            id = { it.id },
            insert = TestQueries::insertStampRally2023,
            insertUserData = insertUserData,
        )

        insertRallies(
            database = database,
            source = rallies2024(),
            id = { it.id },
            insert = TestQueries::insertStampRally2024,
            insertUserData = insertUserData,
        )

        insertRallies(
            database = database,
            source = rallies2025(),
            id = { it.id },
            insert = TestQueries::insertStampRally2025,
            insertUserData = insertUserData,
        )
    }

    private suspend fun <T> insertArtists(
        database: AlleySqlDatabase,
        source: Sequence<T>,
        dataYear: DataYear,
        id: (T) -> String,
        insert: suspend TestQueries.(T) -> Unit,
        insertUserData: Boolean = false,
    ) {
        val values = source.take(TEST_COUNT_ARTISTS).toList()
        values.forEach { database.testQueries.insert(it) }
        if (insertUserData) {
            values.mapIndexed { index, artist ->
                ArtistUserEntry(
                    artistId = id(artist),
                    dataYear = dataYear,
                    favorite = index % 3 == 0,
                    ignored = index % 3 == 1,
                )
            }
                .filter { it.favorite || it.ignored }
                .forEach { database.userEntryQueries.insertArtistUserEntry(it) }
            values.filterIndexed { index, _ -> index % 3 == 2 }
                .forEach {
                    database.notesQueries.updateArtistNotes(
                        artistId = id(it),
                        dataYear = dataYear,
                        notes = "notes${id(it).hashCode()}",
                    )
                }
        }
    }

    private suspend fun <T> insertRallies(
        database: AlleySqlDatabase,
        source: Sequence<T>,
        id: (T) -> String,
        insert: suspend TestQueries.(T) -> Unit,
        insertUserData: Boolean = false,
    ) {
        val values = source.take(TEST_COUNT_RALLIES).toList()
        values.forEach { database.testQueries.insert(it) }
        if (insertUserData) {
            values.mapIndexed { index, rally ->
                StampRallyUserEntry(
                    stampRallyId = id(rally),
                    favorite = index % 3 == 0,
                    ignored = index % 3 == 1,
                )
            }
                .filter { it.favorite || it.ignored }
                .forEach { database.userEntryQueries.insertStampRallyUserEntry(it) }
            values.filterIndexed { index, _ -> index % 3 == 2 }
                .forEach {
                    database.notesQueries.updateStampRallyNotes(id(it), "notes${id(it).hashCode()}")
                }
        }
    }

    private fun ids(seed: String) = sequence {
        val random = Random(seed.hashCode())
        while (true) {
            val bytes = ByteArray(16)
            random.nextBytes(bytes)
            yield(Uuid.fromByteArray(bytes))
        }
    }

    private fun artists2023() = sequence {
        yieldAll(
            ids("artists2023").mapIndexed { index, uuid ->
                val name = uuid.toString().take(10)
                ArtistEntry2023(
                    id = uuid.toString(),
                    booth = "",
                    name = name,
                    artistNames = listOf(name),
                    summary = null,
                    links = emptyList(),
                    catalogLinks = emptyList(),
                    driveLink = null,
                    counter = index.toLong(),
                )
            }
        )
    }

    private fun artists2024() = sequence {
        yieldAll(
            ids("artists2024").mapIndexed { index, uuid ->
                val name = uuid.toString().take(10)
                ArtistEntry2024(
                    id = uuid.toString(),
                    booth = "",
                    name = name,
                    summary = null,
                    links = emptyList(),
                    storeLinks = emptyList(),
                    catalogLinks = emptyList(),
                    driveLink = null,
                    notes = null,
                    seriesInferred = emptyList(),
                    seriesConfirmed = emptyList(),
                    merchInferred = emptyList(),
                    merchConfirmed = emptyList(),
                    counter = index.toLong(),
                )
            }
        )
    }

    private fun artists2025() = sequence {
        yieldAll(
            ids("artists2025").mapIndexed { index, uuid ->
                val name = uuid.toString().take(10)
                ArtistEntry2025(
                    id = uuid.toString(),
                    booth = "",
                    name = name,
                    summary = null,
                    links = emptyList(),
                    linkFlags = 0L,
                    linkFlags2 = 0L,
                    storeLinks = emptyList(),
                    catalogLinks = emptyList(),
                    driveLink = null,
                    notes = null,
                    commissions = emptyList(),
                    commissionOnline = false,
                    commissionOnsite = false,
                    commissionOther = false,
                    commissionVGen = false,
                    seriesInferred = emptyList(),
                    seriesConfirmed = emptyList(),
                    merchInferred = emptyList(),
                    merchConfirmed = emptyList(),
                    counter = index.toLong(),
                )
            }
        )
    }

    private fun rallies2023() = sequence {
        yieldAll(
            ids("rallies2023").mapIndexed { index, uuid ->
                StampRallyEntry2023(
                    id = uuid.toString(),
                    fandom = "",
                    hostTable = "",
                    tables = emptyList(),
                    links = emptyList(),
                    counter = index.toLong(),
                )
            }
        )
    }

    private fun rallies2024() = sequence {
        yieldAll(
            ids("rallies2024").mapIndexed { index, uuid ->
                StampRallyEntry2024(
                    id = uuid.toString(),
                    fandom = "",
                    hostTable = "",
                    tables = emptyList(),
                    links = emptyList(),
                    tableMin = null,
                    totalCost = null,
                    prizeLimit = null,
                    notes = null,
                    counter = index.toLong(),
                )
            }
        )
    }

    private fun rallies2025() = sequence {
        yieldAll(
            ids("rallies2025").mapIndexed { index, uuid ->
                StampRallyEntry2025(
                    id = uuid.toString(),
                    fandom = "",
                    hostTable = "",
                    tables = emptyList(),
                    links = emptyList(),
                    tableMin = null,
                    totalCost = null,
                    prizeLimit = null,
                    notes = null,
                    counter = index.toLong(),
                )
            }
        )
    }
}
