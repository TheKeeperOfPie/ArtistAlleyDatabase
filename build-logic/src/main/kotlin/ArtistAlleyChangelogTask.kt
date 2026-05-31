
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
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
                        val diffs = trackStage("ArtistChangelogDiffs") {
                            snapshotsDirectory.get().asFileTree.files
                                .sortedBy {
                                    it.nameWithoutExtension
                                        .replace("_", ":")
                                        .replace(";", ":")
                                        .let(Instant::parse)
                                }
                                .map { file ->
                                    async {
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
                                            return@async null
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
                                        SnapshotData(
                                            timestamp = instant,
                                            date = date,
                                            artists = artists,
                                            rallies = rallies,
                                        )
                                    }
                                }
                                .awaitAll()
                                .also {
                                    if (it.any { it == null }) {
                                        throw IllegalStateException("Failed to read snapshot files")
                                    }
                                }
                                .filterNotNull()
                                .fold(Diffs()) { before, current ->
                                    val artistDiffs = diffArtists(
                                        lastEditTimes = artistLastEditTimes,
                                        before = before,
                                        current = current,
                                    )
                                    val rallyDiffs = diffRallies(
                                        lastEditTimes = rallyLastEditTimes,
                                        before = before,
                                        current = current,
                                    )
                                    before.copy(
                                        artistDiffs = before.artistDiffs + artistDiffs,
                                        rallyDiffs = before.rallyDiffs + rallyDiffs,
                                        latestArtists = current.artists,
                                        latestRallies = current.rallies,
                                    )
                                }
                        }

                        outputFile.get().asFile.outputStream().use {
                            Json.encodeToStream(
                                value = AlleyChangelog(
                                    artistDiffs = diffs.artistDiffs,
                                    artistLastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to artistLastEditTimes),
                                    rallyDiffs = diffs.rallyDiffs,
                                    rallyLastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to rallyLastEditTimes),
                                ),
                                stream = it,
                            )
                        }
                    }
                }
        }
    }

    private fun filterSnapshot(source: File, target: File) {
        target.writer().use { writer ->
            source.useLines {
                it.filter { line -> filteredTableNames.any { it in line } }
                    .filterNot { it.contains("11111111-1111-1111-1111-111111111111") }
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
                val images = afterArtist.images.toMutableList()
                if (beforeArtist != null) {
                    seriesInferred -= beforeArtist.seriesInferred.toSet()
                    seriesConfirmed -= beforeArtist.seriesConfirmed.toSet()
                    merchInferred -= beforeArtist.merchInferred.toSet()
                    merchConfirmed -= beforeArtist.merchConfirmed.toSet()
                    images -= beforeArtist.images.toSet()
                }
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
        before: Diffs,
        current: SnapshotData,
    ): List<StampRallyDiff> {
        val timestamp = current.timestamp
        val date = current.date
        val rallies = current.rallies
        val beforeRallies = before.latestRallies
        return rallies
            .map { afterRally -> beforeRallies.find { it.id == afterRally.id } to afterRally }
            .mapNotNull { (beforeRally, afterRally) ->
                val rallyId = Uuid.parse(afterRally.id)
                if (beforeRally != afterRally) {
                    lastEditTimes[rallyId] = timestamp
                }
                val images = afterRally.images - beforeRally?.images?.toSet().orEmpty()
                if (beforeRally == null) {
                    StampRallyDiff(
                        stampRallyId = rallyId,
                        date = date,
                        name = afterRally.fandom,
                        images = afterRally.images,
                    )
                } else if (images.isNotEmpty()) {
                    StampRallyDiff(
                        stampRallyId = rallyId,
                        date = date,
                        name = afterRally.fandom,
                        images = images,
                    )
                } else {
                    null
                }
            }
    }

    private data class SnapshotData(
        val timestamp: Instant,
        val date: LocalDate,
        val artists: List<ArtistEntryAnimeExpo2026>,
        val rallies: List<StampRallyEntryAnimeExpo2026>,
    )

    private data class Diffs(
        val artistDiffs: List<ArtistDiff> = emptyList(),
        val rallyDiffs: List<StampRallyDiff> = emptyList(),
        val latestArtists: List<ArtistEntryAnimeExpo2026> = emptyList(),
        val latestRallies: List<StampRallyEntryAnimeExpo2026> = emptyList(),
    )
}
