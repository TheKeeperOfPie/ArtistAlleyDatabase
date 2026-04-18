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
import javax.inject.Inject
import kotlin.time.Instant
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
        val beforeSnapshotFilteredFile = temporaryDir.resolve("before.sql")
        val afterSnapshotFilteredFile = temporaryDir.resolve("after.sql")
        val beforeDatabaseFile = temporaryDir.resolve("before.sqlite")
        val afterDatabaseFile = temporaryDir.resolve("after.sqlite")
        val diffs = snapshotsDirectory.get().asFileTree.files
            .sortedBy {
                it.nameWithoutExtension
                    .replace("_", ":")
                    .replace(";", ":")
                    .let(Instant::parse)
            }
            .windowed(size = 2, step = 1)
            .flatMap { (beforeSnapshot, afterSnapshot) ->
                try {
                    fun verifyDelete(file: File) {
                        if (!file.delete()) {
                            logger.error("Failed to delete ${file.absolutePath}")
                        }
                    }
                    listOf(
                        beforeSnapshotFilteredFile,
                        afterSnapshotFilteredFile,
                        beforeDatabaseFile,
                        afterDatabaseFile,
                    ).forEach(::verifyDelete)

                    // First, create and immediately close the databases to initialize the schemas
                    Utils.createEditDatabase(beforeDatabaseFile).first.close()
                    Utils.createEditDatabase(afterDatabaseFile).first.close()

                    fun filterSnapshot(source: File, target: File) {
                        target.writer().use { writer ->
                            source.useLines {
                                it.filter { it.contains("\"artistEntryAnimeExpo2026\"") }
                                    .filterNot { it.contains("11111111-1111-1111-1111-111111111111") }
                                    .forEach(writer::appendLine)
                            }
                        }
                    }

                    filterSnapshot(beforeSnapshot, beforeSnapshotFilteredFile)
                    filterSnapshot(afterSnapshot, afterSnapshotFilteredFile)

                    if (!Utils.readSqlFile(beforeDatabaseFile, beforeSnapshotFilteredFile)) {
                        logger.error("Failed to apply before ${beforeSnapshot.absolutePath}")
                        return@flatMap emptyList()
                    }
                    if (!Utils.readSqlFile(afterDatabaseFile, afterSnapshotFilteredFile)) {
                        logger.error("Failed to apply after ${afterSnapshot.absolutePath}")
                        return@flatMap emptyList()
                    }
                    val beforeDatabase =
                        Utils.createEditDatabase(beforeDatabaseFile).second.artistEntryAnimeExpo2026Queries.getAllEntries()
                            .executeAsList()
                    val afterDatabase =
                        Utils.createEditDatabase(afterDatabaseFile).second.artistEntryAnimeExpo2026Queries.getAllEntries()
                            .executeAsList()

                    val date = afterSnapshot.nameWithoutExtension
                        .replace("_", ":")
                        .replace(";", ":")
                        .let(Instant::parse)
                        .toLocalDateTime(TimeZone.UTC)
                        .date

                    afterDatabase
                        .map { afterArtist -> beforeDatabase.find { it.id == afterArtist.id } to afterArtist }
                        .mapNotNull { (beforeArtist, afterArtist) ->
                            val artistId = Uuid.parse(afterArtist.id)
                            val seriesInferred = afterArtist.seriesInferred.toMutableSet()
                            val seriesConfirmed = afterArtist.seriesConfirmed.toMutableSet()
                            val merchInferred = afterArtist.merchInferred.toMutableSet()
                            val merchConfirmed = afterArtist.merchConfirmed.toMutableSet()
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
                } catch (t: Throwable) {
                    logger.error("Failed to parse $beforeSnapshot, $afterSnapshot", t)
                    throw t
                }
            }

        outputFile.get().asFile.outputStream().use {
            Json.encodeToStream(diffs, it)
        }
    }
}
