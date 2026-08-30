package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.TestQueries
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
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
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

private const val DEBUG = false
private val TEST_COUNT_ARTISTS = if (DEBUG) 6 else 600
private val TEST_COUNT_RALLIES = if (DEBUG) 3 else 150

class AlleyExporterTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun exportPartial() = runTest {
        val dataYear = DataYear.entries.last { it.hasRallies }
        val buffer = Buffer()
        exportPartial(buffer, dataYear)

        val output = buffer.copy().readString()
        println("Export length ${output.length}: $output")
        val separatorChar = AlleyExporter.SEPARATOR.first()
        println("Separator count: ${output.count { it == separatorChar }}")

        val importResult = import(buffer)
        assertTrue(importResult.success, importResult.error?.message?.leftOrNull())

        val database = importResult.result!!
        assertDataPartial(database, dataYear)
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
        assertAllDataYears(database)
        assertNotes(database)
    }

    private suspend fun assertDataPartial(database: AlleySqlDatabase, dataYear: DataYear) {
        assertData(
            database = database,
            values = artists(dataYear).take(TEST_COUNT_ARTISTS).toList(),
            id = { it.id.toString() },
            resultQuery = { getArtistUserData(dataYear).awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id.toString() },
        )
        assertData(
            database = database,
            values = rallies(dataYear).take(TEST_COUNT_RALLIES).toList(),
            id = { it.id },
            resultQuery = { getStampRallyUserData(dataYear).awaitAsList() },
            testFavoriteAndIgnored = { it.favorite to it.ignored },
            testId = { it.id },
        )
    }

    private suspend fun assertAllDataYears(database: AlleySqlDatabase) {
        DataYear.entries.forEach { dataYear ->
            assertData(
                database = database,
                values = artists(dataYear).take(TEST_COUNT_ARTISTS).toList(),
                id = { it.id.toString() },
                resultQuery = { getArtistUserData(dataYear).awaitAsList() },
                testFavoriteAndIgnored = { it.favorite to it.ignored },
                testId = { it.id.toString() },
            )
        }

        DataYear.entries
            .filter { it.hasRallies }
            .forEach { dataYear ->
                assertData(
                    database = database,
                    values = rallies(dataYear).take(TEST_COUNT_RALLIES).toList(),
                    id = { it.id },
                    resultQuery = { getStampRallyUserData(dataYear).awaitAsList() },
                    testFavoriteAndIgnored = { it.favorite to it.ignored },
                    testId = { it.id },
                )
            }
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
        val expected = DataYear.entries
            .flatMap { dataYear ->
                artists(dataYear)
                    .take(TEST_COUNT_ARTISTS)
                    .filterIndexed { index, _ -> index % 3 == 2 }
                    .map {
                        Triple(
                            it.id.toString(),
                            dataYear,
                            "notes${it.id.toString().hashCode()}"
                        )
                    }
            }
            .sortedWith(compareBy({ it.first }, { it.second }))
            .toList()

        val actual = database.testQueries
            .getArtistNotes()
            .awaitAsList()
            .map { Triple(it.artistId, it.dataYear, it.notes) }

        assertEquals(expected, actual)
    }

    private suspend fun assertStampRallyNotes(database: AlleySqlDatabase) {
        val expected = DataYear.entries
            .filter { it.hasRallies }
            .flatMap {
                rallies(it)
                    .take(TEST_COUNT_RALLIES)
                    .filterIndexed { index, _ -> index % 3 == 2 }
                    .map { it.id to "notes${it.id.hashCode()}" }
            }
            .sortedBy { it.first }
            .toList()

        val actual = database.testQueries
            .getStampRallyNotes()
            .awaitAsList()
            .map { it.stampRallyId to it.notes }

        assertEquals(expected, actual)
    }

    private suspend fun exportPartial(sink: Sink, year: DataYear) {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = true)

        val importExportDao = ImportExportDao(driver = { driver }, database = { database })
        val exporter = AlleyExporter(importExportDao)
        exporter.exportPartial(sink, year)
    }

    private suspend fun exportFull(sink: Sink) {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = true)

        val importExportDao = ImportExportDao(driver = { driver }, database = { database })
        val exporter = AlleyExporter(importExportDao)
        // TODO: Test export metadata
        exporter.exportFull(false, sink)
    }

    private suspend fun import(source: Source): LoadingResult<AlleySqlDatabase> {
        val driver = makeDriver()
        val database = makeDatabase(driver)
        addData(database, insertUserData = false)

        val importExportDao = ImportExportDao(driver = { driver }, database = { database })
        val exporter = AlleyExporter(importExportDao)
        return exporter.import(source).transformResult { database }
    }

    private suspend fun makeDriver() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { AlleySqlDatabase.Schema.awaitCreate(it) }

    private fun makeDatabase(driver: SqlDriver): AlleySqlDatabase =
        DaoUtils.createAlleySqlDatabase(driver)

    private suspend fun addData(database: AlleySqlDatabase, insertUserData: Boolean = false) {
        DataYear.entries.forEach { dataYear ->
            insertArtists(
                database = database,
                source = artists(dataYear),
                dataYear = dataYear,
                id = { it.id.toString() },
                insert = { insertArtist(it) },
                insertUserData = insertUserData,
            )
        }
        DataYear.entries
            .filter { it.hasRallies }
            .forEach { dataYear ->
                insertRallies(
                    database = database,
                    source = rallies(dataYear),
                    id = { it.id },
                    insert = { insertStampRally(it) },
                    insertUserData = insertUserData,
                )
            }
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

    private fun artists(dataYear: DataYear) = sequence {
        yieldAll(
            ids(dataYear.serializedName).mapIndexed { index, uuid ->
                val name = uuid.toString().take(10)
                ArtistEntry(
                    id = uuid,
                    dataYear = dataYear,
                    status = null,
                    booth = "$index",
                    name = name,
                    summary = "summary$index",
                    socialLinks = listOf("https://example.com/social/$index"),
                    storeLinks = listOf("https://example.com/store/$index"),
                    portfolioLinks = listOf("https://example.com/portfolio/$index"),
                    catalogLinks = listOf("https://example.com/catalog/$index"),
                    linkFlags = 0L,
                    linkFlags2 = 0L,
                    notes = null,
                    commissions = listOf("https://example.com/commissions/$index"),
                    commissionFlags = 0L,
                    seriesInferred = listOf("Original", "Original Characters"),
                    seriesConfirmed = listOf("Original", "Vocaloid"),
                    merchInferred = listOf("Charms", "Stickers"),
                    merchConfirmed = listOf("Charms", "Prints"),
                    images = emptyList(),
                    fallbackImageYear = null,
                    tempImages = null,
                    profileImage = null,
                    embeds = null,
                    editorNotes = null,
                    lastEditor = null,
                    lastEditTime = null,
                    verifiedArtist = false,
                    newArtist = false,
                )
            }
        )
    }

    private fun rallies(dataYear: DataYear) = sequence {
        yieldAll(
            ids(dataYear.serializedName).mapIndexed { index, uuid ->
                StampRallyEntry(
                    id = uuid.toString(),
                    dataYear = dataYear,
                    fandom = "fandom$index",
                    tables = listOf(index.toString(), (index + 1).toString()),
                    startTables = setOf(index.toString()),
                    endTables = setOf((index + 1).toString()),
                    links = listOf("https://example.com"),
                    tableMin = TableMin.Price(index),
                    totalCost = index * 2L,
                    prize = "prize$index",
                    prizeLimit = index.toLong(),
                    prizeMerch = listOf("Stickers"),
                    series = listOf("Original", "Original Characters"),
                    merch = listOf("Charms", "Stickers"),
                    notes = "notes$index",
                    images = emptyList(),
                    editorNotes = null,
                    lastEditor = null,
                    lastEditTime = null,
                )
            }
        )
    }
}
