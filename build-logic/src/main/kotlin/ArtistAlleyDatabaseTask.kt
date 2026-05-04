
import ImageUtils.parseScaledImageWidthHeight
import Utils.createEditDatabase
import app.cash.sqldelight.Query
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallySeriesConnection
import com.thekeeperofpie.artistalleydatabase.build_logic.edit.BuildLogicEditDatabase
import com.thekeeperofpie.artistalleydatabase.buildlogic.edit.MutationQueries
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.io.copyTo
import kotlin.io.inputStream
import kotlin.io.nameWithoutExtension
import kotlin.io.resolve
import kotlin.io.walkTopDown
import kotlin.io.writeText
import kotlin.time.Instant
import kotlin.use
import kotlin.uuid.Uuid

@CacheableTask
abstract class ArtistAlleyDatabaseTask : DefaultTask() {

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputsDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImagesAnimeExpo2023: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImagesAnimeExpo2024: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImagesAnimeExpo2025: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImagesAnimeExpo2026: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputImagesAnimeNyc2025: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputEmbeds: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputChangelog: RegularFileProperty

    @get:OutputFile
    abstract val outputDatabaseFile: RegularFileProperty

    @get:OutputFile
    abstract val outputDatabaseHashFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputMetadata: DirectoryProperty

    @get:OutputDirectory
    abstract val outputEmbedImages: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesAnimeExpo2026: DirectoryProperty

    init {
        val projectDirectory = layout.projectDirectory
        inputsDirectory.convention(projectDirectory.dir("inputs"))
        inputImagesAnimeExpo2026.convention(projectDirectory.dir("images/AX2026"))

        val buildDirectory = layout.buildDirectory
        outputDatabaseFile.convention(buildDirectory.file("generated/composeResources/files/database.sqlite"))
        outputDatabaseHashFile.convention(buildDirectory.file("generated/composeResources/files/databaseHash.txt"))
        outputMetadata.convention(buildDirectory.dir("generated/alley-metadata"))
        outputImagesAnimeExpo2026.convention(buildDirectory.dir("generated/composeResources/files/images/AX2026"))
        outputEmbedImages.convention(buildDirectory.dir("generated/composeResources/files/embeds"))
    }

    @TaskAction
    fun process() {
        if (!inputsDirectory.get().asFile.exists()) return
        val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
        val databaseFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        if (databaseFile.exists() && !databaseFile.delete()) {
            throw IllegalStateException(
                "Failed to delete $databaseFile, manually delete to re-process inputs"
            )
        } else {
            var (driver, database) = createEditDatabase(databaseFile)
            driver.close()

            listOf("artists", "stampRallies")
                .flatMap { inputsDirectory.dir(it).get().asFile.listFiles().toList() }
                .forEach { Utils.readSqlFile(databaseFile, it) }

            // tags.sql must come last in order to overwrite legacy data
            listOf("merchLegacy.sql", "seriesLegacy.sql", "tags.sql").forEach {
                val tagFile = inputsDirectory.dir("tags/$it").get().asFile
                if (tagFile.exists()) {
                    Utils.readSqlFile(databaseFile, tagFile)
                }
            }

            val pair = createEditDatabase(databaseFile)
            driver = pair.first
            database = pair.second

            runBlocking {
                @Suppress("NewApi")
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
                    .use {
                        withContext(it.asCoroutineDispatcher()) {
                            trackStage("VerifySeries") { verifySeries(database) }

                            val artistChangelog =
                                trackStage("ArtistChangelog") { addArtistChangelog(database) }

                            trackStage("LegacyArtistImages") {
                                fixLegacyArtistImages(
                                    database,
                                    imageCacheDir
                                )
                            }
                            trackStage("LegacyLinkFlags") { finalizeLegacyLinkFlags(database) }

                            val embedCache = trackStage("LoadEmbedCache") {
                                EmbedCache(
                                    logger = logger,
                                    inputFolder = inputEmbeds.get().asFile,
                                    outputJsonFile = outputMetadata.get().asFile,
                                    workingImagesFolder = outputMetadata.dir("embedImages")
                                        .get().asFile.apply { mkdir() },
                                )
                            }
                            trackStage("FinalizeAnimeExpo2026") {
                                finalizeAnimeExpo2026(
                                    database = database,
                                    imageCacheDir = imageCacheDir,
                                    embedCache = embedCache,
                                    artistLastEditTimes =
                                        artistChangelog?.lastEditTimes[DataYear.ANIME_EXPO_2026].orEmpty()
                                )
                            }
                            trackStage("NewArtists") { calculateNewArtists(database) }

                            trackStage("FinalizeCache") {
                                embedCache.finalizeCache(
                                    scope = this,
                                    imageCacheDir = imageCacheDir,
                                    embedImagesOutputFolder = outputEmbedImages.get().asFile,
                                )
                            }

                            val mutationQueries = database.mutationQueries

                            trackStage("ArtistFallbackYears") {
                                calculateArtistFallbackYears(
                                    mutationQueries
                                )
                            }

                            trackStage("StampRallyConnections") {
                                buildStampRallyConnections(
                                    database
                                )
                            }

                            val artistTagConnections = trackStage("ArtistConnections") {
                                buildArtistConnections(database)
                            }
                            trackStage("SeriesInferredConfirmedCounts") {
                                updateSeriesInferredConfirmedCounts(database, artistTagConnections)
                            }
                            trackStage("MerchYearFlags") {
                                updateMerchYearFlags(database, artistTagConnections)
                            }

                            val (seriesConnections, merchConnections) = artistTagConnections
                            trackStage("InsertTagConnections") {
                                mutationQueries.transaction {
                                    seriesConnections.values.forEach(mutationQueries::insertSeriesConnection)
                                    merchConnections.values.forEach(mutationQueries::insertMerchConnection)
                                }
                            }

                            var shouldFail = false

                            fun logTagError(tagId: String, error: String) {
                                // Split in future years and there is no valid fallback default
                                if (tagId != "Honkai") {
                                    shouldFail = true
                                    logger.error(error)
                                }
                            }

                            trackStage("CheckSeriesIds") {
                                val allEnteredSeriesIds =
                                    seriesConnections.map { it.value.seriesId }.toSet()
                                val allValidSeriesIds =
                                    database.seriesQueries.getSeries().executeAsList().map { it.id }
                                        .toSet()
                                val seriesDiff = allEnteredSeriesIds - allValidSeriesIds
                                if (seriesDiff.isNotEmpty()) {
                                    seriesDiff.forEach { badSeries ->
                                        logTagError(
                                            badSeries,
                                            "Entered series does not match valid series: $badSeries"
                                        )
                                        val brokenArtists = seriesConnections
                                            .filter { it.value.seriesId == badSeries }
                                            .map { it.value.artistId }
                                        logTagError(badSeries, "Broken artists: $brokenArtists")
                                    }
                                }
                                val seriesWithExtraSpaces =
                                    allValidSeriesIds.filter { it.endsWith(" ") }
                                if (seriesWithExtraSpaces.isNotEmpty()) {
                                    logger.error("Series with extra spaces: $seriesWithExtraSpaces")
                                }
                            }

                            trackStage("CheckMerchIds") {
                                val allEnteredMerchIds =
                                    merchConnections.map { it.value.merchId }.toSet()
                                val allValidMerchIds =
                                    database.merchQueries.getMerch().executeAsList().map { it.name }
                                        .toSet()
                                val merchDiff = allEnteredMerchIds - allValidMerchIds
                                if (merchDiff.isNotEmpty()) {
                                    merchDiff.forEach { badMerch ->
                                        logTagError(
                                            badMerch,
                                            "Entered merch does not match valid merch: $badMerch"
                                        )
                                        val brokenArtists = merchConnections
                                            .filter { it.value.merchId == badMerch }
                                            .map { it.value.artistId }
                                        logTagError(badMerch, "Broken artists: $brokenArtists")
                                    }
                                }
                                val merchWithExtraSpaces =
                                    allValidMerchIds.filter { it.endsWith(" ") }
                                if (merchWithExtraSpaces.isNotEmpty()) {
                                    logger.error("Merch with extra spaces: $merchWithExtraSpaces")
                                }
                            }

                            if (shouldFail) {
                                throw IllegalStateException("Broken tags must be resolved")
                            }

                            trackStage("CleanUpForRelease") {
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
                                    driver.execute(
                                        null,
                                        "INSERT INTO $it($it) VALUES('rebuild');",
                                        0,
                                        null
                                    )
                                        .await()
                                    driver.execute(
                                        null,
                                        "INSERT INTO $it($it) VALUES('optimize');",
                                        0,
                                        null
                                    )
                                        .await()
                                }

                                driver.execute(null, "VACUUM;", 0, null).await()
                                driver.execute(null, "PRAGMA optimize;", 0, null).await()
                            }
                        }
                    }
            }

            driver.close()

            databaseFile.copyTo(outputDatabaseFile.get().asFile, overwrite = true)
            val hash = Utils.hash(databaseFile)

            databaseFile.delete()

            outputDatabaseHashFile.get().asFile.writeText(hash.toString())
        }
    }

    private fun verifySeries(database: BuildLogicEditDatabase) {
        val series = database.mutationQueries.getSeries().executeAsList()
        val brokenSeries = series
            .filter {
                listOf(it.titleRomaji, it.titleNative, it.titleEnglish, it.titlePreferred)
                    .any { it.isBlank() }
            }
            .map { it.id }
        if (brokenSeries.isNotEmpty()) {
            logger.error("Broken series missing titles: $brokenSeries")
            throw IllegalStateException("Broken series missing titles")
        }
    }

    private suspend fun finalizeLegacyLinkFlags(database: BuildLogicEditDatabase) {
        database.mutationQueries.getAllArtistEntryAnimeExpo2025()
            .executeAsList()
            .forEach { artist ->
                val (linkFlags, linkFlags2) = Link.parseFlags(
                    socialLinks = artist.links,
                    storeLinks = artist.storeLinks,
                    portfolioLinks = emptyList(),
                    catalogLinks = artist.catalogLinks,
                )

                database.mutationQueries.updateArtistEntryAnimeExpo2025(
                    artist.copy(
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                    )
                ).await()
            }
        database.mutationQueries.getAllArtistEntryAnimeNyc2024()
            .executeAsList()
            .forEach { artist ->
                val (linkFlags, linkFlags2) = Link.parseFlags(
                    socialLinks = artist.links,
                    storeLinks = artist.storeLinks,
                    portfolioLinks = emptyList(),
                    catalogLinks = artist.catalogLinks,
                )

                database.mutationQueries.updateArtistEntryAnimeNyc2024(
                    artist.copy(
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                    )
                ).await()
            }
        database.mutationQueries.getAllArtistEntryAnimeNyc2025()
            .executeAsList()
            .forEach { artist ->
                val (linkFlags, linkFlags2) = Link.parseFlags(
                    socialLinks = artist.links,
                    storeLinks = artist.storeLinks,
                    portfolioLinks = emptyList(),
                    catalogLinks = artist.catalogLinks,
                )

                database.mutationQueries.updateArtistEntryAnimeNyc2025(
                    artist.copy(
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                    )
                ).await()
            }
    }

    private suspend fun finalizeAnimeExpo2026(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
        embedCache: EmbedCache,
        artistLastEditTimes: Map<Uuid, Instant>,
    ) = coroutineScope {
        val verifiedArtistIds =
            inputsDirectory.dir("snapshots/animeExpo2026/form").get().asFile.listFiles()
                .maxByOrNull {
                    Instant.parse(
                        it.nameWithoutExtension
                            .replace("_", ":")
                            .replace(";", ":")
                    )
                }
                ?.let { snapshotFile ->
                    val file = temporaryDir.resolve("animeExpo2026Form.sqlite")
                    // First, create and immediately close the databases to initialize the schemas
                    Utils.createFormDatabase(file).first.close()

                    if (!Utils.readSqlFile(file, snapshotFile)) {
                        logger.error("Failed to apply before ${snapshotFile.absolutePath}")
                        return@let emptyList()
                    }
                    val (driver, database) = Utils.createFormDatabase(file)
                    driver.use {
                        database.verifiedQueries.getVerifiedArtistIds().executeAsList()
                    }.also {
                        file.delete()
                    }
                }
                .orEmpty()

        val mutationQueries = database.mutationQueries
        val artistUpdates = mutationQueries.getAllArtistEntryAnimeExpo2026()
            .executeAsList()
            .map { artist ->
                async {
                    val inference = ArtistInferenceProvider(database, artist.id)
                    val socialLinks = artist.socialLinks.ifEmpty { inference.socialLinks }
                    val storeLinks = artist.storeLinks.ifEmpty { inference.storeLinks }
                    val seriesInferred =
                        artist.seriesInferred.ifEmpty { inference.seriesInferred }
                    val merchInferred = artist.merchInferred.ifEmpty { inference.merchInferred }

                    val (linkFlags, linkFlags2) = Link.parseFlags(
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = artist.portfolioLinks,
                        catalogLinks = artist.catalogLinks,
                    )
                    val commissionFlags = CommissionType.parseFlags(artist.commissions)

                    val artistId = Uuid.parse(artist.id)

                    // Don't expose raw edit times from backend, just mirror the changelog dates
                    val lastEditTime = artistLastEditTimes[artistId]
                        ?.toLocalDateTime(TimeZone.UTC)
                        ?.date
                        ?.atStartOfDayIn(TimeZone.UTC)
                    val embedLinks = (artist.portfolioLinks + socialLinks + storeLinks +
                            artist.commissions.filter { it.startsWith("http") })
                    val artistImages = calculateArtistImages(
                        embedCache = embedCache,
                        artistId = artist.id,
                        year = DataYear.ANIME_EXPO_2026,
                        isFinalCatalog = artist.catalogLinks.isNotEmpty() ||
                                artist.seriesConfirmed.isNotEmpty() ||
                                artist.merchConfirmed.isNotEmpty(),
                        images = artist.images,
                        embedLinks = embedLinks,
                    )
                    artist.copy(
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        seriesInferred = seriesInferred,
                        merchInferred = merchInferred,
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        commissionFlags = commissionFlags,
                        images = artistImages.catalogImages,
                        tempImages = artistImages.tempImages,
                        embeds = artistImages.embeds,
                        lastEditTime = lastEditTime,
                        verifiedArtist = verifiedArtistIds.contains(artistId),
                    ) to artistImages.imagesToCompress
                }
            }
            .awaitAll()

        val allRetainedImages = artistUpdates.flatMap { it.second }
            .map {
                async {
                    val output =
                        outputImagesAnimeExpo2026.get().asFile.resolve(it.path)
                    val (width, height, resized) = parseScaledImageWidthHeight(
                        logger = logger,
                        imageCacheDir = imageCacheDir,
                        file = it.imageFile,
                    )
                    ImageUtils.compressAndRename(
                        logger = logger,
                        input = it.imageFile,
                        resized = resized,
                        width = width,
                        height = height,
                        target = output,
                    )
                    output
                }
            }
            .awaitAll()
        outputImagesAnimeExpo2026.get().asFile
            .walkTopDown()
            .filter { it.isFile }
            .filterNot { it in allRetainedImages }
            .toList()
            .forEach { it.delete() }

        database.transaction {
            artistUpdates.map { it.first }.forEach {
                mutationQueries.updateArtistEntryAnimeExpo2026(it)
            }
        }
    }

    private suspend fun calculateArtistImages(
        embedCache: EmbedCache,
        artistId: String,
        @Suppress("SameParameterValue") year: DataYear,
        isFinalCatalog: Boolean,
        images: List<DatabaseImage>,
        embedLinks: List<String>,
    ): ArtistImages {
        val artistImagesDir = when (year) {
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> throw IllegalStateException()
            DataYear.ANIME_EXPO_2026 -> inputImagesAnimeExpo2026
        }.dir("artist/$artistId").get().asFile
        val embeds = embedLinks
            .sortedBy {
                // Sort Linktree last because the embed is not very useful
                when (Link.parse(it)?.type) {
                    Link.Type.LINKTREE -> 1
                    else -> 0
                }
            }
            .mapNotNull {
                val (link, catalogImage) = embedCache.getEmbedCatalogImage(it)
                    ?: return@mapNotNull null
                Triple(it, link, catalogImage)
            }
            .distinctBy { it.second }
            .distinctBy { it.third.name }
            .associate { it.first to it.third }
            .ifEmpty { null }
        val files = if (artistImagesDir.exists()) artistImagesDir.listFiles() else emptyArray()
        val finalImages = images.mapNotNull {
            val imageName = it.name.substringAfterLast("/").substringBeforeLast(".")
            val imageFile = files.find { it.name.startsWith(imageName) }
            if (imageFile == null) {
                logger.error("Failed to find $it")
                return@mapNotNull null
            }
            val hash = ImageUtils.hash(imageFile)
            val nameWithHash = it.name.substringBeforeLast("/") + "/$imageName-$hash.webp"
            it.copy(name = nameWithHash) to
                    ImageToCompress(
                        path = nameWithHash.removePrefix(year.folderName).removePrefix("/"),
                        imageFile = imageFile,
                    )
        }
        return ArtistImages(
            catalogImages = if (isFinalCatalog) finalImages.map { it.first } else emptyList(),
            tempImages = if (isFinalCatalog) emptyList() else finalImages.map { it.first },
            embeds = embeds,
            imagesToCompress = finalImages.map { it.second },
        )
    }

    private fun calculateNewArtists(database: BuildLogicEditDatabase) {
        val mutationQueries = database.mutationQueries
        val animeExpo2023 = mutationQueries.getAllArtistEntryAnimeExpo2023().executeAsList()
        val animeExpo2023Ids = animeExpo2023.mapTo(mutableSetOf()) { it.id }
        val animeExpo2024 = mutationQueries.getAllArtistEntryAnimeExpo2024().executeAsList()
        val animeExpo2024Ids = animeExpo2024.mapTo(mutableSetOf()) { it.id }
        val animeExpo2025 = mutationQueries.getAllArtistEntryAnimeExpo2025().executeAsList()
        val animeExpo2025Ids = animeExpo2025.mapTo(mutableSetOf()) { it.id }
        val animeExpo2026 = mutationQueries.getAllArtistEntryAnimeExpo2026().executeAsList()

        val animeNyc2024 = mutationQueries.getAllArtistEntryAnimeNyc2024().executeAsList()
        val animeNyc2024Ids = animeNyc2024.mapTo(mutableSetOf()) { it.id }
        val animeNyc2025 = mutationQueries.getAllArtistEntryAnimeNyc2025().executeAsList()

        database.transaction {
            animeExpo2024.forEach { artist ->
                val isNewArtist = artist.id !in animeExpo2023Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntryAnimeExpo2024(artist.copy(newArtist = true))
                }
            }

            animeExpo2025.forEach { artist ->
                val isNewArtist = artist.id !in animeExpo2023Ids &&
                        artist.id !in animeExpo2024Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntryAnimeExpo2025(artist.copy(newArtist = true))
                }
            }

            animeExpo2026.forEach { artist ->
                val isNewArtist = artist.id !in animeExpo2023Ids &&
                        artist.id !in animeExpo2024Ids &&
                        artist.id !in animeExpo2025Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntryAnimeExpo2026(artist.copy(newArtist = true))
                }
            }

            animeNyc2025.forEach { artist ->
                val isNewArtist = artist.id !in animeNyc2024Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntryAnimeNyc2025(artist.copy(newArtist = true))
                }
            }
        }
    }

    private val DataYear.Dates.start
        get() = LocalDate(year = year, month = month, day = startDay)

    private fun MutationQueries.getFallbackImages(
        year: DataYear,
        id: String,
    ): Pair<DataYear, List<DatabaseImage>>? =
        DataYear.entries
            .filter { it.dates.start < year.dates.start }
            .sortedByDescending { it.dates.start }
            .firstNotNullOfOrNull { queryYear ->
                when (queryYear) {
                    DataYear.ANIME_EXPO_2023 -> null
                    DataYear.ANIME_EXPO_2024 -> getImagesAnimeExpo2024(id).executeAsOneOrNull()
                    DataYear.ANIME_EXPO_2025 -> getImagesAnimeExpo2025(id).executeAsOneOrNull()
                    DataYear.ANIME_EXPO_2026 -> getImagesAnimeExpo2026(id).executeAsOneOrNull()
                    DataYear.ANIME_NYC_2024 -> getImagesAnimeNyc2024(id).executeAsOneOrNull()
                    DataYear.ANIME_NYC_2025 -> getImagesAnimeNyc2025(id).executeAsOneOrNull()
                }.orEmpty()
                    .ifEmpty { null }
                    ?.let { queryYear to it }
            }

    private suspend fun <T : Any> fixLegacyArtistImages(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
        dataYear: DataYear,
        entries: Query<T>,
        artistId: (T) -> String,
        updateImages: MutationQueries.(List<DatabaseImage>, id: String) -> Unit,
    ) = coroutineScope {
        val fixedImages = entries.executeAsList()
            .map {
                async {
                    val id = artistId(it)
                    id to findArtistImages(imageCacheDir, dataYear, id)
                }
            }
            .awaitAll()
        database.transaction {
            fixedImages.forEach { (id, images) ->
                database.mutationQueries.updateImages(images, id)
            }
        }
    }

    private fun <T : Any> fixLegacyRallyImages(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
        dataYear: DataYear,
        entries: Query<T>,
        rallyId: (T) -> String,
        hostTable: (T) -> String,
        fandom: (T) -> String,
        updateImages: MutationQueries.(List<DatabaseImage>, id: String) -> Unit,
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

    private fun addArtistChangelog(database: BuildLogicEditDatabase): ArtistChangelog? {
        val file = inputChangelog.get().asFile
        if (!file.exists()) return null
        val artistChangelog = file.inputStream().use {
            Json.decodeFromStream<ArtistChangelog>(it)
        }
        database.transaction {
            artistChangelog.additions.forEach {
                database.mutationQueries.insertArtistEntryAnimeExpo2026Changelog(
                    ArtistEntryAnimeExpo2026Changelog(
                        artistId = it.artistId,
                        date = it.date.toString(),
                        booth = it.booth,
                        name = it.name,
                        seriesInferred = it.seriesInferred?.toList(),
                        seriesConfirmed = it.seriesConfirmed?.toList(),
                        merchInferred = it.merchInferred?.toList(),
                        merchConfirmed = it.merchConfirmed?.toList(),
                        isBrandNew = it.isBrandNew,
                    )
                )
            }
        }

        return artistChangelog
    }

    private suspend fun fixLegacyArtistImages(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
    ) {
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

    private fun findArtistImages(
        imageCacheDir: File,
        year: DataYear,
        id: String,
    ): List<DatabaseImage> {
        val folder = when (year) {
            DataYear.ANIME_EXPO_2023 -> inputImagesAnimeExpo2023
            DataYear.ANIME_EXPO_2024 -> inputImagesAnimeExpo2024
            DataYear.ANIME_EXPO_2025 -> inputImagesAnimeExpo2025
            DataYear.ANIME_NYC_2025 -> inputImagesAnimeNyc2025
            else -> throw IllegalArgumentException()
        }.dir("catalogs")
            .get()
            .asFile
            .listFiles()
            ?.find { it.name.endsWith(id) }
            ?: return emptyList()
        return folder
            .listFiles()
            .filterNotNull()
            .sortedBy { it.name.substringBefore("-").trim().toInt() }
            .map {
                val (width, height, _) = ImageUtils.parseScaledImageWidthHeight(
                    logger = logger,
                    imageCacheDir = imageCacheDir,
                    file = it,
                )
                DatabaseImage("${folder.name}/${it.name}", width, height)
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
    ): List<DatabaseImage> {
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
        val folder = when (year) {
            DataYear.ANIME_EXPO_2023 -> inputImagesAnimeExpo2023
            DataYear.ANIME_EXPO_2024 -> inputImagesAnimeExpo2024
            DataYear.ANIME_EXPO_2025 -> inputImagesAnimeExpo2025
            DataYear.ANIME_EXPO_2026 -> return emptyList()
            DataYear.ANIME_NYC_2024 -> return emptyList()
            DataYear.ANIME_NYC_2025 -> inputImagesAnimeNyc2025
        }.dir("rallies")
            .get()
            .asFile
            .listFiles()
            ?.find { it.name.startsWith(targetName) }
            ?: return emptyList()
        return folder
            .listFiles()
            .filterNotNull()
            .sortedBy { it.name.substringBefore("-").trim().toInt() }
            .map {
                val (width, height, _) = ImageUtils.parseScaledImageWidthHeight(
                    logger = logger,
                    imageCacheDir = imageCacheDir,
                    file = it,
                )
                DatabaseImage("${folder.name}/${it.name}", width, height)
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

    private fun buildArtistConnections(database: BuildLogicEditDatabase): ArtistTagConnections {
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

    private fun calculateArtistFallbackYears(mutationQueries: MutationQueries) {
        // Need to reverse to ensure that earlier years don't overwrite
        // before later years can calculate a fallback
        DataYear.entries.sortedByDescending { it.dates.start }.forEach { year ->
            when (year) {
                // 2023 is the earliest year and doesn't have any fallback data
                DataYear.ANIME_EXPO_2023 -> Unit
                DataYear.ANIME_EXPO_2024 -> mutationQueries.getAllArtistEntryAnimeExpo2024()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeExpo2024(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
                DataYear.ANIME_EXPO_2025 -> mutationQueries.getAllArtistEntryAnimeExpo2025()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeExpo2025(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
                DataYear.ANIME_EXPO_2026 -> mutationQueries.getAllArtistEntryAnimeExpo2026()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeExpo2026(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
                DataYear.ANIME_NYC_2024 -> mutationQueries.getAllArtistEntryAnimeNyc2024()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeNyc2024(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
                DataYear.ANIME_NYC_2025 -> mutationQueries.getAllArtistEntryAnimeNyc2025()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeNyc2025(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
            }
        }
    }

    private fun buildStampRallyConnections(database: BuildLogicEditDatabase) {
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
        database: BuildLogicEditDatabase,
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
        database: BuildLogicEditDatabase,
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
    private class ArtistInferenceProvider(database: BuildLogicEditDatabase, artistId: String) {
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

    private data class ArtistImages(
        val catalogImages: List<DatabaseImage>,
        val tempImages: List<DatabaseImage>,
        val embeds: Map<String, DatabaseImage>?,
        val imagesToCompress: List<ImageToCompress>,
    )

    private data class ImageToCompress(
        val path: String,
        val imageFile: File,
    )
}
