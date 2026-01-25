
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.uuid.Uuid

@CacheableTask
abstract class ArtistAlleyChangelogTask : DefaultTask() {

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val snapshotsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        snapshotsDirectory.convention(layout.projectDirectory.dir("inputs/snapshots/animeExpo2026"))
        outputFile.convention(layout.buildDirectory.file("generated/changelog.json"))
    }

    @TaskAction
    fun process() {
        val before = temporaryDir.resolve("before.sqlite")
        val after = temporaryDir.resolve("after.sqlite")
        val diffs  = snapshotsDirectory.get().asFileTree.files
            .windowed(size = 2, step = 1)
            .flatMap { (beforeSnapshot, afterSnapshot) ->
                before.delete()
                after.delete()
                Utils.readSqlFile(before, beforeSnapshot)
                Utils.readSqlFile(after, afterSnapshot)
                val beforeDatabase =
                    Utils.createDatabase(before).second.artistEntryAnimeExpo2026Queries.getAllEntries()
                        .executeAsList()
                val afterDatabase =
                    Utils.createDatabase(after).second.artistEntryAnimeExpo2026Queries.getAllEntries()
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
            }

        outputFile.get().asFile.outputStream().use {
            Json.encodeToStream(diffs, it)
        }
    }
}
