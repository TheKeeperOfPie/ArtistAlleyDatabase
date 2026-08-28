import ImageUtils.parseScaledImageWidthHeight
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.artistEntry.GetEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntryChangelog
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntryChangelog
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyPrizeMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallySeriesConnection
import com.thekeeperofpie.artistalleydatabase.build_logic.edit.BuildLogicEditDatabase
import com.thekeeperofpie.artistalleydatabase.buildlogic.edit.MutationQueries
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
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
    abstract val inputImagesAnimeNyc2026: DirectoryProperty

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

    @get:OutputDirectory
    abstract val outputImagesAnimeNyc2026: DirectoryProperty

    init {
        val projectDirectory = layout.projectDirectory
        inputsDirectory.convention(projectDirectory.dir("inputs"))
        inputImagesAnimeExpo2026.convention(projectDirectory.dir("images/AX2026"))
        inputImagesAnimeNyc2026.convention(projectDirectory.dir("images/ANYC2026"))

        val buildDirectory = layout.buildDirectory
        outputDatabaseFile.convention(buildDirectory.file("generated/composeResources/files/database.sqlite"))
        outputDatabaseHashFile.convention(buildDirectory.file("generated/composeResources/files/databaseHash.txt"))
        outputMetadata.convention(buildDirectory.dir("generated/alley-metadata"))
        outputImagesAnimeExpo2026.convention(buildDirectory.dir("generated/composeResources/files/images/AX2026"))
        outputImagesAnimeNyc2026.convention(buildDirectory.dir("generated/composeResources/files/images/ANYC2026"))
        outputEmbedImages.convention(buildDirectory.dir("generated/composeResources/files/embeds"))
    }

    @TaskAction
    fun process() {
        if (!inputsDirectory.get().asFile.exists()) return
        val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
        runBlocking {
            val (driver, database) = Utils.createEditDatabase()
            driver.use {
                listOf("artists", "stampRallies")
                    .flatMap {
                        inputsDirectory.dir(it).get().asFile.listFiles().orEmpty().toList()
                    }
                    .forEach { Utils.readSqlFile(driver, database, it) }

                trackStage("TranslateLegacyTables") { translateLegacyTables(database) }

                // tags.sql must come last in order to overwrite legacy data
                listOf("merchLegacy.sql", "seriesLegacy.sql", "tags.sql").forEach {
                    val tagFile = inputsDirectory.dir("tags/$it").get().asFile
                    if (tagFile.exists()) {
                        Utils.readSqlFile(driver, database, tagFile)
                    }
                }

                @Suppress("NewApi")
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
                    .use {
                        withContext(it.asCoroutineDispatcher()) {
                            trackStage("VerifySeries") { verifySeries(database) }
                            trackStage("FixLegacySeriesSources") {
                                fixLegacySeriesSources(database)
                            }

                            val alleyChangelog =
                                trackStage("Changelog") { addChangelog(database) }

                            trackStage("LegacyArtistImages") {
                                fixLegacyArtistImages(database, imageCacheDir)
                            }
                            trackStage("FinalizeFlags") { finalizeFlags(database) }

                            val embedCache = trackStage("LoadEmbedCache") {
                                EmbedCache(
                                    logger = logger,
                                    inputFolder = inputEmbeds.get().asFile,
                                    outputJsonFile = outputMetadata.get().asFile,
                                    workingImagesFolder = outputMetadata.dir("embedImages")
                                        .get().asFile.apply { mkdir() },
                                )
                            }

                            val retainedImageAnimeExpo2026 = mutableSetOf<File>()

                            retainedImageAnimeExpo2026 += trackStage("FinalizeArtistsAnimeExpo2026") {
                                finalizeArtists(
                                    database = database,
                                    dataYear = DataYear.ANIME_EXPO_2026,
                                    imageCacheDir = imageCacheDir,
                                    embedCache = embedCache,
                                    artistLastEditTimes =
                                        alleyChangelog?.artistLastEditTimes[DataYear.ANIME_EXPO_2026].orEmpty()
                                )
                            }
                            retainedImageAnimeExpo2026 += trackStage("FinalizeRalliesAnimeExpo2026") {
                                finalizeRallies(
                                    database = database,
                                    dataYear = DataYear.ANIME_EXPO_2026,
                                    imageCacheDir = imageCacheDir,
                                    rallyLastEditTimes =
                                        alleyChangelog?.rallyLastEditTimes[DataYear.ANIME_EXPO_2026].orEmpty()
                                )
                            }

                            outputImagesAnimeExpo2026.get().asFile
                                .walkTopDown()
                                .filter { it.isFile }
                                .filterNot { it in retainedImageAnimeExpo2026 }
                                .toList()
                                .forEach { it.delete() }

                            val retainedImageAnimeNyc2026 = mutableSetOf<File>()

                            retainedImageAnimeNyc2026 += trackStage("FinalizeArtistsAnimeNyc2026") {
                                finalizeArtistsAnimeNyc2026(
                                    database = database,
                                    imageCacheDir = imageCacheDir,
                                    embedCache = embedCache,
                                    artistLastEditTimes =
                                        alleyChangelog?.artistLastEditTimes[DataYear.ANIME_EXPO_2026].orEmpty()
                                )
                            }

                            outputImagesAnimeNyc2026.get().asFile
                                .walkTopDown()
                                .filter { it.isFile }
                                .filterNot { it in retainedImageAnimeNyc2026 }
                                .toList()
                                .forEach { it.delete() }

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
                                calculateArtistFallbackYears(mutationQueries)
                            }

                            trackStage("StampRallyConnections") {
                                buildStampRallyConnections(database)
                            }

                            val artistTagConnections = trackStage("ArtistConnections") {
                                buildArtistConnections(driver, database)
                            }
                            trackStage("SeriesInferredConfirmedCounts") {
                                updateSeriesInferredConfirmedCounts(
                                    database,
                                    artistTagConnections
                                )
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

                            trackStage("CheckMerchIds") {
                                val allEnteredMerchIds =
                                    merchConnections.map { it.value.merchId }.toSet()
                                val allValidMerchIds =
                                    database.merchQueries.getMerch().executeAsList()
                                        .map { it.name }
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
                                            .map { it.value.artistRowId }
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

                            trackStage("CheckLinks") {
                                checkLinks(database)
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
                                    "artistEntry_fts",
                                    "artistEntryAnimeNyc2026_fts",
                                    "stampRallyEntry_fts",
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

                val outputFile = outputDatabaseFile.get().asFile
                trackStage("WriteDatabaseFile") {
                    driver.getConnection().createStatement().use {
                        it.executeUpdate("backup to ${outputFile.absolutePath}")
                    }
                }

                val hash = Utils.hash(outputFile)
                outputDatabaseHashFile.get().asFile.writeText(hash.toString())
            }
        }
    }

    private fun translateLegacyTables(database: BuildLogicEditDatabase) {
        val artistEntriesAnimeExpo2023 =
            database.legacyQueries.getAllArtistEntryAnimeExpo2023().executeAsList()
        database.transaction {
            artistEntriesAnimeExpo2023.forEach { artist ->
                val links =
                    artist.links.groupBy { Link.parse(it)?.type?.category }.toMutableMap()
                val socialLinks = links.remove(LinkCategory.SOCIALS).orEmpty() +
                        links.remove(LinkCategory.SUPPORT).orEmpty() +
                        links.remove(LinkCategory.OTHER).orEmpty() +
                        links.remove(null).orEmpty()
                val storeLinks = links.remove(LinkCategory.STORES).orEmpty()
                val portfolioLinks = links.remove(LinkCategory.PORTFOLIOS).orEmpty()
                val commissionLinks = links.remove(LinkCategory.COMMISSIONS).orEmpty()
                if (links.isNotEmpty()) {
                    throw IllegalStateException("Failed to map all links: $links")
                }
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_EXPO_2023,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = if (artist.images.isNotEmpty()) {
                            portfolioLinks
                        } else {
                            portfolioLinks + artist.catalogLinks
                        },
                        catalogLinks = if (artist.images.isNotEmpty()) {
                            artist.catalogLinks
                        } else {
                            emptyList()
                        },
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = null,
                        commissions = commissionLinks,
                        commissionFlags = -1,
                        seriesInferred = emptyList(),
                        seriesConfirmed = emptyList(),
                        merchInferred = emptyList(),
                        merchConfirmed = emptyList(),
                        images = artist.images,
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
                )
            }
        }

        val stampRallyEntriesAnimeExpo2023 =
            database.legacyQueries.getStampRalliesAnimeExpo2023().executeAsList()
        database.transaction {
            stampRallyEntriesAnimeExpo2023.forEach {
                database.mutationQueries.updateStampRallyEntry(
                    StampRallyEntry(
                        id = it.id,
                        dataYear = DataYear.ANIME_EXPO_2023,
                        fandom = it.fandom,
                        tables = it.tables.map { it.substringBeforeLast("-").trim() },
                        startTables = null,
                        endTables = null,
                        links = it.links,
                        tableMin = null,
                        totalCost = null,
                        prize = null,
                        prizeLimit = null,
                        prizeMerch = null,
                        series = emptyList(),
                        merch = emptyList(),
                        notes = null,
                        images = it.images,
                        editorNotes = null,
                        lastEditor = null,
                        lastEditTime = null,
                    )
                )
            }
        }

        val artistEntriesAnimeExpo2024 =
            database.legacyQueries.getAllArtistEntryAnimeExpo2024().executeAsList()
        database.transaction {
            artistEntriesAnimeExpo2024.forEach { artist ->
                val links =
                    artist.links.groupBy { Link.parse(it)?.type?.category }.toMutableMap()
                val socialLinks = links.remove(LinkCategory.SOCIALS).orEmpty() +
                        links.remove(LinkCategory.SUPPORT).orEmpty() +
                        links.remove(LinkCategory.OTHER).orEmpty() +
                        links.remove(null).orEmpty()
                val storeLinks = links.remove(LinkCategory.STORES).orEmpty()
                val portfolioLinks = links.remove(LinkCategory.PORTFOLIOS).orEmpty()
                val commissionLinks = links.remove(LinkCategory.COMMISSIONS).orEmpty()
                if (links.isNotEmpty()) {
                    throw IllegalStateException("Failed to map all links: $links")
                }
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_EXPO_2024,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = if (artist.images.isNotEmpty()) {
                            portfolioLinks
                        } else {
                            portfolioLinks + artist.catalogLinks
                        },
                        catalogLinks = if (artist.images.isNotEmpty()) {
                            artist.catalogLinks
                        } else {
                            emptyList()
                        },
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = artist.notes,
                        commissions = commissionLinks,
                        commissionFlags = -1,
                        seriesInferred = artist.seriesInferred,
                        seriesConfirmed = artist.seriesConfirmed,
                        merchInferred = artist.merchInferred,
                        merchConfirmed = artist.merchConfirmed,
                        images = artist.images,
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
                )
            }
        }

        val stampRallyEntriesAnimeExpo2024 =
            database.legacyQueries.getStampRalliesAnimeExpo2024().executeAsList()
        database.transaction {
            stampRallyEntriesAnimeExpo2024.forEach {
                database.mutationQueries.updateStampRallyEntry(
                    StampRallyEntry(
                        id = it.id,
                        dataYear = DataYear.ANIME_EXPO_2024,
                        fandom = it.fandom,
                        tables = it.tables.map { it.substringBeforeLast("-").trim() },
                        startTables = null,
                        endTables = null,
                        links = it.links,
                        tableMin = it.tableMin,
                        totalCost = it.totalCost,
                        prize = null,
                        prizeLimit = it.prizeLimit,
                        prizeMerch = null,
                        series = emptyList(),
                        merch = emptyList(),
                        notes = it.notes,
                        images = it.images,
                        editorNotes = null,
                        lastEditor = null,
                        lastEditTime = null,
                    )
                )
            }
        }

        val artistEntriesAnimeNyc2024 =
            database.legacyQueries.getAllArtistEntryAnimeNyc2024().executeAsList()
        database.transaction {
            artistEntriesAnimeNyc2024.forEach { artist ->
                val links =
                    artist.links.groupBy { Link.parse(it)?.type?.category }.toMutableMap()
                val socialLinks = links.remove(LinkCategory.SOCIALS).orEmpty() +
                        links.remove(LinkCategory.SUPPORT).orEmpty() +
                        links.remove(LinkCategory.OTHER).orEmpty() +
                        links.remove(null).orEmpty()
                val storeLinks = links.remove(LinkCategory.STORES).orEmpty()
                val portfolioLinks = links.remove(LinkCategory.PORTFOLIOS).orEmpty()
                val commissionLinks = links.remove(LinkCategory.COMMISSIONS).orEmpty()
                if (links.isNotEmpty()) {
                    throw IllegalStateException("Failed to map all links: $links")
                }
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_NYC_2024,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = if (artist.images.isNotEmpty()) {
                            portfolioLinks
                        } else {
                            portfolioLinks + artist.catalogLinks
                        },
                        catalogLinks = if (artist.images.isNotEmpty()) {
                            artist.catalogLinks
                        } else {
                            emptyList()
                        },
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = artist.notes.also {
                            if (!it.isNullOrBlank()) {
                                logger.lifecycle("Notes for ${artist.name}: $it")
                            }
                        },
                        commissions = commissionLinks + artist.commissions,
                        commissionFlags = -1,
                        seriesInferred = artist.seriesInferred,
                        seriesConfirmed = artist.seriesConfirmed,
                        merchInferred = artist.merchInferred,
                        merchConfirmed = artist.merchConfirmed,
                        images = artist.images,
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
                )
            }
        }

        val artistEntriesAnimeExpo2025 =
            database.legacyQueries.getAllArtistEntryAnimeExpo2025().executeAsList()
        database.transaction {
            artistEntriesAnimeExpo2025.forEach { artist ->
                val links =
                    artist.links.groupBy { Link.parse(it)?.type?.category }.toMutableMap()
                val socialLinks = links.remove(LinkCategory.SOCIALS).orEmpty() +
                        links.remove(LinkCategory.SUPPORT).orEmpty() +
                        links.remove(LinkCategory.OTHER).orEmpty() +
                        links.remove(null).orEmpty()
                val storeLinks = links.remove(LinkCategory.STORES).orEmpty()
                val portfolioLinks = links.remove(LinkCategory.PORTFOLIOS).orEmpty()
                val commissionLinks = links.remove(LinkCategory.COMMISSIONS).orEmpty()
                if (links.isNotEmpty()) {
                    throw IllegalStateException("Failed to map all links: $links")
                }
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_EXPO_2025,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = if (artist.images.isNotEmpty()) {
                            portfolioLinks
                        } else {
                            portfolioLinks + artist.catalogLinks
                        },
                        catalogLinks = if (artist.images.isNotEmpty()) {
                            artist.catalogLinks
                        } else {
                            emptyList()
                        },
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = artist.notes,
                        commissions = commissionLinks + artist.commissions,
                        commissionFlags = -1,
                        seriesInferred = artist.seriesInferred,
                        seriesConfirmed = artist.seriesConfirmed,
                        merchInferred = artist.merchInferred,
                        merchConfirmed = artist.merchConfirmed,
                        images = artist.images,
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
                )
            }
        }

        val stampRallyEntriesAnimeExpo2025 =
            database.legacyQueries.getStampRalliesAnimeExpo2025().executeAsList()
        database.transaction {
            stampRallyEntriesAnimeExpo2025.forEach {
                database.mutationQueries.updateStampRallyEntry(
                    StampRallyEntry(
                        id = it.id,
                        dataYear = DataYear.ANIME_EXPO_2025,
                        fandom = it.fandom,
                        tables = it.tables.map { it.substringBeforeLast("-").trim() },
                        startTables = null,
                        endTables = null,
                        links = it.links,
                        tableMin = it.tableMin,
                        totalCost = it.totalCost,
                        prize = it.prize,
                        prizeLimit = it.prizeLimit,
                        prizeMerch = null,
                        series = it.series,
                        merch = emptyList(),
                        notes = it.notes,
                        images = it.images,
                        editorNotes = null,
                        lastEditor = null,
                        lastEditTime = null,
                    )
                )
            }
        }

        val artistEntriesAnimeNyc2025 =
            database.legacyQueries.getAllArtistEntryAnimeNyc2025().executeAsList()
        database.transaction {
            artistEntriesAnimeNyc2025.forEach { artist ->
                val links =
                    artist.links.groupBy { Link.parse(it)?.type?.category }.toMutableMap()
                val socialLinks = links.remove(LinkCategory.SOCIALS).orEmpty() +
                        links.remove(LinkCategory.SUPPORT).orEmpty() +
                        links.remove(LinkCategory.OTHER).orEmpty() +
                        links.remove(null).orEmpty()
                val storeLinks = links.remove(LinkCategory.STORES).orEmpty()
                val portfolioLinks = links.remove(LinkCategory.PORTFOLIOS).orEmpty()
                val commissionLinks = links.remove(LinkCategory.COMMISSIONS).orEmpty()
                if (links.isNotEmpty()) {
                    throw IllegalStateException("Failed to map all links: $links")
                }
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_NYC_2025,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = if (artist.images.isNotEmpty()) {
                            portfolioLinks
                        } else {
                            portfolioLinks + artist.catalogLinks
                        },
                        catalogLinks = if (artist.images.isNotEmpty()) {
                            artist.catalogLinks
                        } else {
                            emptyList()
                        },
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = artist.notes,
                        commissions = commissionLinks + artist.commissions,
                        commissionFlags = -1,
                        seriesInferred = artist.seriesInferred,
                        seriesConfirmed = artist.seriesConfirmed,
                        merchInferred = artist.merchInferred,
                        merchConfirmed = artist.merchConfirmed,
                        images = artist.images,
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
                )
            }
        }

        val artistEntriesAnimeExpo2026 =
            database.legacyQueries.getAllArtistEntryAnimeExpo2026().executeAsList()
        database.transaction {
            artistEntriesAnimeExpo2026.forEach { artist ->
                database.mutationQueries.updateArtistEntry(
                    ArtistEntry(
                        id = Uuid.parse(artist.id),
                        dataYear = DataYear.ANIME_EXPO_2026,
                        status = ArtistStatus.UNKNOWN,
                        booth = artist.booth,
                        name = artist.name,
                        summary = artist.summary,
                        socialLinks = artist.socialLinks,
                        storeLinks = artist.storeLinks,
                        portfolioLinks = artist.portfolioLinks,
                        catalogLinks = artist.catalogLinks,
                        linkFlags = -1,
                        linkFlags2 = -1,
                        notes = artist.notes,
                        commissions = artist.commissions,
                        commissionFlags = -1,
                        seriesInferred = artist.seriesInferred,
                        seriesConfirmed = artist.seriesConfirmed,
                        merchInferred = artist.merchInferred,
                        merchConfirmed = artist.merchConfirmed,
                        images = artist.images,
                        fallbackImageYear = null,
                        tempImages = null,
                        profileImage = null,
                        embeds = null,
                        editorNotes = null,
                        lastEditor = null,
                        lastEditTime = artist.lastEditTime,
                        verifiedArtist = false,
                        newArtist = false,
                    )
                )
            }
        }

        val stampRallyEntriesAnimeExpo2026 =
            database.legacyQueries.getStampRalliesAnimeExpo2026().executeAsList()
        database.transaction {
            stampRallyEntriesAnimeExpo2026.forEach {
                database.mutationQueries.updateStampRallyEntry(
                    StampRallyEntry(
                        id = it.id,
                        dataYear = DataYear.ANIME_EXPO_2026,
                        fandom = it.fandom,
                        tables = it.tables.map { it.substringBeforeLast("-").trim() },
                        startTables = it.startTables,
                        endTables = it.endTables,
                        links = it.links,
                        tableMin = it.tableMin,
                        totalCost = it.totalCost,
                        prize = it.prize,
                        prizeLimit = it.prizeLimit,
                        prizeMerch = it.prizeMerch,
                        series = it.series,
                        merch = it.merch,
                        notes = it.notes,
                        images = it.images,
                        editorNotes = null,
                        lastEditor = null,
                        lastEditTime = it.lastEditTime,
                    )
                )
            }
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

    private suspend fun finalizeFlags(database: BuildLogicEditDatabase) {
        DataYear.entries.forEach {
            database.artistEntryQueries.getAllEntries(it)
                .executeAsList()
                .forEach { artist ->
                    val (linkFlags, linkFlags2) = Link.parseFlags(
                        socialLinks = artist.socialLinks,
                        storeLinks = artist.storeLinks,
                        portfolioLinks = artist.portfolioLinks,
                        catalogLinks = artist.catalogLinks,
                    )
                    val commissionFlags = CommissionType.parseFlags(artist.commissions)

                    database.mutationQueries.updateArtistEntry(
                        artist.copy(
                            linkFlags = linkFlags,
                            linkFlags2 = linkFlags2,
                            commissionFlags = commissionFlags,
                        )
                    ).await()
                }
        }
    }

    private suspend fun finalizeArtists(
        database: BuildLogicEditDatabase,
        dataYear: DataYear,
        imageCacheDir: File,
        embedCache: EmbedCache,
        artistLastEditTimes: Map<Uuid, Instant>,
    ) = coroutineScope {
        val verifiedArtistIds = when (dataYear) {
            DataYear.ANIME_EXPO_2026 -> inputsDirectory.dir("snapshots/animeExpo2026/form")
            else -> null
        }?.get()?.asFile?.listFiles()
            ?.maxByOrNull {
                Instant.parse(
                    it.nameWithoutExtension
                        .replace("_", ":")
                        .replace(";", ":")
                )
            }
            ?.let { snapshotFile ->
                val (driver, database) = Utils.createFormDatabase()
                if (!Utils.readSqlFile(driver, database, snapshotFile)) {
                    logger.error("Failed to apply before ${snapshotFile.absolutePath}")
                    return@let emptyList()
                }
                driver.use {
                    database.verifiedQueries.getVerifiedArtistIds(dataYear)
                        .executeAsList()
                }
            }
            .orEmpty()

        val mutationQueries = database.mutationQueries
        val artistUpdates = mutationQueries.getArtistEntriesByYear(dataYear)
            .executeAsList()
            .map { artist ->
                async {
                    // TODO: Validate this somewhere else
                    if (artist.seriesInferred.distinct().size != artist.seriesInferred.size ||
                        artist.seriesConfirmed.distinct().size != artist.seriesConfirmed.size ||
                        artist.merchInferred.distinct().size != artist.merchInferred.size ||
                        artist.merchConfirmed.distinct().size != artist.merchConfirmed.size
                    ) {
                        logger.error("Duplicate failure for artist ${artist.booth} - ${artist.name}")
                    }
                    val inference = ArtistInferenceProvider(database, artist.id.toString())
                    val socialLinks = artist.socialLinks.ifEmpty { inference.socialLinks }
                    val storeLinks = artist.storeLinks.ifEmpty { inference.storeLinks }
                    val seriesInferred = artist.seriesInferred
                        .ifEmpty { inference.seriesInferred } - artist.seriesConfirmed.toSet()
                    val merchInferred = artist.merchInferred
                        .ifEmpty { inference.merchInferred } - artist.merchConfirmed.toSet()


                    val (linkFlags, linkFlags2) = Link.parseFlags(
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        portfolioLinks = artist.portfolioLinks,
                        catalogLinks = artist.catalogLinks,
                    )
                    val commissionFlags = CommissionType.parseFlags(artist.commissions)

                    // Don't expose raw edit times from backend, just mirror the changelog dates
                    val lastEditTime = artistLastEditTimes[artist.id]
                        ?.toLocalDateTime(TimeZone.UTC)
                        ?.date
                        ?.atStartOfDayIn(TimeZone.UTC)
                    val embedLinks = (artist.portfolioLinks + socialLinks + storeLinks +
                            artist.commissions.filter { it.startsWith("http") })
                    val artistImages = calculateArtistImages(
                        imageCacheDir = imageCacheDir,
                        embedCache = embedCache,
                        artistId = artist.id.toString(),
                        year = dataYear,
                        isFinalCatalog = artist.catalogLinks.isNotEmpty() ||
                                artist.seriesConfirmed.isNotEmpty() ||
                                artist.merchConfirmed.isNotEmpty(),
                        profileImage = artist.profileImage,
                        images = artist.images,
                        embedLinks = embedLinks,
                    )
                    val newArtist = artist.copy(
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        seriesInferred = seriesInferred,
                        merchInferred = merchInferred,
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        commissionFlags = commissionFlags,
                        images = artistImages.catalogImages.map { it.final },
                        tempImages = artistImages.tempImages.map { it.final },
                        profileImage = artistImages.customProfileImage?.final
                            ?: artistImages.embedProfileImage,
                        embeds = artistImages.largeEmbeds,
                        lastEditTime = lastEditTime,
                        verifiedArtist = verifiedArtistIds.contains(artist.id),
                    )
                    val imagesToCompress =
                        listOfNotNull(artistImages.customProfileImage?.imageToCompress) +
                                artistImages.catalogImages.map { it.imageToCompress } +
                                artistImages.tempImages.map { it.imageToCompress }
                    Triple(newArtist, imagesToCompress, artistImages)
                }
            }
            .awaitAll()

        val allRetainedImages = artistUpdates.flatMap { it.second }
            .map {
                async {
                    val output = when (dataYear) {
                        DataYear.ANIME_EXPO_2026 -> outputImagesAnimeExpo2026
                        else -> null
                    }?.get()?.asFile?.resolve(it.path) ?: return@async null
                    ImageUtils.compressAndRenameHashed(
                        logger = logger,
                        input = it.imageFile,
                        resized = it.resized,
                        width = it.width,
                        height = it.height,
                        target = output,
                    )
                    output
                }
            }
            .awaitAll()
            .filterNotNull()

        val changelog = database.artistChangelogEntryQueries
            .getAllEntries(dataYear = dataYear, catalogsOnly = 1L)
            .executeAsList()
            .groupBy { it.artistId }
        database.transaction {
            artistUpdates.forEach { (artist, _, artistImages) ->
                mutationQueries.updateArtistEntry(artist)
                val isTempImages = artistImages.tempImages.isNotEmpty()
                changelog[artist.id]
                    ?.map {
                        it.copy(
                            images = it.images?.mapNotNull { changelogImage ->
                                if (isTempImages) {
                                    artistImages.tempImages.find { it.original.name == changelogImage.name }
                                } else {
                                    artistImages.catalogImages.find { it.original.name == changelogImage.name }
                                }?.final
                            }?.ifEmpty { null },
                            isTempImages = isTempImages,
                        )
                    }
                    ?.forEach(mutationQueries::insertArtistChangelogEntry)
            }
        }
        allRetainedImages
    }

    private suspend fun finalizeArtistsAnimeNyc2026(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
        embedCache: EmbedCache,
        artistLastEditTimes: Map<Uuid, Instant>,
    ) = coroutineScope {
        val verifiedArtistIds =
            inputsDirectory.dir("snapshots/animeNyc2026/form").get().asFile.listFiles()
                .orEmpty()
                .maxByOrNull {
                    Instant.parse(
                        it.nameWithoutExtension
                            .replace("_", ":")
                            .replace(";", ":")
                    )
                }
                ?.let { snapshotFile ->
                    val (driver, database) = Utils.createFormDatabase()
                    if (!Utils.readSqlFile(driver, database, snapshotFile)) {
                        logger.error("Failed to apply before ${snapshotFile.absolutePath}")
                        return@let emptyList()
                    }
                    driver.use {
                        database.verifiedQueries.getVerifiedArtistIds(DataYear.ANIME_NYC_2026)
                            .executeAsList()
                    }
                }
                .orEmpty()

        val mutationQueries = database.mutationQueries
        val artistUpdates = mutationQueries.getAllArtistEntryAnimeNyc2026()
            .executeAsList()
            .map { artist ->
                async {
                    // TODO: Validate this somewhere else
                    if (artist.seriesInferred.distinct().size != artist.seriesInferred.size ||
                        artist.seriesConfirmed.distinct().size != artist.seriesConfirmed.size ||
                        artist.merchInferred.distinct().size != artist.merchInferred.size ||
                        artist.merchConfirmed.distinct().size != artist.merchConfirmed.size
                    ) {
                        logger.error("Duplicate failure for artist ${artist.booth} - ${artist.name}")
                    }
                    val inference = ArtistInferenceProvider(database, artist.id)
                    val socialLinks = artist.socialLinks.ifEmpty { inference.socialLinks }
                    val storeLinks = artist.storeLinks.ifEmpty { inference.storeLinks }
                    val seriesInferred = artist.seriesInferred
                        .ifEmpty { inference.seriesInferred } - artist.seriesConfirmed.toSet()
                    val merchInferred = artist.merchInferred
                        .ifEmpty { inference.merchInferred } - artist.merchConfirmed.toSet()

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
                        imageCacheDir = imageCacheDir,
                        embedCache = embedCache,
                        artistId = artist.id,
                        year = DataYear.ANIME_NYC_2026,
                        isFinalCatalog = artist.catalogLinks.isNotEmpty() ||
                                artist.seriesConfirmed.isNotEmpty() ||
                                artist.merchConfirmed.isNotEmpty(),
                        profileImage = artist.profileImage,
                        images = artist.images,
                        embedLinks = embedLinks,
                    )
                    val newArtist = artist.copy(
                        socialLinks = socialLinks,
                        storeLinks = storeLinks,
                        seriesInferred = seriesInferred.filter { !it.startsWith("Commission") },
                        merchInferred = merchInferred.filter { !it.startsWith("Commission") },
                        linkFlags = linkFlags,
                        linkFlags2 = linkFlags2,
                        commissionFlags = commissionFlags,
                        images = artistImages.catalogImages.map { it.final },
                        tempImages = artistImages.tempImages.map { it.final },
                        profileImage = artistImages.customProfileImage?.final
                            ?: artistImages.embedProfileImage,
                        embeds = artistImages.largeEmbeds,
                        lastEditTime = lastEditTime,
                        verifiedArtist = verifiedArtistIds.contains(artistId),
                    )
                    val imagesToCompress =
                        listOfNotNull(artistImages.customProfileImage?.imageToCompress) +
                                artistImages.catalogImages.map { it.imageToCompress } +
                                artistImages.tempImages.map { it.imageToCompress }
                    Triple(newArtist, imagesToCompress, artistImages)
                }
            }
            .awaitAll()

        val allRetainedImages = artistUpdates.flatMap { it.second }
            .map {
                async {
                    val output =
                        outputImagesAnimeNyc2026.get().asFile.resolve(it.path)
                    ImageUtils.compressAndRenameHashed(
                        logger = logger,
                        input = it.imageFile,
                        resized = it.resized,
                        width = it.width,
                        height = it.height,
                        target = output,
                    )
                    output
                }
            }
            .awaitAll()

        val changelog = database.artistEntryAnimeNyc2026Queries.getChangelog(1L).executeAsList()
            .groupBy { it.artistId }
        database.transaction {
            artistUpdates.forEach { (artist, _, artistImages) ->
                mutationQueries.updateArtistEntryAnimeNyc2026(artist)
                val isTempImages = artistImages.tempImages.isNotEmpty()
                changelog[Uuid.parse(artist.id)]
                    ?.map {
                        it.copy(
                            images = it.images?.mapNotNull { changelogImage ->
                                if (isTempImages) {
                                    artistImages.tempImages.find { it.original.name == changelogImage.name }
                                } else {
                                    artistImages.catalogImages.find { it.original.name == changelogImage.name }
                                }?.final
                            }?.ifEmpty { null },
                            isTempImages = isTempImages,
                        )
                    }
                    ?.forEach(mutationQueries::insertArtistEntryAnimeNyc2026Changelog)
            }
        }
        allRetainedImages
    }

    private suspend fun finalizeRallies(
        database: BuildLogicEditDatabase,
        dataYear: DataYear,
        imageCacheDir: File,
        rallyLastEditTimes: Map<Uuid, Instant>,
    ) = coroutineScope {
        val mutationQueries = database.mutationQueries
        val rallyUpdates = mutationQueries.getStampRallyEntriesByYear(dataYear)
            .executeAsList()
            .map { rally ->
                async {
                    val rallyId = Uuid.parse(rally.id)

                    // Don't expose raw edit times from backend, just mirror the changelog dates
                    val lastEditTime = rallyLastEditTimes[rallyId]
                        ?.toLocalDateTime(TimeZone.UTC)
                        ?.date
                        ?.atStartOfDayIn(TimeZone.UTC)
                    val rallyImages = calculateRallyImages(
                        imageCacheDir = imageCacheDir,
                        rallyId = rally.id,
                        year = dataYear,
                        images = rally.images,
                    )
                    val newRally = rally.copy(
                        images = rallyImages.map { it.final },
                        lastEditTime = lastEditTime,
                    )
                    val imagesToCompress = rallyImages.map { it.imageToCompress }
                    Triple(newRally, imagesToCompress, rallyImages)
                }
            }
            .awaitAll()

        val allRetainedImages = rallyUpdates.flatMap { it.second }
            .map {
                async {
                    val output = when (dataYear) {
                        DataYear.ANIME_EXPO_2026 -> outputImagesAnimeExpo2026
                        else -> null
                    }?.get()?.asFile?.resolve(it.path) ?: return@async null
                    ImageUtils.compressAndRenameHashed(
                        logger = logger,
                        input = it.imageFile,
                        resized = it.resized,
                        width = it.width,
                        height = it.height,
                        target = output,
                    )
                    output
                }
            }
            .awaitAll()
            .filterNotNull()

        val changelog = database.stampRallyChangelogEntryQueries
            .getAllEntries(dataYear)
            .executeAsList()
            .groupBy { it.stampRallyId }
        database.transaction {
            rallyUpdates.forEach { (rally, _, rallyImages) ->
                mutationQueries.updateStampRallyEntry(rally)
                changelog[Uuid.parse(rally.id)]
                    ?.map {
                        it.copy(
                            images = it.images?.mapNotNull { changelogImage ->
                                rallyImages.find { it.original == changelogImage }?.final
                            }?.ifEmpty { null },
                        )
                    }
                    ?.forEach(mutationQueries::insertStampRallyChangelogEntry)
            }
        }

        allRetainedImages
    }

    private suspend fun calculateArtistImages(
        imageCacheDir: File,
        embedCache: EmbedCache,
        artistId: String,
        @Suppress("SameParameterValue") year: DataYear,
        isFinalCatalog: Boolean,
        profileImage: DatabaseImage?,
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
            DataYear.ANIME_NYC_2026 -> inputImagesAnimeNyc2026
        }.dir("artist/$artistId").get().asFile
        val files = if (artistImagesDir.exists()) {
            artistImagesDir.listFiles() ?: emptyArray()
        } else {
            emptyArray()
        }
        val finalImages = images.mapNotNull {
            finalizeImage(
                imageCacheDir = imageCacheDir,
                year = year,
                files = files,
                it = it,
                resizeTarget = ImageUtils.NORMAL_RESIZE_TARGET,
            )
        }

        val allEmbeds = embedLinks
            .filter { Link.parse(it)?.shouldEmbed != false }
            .mapNotNull {
                val (link, catalogImage) = embedCache.getEmbedCatalogImage(it)
                    ?: return@mapNotNull null
                Triple(it, link, catalogImage)
            }
            .distinctBy { it.second }
            .distinctBy { it.third.name }
            .associate { it.first to it.third }

        val (largeEmbeds, smallEmbeds) = allEmbeds
            .toList()
            .partition { (_, image) ->
                val width = image.width ?: return@partition false
                val height = image.height ?: return@partition false
                width >= EMBED_MIN_DIMENSION || height >= EMBED_MIN_DIMENSION
            }


        val customProfileImage = profileImage?.let {
            finalizeImage(
                imageCacheDir = imageCacheDir,
                year = year,
                files = files,
                it = it,
                // TODO: Center crop here rather than at runtime
                resizeTarget = ImageUtils.THUMBNAIL_RESIZE_TARGET,
                finalNamePrefix = "custom-" // TODO: Use a better mechanism for this
            )
        }

        val embedProfileImage = smallEmbeds
            .ifEmpty { largeEmbeds }
            .minByOrNull { Link.parse(it.first)?.type?.category == LinkCategory.SOCIALS }
            ?.second

        return ArtistImages(
            catalogImages = if (isFinalCatalog) finalImages else emptyList(),
            tempImages = if (isFinalCatalog) emptyList() else finalImages,
            customProfileImage = customProfileImage,
            embedProfileImage = embedProfileImage,
            largeEmbeds = largeEmbeds.toMap(),
        )
    }

    private fun calculateRallyImages(
        imageCacheDir: File,
        rallyId: String,
        @Suppress("SameParameterValue") year: DataYear,
        images: List<DatabaseImage>,
    ): List<FinalImage> {
        val rallyImagesDir = when (year) {
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> throw IllegalStateException()
            DataYear.ANIME_EXPO_2026 -> inputImagesAnimeExpo2026
            DataYear.ANIME_NYC_2026 -> inputImagesAnimeNyc2026
        }.dir("rally/$rallyId").get().asFile
        val files = if (rallyImagesDir.exists()) {
            rallyImagesDir.listFiles() ?: emptyArray()
        } else {
            emptyArray()
        }
        return images.mapNotNull {
            finalizeImage(
                imageCacheDir = imageCacheDir,
                year = year,
                files = files,
                it = it,
                resizeTarget = ImageUtils.NORMAL_RESIZE_TARGET,
            )
        }
    }

    private fun finalizeImage(
        imageCacheDir: File,
        year: DataYear,
        files: Array<File>,
        it: DatabaseImage,
        resizeTarget: Int,
        finalNamePrefix: String = "",
    ): FinalImage? {
        val imageName = it.name.substringAfterLast("/").substringBeforeLast(".")
        val imageFile = files.find { it.name.startsWith(imageName) }
        if (imageFile == null) {
            logger.error("Failed to find $it")
            return null
        }
        val hash = ImageUtils.hash(imageFile, resizeTarget)
        val nameWithHash = it.name.substringBeforeLast("/") + "/$imageName-$hash.webp"
        val (width, height, resized) = parseScaledImageWidthHeight(
            logger = logger,
            imageCacheDir = imageCacheDir,
            file = imageFile,
            resizeTarget = resizeTarget,
        )
        return FinalImage(
            original = it,
            final = it.copy(name = finalNamePrefix + nameWithHash, width = width, height = height),
            imageToCompress = ImageToCompress(
                path = nameWithHash.removePrefix(year.folderName).removePrefix("/"),
                imageFile = imageFile,
                resized = resized,
                width = width,
                height = height,
            ),
        )
    }

    private fun calculateNewArtists(database: BuildLogicEditDatabase) {
        val mutationQueries = database.mutationQueries
        val animeExpo2023 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_EXPO_2023).executeAsList()
        val animeExpo2023Ids = animeExpo2023.mapTo(mutableSetOf()) { it.id.toString() }
        val animeExpo2024 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_EXPO_2024).executeAsList()
        val animeExpo2024Ids = animeExpo2024.mapTo(mutableSetOf()) { it.id.toString() }
        val animeExpo2025 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_EXPO_2025).executeAsList()
        val animeExpo2025Ids = animeExpo2025.mapTo(mutableSetOf()) { it.id.toString() }
        val animeExpo2026 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_EXPO_2026).executeAsList()

        val animeNyc2024 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_NYC_2024).executeAsList()
        val animeNyc2024Ids = animeNyc2024.mapTo(mutableSetOf()) { it.id.toString() }
        val animeNyc2025 =
            mutationQueries.getArtistEntriesByYear(DataYear.ANIME_NYC_2025).executeAsList()
        val animeNyc2025Ids = animeNyc2025.mapTo(mutableSetOf()) { it.id.toString() }
        val animeNyc2026 = mutationQueries.getAllArtistEntryAnimeNyc2026().executeAsList()

        database.transaction {
            animeExpo2024.forEach { artist ->
                val isNewArtist = artist.id.toString() !in animeExpo2023Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntry(artist.copy(newArtist = true))
                }
            }

            animeExpo2025.forEach { artist ->
                val isNewArtist = artist.id.toString() !in animeExpo2023Ids &&
                        artist.id.toString() !in animeExpo2024Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntry(artist.copy(newArtist = true))
                }
            }

            animeExpo2026.forEach { artist ->
                val isNewArtist = artist.id.toString() !in animeExpo2023Ids &&
                        artist.id.toString() !in animeExpo2024Ids &&
                        artist.id.toString() !in animeExpo2025Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntry(artist.copy(newArtist = true))
                }
            }

            animeNyc2025.forEach { artist ->
                val isNewArtist = artist.id.toString() !in animeNyc2024Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntry(artist.copy(newArtist = true))
                }
            }

            animeNyc2026.forEach { artist ->
                val isNewArtist = artist.id !in animeNyc2024Ids &&
                        artist.id !in animeNyc2025Ids
                if (isNewArtist) {
                    mutationQueries.updateArtistEntryAnimeNyc2026(artist.copy(newArtist = true))
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
                    DataYear.ANIME_NYC_2026 -> getImagesAnimeNyc2026(id).executeAsOneOrNull()
                    else -> getArtistImages(queryYear, Uuid.parse(id)).executeAsOneOrNull()
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

    private fun addChangelog(database: BuildLogicEditDatabase): AlleyChangelog? {
        val file = inputChangelog.get().asFile
        if (!file.exists()) return null
        val alleyChangelog = file.inputStream().use {
            Json.decodeFromStream<AlleyChangelog>(it)
        }
        database.transaction {
            alleyChangelog.artistDiffs[DataYear.ANIME_EXPO_2026]?.forEach {
                database.mutationQueries.insertArtistChangelogEntry(
                    ArtistChangelogEntry(
                        artistId = it.artistId,
                        dataYear = DataYear.ANIME_EXPO_2026,
                        date = it.date.toString(),
                        booth = it.booth,
                        name = it.name,
                        seriesInferred = it.seriesInferred,
                        seriesConfirmed = it.seriesConfirmed,
                        merchInferred = it.merchInferred,
                        merchConfirmed = it.merchConfirmed,
                        isBrandNew = it.isBrandNew,
                        images = it.images,
                        isTempImages = false, // Will be set during database processing
                    )
                )
            }
        }
        database.transaction {
            alleyChangelog.artistDiffs[DataYear.ANIME_NYC_2026]?.forEach {
                database.mutationQueries.insertArtistEntryAnimeNyc2026Changelog(
                    ArtistEntryAnimeNyc2026Changelog(
                        artistId = it.artistId,
                        date = it.date.toString(),
                        booth = it.booth,
                        name = it.name,
                        seriesInferred = it.seriesInferred,
                        seriesConfirmed = it.seriesConfirmed,
                        merchInferred = it.merchInferred,
                        merchConfirmed = it.merchConfirmed,
                        isBrandNew = it.isBrandNew,
                        images = it.images,
                        isTempImages = false, // Will be set during database processing
                    )
                )
            }
        }

        database.transaction {
            alleyChangelog.rallyDiffs[DataYear.ANIME_EXPO_2026]?.forEach {
                database.mutationQueries.insertStampRallyChangelogEntry(
                    StampRallyChangelogEntry(
                        stampRallyId = it.stampRallyId,
                        dataYear = DataYear.ANIME_EXPO_2026,
                        date = it.date.toString(),
                        images = it.images,
                        isBrandNew = it.isBrandNew,
                    )
                )
            }
        }

        database.transaction {
            alleyChangelog.seriesDiffs.forEach {
                database.mutationQueries.insertSeriesChangelog(
                    SeriesEntryChangelog(
                        date = it.date.toString(),
                        seriesIds = it.seriesIds,
                    )
                )
            }
        }

        database.transaction {
            alleyChangelog.merchDiffs.forEach {
                database.mutationQueries.insertMerchChangelog(
                    MerchEntryChangelog(
                        date = it.date.toString(),
                        merchIds = it.merchIds,
                    )
                )
            }
        }

        return alleyChangelog
    }

    private suspend fun fixLegacyArtistImages(
        database: BuildLogicEditDatabase,
        imageCacheDir: File,
    ) {
        // TODO: Consolidate
        listOf(
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2025
        )
            .forEach { dataYear ->
                fixLegacyArtistImages(
                    database = database,
                    imageCacheDir = imageCacheDir,
                    dataYear = dataYear,
                    entries = database.artistEntryQueries.getAllEntries(dataYear),
                    artistId = { it.id.toString() },
                    updateImages = { images, id ->
                        updateArtistEntryImages(images, dataYear, Uuid.parse(id))
                    },
                )
            }

        listOf(DataYear.ANIME_EXPO_2023, DataYear.ANIME_EXPO_2024, DataYear.ANIME_EXPO_2025)
            .forEach { dataYear ->
                fixLegacyRallyImages(
                    database = database,
                    imageCacheDir = imageCacheDir,
                    dataYear = dataYear,
                    entries = database.stampRallyEntryQueries.getAllEntries(dataYear),
                    rallyId = { it.id },
                    hostTable = { it.tables.first() },
                    fandom = { it.fandom },
                    updateImages = MutationQueries::updateStampRallyImages,
                )
            }
    }

    // Coerces AniList series source to match their aniListType
    private fun fixLegacySeriesSources(database: BuildLogicEditDatabase) {
        database.transaction {
            val mutationQueries = database.mutationQueries
            database.seriesQueries.getSeries()
                .executeAsList()
                .asSequence()
                .filter { it.source == null || it.source == SeriesSource.NONE }
                .filter { it.aniListType != null }
                .forEach {
                    val source = when (it.aniListType) {
                        "ANIME" -> SeriesSource.ANIME
                        "MANGA" -> SeriesSource.MANGA
                        else -> it.source
                    }
                    val series = SeriesEntry(
                        id = it.id,
                        uuid = it.uuid,
                        notes = it.notes,
                        aniListId = it.aniListId,
                        aniListType = it.aniListType,
                        wikipediaId = it.wikipediaId,
                        tmdbId = it.tmdbId,
                        tmdbType = it.tmdbType,
                        steamId = it.steamId,
                        steamImagePath = it.steamImagePath,
                        openLibraryId = it.openLibraryId,
                        source = source,
                        titlePreferred = it.titlePreferred,
                        titleEnglish = it.titleEnglish,
                        titleRomaji = it.titleRomaji,
                        titleNative = it.titleNative,
                        synonyms = it.synonyms,
                        link = it.link,
                        inferred2024 = it.inferred2024,
                        inferred2025 = it.inferred2025,
                        inferredAnimeExpo2026 = it.inferredAnimeExpo2026,
                        inferredAnimeNyc2024 = it.inferredAnimeNyc2024,
                        inferredAnimeNyc2025 = it.inferredAnimeNyc2025,
                        inferredAnimeNyc2026 = it.inferredAnimeNyc2026,
                        confirmed2024 = it.confirmed2024,
                        confirmed2025 = it.confirmed2025,
                        confirmedAnimeExpo2026 = it.confirmedAnimeExpo2026,
                        confirmedAnimeNyc2024 = it.confirmedAnimeNyc2024,
                        confirmedAnimeNyc2025 = it.confirmedAnimeNyc2025,
                        confirmedAnimeNyc2026 = it.confirmedAnimeNyc2026,
                    )
                    mutationQueries.insertSeries(series)
                }
        }
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
                val (width, height, _) = parseScaledImageWidthHeight(
                    logger = logger,
                    imageCacheDir = imageCacheDir,
                    file = it,
                    resizeTarget = ImageUtils.NORMAL_RESIZE_TARGET,
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
            else -> id
        }
        val folder = when (year) {
            DataYear.ANIME_EXPO_2023 -> inputImagesAnimeExpo2023
            DataYear.ANIME_EXPO_2024 -> inputImagesAnimeExpo2024
            DataYear.ANIME_EXPO_2025 -> inputImagesAnimeExpo2025
            DataYear.ANIME_EXPO_2026 -> return emptyList()
            DataYear.ANIME_NYC_2024 -> return emptyList()
            DataYear.ANIME_NYC_2025 -> inputImagesAnimeNyc2025
            DataYear.ANIME_NYC_2026 -> return emptyList()
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
                val (width, height, _) = parseScaledImageWidthHeight(
                    logger = logger,
                    imageCacheDir = imageCacheDir,
                    file = it,
                    resizeTarget = ImageUtils.NORMAL_RESIZE_TARGET,
                )
                DatabaseImage("${folder.name}/${it.name}", width, height)
            }
    }

    private data class ArtistTagConnections(
        val series: MutableMap<Pair<Long, Long>, ArtistSeriesConnection> = mutableMapOf(),
        val merch: MutableMap<Pair<Long, String>, ArtistMerchConnection> = mutableMapOf(),
    ) {
        fun addConnection(connection: ArtistSeriesConnection) {
            val idPair = connection.let { it.artistRowId to it.seriesRowId }
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
            val idPair = connection.let { it.artistRowId to it.merchId }
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

    private suspend fun buildArtistConnections(
        driver: SqlDriver,
        database: BuildLogicEditDatabase,
    ): ArtistTagConnections {
        val connections = ArtistTagConnections()

        val seriesIdsToRowIds = database.mutationQueries.getSeries().executeAsList()
            .associate { it.id to it.rowid }

        suspend fun executeQuery(tableName: String) = driver.executeQuery(
            identifier = null,
            sql = """
                SELECT rowid, seriesInferred, seriesConfirmed, merchInferred, merchConfirmed
                FROM $tableName;
            """.trimIndent(),
            mapper = { it.toArtistConnectionDataList(seriesIdsToRowIds) },
            parameters = 0,
        ).await()

        DataYear.entries.forEach { dataYear ->
            if (dataYear != DataYear.ANIME_NYC_2026) {
                return@forEach
            }
            executeQuery(dataYear.artistTableName).forEach {
                connections.addArtistConnections(dataYear, it)
            }
        }

        database.mutationQueries.getArtistEntries().executeAsList().forEach {
            connections.addArtistConnections(
                it.dataYear,
                ArtistConnectionData(
                    artistRowId = it.rowid,
                    seriesInferred = it.seriesInferred.mapNotNull { seriesIdsToRowIds[it] },
                    seriesConfirmed = it.seriesConfirmed.mapNotNull { seriesIdsToRowIds[it] },
                    merchInferred = it.merchInferred,
                    merchConfirmed = it.merchConfirmed,
                )
            )
        }

        return connections
    }

    private fun ArtistTagConnections.addArtistConnections(
        dataYear: DataYear,
        data: ArtistConnectionData,
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
            DataYear.ANIME_NYC_2026 -> TagYearFlag.getFlags(hasAnimeNyc2026Inferred = true) to
                    TagYearFlag.getFlags(hasAnimeNyc2026Confirmed = true)
        }
        val artistRowId = data.artistRowId
        data.seriesInferred.forEach {
            addConnection(
                ArtistSeriesConnection(
                    artistRowId = artistRowId,
                    seriesRowId = it,
                    yearFlags = inferredFlag
                )
            )
        }
        data.seriesConfirmed.forEach {
            addConnection(
                ArtistSeriesConnection(
                    artistRowId = artistRowId,
                    seriesRowId = it,
                    yearFlags = confirmedFlag,
                )
            )
        }

        data.merchInferred.forEach {
            addConnection(
                ArtistMerchConnection(
                    artistRowId = artistRowId,
                    merchId = it,
                    yearFlags = inferredFlag
                )
            )
        }
        data.merchConfirmed.forEach {
            addConnection(
                ArtistMerchConnection(
                    artistRowId = artistRowId,
                    merchId = it,
                    yearFlags = confirmedFlag
                )
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
                DataYear.ANIME_NYC_2026 -> mutationQueries.getAllArtistEntryAnimeNyc2026()
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id)
                                ?: return@forEach
                        mutationQueries.updateArtistEntryAnimeNyc2026(
                            artist.copy(
                                images = fallbackImages,
                                fallbackImageYear = fallbackImagesYear,
                            )
                        )
                    }
                else -> mutationQueries.getArtistEntriesByYear(year)
                    .executeAsList()
                    .forEach { artist ->
                        if (artist.images.isNotEmpty()) return@forEach
                        val (fallbackImagesYear, fallbackImages) =
                            mutationQueries.getFallbackImages(year, artist.id.toString())
                                ?: return@forEach
                        mutationQueries.updateArtistEntry(
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
        val artistConnections = mutableListOf<StampRallyArtistConnection>()
        val seriesConnections = mutableListOf<StampRallySeriesConnection>()
        val merchConnections = mutableListOf<StampRallyMerchConnection>()
        val prizeMerchConnections = mutableListOf<StampRallyPrizeMerchConnection>()
        database.mutationQueries.getStampRallyEntries().executeAsList().forEach {
            val stampRallyRowId = it.rowid
            val dataYear = it.dataYear
            artistConnections += it.tables
                .mapNotNull {
                    database.mutationQueries.getRowIdsByBooth(dataYear, it)
                        .executeAsList()
                        .firstOrNull()
                }
                .map {
                    StampRallyArtistConnection(
                        stampRallyRowId = stampRallyRowId,
                        artistRowId = it
                    )
                }
            seriesConnections += it.series
                .map {
                    StampRallySeriesConnection(
                        stampRallyRowId = stampRallyRowId,
                        seriesId = it,
                        dataYear = dataYear,
                    )
                }
            merchConnections += it.merch
                .map {
                    StampRallyMerchConnection(
                        stampRallyRowId = stampRallyRowId,
                        merchId = it,
                        dataYear = dataYear,
                    )
                }
            prizeMerchConnections += it.prizeMerch
                ?.map {
                    StampRallyPrizeMerchConnection(
                        stampRallyRowId = stampRallyRowId,
                        merchId = it,
                        dataYear = dataYear,
                    )
                }
                .orEmpty()
        }

        database.transaction {
            val mutationQueries = database.mutationQueries
            artistConnections.forEach(mutationQueries::insertArtistConnection)
            seriesConnections.forEach(mutationQueries::insertStampRallySeriesConnection)
            merchConnections.forEach(mutationQueries::insertStampRallyMerchConnection)
            prizeMerchConnections.forEach(mutationQueries::insertStampRallyPrizeMerchConnection)
        }
    }

    private fun updateSeriesInferredConfirmedCounts(
        database: BuildLogicEditDatabase,
        artistTagConnections: ArtistTagConnections,
    ) {
        database.mutationQueries.getSeries().executeAsList().forEach {
            val seriesRowId = it.rowid
            val connections =
                artistTagConnections.series.filter { it.value.seriesRowId == seriesRowId }

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
            val inferredAnimeNyc2026 = connections.count {
                TagYearFlag.hasFlag(
                    it.value.yearFlags,
                    TagYearFlag.ANIME_NYC_2026_INFERRED
                )
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
            val confirmedAnimeNyc2026 = connections.count {
                TagYearFlag.hasFlag(
                    it.value.yearFlags,
                    TagYearFlag.ANIME_NYC_2026_CONFIRMED
                )
            }

            database.mutationQueries.updateSeriesTagCounts(
                inferredAnimeExpo2024 = inferredAnimeExpo2024.toLong(),
                inferredAnimeExpo2025 = inferredAnimeExpo2025.toLong(),
                inferredAnimeExpo2026 = inferredAnimeExpo2026.toLong(),
                inferredAnimeNyc2024 = inferredAnimeNyc2024.toLong(),
                inferredAnimeNyc2025 = inferredAnimeNyc2025.toLong(),
                inferredAnimeNyc2026 = inferredAnimeNyc2026.toLong(),
                confirmedAnimeExpo2024 = confirmedAnimeExpo2024.toLong(),
                confirmedAnimeExpo2025 = confirmedAnimeExpo2025.toLong(),
                confirmedAnimeExpo2026 = confirmedAnimeExpo2026.toLong(),
                confirmedAnimeNyc2024 = confirmedAnimeNyc2024.toLong(),
                confirmedAnimeNyc2025 = confirmedAnimeNyc2025.toLong(),
                confirmedAnimeNyc2026 = confirmedAnimeNyc2026.toLong(),
                id = it.id,
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

    private fun checkLinks(database: BuildLogicEditDatabase) {
        database.artistEntryAnimeNyc2026Queries.getAllEntries()
            .executeAsList()
            .forEach { artist ->
                val badLinkPrefix = "Bad link for ${artist.booth} ${artist.name}"
                val allLinks =
                    artist.socialLinks + artist.storeLinks + artist.portfolioLinks + artist.catalogLinks
                allLinks.filter { it.contains("?") }.forEach {
                    logger.error("$badLinkPrefix, contains query: $it")
                }

                val groupedLinks =
                    allLinks.groupBy { Link.parse(it)?.let { it.type to it.identifier } }
                groupedLinks
                    .filterKeys { it != null }
                    .filterValues { it.size > 1 }
                    .plus(
                        groupedLinks[null]
                            ?.groupBy { it }
                            ?.filterValues { it.size > 1 }
                            .orEmpty()
                    )
                    .forEach {
                        logger.error("$badLinkPrefix, duplicate: ${it.value.first()}")
                    }

                if (artist.catalogLinks.isNotEmpty() && artist.images.isEmpty()) {
                    logger.error("Bad catalog link for ${artist.name}, no images recorded")
                }

                val badSocialLinks = artist.socialLinks.filter {
                    val type = Link.parse(it)?.type
                    type != null &&
                            (type.category != LinkCategory.SOCIALS &&
                                    type.category != LinkCategory.SUPPORT)
                }

                val badStoreLinks = artist.storeLinks.filter {
                    val type = Link.parse(it)?.type
                    type != null &&
                            (type.category != LinkCategory.STORES &&
                                    !it.contains("shop", ignoreCase = true))
                }

                val badPortfolioLinks = artist.portfolioLinks.filter {
                    val type = Link.parse(it)?.type
                    type != null && type.category != LinkCategory.PORTFOLIOS
                }

                (badSocialLinks + badStoreLinks + badPortfolioLinks).forEach {
                    logger.error("$badLinkPrefix, wrong category: $it")
                }
            }
    }

    private fun SqlCursor.toArtistConnectionDataList(seriesIdsToRowIds: Map<String, Long>) =
        QueryResult.Value(
            buildList {
                while (next().value) {
                    add(
                        ArtistConnectionData(
                            artistRowId = getLong(0)!!,
                            seriesInferred = getString(1)
                                ?.let { Json.decodeFromString<List<String>>(it) }
                                ?.mapNotNull { seriesIdsToRowIds[it] }
                                .orEmpty(),
                            seriesConfirmed = getString(2)
                                ?.let { Json.decodeFromString<List<String>>(it) }
                                ?.mapNotNull { seriesIdsToRowIds[it] }
                                .orEmpty(),
                            merchInferred = getString(3)
                                ?.let { Json.decodeFromString<List<String>>(it) }
                                .orEmpty(),
                            merchConfirmed = getString(4)
                                ?.let { Json.decodeFromString<List<String>>(it) }
                                .orEmpty(),
                        )
                    )
                }
            }
        )

    // Copied from ArtistInference since it isn't accessible here
    private class ArtistInferenceProvider(
        private val database: BuildLogicEditDatabase,
        private val artistId: String,
    ) {
        private fun getEntry(dataYear: DataYear) =
            database.artistEntryQueries.getEntry(dataYear, Uuid.parse(artistId))
                .executeAsOneOrNull()

        private fun cascadeAll(
            value: GetEntry.() -> List<String>,
        ): List<String> = listOf(
            DataYear.ANIME_EXPO_2026,
            DataYear.ANIME_NYC_2026,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_NYC_2025,
            DataYear.ANIME_NYC_2024,
        ).firstNotNullOfOrNull {
            getEntry(it)?.value()?.ifEmpty { null }
        }.orEmpty()

        val socialLinks
            get() = cascadeAll { socialLinks }

        val storeLinks
            get() = cascadeAll { storeLinks }

        val seriesInferred
            get() = cascadeAll { seriesConfirmed.ifEmpty { seriesInferred } }

        val merchInferred
            get() = cascadeAll { merchConfirmed.ifEmpty { merchInferred } }
    }

    private data class ArtistImages(
        val catalogImages: List<FinalImage>,
        val tempImages: List<FinalImage>,
        val customProfileImage: FinalImage?,
        val embedProfileImage: DatabaseImage?,
        val largeEmbeds: Map<String, DatabaseImage>?,
    )

    private data class FinalImage(
        val original: DatabaseImage,
        val final: DatabaseImage,
        val imageToCompress: ImageToCompress,
    )

    private data class ImageToCompress(
        val path: String,
        val imageFile: File,
        val resized: Boolean,
        val width: Int,
        val height: Int,
    )

    private data class ArtistConnectionData(
        val artistRowId: Long,
        val seriesInferred: List<Long>,
        val seriesConfirmed: List<Long>,
        val merchInferred: List<String>,
        val merchConfirmed: List<String>,
    )

    companion object {
        private const val EMBED_MIN_DIMENSION = 300
    }
}
