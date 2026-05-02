
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    @TaskAction
    fun process() {
        if (!snapshotsDirectory.get().asFile.exists()) return

        runBlocking {
            @Suppress("NewApi")
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
                .use {
                    withContext(it.asCoroutineDispatcher()) {
                        val lastEditTimes = mutableMapOf<Uuid, Instant>()
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
                                        val artists =
                                            Utils.createEditDatabase(databaseFile).second.mutationQueries.getAllArtistEntryAnimeExpo2026()
                                                .executeAsList()
                                        Triple(instant, date, artists)
                                    }
                                }
                                .awaitAll()
                                .filterNotNull()
                                .fold(emptyList<ArtistDiff>() to emptyList<ArtistEntryAnimeExpo2026>()) { before, current ->
                                    val (instant, date, artists) = current
                                    val beforeArtists = before.second
                                    if (beforeArtists.isEmpty()) {
                                        return@fold before.first to artists
                                    }
                                    val diffs = before.first + artists
                                        .map { afterArtist -> beforeArtists.find { it.id == afterArtist.id } to afterArtist }
                                        .mapNotNull { (beforeArtist, afterArtist) ->
                                            val artistId = Uuid.parse(afterArtist.id)
                                            if (beforeArtist != afterArtist) {
                                                lastEditTimes[artistId] = instant
                                            }
                                            val seriesInferred =
                                                afterArtist.seriesInferred.toMutableSet()
                                            val seriesConfirmed =
                                                afterArtist.seriesConfirmed.toMutableSet()
                                            val merchInferred =
                                                afterArtist.merchInferred.toMutableSet()
                                            val merchConfirmed =
                                                afterArtist.merchConfirmed.toMutableSet()
                                            if (beforeArtist != null) {
                                                seriesInferred -= beforeArtist.seriesInferred.toSet()
                                                seriesConfirmed -= beforeArtist.seriesConfirmed.toSet()
                                                merchInferred -= beforeArtist.merchInferred.toSet()
                                                merchConfirmed -= beforeArtist.merchConfirmed.toSet()
                                            }
                                            if (beforeArtist != null && seriesInferred.isEmpty() && seriesConfirmed.isEmpty() &&
                                                merchInferred.isEmpty() && merchConfirmed.isEmpty()
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
                                            )
                                        }

                                    diffs to artists
                                }
                        }

                        outputFile.get().asFile.outputStream().use {
                            Json.encodeToStream(
                                value = ArtistChangelog(
                                    additions = diffs.first,
                                    lastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to lastEditTimes)
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
                it.filter { it.contains("\"artistEntryAnimeExpo2026\"") }
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
}
