
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.io.nameWithoutExtension
import kotlin.io.outputStream
import kotlin.io.resolve
import kotlin.io.useLines
import kotlin.io.writer
import kotlin.time.Instant
import kotlin.use
import kotlin.uuid.Uuid

@CacheableTask
abstract class ArtistAlleyChangelogTask : DefaultTask() {

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val legacySeriesFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val legacyMerchFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val snapshotsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        snapshotsDirectory.convention(layout.projectDirectory.dir("inputs/snapshots/animeExpo2026/edit"))
        outputFile.convention(layout.buildDirectory.file("generated/changelog.json"))
    }

    private val filteredTableNames = listOf(
        "artistEntryAnimeExpo2026",
        "stampRallyEntryAnimeExpo2026",
        "seriesEntry",
        "merchEntry",
    ).map { "\"$it\"" }.toSet()

    @TaskAction
    fun process() {
        if (!snapshotsDirectory.get().asFile.exists()) return

        runBlocking {
            @Suppress("NewApi")
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
                .use {
                    withContext(it.asCoroutineDispatcher()) {
                        val artistLastEditTimes = mutableMapOf<Uuid, Instant>()
                        val rallyLastEditTimes = mutableMapOf<Uuid, Instant>()

                        val legacySeriesIds = loadLegacySeriesIds()
                        val legacyMerchIds = loadLegacyMerchIds()

                        val snapshotFiles = snapshotsDirectory.get().asFileTree.files
                            .sortedBy {
                                it.nameWithoutExtension
                                    .replace("_", ":")
                                    .replace(";", ":")
                                    .let(Instant::parse)
                            }

                        val latestArtistIds = mutableSetOf<Uuid>()
                        val latestRallyIds = mutableSetOf<Uuid>()
                        val latestImages = mutableSetOf<DatabaseImage>()
                        val latestSnapshot = snapshotFiles.lastOrNull()
                            ?.let(::readSnapshot)
                        if (latestSnapshot != null) {
                            latestArtistIds += latestSnapshot.artists.map { Uuid.parse(it.id) }
                            latestRallyIds += latestSnapshot.rallies.map { Uuid.parse(it.id) }
                            latestImages += latestSnapshot.artists.flatMap { it.images }
                            latestImages += latestSnapshot.rallies.flatMap { it.images }
                        }

                        val diffs = trackStage("ArtistChangelogDiffs") {
                            snapshotFiles
                                .map { async { readSnapshot(it) } }
                                .awaitAll()
                                .also {
                                    if (it.any { it == null }) {
                                        throw IllegalStateException("Failed to read snapshot files")
                                    }
                                }
                                .filterNotNull()
                                .fold(
                                    Diffs(
                                        latestSeriesIds = legacySeriesIds,
                                        latestMerchIds = legacyMerchIds,
                                    )
                                ) { before, current ->
                                    val artistDiffs = diffArtists(
                                        lastEditTimes = artistLastEditTimes,
                                        latestArtistIds = latestArtistIds,
                                        latestImages = latestImages,
                                        before = before,
                                        current = current,
                                    )
                                    val rallyDiffs = diffRallies(
                                        lastEditTimes = rallyLastEditTimes,
                                        latestRallyIds = latestRallyIds,
                                        latestImages = latestImages,
                                        before = before,
                                        current = current,
                                    )
                                    val seriesDiff = diffSeries(
                                        before = before,
                                        current = current,
                                    )
                                    val merchDiff = diffMerch(
                                        before = before,
                                        current = current,
                                    )

                                    before.copy(
                                        artistDiffs = before.artistDiffs + artistDiffs,
                                        rallyDiffs = before.rallyDiffs + rallyDiffs,
                                        seriesDiffs = before.seriesDiffs + listOfNotNull(seriesDiff),
                                        merchDiffs = before.merchDiffs + listOfNotNull(merchDiff),
                                        latestArtists = current.artists,
                                        latestRallies = current.rallies,
                                        latestSeriesIds = before.latestSeriesIds + current.seriesIds,
                                        latestMerchIds = before.latestMerchIds + current.merchIds,
                                    )
                                }
                        }

                        outputFile.get().asFile.outputStream().use {
                            // TODO: Filter to values that still exist in the latest snapshot, to
                            //  ignore deleted entries
                            Json.encodeToStream(
                                value = AlleyChangelog(
                                    artistDiffs = diffs.artistDiffs,
                                    artistLastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to artistLastEditTimes),
                                    rallyDiffs = diffs.rallyDiffs,
                                    rallyLastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to rallyLastEditTimes),
                                    seriesDiffs = diffs.seriesDiffs,
                                    merchDiffs = diffs.merchDiffs,
                                ),
                                stream = it,
                            )
                        }
                    }
                }
        }
    }

    private fun readSnapshot(file: File): SnapshotData? {
        val snapshotFilteredFile =
            temporaryDir.resolve("database-${file.name}.sql")
        val databaseFile =
            temporaryDir.resolve("database-${file.name}.sqlite")
        listOf(
            snapshotFilteredFile,
            databaseFile
        ).forEach(::verifyDelete)

        // First, create and immediately close the databases to initialize the schemas
        Utils.createEditDatabase(databaseFile).first.close()
        filterSnapshot(file, snapshotFilteredFile)
        if (!Utils.readSqlFile(
                databaseFile,
                snapshotFilteredFile
            )
        ) {
            logger.error("Failed to read ${file.absolutePath}")
            return null
        }

        val instant = file.nameWithoutExtension
            .replace("_", ":")
            .replace(";", ":")
            .let(Instant::parse)
        val date = instant
            .toLocalDateTime(TimeZone.UTC)
            .date
        val database = Utils.createEditDatabase(databaseFile)
        val mutationQueries = database.second.mutationQueries
        val artists =
            mutationQueries.getAllArtistEntryAnimeExpo2026()
                .executeAsList()
        val rallies =
            mutationQueries.getAllStampRallyEntryAnimeExpo2026()
                .executeAsList()
        val seriesIds = mutationQueries.getSeries()
            .executeAsList()
            .map { it.id }
            .toSet()
        val merchIds = mutationQueries.getMerch()
            .executeAsList()
            .map { it.name }
            .toSet()
        return SnapshotData(
            timestamp = instant,
            date = date,
            artists = artists,
            rallies = rallies,
            seriesIds = seriesIds,
            merchIds = merchIds,
        )
    }

    private fun loadLegacySeriesIds(): Set<String> {
        val seriesDatabaseFile =
            temporaryDir.resolve("database-series-legacy.sqlite")
                .apply(::verifyDelete)
        val seriesLegacySql = legacySeriesFile.get().asFile
        // First, create and immediately close the databases to initialize the schemas
        Utils.createEditDatabase(seriesDatabaseFile).first.close()
        if (!Utils.readSqlFile(seriesDatabaseFile, seriesLegacySql)) {
            throw IllegalStateException("Failed to read ${seriesLegacySql.absolutePath}")
        }
        val seriesDatabase = Utils.createEditDatabase(seriesDatabaseFile).second
        return seriesDatabase.mutationQueries.getSeries().executeAsList().map { it.id }.toSet()
    }

    private fun loadLegacyMerchIds(): Set<String> {
        val merchDatabaseFile = temporaryDir.resolve("database-merch-legacy.sqlite")
            .apply(::verifyDelete)
        val merchLegacySql = legacyMerchFile.get().asFile
        // First, create and immediately close the databases to initialize the schemas
        Utils.createEditDatabase(merchDatabaseFile).first.close()
        if (!Utils.readSqlFile(merchDatabaseFile, merchLegacySql)) {
            throw IllegalStateException("Failed to read ${merchLegacySql.absolutePath}")
        }
        val merchDatabase = Utils.createEditDatabase(merchDatabaseFile).second
        return merchDatabase.mutationQueries.getMerch().executeAsList().map { it.name } .toSet()
    }

    private fun filterSnapshot(source: File, target: File) {
        target.writer().use { writer ->
            source.useLines {
                it.filter { line -> filteredTableNames.any { it in line } }
                    .filterNot { it.contains("11111111-1111-1111-1111-111111111111") }
                    .filterNot { it.contains("22222222-2222-2222-2222-222222222222") }
                    .forEach(writer::appendLine)
            }
        }
    }

    private fun verifyDelete(file: File) {
        if (file.exists() && !file.delete()) {
            throw IllegalStateException("Failed to delete ${file.absolutePath}")
        }
    }

    private fun diffArtists(
        lastEditTimes: MutableMap<Uuid, Instant>,
        latestArtistIds: Set<Uuid>,
        latestImages: Set<DatabaseImage>,
        before: Diffs,
        current: SnapshotData,
    ): List<ArtistDiff> {
        val timestamp = current.timestamp
        val date = current.date
        val artists = current.artists
        val beforeArtists = before.latestArtists
        return artists
            .map { afterArtist -> beforeArtists.find { it.id == afterArtist.id } to afterArtist }
            .mapNotNull { (beforeArtist, afterArtist) ->
                val artistId = Uuid.parse(afterArtist.id)
                if (artistId !in latestArtistIds) return@mapNotNull null
                if (beforeArtist != afterArtist) {
                    lastEditTimes[artistId] = timestamp
                }
                val seriesInferred =
                    afterArtist.seriesInferred.toMutableSet()
                val seriesConfirmed =
                    afterArtist.seriesConfirmed.toMutableSet()
                val merchInferred =
                    afterArtist.merchInferred.toMutableSet()
                val merchConfirmed =
                    afterArtist.merchConfirmed.toMutableSet()
                val images = afterArtist.images
                    .takeIf { seriesConfirmed.isNotEmpty() || merchConfirmed.isNotEmpty() }
                    .orEmpty()
                    .toMutableList()
                if (beforeArtist != null) {
                    seriesInferred -= beforeArtist.seriesInferred.toSet()
                    seriesConfirmed -= beforeArtist.seriesConfirmed.toSet()
                    merchInferred -= beforeArtist.merchInferred.toSet()
                    merchConfirmed -= beforeArtist.merchConfirmed.toSet()
                    images -= beforeArtist.images.toSet()
                }
                images.retainAll(latestImages)
                if (beforeArtist != null && listOf(
                        seriesInferred,
                        seriesConfirmed,
                        merchInferred,
                        merchConfirmed,
                        images,
                    ).all { it.isEmpty() }
                ) {
                    return@mapNotNull null
                }

                ArtistDiff(
                    artistId = artistId,
                    date = date,
                    booth = afterArtist.booth?.ifEmpty { null },
                    name = afterArtist.name,
                    seriesInferred = seriesInferred.takeUnless { it.isEmpty() },
                    seriesConfirmed = seriesConfirmed.takeUnless { it.isEmpty() },
                    merchInferred = merchInferred.takeUnless { it.isEmpty() },
                    merchConfirmed = merchConfirmed.takeUnless { it.isEmpty() },
                    isBrandNew = beforeArtist == null,
                    images = images.ifEmpty { null },
                )
            }
    }

    private fun diffRallies(
        lastEditTimes: MutableMap<Uuid, Instant>,
        latestRallyIds: Set<Uuid>,
        latestImages: Set<DatabaseImage>,
        before: Diffs,
        current: SnapshotData,
    ): List<StampRallyDiff> {
        val timestamp = current.timestamp
        val date = current.date
        val rallies = current.rallies
        val beforeRallies = before.latestRallies
        val beforeRallyIds = beforeRallies.map { it.id }
        return rallies
            .map { afterRally -> beforeRallies.find { it.id == afterRally.id } to afterRally }
            .mapNotNull { (beforeRally, afterRally) ->
                val rallyId = Uuid.parse(afterRally.id)
                if (rallyId !in latestRallyIds) return@mapNotNull null
                if (beforeRally != afterRally) {
                    lastEditTimes[rallyId] = timestamp
                }
                val images = (afterRally.images - beforeRally?.images?.toSet().orEmpty()).toMutableList()
                images.retainAll(latestImages)
                val isBrandNew = rallyId.toString() !in beforeRallyIds
                if (beforeRally == null) {
                    StampRallyDiff(
                        stampRallyId = rallyId,
                        date = date,
                        name = afterRally.fandom,
                        images = afterRally.images,
                        isBrandNew = isBrandNew,
                    )
                } else if (images.isNotEmpty()) {
                    StampRallyDiff(
                        stampRallyId = rallyId,
                        date = date,
                        name = afterRally.fandom,
                        images = images,
                        isBrandNew = isBrandNew,
                    )
                } else {
                    null
                }
            }
    }

    private fun diffSeries(before: Diffs, current: SnapshotData): SeriesDiff? {
        val diff = current.seriesIds - before.latestSeriesIds
        if (diff.isEmpty()) return null
        return SeriesDiff(date = current.date, diff)
    }

    private fun diffMerch(before: Diffs, current: SnapshotData): MerchDiff? {
        val diff = current.merchIds - before.latestMerchIds
        if (diff.isEmpty()) return null
        return MerchDiff(date = current.date, diff)
    }

    private data class SnapshotData(
        val timestamp: Instant,
        val date: LocalDate,
        val artists: List<ArtistEntryAnimeExpo2026>,
        val rallies: List<StampRallyEntryAnimeExpo2026>,
        val seriesIds: Set<String>,
        val merchIds: Set<String>,
    )

    private data class Diffs(
        val artistDiffs: List<ArtistDiff> = emptyList(),
        val rallyDiffs: List<StampRallyDiff> = emptyList(),
        val seriesDiffs: List<SeriesDiff> = emptyList(),
        val merchDiffs: List<MerchDiff> = emptyList(),
        val latestArtists: List<ArtistEntryAnimeExpo2026> = emptyList(),
        val latestRallies: List<StampRallyEntryAnimeExpo2026> = emptyList(),
        val latestSeriesIds: Set<String> = emptySet(),
        val latestMerchIds: Set<String> = emptySet(),
    )
}
