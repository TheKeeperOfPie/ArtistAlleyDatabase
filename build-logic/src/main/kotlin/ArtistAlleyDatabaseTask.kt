import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2025
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallySeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.build_logic.BuildLogicDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.AnimeNycExhibitorTags
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link.Companion.parse
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link.Type
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Instant

@CacheableTask
abstract class ArtistAlleyDatabaseTask : DefaultTask() {

    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
        private const val SERIES_CSV_NAME = "series.csv"
        private const val MERCH_CSV_NAME = "merch.csv"
        private const val DATABASE_CHUNK_SIZE = 50

        private val commaRegex = Regex(",\\s?")
    }

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputsDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImages: DirectoryProperty

    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    private val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    private val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
        override fun decode(databaseValue: String) =
            DataYear.entries.first { it.serializedName == databaseValue }

        override fun encode(value: DataYear) = value.serializedName
    }

    private val listCatalogImageAdapter = object : ColumnAdapter<List<CatalogImage>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<CatalogImage>>(databaseValue)

        override fun encode(value: List<CatalogImage>) = Json.encodeToString(value)
    }

    private val artistStatusAdapter = object : ColumnAdapter<ArtistStatus, String> {
        override fun decode(databaseValue: String) =
            ArtistStatus.entries.find { it.name == databaseValue } ?: ArtistStatus.UNKNOWN

        override fun encode(value: ArtistStatus) = value.name
    }

    private val instantAdapter = object : ColumnAdapter<Instant, String> {
        override fun decode(databaseValue: String) = try {
            Instant.parse(databaseValue)
        } catch (_: IllegalArgumentException) {
            Instant.DISTANT_PAST
        }

        override fun encode(value: Instant) = value.toString()
    }

    init {
        inputsDirectory.convention(layout.projectDirectory.dir("inputs"))
        outputResources.convention(layout.buildDirectory.dir("generated/composeResources"))
    }

    @TaskAction
    fun process() {
        val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
        val databaseFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        if (databaseFile.exists() && !databaseFile.delete()) {
            throw IllegalStateException(
                "Failed to delete $databaseFile, manually delete to re-process inputs"
            )
        } else {
            var (driver, database) = createDatabase(databaseFile)

            val seriesConnections = mutableMapOf<Pair<String, String>, ArtistSeriesConnection>()
            val merchConnections = mutableMapOf<Pair<String, String>, ArtistMerchConnection>()

            val artists2023 = parseArtists2023(imageCacheDir, database)

            val (artists2024, seriesConnections2024, merchConnections2024) =
                parseArtists2024(imageCacheDir, database)
            seriesConnections2024.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnections2024.forEach { merchConnections.addMerchConnection(it) }

            val (artists2025, seriesConnections2025, merchConnections2025) =
                parseArtists2025(imageCacheDir, database, artists2023, artists2024)
            seriesConnections2025.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnections2025.forEach { merchConnections.addMerchConnection(it) }

            val (artistsAnimeNyc2024, seriesConnectionsAnimeNyc2024, merchConnectionsAnimeNyc2024) =
                parseArtistsAnimeNyc2024(
                    imageCacheDir,
                    database,
                    artists2023,
                    artists2024,
                    artists2025
                )
            seriesConnectionsAnimeNyc2024.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnectionsAnimeNyc2024.forEach { merchConnections.addMerchConnection(it) }

            val (_, seriesConnectionsAnimeNyc2025, merchConnectionsAnimeNyc2025) =
                parseArtistsAnimeNyc2025(
                    imageCacheDir,
                    database,
                    artists2023,
                    artists2024,
                    artistsAnimeNyc2024,
                    artists2025
                )
            seriesConnectionsAnimeNyc2025.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnectionsAnimeNyc2025.forEach { merchConnections.addMerchConnection(it) }

            val mutationQueries = database.mutationQueries
            mutationQueries.transaction {
                seriesConnections.values.forEach(mutationQueries::insertSeriesConnection)
                merchConnections.values.forEach(mutationQueries::insertMerchConnection)
            }

            val seriesEntries = parseSeries(database, seriesConnections)
            val merchEntries = parseMerch(database, merchConnections)

            val allEnteredSeriesIds = seriesConnections.map { it.value.seriesId }.toSet()
            val allValidSeriesIds = seriesEntries.map { it.id }.toSet()
            val seriesDiff = allEnteredSeriesIds - allValidSeriesIds
            if (seriesDiff.isNotEmpty()) {
                seriesDiff.forEach { badSeries ->
                    logger.warn("Entered series does not match valid series: $badSeries")
                    val brokenArtists = seriesConnections
                        .filter { it.value.seriesId == badSeries }
                        .map { it.value.artistId }
                    logger.warn("Broken artists: $brokenArtists")
                }
            }
            val seriesWithExtraSpaces = allValidSeriesIds.filter { it.endsWith(" ") }
            if (seriesWithExtraSpaces.isNotEmpty()) {
                logger.error("Series with extra spaces: $seriesWithExtraSpaces")
            }

            val allEnteredMerchIds = merchConnections.map { it.value.merchId }.toSet()
            val allValidMerchIds = merchEntries.map { it.name }.toSet()
            val merchDiff = allEnteredMerchIds - allValidMerchIds
            if (merchDiff.isNotEmpty()) {
                merchDiff.forEach { badSeries ->
                    logger.warn("Entered merch does not match valid merch: $badSeries")
                    val brokenArtists = merchConnections
                        .filter { it.value.merchId == badSeries }
                        .map { it.value.artistId }
                    logger.warn("Broken artists: $brokenArtists")
                }
            }
            val merchWithExtraSpaces = allValidMerchIds.filter { it.endsWith(" ") }
            if (merchWithExtraSpaces.isNotEmpty()) {
                logger.error("Merch with extra spaces: $merchWithExtraSpaces")
            }

            parseStampRallies2023(imageCacheDir, artists2023, database)
            parseStampRallies2024(imageCacheDir, artists2024, database)
            parseStampRallies2025(imageCacheDir, artists2025, database)


            runBlocking {
                val animeExpo2026 = inputsDirectory.file("animeExpo2026").get().asFile
                val animeExpo2026Database = animeExpo2026.resolve("database.sql")
                if (animeExpo2026Database.exists()) {
                    driver.close()
                    val success = ProcessBuilder(
                        "sqlite3",
                        databaseFile.absolutePath,
                        "\".read \'${animeExpo2026Database.absolutePath}\'\""
                    )
                        .inheritIO()
                        .redirectErrorStream(true)
                        .start()
                        .waitFor(30, TimeUnit.SECONDS)
                    if (!success) {
                        logger.error("Failed to apply animeExpo2026 database")
                    }

                    val pair = createDatabase(databaseFile)
                    driver = pair.first
                    database = pair.second

                    database.mutationQueries.getAllArtistEntryAnimeExpo2026Images().executeAsList()
                        .forEach {
                            val images =
                                findArtistImages(imageCacheDir, DataYear.ANIME_EXPO_2026, it.id)
                            val updatedImages = it.images.map { original ->
                                images.first { it.name.contains(original.name) }
                            }
                            database.mutationQueries
                                .updateArtistEntryAnimeExpo2026Images(updatedImages, it.id)
                        }
                }


                database.mutationQueries.cleanUpForRelease().await()
                // Don't retain user tables (merged from depending on :modules:alley:user)
                listOf(
                    "artistUserEntry",
                    "stampRallyUserEntry",
                    "artistNotes",
                    "stampRallyNotes",
                    "imageEntry",
                ).forEach {
                    driver.execute(null, "DROP TABLE $it;", 0, null).await()
                }

                val ftsTables = listOf(
                    "artistEntry2023_fts",
                    "artistEntry2024_fts",
                    "artistEntry2025_fts",
                    "artistEntryAnimeExpo2026_fts",
                    "artistEntryAnimeNyc2024_fts",
                    "artistEntryAnimeNyc2025_fts",
                    "stampRallyEntry2023_fts",
                    "stampRallyEntry2024_fts",
                    "stampRallyEntry2025_fts",
                    "seriesEntry_fts",
                    "merchEntry_fts",
                )

                ftsTables.forEach {
                    driver.execute(null, "INSERT INTO $it($it) VALUES('rebuild');", 0, null).await()
                    driver.execute(null, "INSERT INTO $it($it) VALUES('optimize');", 0, null)
                        .await()
                }

                driver.execute(null, "VACUUM;", 0, null).await()
            }

            driver.close()

            databaseFile.copyTo(
                outputResources.file("files/database.sqlite").get().asFile,
                overwrite = true,
            )
            val hash = Utils.hash(databaseFile)

            databaseFile.delete()

            outputResources.file("files/databaseHash.txt").get().asFile.writeText(hash.toString())
        }
    }

    private fun createDatabase(dbFile: File): Pair<JdbcSqliteDriver, BuildLogicDatabase> {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        try {
            BuildLogicDatabase.Schema.create(driver)
        } catch (_: Throwable) {
            Thread.sleep(5000)
            BuildLogicDatabase.Schema.create(driver)
        }
        val database = BuildLogicDatabase(
            driver = driver,
            artistEntry2023Adapter = ArtistEntry2023.Adapter(
                artistNamesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistEntry2024Adapter = ArtistEntry2024.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistEntry2025Adapter = ArtistEntry2025.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistEntryAnimeExpo2026Adapter = ArtistEntryAnimeExpo2026.Adapter(
                statusAdapter = artistStatusAdapter,
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
                lastEditTimeAdapter = instantAdapter,
            ),
            artistEntryAnimeNyc2024Adapter = ArtistEntryAnimeNyc2024.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistEntryAnimeNyc2025Adapter = ArtistEntryAnimeNyc2025.Adapter(
                linksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                catalogLinksAdapter = listStringAdapter,
                seriesInferredAdapter = listStringAdapter,
                seriesConfirmedAdapter = listStringAdapter,
                merchInferredAdapter = listStringAdapter,
                merchConfirmedAdapter = listStringAdapter,
                commissionsAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                seriesAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            stampRallyEntryAnimeExpo2026Adapter = StampRallyEntryAnimeExpo2026.Adapter(
                tablesAdapter = listStringAdapter,
                linksAdapter = listStringAdapter,
                seriesAdapter = listStringAdapter,
                imagesAdapter = listCatalogImageAdapter,
            ),
            artistNotesAdapter = ArtistNotes.Adapter(
                dataYearAdapter = dataYearAdapter,
            ),
            artistUserEntryAdapter = ArtistUserEntry.Adapter(
                dataYearAdapter = dataYearAdapter,
            ),
            seriesEntryAdapter = SeriesEntry.Adapter(
                sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
                    override fun decode(databaseValue: String) =
                        SeriesSource.entries.find { it.name == databaseValue }
                            ?: SeriesSource.NONE

                    override fun encode(value: SeriesSource) = value.name
                },
                synonymsAdapter = listStringAdapter,
            )
        )
        return driver to database
    }

    private fun parseArtists2023(
        imageCacheDir: File,
        database: BuildLogicDatabase,
    ): List<ArtistEntry2023> {
        val artistsCsv2023 = inputsDirectory.file("2023/$ARTISTS_CSV_NAME").get()
        if (!artistsCsv2023.asFile.exists()) return emptyList()
        return open(artistsCsv2023).use {
            var counter = 1L
            read(it)
                .map {
                    // "Booth","Table name","Artist names","Region","Summary","Contact","Links",
                    // "Catalog link","Commissions?","UUIDs","Catalog"
                    val ids = it["UUIDs"]!!.split("\n")
                    val booth = it["Booth"].orEmpty()
                    val tableName = it["Table name"].orEmpty()
                    val allArtistNames = it["Artist names"].orEmpty()
                        .split("\n\n")
                        .map { it.split("\n") }
                    val summary = it["Summary"]

                    val contactLinks = it["Contact"].orEmpty().split("\n\n")
                        .map { it.split("\n").filter(String::isNotBlank) }
                    val links = it["Links"].orEmpty().split("\n\n")
                        .map { it.split("\n").filter(String::isNotBlank) }
                    val catalogLinks = it["Catalog link"].orEmpty().split("\n\n")
                        .map { it.split("\n").filter(String::isNotBlank) }

                    val driveLink = it["Catalog"]

                    ids.mapIndexed { index, artistId ->
                        val artistNames = allArtistNames.getOrElse(index) { emptyList() }
                        val firstArtistName = artistNames.firstOrNull()
                        val name = if (allArtistNames.any {
                                it.any { tableName.contains(it) || it.contains(tableName) }
                            }) {
                            firstArtistName ?: tableName
                        } else if (firstArtistName != null && name != firstArtistName) {
                            "$tableName - $firstArtistName"
                        } else {
                            tableName
                        }
                        ArtistEntry2023(
                            id = artistId,
                            booth = booth,
                            name = name,
                            artistNames = artistNames,
                            summary = summary,
                            links = (contactLinks.getOrElse(index) { emptyList() } +
                                    links.getOrElse(index) { emptyList() }).distinct(),
                            catalogLinks = catalogLinks.getOrElse(index) { emptyList() },
                            driveLink = driveLink,
                            images = findArtistImages(
                                imageCacheDir = imageCacheDir,
                                year = DataYear.ANIME_EXPO_2023,
                                id = artistId,
                            ),
                            counter = counter++,
                        )
                    }
                }
                .flatten()
                .chunked(100)
                .onEach {
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertArtist2023)
                    }
                }
                .flatten()
                .toList()
        }
    }

    private fun parseArtists2024(
        imageCacheDir: File,
        database: BuildLogicDatabase,
    ): Triple<List<ArtistEntry2024>, MutableList<ArtistSeriesConnection>, MutableList<ArtistMerchConnection>> {
        val seriesConnections = mutableListOf<ArtistSeriesConnection>()
        val merchConnections = mutableListOf<ArtistMerchConnection>()
        val artistsCsv2024 = inputsDirectory.file("2024/$ARTISTS_CSV_NAME").get()
        if (!artistsCsv2024.asFile.exists()) return Triple(
            emptyList(),
            mutableListOf(),
            mutableListOf()
        )
        val artists2024 = open(artistsCsv2024).use {
            var counter = 1L
            read(it)
                .map {
                    // Booth,Artist,Summary,Links,Store,Catalog / table,
                    // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                    // Merch - Confirmed,Drive,Catalog images
                    val id = it["UUID"]!!
                    val booth = it["Booth"].orEmpty()
                    val artist = it["Artist"].orEmpty()
                    val summary = it["Summary"]

                    val newLineRegex = Regex("\n\\s?")
                    val links = it["Links"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val storeLinks = it["Store"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val catalogLinks = it["Catalog / table"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val driveLink = it["Drive"]

                    val commaRegex = Regex(",\\s?")
                    val seriesInferredRaw = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    val merchInferredRaw = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val notes = it["Notes"]

                    val seriesInferred = (seriesInferredRaw - seriesConfirmed).sorted()
                    val merchInferred = (merchInferredRaw - merchConfirmed).sorted()

                    val artistEntry = ArtistEntry2024(
                        id = id,
                        booth = booth,
                        name = artist,
                        summary = summary,
                        links = links,
                        storeLinks = storeLinks,
                        catalogLinks = catalogLinks,
                        driveLink = driveLink,
                        seriesInferred = seriesInferred,
                        seriesConfirmed = seriesConfirmed,
                        merchInferred = merchInferred,
                        merchConfirmed = merchConfirmed,
                        notes = notes,
                        images = findArtistImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_EXPO_2024,
                            id = id,
                        ),
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2024Inferred = true),
                            )
                        }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2024Confirmed = true),
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2024Inferred = true),
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2024Confirmed = true),
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .onEach {
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtist2024)
                    }
                    it.flatMap { it.second }.forEach(seriesConnections::add)
                    it.flatMap { it.third }.forEach(merchConnections::add)
                }
                .map { it.map { it.first } }
                .flatten()
                .toList()
        }
        return Triple(artists2024, seriesConnections, merchConnections)
    }

    private fun parseArtists2025(
        imageCacheDir: File,
        database: BuildLogicDatabase,
        artists2023: List<ArtistEntry2023>,
        artists2024: List<ArtistEntry2024>,
    ): Triple<List<ArtistEntry2025>, MutableList<ArtistSeriesConnection>, MutableList<ArtistMerchConnection>> {
        val existingIds = mutableSetOf<String>()
        val artists2023ById = artists2023.associateBy { it.id }
        val artists2024ById = artists2024.associateBy { it.id }
        val seriesConnections = mutableListOf<ArtistSeriesConnection>()
        val merchConnections = mutableListOf<ArtistMerchConnection>()
        val artistsCsv2025 = inputsDirectory.file("2025/$ARTISTS_CSV_NAME").get()
        if (!artistsCsv2025.asFile.exists()) return Triple(
            emptyList(),
            mutableListOf(),
            mutableListOf()
        )
        val artists2025 = open(artistsCsv2025).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Input,Booth,Artist,Summary,Links,Store,Catalog - Inferred,Series - Inferred,
                    // Merch - Inferred,Notes,Commissions
                    val id = it["UUID"]!!
                    val artist = it["Artist"].orEmpty()
                    val booth = it["Booth"]
                    val summary = it["Summary"]

                    if (artist.isBlank()) return@mapNotNull null
                    val artist2023 = artists2023ById[id]
                    val artist2024 = artists2024ById[id]

                    val newLineRegex = Regex("\n\\s?")
                    val links = it["Links"]
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        ?.ifEmpty { artist2024?.links ?: artist2023?.links }
                        .orEmpty()

                    val storeLinks = it["Store"]
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        ?.ifEmpty { artist2024?.storeLinks }
                        .orEmpty()
                    val catalogLinks = it["Catalog - Confirmed"]
                        ?.ifEmpty { it["Catalog - Inferred"] }
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        .orEmpty()
                    val driveLink = it["Drive"]

                    var seriesInferredRaw = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (seriesInferredRaw.isEmpty() && artist2024 != null) {
                        seriesInferredRaw = artist2024.seriesConfirmed.ifEmpty {
                            (artist2024.seriesInferred + artist2024.seriesConfirmed).distinct()
                        }
                    }

                    var merchInferredRaw = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (merchInferredRaw.isEmpty() && artist2024 != null) {
                        merchInferredRaw = artist2024.merchConfirmed.ifEmpty {
                            (artist2024.merchInferred + artist2024.merchConfirmed).distinct()
                        }
                    }

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val notes = it["Notes"]
                    val commissions = it["Commissions"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val seriesInferred = (seriesInferredRaw - seriesConfirmed).sorted()
                    val merchInferred = (merchInferredRaw - merchConfirmed).sorted()

                    val linkTypes = (links + catalogLinks).map {
                        parse(it)?.type ?: Type.OTHER_NON_STORE
                    }
                    val storeLinkTypes = storeLinks.map {
                        parse(it)?.type ?: Type.OTHER_STORE
                    }

                    val (linkFlags, linkFlags2) = Link.parseFlags(linkTypes + storeLinkTypes)
                    val commissionFlags = CommissionType.parseFlags(commissions)
                    val artistEntry = ArtistEntry2025(
                        id = id,
                        booth = booth?.takeIf { it.length == 3 },
                        name = artist,
                        summary = summary,
                        links = links,
                        storeLinks = storeLinks,
                        catalogLinks = catalogLinks,
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        driveLink = driveLink,
                        seriesInferred = seriesInferred,
                        seriesConfirmed = seriesConfirmed,
                        merchInferred = merchInferred,
                        merchConfirmed = merchConfirmed,
                        notes = notes,
                        commissions = commissions,
                        commissionFlags = commissionFlags,
                        images = findArtistImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_EXPO_2025,
                            id = id,
                        ),
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred.map {
                        ArtistSeriesConnection(
                            artistId = id,
                            seriesId = it,
                            yearFlags = TagYearFlag.getFlags(hasAnimeExpo2025Inferred = true),
                        )
                    }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2025Confirmed = true),
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2025Inferred = true),
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeExpo2025Confirmed = true),
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .onEach {
                    it.forEach { (artist, _, _) ->
                        if (!existingIds.add(artist.id)) {
                            logger.error("Duplicate ID ${artist.id}")
                        }
                        if (artist.links.isEmpty()) {
                            logger.error("${artist.booth} ${artist.name} ${artist.id} has no links")
                        }
                    }
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtist2025)
                    }
                    it.flatMap { it.second }.forEach(seriesConnections::add)
                    it.flatMap { it.third }.forEach(merchConnections::add)
                }
                .map { it.map { it.first } }
                .flatten()
                .toList()
        }
        return Triple(artists2025, seriesConnections, merchConnections)
    }

    private fun parseArtistsAnimeNyc2024(
        imageCacheDir: File,
        database: BuildLogicDatabase,
        artists2023: List<ArtistEntry2023>,
        artists2024: List<ArtistEntry2024>,
        artists2025: List<ArtistEntry2025>,
    ): Triple<List<ArtistEntryAnimeNyc2024>, MutableList<ArtistSeriesConnection>, MutableList<ArtistMerchConnection>> {
        val existingIds = mutableSetOf<String>()
        val artists2023ById = artists2023.associateBy { it.id }
        val artists2024ById = artists2024.associateBy { it.id }
        val artists2025ById = artists2025.associateBy { it.id }
        val seriesConnections = mutableListOf<ArtistSeriesConnection>()
        val merchConnections = mutableListOf<ArtistMerchConnection>()
        val artistsCsvAnimeNyc2024 = inputsDirectory.file("animeNyc2024/$ARTISTS_CSV_NAME").get()
        if (!artistsCsvAnimeNyc2024.asFile.exists()) return Triple(
            emptyList(),
            mutableListOf(),
            mutableListOf()
        )
        val artistsAnimeNyc2024 = open(artistsCsvAnimeNyc2024).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Input,Booth,Artist,Summary,Links,Store,Catalog - Inferred,Series - Inferred,
                    // Merch - Inferred,Notes,Commissions
                    val id = (it["UUID"].takeUnless { it == "MATCH" } ?: return@mapNotNull null)
                        .also { UUID.fromString(it) }
                    val artist = it["Artist"].orEmpty()
                    val booth = it["Booth"]?.takeUnless { it.length > 4 }
                    val summary = it["Summary"]

                    if (artist.isBlank()) return@mapNotNull null
                    val artist2023 = artists2023ById[id]
                    val artist2024 = artists2024ById[id]
                    val artist2025 = artists2025ById[id]

                    val newLineRegex = Regex("\n\\s?")
                    val links = artist2025?.links.orEmpty().ifEmpty {
                        it["Links"]
                            ?.split(newLineRegex)
                            ?.filter(String::isNotBlank)
                            ?.ifEmpty {
                                artist2025?.links ?: artist2024?.links ?: artist2023?.links
                            }
                            .orEmpty()
                    }

                    val storeLinks = artist2025?.storeLinks.orEmpty().ifEmpty {
                        it["Store"]
                            ?.split(newLineRegex)
                            ?.filter(String::isNotBlank)
                            ?.ifEmpty { artist2025?.storeLinks ?: artist2024?.storeLinks }
                            .orEmpty()
                    }
                    val catalogLinks = it["Catalog - Confirmed"]
                        ?.ifEmpty { it["Catalog - Inferred"] }
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        .orEmpty()
                    val driveLink = it["Drive"]

                    var seriesInferredRaw = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (artist2025 != null) {
                        seriesInferredRaw =
                            (seriesInferredRaw + artist2025.seriesConfirmed).distinct()
                    }
                    if (seriesInferredRaw.isEmpty() && artist2024 != null) {
                        seriesInferredRaw = artist2024.seriesConfirmed.ifEmpty {
                            (artist2024.seriesInferred + artist2024.seriesConfirmed).distinct()
                        }
                    }

                    var merchInferredRaw = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (artist2025 != null) {
                        merchInferredRaw = (merchInferredRaw + artist2025.merchConfirmed).distinct()
                    }
                    if (merchInferredRaw.isEmpty() && artist2024 != null) {
                        merchInferredRaw = artist2024.merchConfirmed.ifEmpty {
                            (artist2024.merchInferred + artist2024.merchConfirmed).distinct()
                        }
                    }

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val notes = it["Notes"]
                    val commissions = it["Commissions"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val seriesInferred = (seriesInferredRaw - seriesConfirmed).sorted()
                    val merchInferred = (merchInferredRaw - merchConfirmed).sorted()

                    val linkTypes = (links + catalogLinks).map {
                        parse(it)?.type ?: Type.OTHER_NON_STORE
                    }
                    val storeLinkTypes = storeLinks.map {
                        parse(it)?.type ?: Type.OTHER_STORE
                    }

                    val (linkFlags, linkFlags2) = Link.parseFlags(linkTypes + storeLinkTypes)
                    val commissionFlags = CommissionType.parseFlags(commissions)
                    val artistEntry = ArtistEntryAnimeNyc2024(
                        id = id,
                        booth = booth?.takeIf { it.length == 3 },
                        name = artist,
                        summary = summary,
                        links = links,
                        storeLinks = storeLinks,
                        catalogLinks = catalogLinks,
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        driveLink = driveLink,
                        seriesInferred = seriesInferred,
                        seriesConfirmed = seriesConfirmed,
                        merchInferred = merchInferred,
                        merchConfirmed = merchConfirmed,
                        notes = notes,
                        commissions = commissions,
                        commissionFlags = commissionFlags,
                        images = findArtistImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_NYC_2024,
                            id = id,
                        ),
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred.map {
                        ArtistSeriesConnection(
                            artistId = id,
                            seriesId = it,
                            yearFlags = TagYearFlag.getFlags(hasAnimeNyc2024Inferred = true),
                        )
                    }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2024Confirmed = true),
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2024Inferred = true),
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2024Confirmed = true),
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .onEach {
                    it.forEach { (artist, _, _) ->
                        if (!existingIds.add(artist.id)) {
                            logger.error("Duplicate ID ${artist.id}")
                        }
                        if (artist.links.isEmpty()) {
                            logger.error("${artist.booth} ${artist.name} ${artist.id} has no links")
                        }
                    }
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtistAnimeNyc2024)
                    }
                    it.flatMap { it.second }.forEach(seriesConnections::add)
                    it.flatMap { it.third }.forEach(merchConnections::add)
                }
                .map { it.map { it.first } }
                .flatten()
                .toList()
        }
        return Triple(artistsAnimeNyc2024, seriesConnections, merchConnections)
    }

    private fun parseArtistsAnimeNyc2025(
        imageCacheDir: File,
        database: BuildLogicDatabase,
        artists2023: List<ArtistEntry2023>,
        artists2024: List<ArtistEntry2024>,
        artistsAnimeNyc2024: List<ArtistEntryAnimeNyc2024>,
        artists2025: List<ArtistEntry2025>,
    ): Triple<List<ArtistEntryAnimeNyc2025>, MutableList<ArtistSeriesConnection>, MutableList<ArtistMerchConnection>> {
        val existingIds = mutableSetOf<String>()
        val artists2023ById = artists2023.associateBy { it.id }
        val artists2024ById = artists2024.associateBy { it.id }
        val artistsAnimeNyc2024ById = artistsAnimeNyc2024.associateBy { it.id }
        val artists2025ById = artists2025.associateBy { it.id }
        val seriesConnections = mutableListOf<ArtistSeriesConnection>()
        val merchConnections = mutableListOf<ArtistMerchConnection>()
        val artistsCsvAnimeNyc2025 = inputsDirectory.file("animeNyc2025/$ARTISTS_CSV_NAME").get()
        if (!artistsCsvAnimeNyc2025.asFile.exists()) return Triple(
            emptyList(),
            mutableListOf(),
            mutableListOf()
        )
        val artistsAnimeNyc2025 = open(artistsCsvAnimeNyc2025).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Input,Booth,Artist,Summary,Links,Store,Catalog - Inferred,Series - Inferred,
                    // Merch - Inferred,Notes,Commissions
                    val id = it["UUID"]!!.also { UUID.fromString(it) }
                    val artist = it["Artist"].orEmpty()
                    val booth = it["Booth"]?.takeUnless { it.length > 4 }
                    val summary = it["Summary"]

                    if (artist.isBlank()) return@mapNotNull null
                    val artist2023 = artists2023ById[id]
                    val artist2024 = artists2024ById[id]
                    val artistAnimeNyc2024 = artistsAnimeNyc2024ById[id]
                    val artist2025 = artists2025ById[id]

                    val newLineRegex = Regex("\n\\s?")
                    val links = it["Links"]
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        ?.ifEmpty {
                            artist2025?.links ?: artist2024?.links ?: artistAnimeNyc2024?.links
                            ?: artist2023?.links
                        }
                        .orEmpty()

                    val storeLinks = it["Store"]
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        ?.ifEmpty {
                            artist2025?.storeLinks ?: artistAnimeNyc2024?.storeLinks
                            ?: artist2024?.storeLinks
                        }
                        .orEmpty()
                    val catalogLinks = it["Catalog - Confirmed"]
                        ?.ifEmpty { it["Catalog - Inferred"] }
                        ?.split(newLineRegex)
                        ?.filter(String::isNotBlank)
                        .orEmpty()
                    val driveLink = it["Drive"]

                    var seriesInferredRaw = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (seriesInferredRaw.isEmpty() && artist2025 != null) {
                        seriesInferredRaw = artist2025.seriesConfirmed.ifEmpty {
                            artist2025.seriesInferred.distinct()
                        }
                    }
                    if (seriesInferredRaw.isEmpty() && artist2024 != null) {
                        seriesInferredRaw = artist2024.seriesConfirmed.ifEmpty {
                            artist2024.seriesInferred
                        }
                    }
                    if (seriesInferredRaw.isEmpty() && artistAnimeNyc2024 != null) {
                        seriesInferredRaw = artistAnimeNyc2024.seriesConfirmed.ifEmpty {
                            artistAnimeNyc2024.seriesInferred
                        }
                    }

                    var merchInferredRaw = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    if (merchInferredRaw.isEmpty() && artist2025 != null) {
                        merchInferredRaw = artist2025.merchConfirmed.ifEmpty {
                            artist2025.merchInferred
                        }
                    }
                    if (merchInferredRaw.isEmpty() && artist2024 != null) {
                        merchInferredRaw = artist2024.merchConfirmed.ifEmpty {
                            artist2024.merchInferred
                        }
                    }
                    if (merchInferredRaw.isEmpty() && artistAnimeNyc2024 != null) {
                        merchInferredRaw = artistAnimeNyc2024.merchConfirmed.ifEmpty {
                            artistAnimeNyc2024.merchInferred
                        }
                    }

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val notes = it["Notes"]
                    val commissions = it["Commissions"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val seriesInferred = (seriesInferredRaw - seriesConfirmed).sorted()
                    val merchInferred = (merchInferredRaw - merchConfirmed).sorted()

                    val linkTypes = (links + catalogLinks).map {
                        parse(it)?.type ?: Type.OTHER_NON_STORE
                    }
                    val storeLinkTypes = storeLinks.map {
                        parse(it)?.type ?: Type.OTHER_STORE
                    }

                    val (linkFlags, linkFlags2) = Link.parseFlags(linkTypes + storeLinkTypes)
                    val commissionFlags = CommissionType.parseFlags(commissions)

                    val exhibitorTags = it["Exhibitor Tags"]
                        ?.split("\n")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                    val exhibitorTagFlags = AnimeNycExhibitorTags.parseFlags(exhibitorTags)

                    val artistEntry = ArtistEntryAnimeNyc2025(
                        id = id,
                        booth = booth?.takeIf { it.length == 3 },
                        name = artist,
                        summary = summary,
                        links = links,
                        storeLinks = storeLinks,
                        catalogLinks = catalogLinks,
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        driveLink = driveLink,
                        seriesInferred = seriesInferred,
                        seriesConfirmed = seriesConfirmed,
                        merchInferred = merchInferred,
                        merchConfirmed = merchConfirmed,
                        notes = notes,
                        commissions = commissions,
                        commissionFlags = commissionFlags,
                        exhibitorTagFlags = exhibitorTagFlags,
                        images = findArtistImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_NYC_2025,
                            id = id,
                        ),
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred.map {
                        ArtistSeriesConnection(
                            artistId = id,
                            seriesId = it,
                            yearFlags = TagYearFlag.getFlags(hasAnimeNyc2025Inferred = true),
                        )
                    }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2025Confirmed = true),
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2025Inferred = true),
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                yearFlags = TagYearFlag.getFlags(hasAnimeNyc2025Confirmed = true),
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .onEach {
                    it.forEach { (artist, _, _) ->
                        if (!existingIds.add(artist.id)) {
                            logger.error("Duplicate ID ${artist.id}")
                        }
                        if (artist.links.isEmpty()) {
                            logger.error("${artist.booth} ${artist.name} ${artist.id} has no links")
                        }
                    }
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtistAnimeNyc2025)
                    }
                    it.flatMap { it.second }.forEach(seriesConnections::add)
                    it.flatMap { it.third }.forEach(merchConnections::add)
                }
                .map { it.map { it.first } }
                .flatten()
                .toList()
        }
        return Triple(artistsAnimeNyc2025, seriesConnections, merchConnections)
    }

    private fun findArtistImages(
        imageCacheDir: File,
        year: DataYear,
        id: String,
    ): List<CatalogImage> {
        val folder = inputImages.get()
            .dir("files")
            .dir(year.folderName)
            .dir("catalogs")
            .asFile
            .listFiles()
            ?.find { it.name.endsWith(id) }
            ?: return emptyList()
        return folder
            .listFiles()
            .filterNotNull()
            .sortedBy { it.name.substringBefore("-").trim().toInt() }
            .map {
                val (width, height, _) = ArtistAlleyProcessInputsTask.parseScaledImageWidthHeight(
                    imageCacheDir = imageCacheDir,
                    file = it,
                )
                CatalogImage("${folder.name}/${it.name}", width, height)
            }
    }

    private fun fixRallyName(name: String) = name.replace("'", "_")
        .replace("&", "_")

    private fun findRallyImages(
        imageCacheDir: File,
        year: DataYear,
        id: String,
        hostTable: String?,
        fandom: String?,
    ): List<CatalogImage> {
        hostTable ?: fandom ?: return emptyList()
        val file = "$hostTable$fandom"
        val targetName = when (year) {
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
                -> fixRallyName(file)
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_EXPO_2026,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> id
        }
        val folder = inputImages.get()
            .dir("files")
            .dir(year.folderName)
            .dir("rallies")
            .asFile
            .listFiles()
            ?.find { it.name.startsWith(targetName) }
            ?: return emptyList()
        return folder
            .listFiles()
            .filterNotNull()
            .sortedBy { it.name.substringBefore("-").trim().toInt() }
            .map {
                val (width, height, _) = ArtistAlleyProcessInputsTask.parseScaledImageWidthHeight(
                    imageCacheDir = imageCacheDir,
                    file = it,
                )
                CatalogImage("${folder.name}/${it.name}", width, height)
            }
    }

    private fun parseSeries(
        database: BuildLogicDatabase,
        seriesConnections: MutableMap<Pair<String, String>, ArtistSeriesConnection>,
    ): List<SeriesEntry> {
        val mutationQueries = database.mutationQueries
        val seriesCsv = inputsDirectory.file(SERIES_CSV_NAME).get()
        if (!seriesCsv.asFile.exists()) return emptyList()
        return open(seriesCsv).use {
            var counter = 1L
            read(it)
                .map {
                    // Validated, Series, Notes, AniList ID, AniList Type, Source Type, English,
                    // Romaji, Native, Preferred, Wikipedia ID, External Link,
                    val validated = it["Validated"] == "DONE"
                    val id = it["Series"]!!
                    val uuid = it["UUID"]!!
                    val notes = it["Notes"]
                    val aniListId = it["AniList ID"]?.toLongOrNull()
                    val aniListType = it["AniList Type"]
                    val wikipediaId = it["Wikipedia ID"]?.toLongOrNull()
                    val link = it["External Link"]?.ifBlank { null }
                    val source = it["Source Type"]?.let { value ->
                        if (value.isBlank()) {
                            SeriesSource.NONE
                        } else {
                            SeriesSource.valueOf(value)
                        }
                    }

                    val titleRomaji = it["Romaji"]?.ifBlank { null }?.takeIf { validated }
                    val titleEnglish = it["English"]?.ifBlank { null }?.takeIf { validated }
                    val titleNative = it["Native"]?.ifBlank { null }?.takeIf { validated }
                    val titlePreferred = it["Preferred"]?.ifBlank { null }

                    val connections = seriesConnections.filter { it.value.seriesId == id }

                    val inferred2024 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2024_INFERRED
                        )
                    }
                    val inferred2025 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2025_INFERRED
                        )
                    }
                    val inferredAnimeExpo2026 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2026_INFERRED
                        )
                    }
                    val inferredAnimeNyc2024 = connections.count {
                        TagYearFlag.hasFlag(it.value.yearFlags, TagYearFlag.ANIME_NYC_2024_INFERRED)
                    }
                    val inferredAnimeNyc2025 = connections.count {
                        TagYearFlag.hasFlag(it.value.yearFlags, TagYearFlag.ANIME_NYC_2025_INFERRED)
                    }

                    val confirmed2024 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2024_CONFIRMED
                        )
                    }
                    val confirmed2025 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2025_CONFIRMED
                        )
                    }
                    val confirmedAnimeExpo2026 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_EXPO_2026_CONFIRMED
                        )
                    }
                    val confirmedAnimeNyc2024 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_NYC_2024_CONFIRMED
                        )
                    }
                    val confirmedAnimeNyc2025 = connections.count {
                        TagYearFlag.hasFlag(
                            it.value.yearFlags,
                            TagYearFlag.ANIME_NYC_2025_CONFIRMED
                        )
                    }

                    // TODO: Fully migrate series to UUID
                    SeriesEntry(
                        id = id,
                        uuid = uuid,
                        notes = notes,
                        aniListId = aniListId,
                        aniListType = aniListType,
                        wikipediaId = wikipediaId,
                        source = source,
                        // Fallback so that every field has a value so that it can be sorted
                        titlePreferred = titlePreferred ?: titleRomaji ?: titleEnglish ?: id,
                        titleEnglish = titleEnglish ?: titlePreferred ?: titleRomaji ?: id,
                        titleRomaji = titleRomaji ?: titlePreferred ?: titleEnglish ?: id,
                        titleNative = titleNative ?: titleRomaji ?: titlePreferred ?: titleEnglish
                        ?: id,
                        link = link,
                        synonyms = null,
                        inferred2024 = inferred2024.toLong(),
                        inferred2025 = inferred2025.toLong(),
                        inferredAnimeExpo2026 = inferredAnimeExpo2026.toLong(),
                        inferredAnimeNyc2024 = inferredAnimeNyc2024.toLong(),
                        inferredAnimeNyc2025 = inferredAnimeNyc2025.toLong(),
                        confirmed2024 = confirmed2024.toLong(),
                        confirmed2025 = confirmed2025.toLong(),
                        confirmedAnimeExpo2026 = confirmedAnimeExpo2026.toLong(),
                        confirmedAnimeNyc2024 = confirmedAnimeNyc2024.toLong(),
                        confirmedAnimeNyc2025 = confirmedAnimeNyc2025.toLong(),
                        counter = counter++,
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .onEach {
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertSeries)
                    }
                }
                .flatten()
                .toList()
        }
    }

    private fun parseMerch(
        database: BuildLogicDatabase,
        merchConnections: MutableMap<Pair<String, String>, ArtistMerchConnection>,
    ): List<MerchEntry> {
        val mutationQueries = database.mutationQueries
        val merchCsv = inputsDirectory.file(MERCH_CSV_NAME).get()
        if (!merchCsv.asFile.exists()) return emptyList()
        return open(merchCsv).use {
            read(it)
                .map {
                    // Merch, Notes
                    val name = it["Merch"]!!
                    val uuid = it["UUID"]!!
                    val notes = it["Notes"]
                    val categories = it["Categories"]

                    // TODO: Fully migrate merch to UUID
                    MerchEntry(
                        name = name,
                        uuid = uuid,
                        notes = notes,
                        categories = categories,
                        yearFlags = merchConnections.filter { it.value.merchId == name }
                            .values
                            .fold(0) { flags, connection -> flags or connection.yearFlags },
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .onEach {
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertMerch)
                    }
                }
                .flatten()
                .toList()
        }
    }

    private fun parseStampRallies2023(
        imageCacheDir: File,
        artists2023: List<ArtistEntry2023>,
        database: BuildLogicDatabase,
    ) {
        val mutationQueries = database.mutationQueries
        val stampRalliesCsv2023 = inputsDirectory.file("2023/$STAMP_RALLIES_CSV_NAME").get()
        if (!stampRalliesCsv2023.asFile.exists()) return
        val boothToArtist2023 = artists2023.associateBy { it.booth }
        open(stampRalliesCsv2023).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Theme,Image,Free?,Link,Tables
                    val theme = it["Theme"] ?: return@mapNotNull null
                    val links = it["Link"]!!.split("\n")
                        .filter(String::isNotBlank)

                    data class Table(
                        val booth: String,
                        val names: List<String>,
                    )

                    val tables = it["Tables"]!!.split("\n")
                        .filter(String::isNotBlank)
                        .map {
                            val parts = it.split(" ").map { it.trim() }
                                .filter { !it.startsWith("http") }
                            val booth = parts.find { it.length == 3 } ?: parts.last()
                            val names = if (booth.length == 3) {
                                parts.filter { it.length != 3 }.map { it.removePrefix("@") }
                            } else {
                                listOf(
                                    it.substringBefore("Exhibit Hall")
                                        .substringBefore("Annex")
                                        .trim()
                                )
                            }
                            Table(booth, names)
                        }

                    val hostTable = tables.first { it.booth.length == 3 }.booth

                    val stampRallyId = "2023-$hostTable-$theme"

                    // TODO: Shouldn't include artists that share the table
                    val connections = tables.mapNotNull { boothToArtist2023[it.booth] }
                        .map { StampRallyArtistConnection(stampRallyId, it.id) }

                    StampRallyEntry2023(
                        id = stampRallyId,
                        fandom = theme,
                        tables = tables.map {
                            "${it.booth} - ${it.names.joinToString(separator = ", ")}"
                        },
                        hostTable = hostTable,
                        links = links,
                        images = findRallyImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_EXPO_2023,
                            id = stampRallyId,
                            hostTable = hostTable,
                            fandom = theme,
                        ),
                        counter = counter++,
                    ) to connections
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    val stampRallies = it.map { it.first }
                    val artistConnections = it.flatMap { it.second }
                    mutationQueries.transaction {
                        stampRallies.forEach(mutationQueries::insertStampRally2023)
                        artistConnections.forEach(mutationQueries::insertArtistConnection)
                    }
                }
        }
    }

    private fun parseStampRallies2024(
        imageCacheDir: File,
        artists2024: List<ArtistEntry2024>,
        database: BuildLogicDatabase,
    ) {
        val mutationQueries = database.mutationQueries
        val stampRalliesCsv2024 = inputsDirectory.file("2024/$STAMP_RALLIES_CSV_NAME").get()
        if (!stampRalliesCsv2024.asFile.exists()) return
        val boothToArtist2024 = artists2024.associateBy { it.booth }

        open(stampRalliesCsv2024).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Theme,Link,Tables,Table Min, Total, Notes,Images
                    val theme = it["Theme"]!!
                    val links = it["Link"]!!.split("\n")
                        .filter(String::isNotBlank)
                    val tables = it["Tables"]!!.split("\n")
                        .filter(String::isNotBlank)
                    val hostTable = tables.firstOrNull { it.contains("-") }
                        ?.substringBefore("-")
                        ?.trim() ?: return@mapNotNull null
                    val tableMin = TableMin.parseFromSheet(it["Table Min"])
                    val total = it["Total"]?.removePrefix("$")?.toIntOrNull()
                    val totalCost = total ?: tableMin?.totalCost(tables.size)
                    val prizeLimit = it["Prize Limit"]!!.toIntOrNull()
                    val notes = it["Notes"]

                    val stampRallyId = "2024-$hostTable-$theme"
                    val connections = tables
                        .filter { it.contains("-") }
                        .map { it.substringBefore("-") }
                        .map(String::trim)
                        .filter { it.length == 3 }
                        .map { boothToArtist2024[it]!! }
                        .map { StampRallyArtistConnection(stampRallyId, it.id) }

                    StampRallyEntry2024(
                        id = stampRallyId,
                        fandom = theme,
                        tables = tables,
                        hostTable = hostTable,
                        links = links,
                        tableMin = tableMin?.serializedValue?.toLong(),
                        totalCost = totalCost?.toLong(),
                        prizeLimit = prizeLimit?.toLong(),
                        notes = notes,
                        images = findRallyImages(
                            imageCacheDir = imageCacheDir,
                            year = DataYear.ANIME_EXPO_2024,
                            id = stampRallyId,
                            hostTable = hostTable,
                            fandom = theme,
                        ),
                        counter = counter++,
                    ) to connections
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    val stampRallies = it.map { it.first }
                    val artistConnections = it.flatMap { it.second }
                    mutationQueries.transaction {
                        stampRallies.forEach(mutationQueries::insertStampRally2024)
                        artistConnections.forEach(mutationQueries::insertArtistConnection)
                    }
                }
        }
    }

    private fun parseStampRallies2025(
        imageCacheDir: File,
        artists2025: List<ArtistEntry2025>,
        database: BuildLogicDatabase,
    ) {
        val mutationQueries = database.mutationQueries
        val stampRalliesCsv2025 = inputsDirectory.file("2025/$STAMP_RALLIES_CSV_NAME").get()
        if (!stampRalliesCsv2025.asFile.exists()) return
        val boothToArtist2025 = artists2025.associateBy { it.booth }

        open(stampRalliesCsv2025).use {
            var counter = 1L
            read(it)
                .mapNotNull {
                    // Confirmed, Theme, Prize, Link, Tables, Table Min, Total, Prize Limit,
                    // Series, Notes, UUID, Images
                    val tables = it["Tables"]!!
                        .ifEmpty { return@mapNotNull null }
                        .split("\n")
                        .filter(String::isNotBlank)
                    val confirmed = it["Confirmed"] == "TRUE"
                    val stampRallyId = it["UUID"]!!
                    val theme = it["Theme"]!!.ifEmpty { "Unknown" }
                    val prize = it["Prize"]?.ifBlank { null }
                    val links = it["Link"]!!.split("\n")
                        .filter(String::isNotBlank)
                    val hostTable = tables.firstOrNull()?.trim() ?: return@mapNotNull null
                    val tableMin = TableMin.parseFromSheet(it["Table Min"])
                    val total = it["Total"]?.removePrefix("$")?.toIntOrNull()
                    val totalCost = total ?: tableMin?.totalCost(tables.size)
                    val prizeLimit = it["Prize Limit"]!!.toIntOrNull()
                    val series = it["Series"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                        .sorted()
                    val notes = it["Notes"]

                    val artistConnections = tables.map(String::trim)
                        .filter { it.length == 3 }
                        .map { boothToArtist2025[it]!! }
                        .map { StampRallyArtistConnection(stampRallyId, it.id) }

                    val seriesConnections = series.map(String::trim)
                        .map { StampRallySeriesConnection(stampRallyId, it) }

                    Triple(
                        StampRallyEntry2025(
                            id = stampRallyId,
                            fandom = theme,
                            tables = tables,
                            hostTable = hostTable,
                            links = links,
                            tableMin = tableMin?.serializedValue?.toLong(),
                            totalCost = totalCost?.toLong(),
                            prize = prize,
                            prizeLimit = prizeLimit?.toLong(),
                            series = series,
                            notes = notes,
                            images = findRallyImages(
                                imageCacheDir = imageCacheDir,
                                year = DataYear.ANIME_EXPO_2025,
                                id = stampRallyId,
                                hostTable = hostTable,
                                fandom = theme,
                            ),
                            counter = counter++,
                            confirmed = confirmed,
                        ), artistConnections, seriesConnections
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    val stampRallies = it.map { it.first }
                    val artistConnections = it.flatMap { it.second }
                    val seriesConnections = it.flatMap { it.third }
                    mutationQueries.transaction {
                        stampRallies.forEach(mutationQueries::insertStampRally2025)
                        artistConnections.forEach(mutationQueries::insertArtistConnection)
                        seriesConnections.forEach(mutationQueries::insertStampRallySeriesConnection)
                    }
                }
        }
    }

    private fun open(file: RegularFile) = file.asFile.inputStream().asSource().buffered()

    private fun read(source: Source): Sequence<Map<String, String>> = CSVFormat.RFC4180.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .get()
        .parse(source.asInputStream().reader())
        .asSequence()
        .map { it.toMap() }

    fun MutableMap<Pair<String, String>, ArtistSeriesConnection>.addSeriesConnection(
        seriesConnection: ArtistSeriesConnection,
    ) {
        val idPair = seriesConnection.let { it.artistId to it.seriesId }
        val existing = this[idPair]
        if (existing == null) {
            this[idPair] = seriesConnection
        } else {
            this[idPair] = existing.copy(
                yearFlags = existing.yearFlags or seriesConnection.yearFlags
            )
        }
    }

    fun MutableMap<Pair<String, String>, ArtistMerchConnection>.addMerchConnection(merchConnection: ArtistMerchConnection) {
        val idPair = merchConnection.let { it.artistId to it.merchId }
        val existing = this[idPair]
        if (existing == null) {
            this[idPair] = merchConnection
        } else {
            this[idPair] = existing.copy(
                yearFlags = existing.yearFlags or merchConnection.yearFlags
            )
        }
    }
}
