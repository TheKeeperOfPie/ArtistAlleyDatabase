
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2025
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistSeriesConnection
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
import com.thekeeperofpie.artistalleydatabase.buildlogic.MutationQueries
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Instant

@CacheableTask
abstract class ArtistAlleyDatabaseTask : DefaultTask() {

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
            driver.close()

            listOf("artists", "stampRallies", "tags")
                .flatMap { inputsDirectory.dir(it).get().asFile.listFiles().toList() }
                .forEach { readSqlFile(databaseFile, it) }

            val pair = createDatabase(databaseFile)
            driver = pair.first
            database = pair.second

            runBlocking {
                fixLegacyArtistImages(database, imageCacheDir)

                database.mutationQueries.getAllArtistEntryAnimeExpo2026()
                    .executeAsList()
                    .forEachIndexed { index, artist ->
                        val images =
                            findArtistImages(imageCacheDir, DataYear.ANIME_EXPO_2026, artist.id)
                        val updatedImages = artist.images.map { original ->
                            images.first { it.name.contains(original.name) }
                        }

                        val inference = ArtistInferenceProvider(database, artist.id)
                        val socialLinks = artist.socialLinks.ifEmpty { inference.socialLinks }
                        val storeLinks = artist.storeLinks.ifEmpty { inference.storeLinks }
                        val seriesInferred = artist.seriesInferred.ifEmpty { inference.seriesInferred }
                        val merchInferred = artist.merchInferred.ifEmpty { inference.merchInferred }

                        val (linkFlags, linkFlags2) = Link.parseFlags(
                            socialLinks = socialLinks,
                            storeLinks = storeLinks,
                            portfolioLinks = artist.portfolioLinks,
                            catalogLinks = artist.catalogLinks,
                        )
                        val commissionFlags = CommissionType.parseFlags(artist.commissions)

                        database.mutationQueries.updateArtistEntryAnimeExpo2026(
                            artist.copy(
                                socialLinks = socialLinks,
                                storeLinks = storeLinks,
                                seriesInferred = seriesInferred,
                                merchInferred = merchInferred,
                                linkFlags = linkFlags,
                                linkFlags2 = linkFlags2,
                                commissionFlags = commissionFlags,
                                images = updatedImages,
                                counter = index.toLong() + 1000L,
                            )
                        ).await()
                    }

                buildStampRallyConnections(database)

                val artistTagConnections = buildArtistConnections(database)
                updateSeriesInferredConfirmedCounts(database, artistTagConnections)
                updateMerchYearFlags(database, artistTagConnections)

                val (seriesConnections, merchConnections) = artistTagConnections
                val mutationQueries = database.mutationQueries
                mutationQueries.transaction {
                    seriesConnections.values.forEach(mutationQueries::insertSeriesConnection)
                    merchConnections.values.forEach(mutationQueries::insertMerchConnection)
                }

                val allEnteredSeriesIds = seriesConnections.map { it.value.seriesId }.toSet()
                val allValidSeriesIds =
                    database.seriesQueries.getSeries().executeAsList().map { it.id }.toSet()
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
                val allValidMerchIds =
                    database.merchQueries.getMerch().executeAsList().map { it.name }.toSet()
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
                driver.execute(null, "PRAGMA optimize;", 0, null).await()
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

    private fun <T : Any> fixLegacyArtistImages(
        database: BuildLogicDatabase,
        imageCacheDir: File,
        dataYear: DataYear,
        entries: Query<T>,
        artistId: (T) -> String,
        updateImages: MutationQueries.(List<CatalogImage>, id: String) -> Unit,
    ) {
        entries.executeAsList()
            .forEach {
                val id = artistId(it)
                val images = findArtistImages(imageCacheDir, dataYear, id)
                database.mutationQueries.updateImages(images, id)
            }
    }

    private fun <T : Any> fixLegacyRallyImages(
        database: BuildLogicDatabase,
        imageCacheDir: File,
        dataYear: DataYear,
        entries: Query<T>,
        rallyId: (T) -> String,
        hostTable: (T) -> String,
        fandom: (T) -> String,
        updateImages: MutationQueries.(List<CatalogImage>, id: String) -> Unit,
    ) {
        entries.executeAsList()
            .forEach {
                val id = rallyId(it)
                val images = findRallyImages(
                    imageCacheDir = imageCacheDir,
                    year = dataYear,
                    id = id,
                    hostTable = hostTable(it),
                    fandom = fandom(it),
                )
                database.mutationQueries.updateImages(images, id)
            }
    }

    private fun fixLegacyArtistImages(database: BuildLogicDatabase, imageCacheDir: File) {
        fixLegacyArtistImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2023,
            entries = database.artistEntry2023Queries.getAllEntries(),
            artistId = { it.id },
            updateImages = MutationQueries::updateArtistEntryAnimeExpo2023Images,
        )

        fixLegacyArtistImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2024,
            entries = database.artistEntry2024Queries.getAllEntries(),
            artistId = { it.id },
            updateImages = MutationQueries::updateArtistEntryAnimeExpo2024Images,
        )

        fixLegacyArtistImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2025,
            entries = database.artistEntry2025Queries.getAllEntries(),
            artistId = { it.id },
            updateImages = MutationQueries::updateArtistEntryAnimeExpo2025Images,
        )

        fixLegacyArtistImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_NYC_2024,
            entries = database.artistEntryAnimeNyc2024Queries.getAllEntries(),
            artistId = { it.id },
            updateImages = MutationQueries::updateArtistEntryAnimeNyc2024Images,
        )

        fixLegacyArtistImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_NYC_2025,
            entries = database.artistEntryAnimeNyc2025Queries.getAllEntries(),
            artistId = { it.id },
            updateImages = MutationQueries::updateArtistEntryAnimeNyc2025Images,
        )

        fixLegacyRallyImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2023,
            entries = database.stampRallyEntry2023Queries.getAllEntries(),
            rallyId = { it.id },
            hostTable = { it.hostTable },
            fandom = { it.fandom },
            updateImages = MutationQueries::updateStampRallyAnimeExpo2023Images,
        )

        fixLegacyRallyImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2024,
            entries = database.stampRallyEntry2024Queries.getAllEntries(),
            rallyId = { it.id },
            hostTable = { it.hostTable },
            fandom = { it.fandom },
            updateImages = MutationQueries::updateStampRallyAnimeExpo2024Images,
        )

        fixLegacyRallyImages(
            database = database,
            imageCacheDir = imageCacheDir,
            dataYear = DataYear.ANIME_EXPO_2025,
            entries = database.stampRallyEntry2025Queries.getAllEntries(),
            rallyId = { it.id },
            hostTable = { it.hostTable },
            fandom = { it.fandom },
            updateImages = MutationQueries::updateStampRallyAnimeExpo2025Images,
        )
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
                socialLinksAdapter = listStringAdapter,
                storeLinksAdapter = listStringAdapter,
                portfolioLinksAdapter = listStringAdapter,
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

    private fun readSqlFile(databaseFile: File, sqlFile: File) {
        if (!sqlFile.exists()) return

        val process = ProcessBuilder(
            "sqlite3",
            databaseFile.absolutePath,
            "\".read \'${sqlFile.absolutePath}\'\""
        )
            .inheritIO()
            .redirectErrorStream(true).start()
        val success = process.waitFor(30, TimeUnit.SECONDS)
        if (!success) {
            val errorText = process.inputStream.use {
                it.reader().use {
                    it.readText()
                }
            }
            logger.error("Failed to apply ${sqlFile.absolutePath}")
            errorText.lines().forEach(logger::error)
        }
    }

    private data class ArtistTagConnections(
        val series: MutableMap<Pair<String, String>, ArtistSeriesConnection> = mutableMapOf(),
        val merch: MutableMap<Pair<String, String>, ArtistMerchConnection> = mutableMapOf(),
    ) {
        fun addConnection(connection: ArtistSeriesConnection) {
            val idPair = connection.let { it.artistId to it.seriesId }
            val existing = series[idPair]
            if (existing == null) {
                series[idPair] = connection
            } else {
                series[idPair] = existing.copy(
                    yearFlags = existing.yearFlags or connection.yearFlags
                )
            }
        }

        fun addConnection(connection: ArtistMerchConnection) {
            val idPair = connection.let { it.artistId to it.merchId }
            val existing = merch[idPair]
            if (existing == null) {
                merch[idPair] = connection
            } else {
                merch[idPair] = existing.copy(
                    yearFlags = existing.yearFlags or connection.yearFlags
                )
            }
        }
    }

    private fun buildArtistConnections(database: BuildLogicDatabase): ArtistTagConnections {
        val connections = ArtistTagConnections()

        // ANIME_EXPO_2023 skipped because it didn't have tags

        database.artistEntry2024Queries.getAllEntries().executeAsList().forEach {
            connections.addArtistConnections(
                artistId = it.id,
                dataYear = DataYear.ANIME_EXPO_2024,
                seriesInferred = it.seriesInferred,
                seriesConfirmed = it.seriesConfirmed,
                merchInferred = it.merchInferred,
                merchConfirmed = it.merchConfirmed,
            )
        }

        database.artistEntry2025Queries.getAllEntries().executeAsList().forEach {
            connections.addArtistConnections(
                artistId = it.id,
                dataYear = DataYear.ANIME_EXPO_2025,
                seriesInferred = it.seriesInferred,
                seriesConfirmed = it.seriesConfirmed,
                merchInferred = it.merchInferred,
                merchConfirmed = it.merchConfirmed,
            )
        }

        database.artistEntryAnimeExpo2026Queries.getAllEntries().executeAsList().forEach {
            connections.addArtistConnections(
                artistId = it.id,
                dataYear = DataYear.ANIME_EXPO_2026,
                seriesInferred = it.seriesInferred,
                seriesConfirmed = it.seriesConfirmed,
                merchInferred = it.merchInferred,
                merchConfirmed = it.merchConfirmed,
            )
        }

        database.artistEntryAnimeNyc2024Queries.getAllEntries().executeAsList().forEach {
            connections.addArtistConnections(
                artistId = it.id,
                dataYear = DataYear.ANIME_NYC_2024,
                seriesInferred = it.seriesInferred,
                seriesConfirmed = it.seriesConfirmed,
                merchInferred = it.merchInferred,
                merchConfirmed = it.merchConfirmed,
            )
        }

        database.artistEntryAnimeNyc2025Queries.getAllEntries().executeAsList().forEach {
            connections.addArtistConnections(
                artistId = it.id,
                dataYear = DataYear.ANIME_NYC_2025,
                seriesInferred = it.seriesInferred,
                seriesConfirmed = it.seriesConfirmed,
                merchInferred = it.merchInferred,
                merchConfirmed = it.merchConfirmed,
            )
        }

        return connections
    }

    private fun ArtistTagConnections.addArtistConnections(
        artistId: String,
        dataYear: DataYear,
        seriesInferred: List<String>,
        seriesConfirmed: List<String>,
        merchInferred: List<String>,
        merchConfirmed: List<String>,
    ) {
        val (inferredFlag, confirmedFlag) = when (dataYear) {
            DataYear.ANIME_EXPO_2023 -> 0L to 0L
            DataYear.ANIME_EXPO_2024 -> TagYearFlag.getFlags(hasAnimeExpo2024Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeExpo2024Confirmed = true)
            DataYear.ANIME_EXPO_2025 -> TagYearFlag.getFlags(hasAnimeExpo2025Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeExpo2025Confirmed = true)
            DataYear.ANIME_EXPO_2026 -> TagYearFlag.getFlags(hasAnimeExpo2026Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeExpo2026Confirmed = true)
            DataYear.ANIME_NYC_2024 -> TagYearFlag.getFlags(hasAnimeNyc2024Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeNyc2024Confirmed = true)
            DataYear.ANIME_NYC_2025 -> TagYearFlag.getFlags(hasAnimeNyc2025Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeNyc2025Confirmed = true)
        }
        seriesInferred.forEach {
            addConnection(
                ArtistSeriesConnection(
                    artistId = artistId,
                    seriesId = it,
                    yearFlags = inferredFlag
                )
            )
        }
        seriesConfirmed.forEach {
            addConnection(
                ArtistSeriesConnection(
                    artistId = artistId,
                    seriesId = it,
                    yearFlags = confirmedFlag,
                )
            )
        }

        merchInferred.forEach {
            addConnection(
                ArtistMerchConnection(artistId = artistId, merchId = it, yearFlags = inferredFlag)
            )
        }
        merchConfirmed.forEach {
            addConnection(
                ArtistMerchConnection(artistId = artistId, merchId = it, yearFlags = confirmedFlag)
            )
        }
    }

    private fun buildStampRallyConnections(database: BuildLogicDatabase) {
        database.stampRallyEntry2023Queries.getAllEntries().executeAsList().forEach {
            val stampRallyId = it.id
            it.tables
                .mapNotNull {
                    database.artistEntry2023Queries.getEntriesByBooth(it).executeAsOneOrNull()
                }
                .map { StampRallyArtistConnection(stampRallyId = stampRallyId, artistId = it.id) }
                .forEach(database.mutationQueries::insertArtistConnection)
        }
        database.stampRallyEntry2024Queries.getAllEntries().executeAsList().forEach {
            val stampRallyId = it.id
            it.tables
                .mapNotNull {
                    database.artistEntry2024Queries.getEntriesByBooth(it).executeAsOneOrNull()
                }
                .map { StampRallyArtistConnection(stampRallyId = stampRallyId, artistId = it.id) }
                .forEach(database.mutationQueries::insertArtistConnection)
        }
        database.stampRallyEntry2025Queries.getAllEntries().executeAsList().forEach {
            val stampRallyId = it.id
            it.tables
                .mapNotNull {
                    database.artistEntry2025Queries.getEntriesByBooth(it).executeAsOneOrNull()
                }
                .map { StampRallyArtistConnection(stampRallyId = stampRallyId, artistId = it.id) }
                .forEach(database.mutationQueries::insertArtistConnection)
            it.series
                .map { StampRallySeriesConnection(stampRallyId = stampRallyId, seriesId = it) }
                .forEach(database.mutationQueries::insertStampRallySeriesConnection)
        }
    }

    private fun updateSeriesInferredConfirmedCounts(
        database: BuildLogicDatabase,
        artistTagConnections: ArtistTagConnections,
    ) {
        database.seriesQueries.getSeries().executeAsList().forEach {
            val seriesId = it.id
            val connections = artistTagConnections.series.filter { it.value.seriesId == seriesId }

            val inferredAnimeExpo2024 = connections.count {
                TagYearFlag.hasFlag(
                    it.value.yearFlags,
                    TagYearFlag.ANIME_EXPO_2024_INFERRED
                )
            }
            val inferredAnimeExpo2025 = connections.count {
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

            val confirmedAnimeExpo2024 = connections.count {
                TagYearFlag.hasFlag(
                    it.value.yearFlags,
                    TagYearFlag.ANIME_EXPO_2024_CONFIRMED
                )
            }
            val confirmedAnimeExpo2025 = connections.count {
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

            database.mutationQueries.updateSeriesTagCounts(
                inferredAnimeExpo2024 = inferredAnimeExpo2024.toLong(),
                inferredAnimeExpo2025 = inferredAnimeExpo2025.toLong(),
                inferredAnimeExpo2026 = inferredAnimeExpo2026.toLong(),
                inferredAnimeNyc2024 = inferredAnimeNyc2024.toLong(),
                inferredAnimeNyc2025 = inferredAnimeNyc2025.toLong(),
                confirmedAnimeExpo2024 = confirmedAnimeExpo2024.toLong(),
                confirmedAnimeExpo2025 = confirmedAnimeExpo2025.toLong(),
                confirmedAnimeExpo2026 = confirmedAnimeExpo2026.toLong(),
                confirmedAnimeNyc2024 = confirmedAnimeNyc2024.toLong(),
                confirmedAnimeNyc2025 = confirmedAnimeNyc2025.toLong(),
                id = seriesId,
            )
        }
    }

    private fun updateMerchYearFlags(
        database: BuildLogicDatabase,
        artistTagConnections: ArtistTagConnections,
    ) {
        database.merchQueries.getMerch().executeAsList().forEach {
            val merchId = it.name
            val yearFlags = artistTagConnections.merch.filterValues { it.merchId == merchId }
                .values
                .fold(0L) { flags, connection -> flags or connection.yearFlags }
            database.mutationQueries.updateMerchYearFlags(yearFlags, merchId)
        }
    }

    // Copied from ArtistInference since it isn't accessible here
    private class ArtistInferenceProvider(database: BuildLogicDatabase, artistId: String) {
        private val animeExpo2023 by lazy {
            database.artistEntry2023Queries.getEntry(artistId).executeAsOneOrNull()
        }
        private val animeExpo2024 by lazy {
            database.artistEntry2024Queries.getEntry(artistId).executeAsOneOrNull()
        }
        private val animeExpo2025 by lazy {
            database.artistEntry2025Queries.getEntry(artistId).executeAsOneOrNull()
        }
        private val animeNyc2024 by lazy {
            database.artistEntryAnimeNyc2024Queries.getEntry(artistId).executeAsOneOrNull()
        }
        private val animeNyc2025 by lazy {
            database.artistEntryAnimeNyc2025Queries.getEntry(artistId).executeAsOneOrNull()
        }

        private fun List<String>?.ifNullOrEmpty(defaultValue: () -> List<String>?) =
            if (this.isNullOrEmpty()) defaultValue() else this

        private fun cascadeAll(
            valueAnimeExpo2023: com.thekeeperofpie.artistalleydatabase.alley.artistEntry2023.GetEntry.() -> List<String>,
            valueAnimeExpo2024: com.thekeeperofpie.artistalleydatabase.alley.artistEntry2024.GetEntry.() -> List<String>,
            valueAnimeExpo2025: com.thekeeperofpie.artistalleydatabase.alley.artistEntry2025.GetEntry.() -> List<String>,
            valueAnimeNyc2024: com.thekeeperofpie.artistalleydatabase.alley.artistEntryAnimeNyc2024.GetEntry.() -> List<String>,
            valueAnimeNyc2025: com.thekeeperofpie.artistalleydatabase.alley.artistEntryAnimeNyc2025.GetEntry.() -> List<String>,
        ): List<String> = animeExpo2025?.valueAnimeExpo2025()
            .ifNullOrEmpty { animeExpo2024?.valueAnimeExpo2024() }
            .ifNullOrEmpty { animeNyc2025?.valueAnimeNyc2025() }
            .ifNullOrEmpty { animeNyc2024?.valueAnimeNyc2024() }
            .ifNullOrEmpty { animeExpo2023?.valueAnimeExpo2023() }
            .orEmpty()

        val socialLinks
            get() = cascadeAll(
                { links },
                { links },
                { links },
                { links },
                { links },
            )

        val storeLinks
            get() = cascadeAll(
                { emptyList() },
                { storeLinks },
                { storeLinks },
                { storeLinks },
                { storeLinks },
            )

        val seriesInferred
            get() = cascadeAll(
                { emptyList() },
                { seriesConfirmed.ifEmpty { seriesInferred } },
                { seriesConfirmed.ifEmpty { seriesInferred } },
                { seriesConfirmed.ifEmpty { seriesInferred } },
                { seriesConfirmed.ifEmpty { seriesInferred } },
            )

        val merchInferred
            get() = cascadeAll(
                { emptyList() },
                { merchConfirmed.ifEmpty { merchInferred } },
                { merchConfirmed.ifEmpty { merchInferred } },
                { merchConfirmed.ifEmpty { merchInferred } },
                { merchConfirmed.ifEmpty { merchInferred } },
            )
    }
}
