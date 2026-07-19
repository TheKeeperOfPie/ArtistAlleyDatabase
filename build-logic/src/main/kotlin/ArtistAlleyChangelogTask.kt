import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeNyc2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.buildlogic.edit.MutationQueries
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    abstract val animeExpo2026SnapshotsDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val animeNyc2026SnapshotsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        animeExpo2026SnapshotsDirectory.convention(layout.projectDirectory.dir("inputs/snapshots/animeExpo2026/edit"))
        animeNyc2026SnapshotsDirectory.convention(layout.projectDirectory.dir("inputs/snapshots/animeNyc2026/edit"))
        outputFile.convention(layout.buildDirectory.file("generated/changelog.json"))
    }

    private val filteredTableNames = listOf(
        "artistEntryAnimeExpo2026",
        "stampRallyEntryAnimeExpo2026",
        "artistEntryAnimeNyc2026",
        "seriesEntry",
        "merchEntry",
    ).map { "\"$it\"" }.toSet()

    @TaskAction
    fun process() {
        runBlocking {
            @Suppress("NewApi")
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
                .use {
                    withContext(it.asCoroutineDispatcher()) {
                        val tagDiffs = parseTags()
                        val (
                            animeExpo2026ArtistLastEditTimes,
                            animeExpo2026RallyLastEditTimes,
                            animeExpo2026Diffs,
                        ) = parseAnimeExpo2026()
                        val (
                            animeNyc2026ArtistLastEditTimes,
                            animeNyc2026Diffs,
                        ) = parseAnimeNyc2026()

                        outputFile.get().asFile.outputStream().use {
                            // TODO: Filter to values that still exist in the latest snapshot, to
                            //  ignore deleted entries
                            Json.encodeToStream(
                                value = AlleyChangelog(
                                    artistDiffs = mapOf(
                                        DataYear.ANIME_EXPO_2026 to animeExpo2026Diffs.artistDiffs,
                                        DataYear.ANIME_NYC_2026 to animeNyc2026Diffs.artistDiffs,
                                    ),
                                    artistLastEditTimes = mapOf(
                                        DataYear.ANIME_EXPO_2026 to animeExpo2026ArtistLastEditTimes,
                                        DataYear.ANIME_NYC_2026 to animeNyc2026ArtistLastEditTimes,
                                    ),
                                    rallyDiffs = mapOf(DataYear.ANIME_EXPO_2026 to animeExpo2026Diffs.rallyDiffs),
                                    rallyLastEditTimes = mapOf(DataYear.ANIME_EXPO_2026 to animeExpo2026RallyLastEditTimes),
                                    seriesDiffs = tagDiffs.seriesDiffs,
                                    merchDiffs = tagDiffs.merchDiffs,
                                ),
                                stream = it,
                            )
                        }
                    }
                }
        }
    }

    private suspend fun parseTags() = coroutineScope {
        val legacySeriesIds = loadLegacySeriesIds()
        val legacyMerchIds = loadLegacyMerchIds()

        val snapshotFiles = animeExpo2026SnapshotsDirectory.get().asFileTree.files
            .plus(animeNyc2026SnapshotsDirectory.get().asFileTree.files)
            .asSequence()
            .map(::readSnapshotFile)
            .groupBy { it.date }
            .toList()
            .map { it.second.maxBy { it.timestamp } }
            .sortedBy { it.timestamp }
            .toList()

        trackStage("ArtistChangelogTagDiffs") {
            snapshotFiles
                .map { async { readTagSnapshot(it) } }
                .awaitAll()
                .also {
                    if (it.any { it == null }) {
                        throw IllegalStateException("Failed to read snapshot files")
                    }
                }
                .filterNotNull()
                .fold(
                    TagDiffs(
                        latestSeriesIds = legacySeriesIds,
                        latestMerchIds = legacyMerchIds,
                    )
                ) { before, current ->
                    val seriesDiff = diffSeries(
                        before = before,
                        current = current,
                    )
                    val merchDiff = diffMerch(
                        before = before,
                        current = current,
                    )

                    before.copy(
                        seriesDiffs = before.seriesDiffs + listOfNotNull(seriesDiff),
                        merchDiffs = before.merchDiffs + listOfNotNull(merchDiff),
                        latestSeriesIds = before.latestSeriesIds + current.seriesIds,
                        latestMerchIds = before.latestMerchIds + current.merchIds,
                    )
                }
        }
    }

    private suspend fun parseAnimeExpo2026() = coroutineScope {
        val artistLastEditTimes = mutableMapOf<Uuid, Instant>()
        val rallyLastEditTimes = mutableMapOf<Uuid, Instant>()
        if (!animeExpo2026SnapshotsDirectory.get().asFile.exists()) {
            return@coroutineScope Triple(artistLastEditTimes, rallyLastEditTimes, DataYearDiffs())
        }

        val snapshotFiles = animeExpo2026SnapshotsDirectory.get().asFileTree.files
            .map(::readSnapshotFile)
            .groupBy { it.date }
            .toList()
            .map { it.second.maxBy { it.timestamp } }
            .sortedBy { it.timestamp }

        val latestArtists = mutableSetOf<ArtistEntry>()
        val latestRallyIds = mutableSetOf<Uuid>()
        val latestImageNames = mutableSetOf<String>()
        val latestSnapshot = snapshotFiles.lastOrNull()
            ?.let {
                readSnapshot(
                    snapshotFile = it,
                    artists = {
                        getAllArtistEntryAnimeExpo2026().executeAsList().map(::ArtistEntry)
                    },
                    rallies = { getAllStampRallyEntryAnimeExpo2026().executeAsList() },
                )
            }
        if (latestSnapshot != null) {
            latestArtists += latestSnapshot.artists
            latestRallyIds += latestSnapshot.rallies.map { Uuid.parse(it.id) }

            // Check only names so that width/height presence doesn't matter
            latestImageNames += latestSnapshot.artists.flatMap { it.images.map { it.name } }
            latestImageNames += latestSnapshot.rallies.flatMap { it.images.map { it.name } }
        }

        val diffs = trackStage("ArtistChangelogDiffs") {
            snapshotFiles
                .map {
                    async {
                        readSnapshot(
                            snapshotFile = it,
                            artists = {
                                getAllArtistEntryAnimeExpo2026().executeAsList().map(::ArtistEntry)
                            },
                            rallies = { getAllStampRallyEntryAnimeExpo2026().executeAsList() },
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
                .fold(DataYearDiffs()) { before, current ->
                    val artistDiffs = diffArtists(
                        lastEditTimes = artistLastEditTimes,
                        latestArtists = latestArtists,
                        latestImageNames = latestImageNames,
                        before = before,
                        current = current,
                    )
                    val rallyDiffs = diffRallies(
                        lastEditTimes = rallyLastEditTimes,
                        latestRallyIds = latestRallyIds,
                        latestImageNames = latestImageNames,
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

        Triple(artistLastEditTimes, rallyLastEditTimes, diffs)
    }

    private suspend fun parseAnimeNyc2026() = coroutineScope {
        val artistLastEditTimes = mutableMapOf<Uuid, Instant>()
        if (!animeNyc2026SnapshotsDirectory.get().asFile.exists()) {
            return@coroutineScope artistLastEditTimes to DataYearDiffs()
        }

        val snapshotFiles = animeNyc2026SnapshotsDirectory.get().asFileTree.files
            .map(::readSnapshotFile)
            .groupBy { it.date }
            .toList()
            .map { it.second.maxBy { it.timestamp } }
            .sortedBy { it.timestamp }

        val latestArtists = mutableSetOf<ArtistEntry>()
        val latestImageNames = mutableSetOf<String>()
        val latestSnapshot = snapshotFiles.lastOrNull()
            ?.let {
                readSnapshot(
                    snapshotFile = it,
                    artists = {
                        getAllArtistEntryAnimeNyc2026().executeAsList().map(::ArtistEntry)
                    },
                    rallies = { emptyList() },
                )
            }
        if (latestSnapshot != null) {
            latestArtists += latestSnapshot.artists

            // Check only names so that width/height presence doesn't matter
            latestImageNames += latestSnapshot.artists.flatMap { it.images.map { it.name } }
        }

        val diffs = trackStage("ArtistChangelogDiffs") {
            snapshotFiles
                .map {
                    async {
                        readSnapshot(
                            snapshotFile = it,
                            artists = {
                                getAllArtistEntryAnimeNyc2026().executeAsList().map(::ArtistEntry)
                            },
                            rallies = { emptyList() },
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
                .fold(DataYearDiffs()) { before, current ->
                    val artistDiffs = diffArtists(
                        lastEditTimes = artistLastEditTimes,
                        latestArtists = latestArtists,
                        latestImageNames = latestImageNames,
                        before = before,
                        current = current,
                    )

                    before.copy(
                        artistDiffs = before.artistDiffs + artistDiffs,
                        latestArtists = current.artists,
                    )
                }
        }

        artistLastEditTimes to diffs
    }

    private fun readSnapshotFile(file: File): SnapshotFile {
        val timestamp = file.nameWithoutExtension
            .replace("_", ":")
            .replace(";", ":")
            .let(Instant::parse)
        val date = timestamp
            .toLocalDateTime(TimeZone.UTC)
            .date
        return SnapshotFile(
            timestamp = timestamp,
            date = date,
            file = file,
        )
    }

    private fun readTagSnapshot(snapshotFile: SnapshotFile): TagSnapshotData? {
        val databaseFile = temporaryDir.resolve("database-${snapshotFile.file.name}.sqlite")
        val snapshotFilteredFile = temporaryDir.resolve("database-${snapshotFile.file.name}.sql")
        listOf(snapshotFilteredFile, databaseFile).forEach(::verifyDelete)

        // First, create and immediately close the databases to initialize the schemas
        Utils.createEditDatabase(databaseFile).first.close()
        filterSnapshot(snapshotFile.file, snapshotFilteredFile)
        if (!Utils.readSqlFile(databaseFile, snapshotFilteredFile)) {
            logger.error("Failed to read ${snapshotFile.file.absolutePath}")
            return null
        }

        val database = Utils.createEditDatabase(databaseFile)
        val mutationQueries = database.second.mutationQueries
        val seriesIds = mutationQueries.getSeries()
            .executeAsList()
            .map { it.id }
            .toSet()
        val merchIds = mutationQueries.getMerch()
            .executeAsList()
            .map { it.name }
            .toSet()
        return TagSnapshotData(
            timestamp = snapshotFile.timestamp,
            date = snapshotFile.date,
            seriesIds = seriesIds,
            merchIds = merchIds,
        )
    }

    private fun readSnapshot(
        snapshotFile: SnapshotFile,
        artists: MutationQueries.() -> List<ArtistEntry>,
        rallies: MutationQueries.() -> List<StampRallyEntryAnimeExpo2026>,
    ): DataYearSnapshotData? {
        val databaseFile = temporaryDir.resolve("database-${snapshotFile.file.name}.sqlite")
        val snapshotFilteredFile = temporaryDir.resolve("database-${snapshotFile.file.name}.sql")
        listOf(snapshotFilteredFile, databaseFile).forEach(::verifyDelete)

        // First, create and immediately close the databases to initialize the schemas
        Utils.createEditDatabase(databaseFile).first.close()
        filterSnapshot(snapshotFile.file, snapshotFilteredFile)
        if (!Utils.readSqlFile(databaseFile, snapshotFilteredFile)) {
            logger.error("Failed to read ${snapshotFile.file.absolutePath}")
            return null
        }

        val database = Utils.createEditDatabase(databaseFile)
        val mutationQueries = database.second.mutationQueries
        return DataYearSnapshotData(
            timestamp = snapshotFile.timestamp,
            date = snapshotFile.date,
            artists = mutationQueries.artists(),
            rallies = mutationQueries.rallies(),
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
        return merchDatabase.mutationQueries.getMerch().executeAsList().map { it.name }.toSet()
    }

    private fun filterSnapshot(source: File, target: File) {
        target.writer().use { writer ->
            source.useLines {
                it.filter { line -> filteredTableNames.any { it in line } }
                    .filterNot { it.contains("11111111-1111-1111-1111-111111111111") }
                    .filterNot { it.contains("22222222-2222-2222-2222-222222222222") }
                    .map {
                        it.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
                            .replace("INSERT INTO", "INSERT OR REPLACE INTO")
                    }
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
        latestArtists: Set<ArtistEntry>,
        latestImageNames: Set<String>,
        before: DataYearDiffs,
        current: DataYearSnapshotData,
    ): List<ArtistDiff> {
        val timestamp = current.timestamp
        val date = current.date
        val artists = current.artists
        val beforeArtists = before.latestArtists
        return artists
            .map { afterArtist -> beforeArtists.find { it.id == afterArtist.id } to afterArtist }
            .mapNotNull { (beforeArtist, afterArtist) ->
                val latestArtist = latestArtists.find { it.id == afterArtist.id }
                    ?: return@mapNotNull null
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
                val images = afterArtist.images
                    .takeIf {
                        afterArtist.seriesConfirmed.isNotEmpty()
                                || afterArtist.merchConfirmed.isNotEmpty()
                    }
                    .orEmpty()
                    .toMutableList()
                if (beforeArtist != null) {
                    seriesInferred -= beforeArtist.seriesInferred.toSet()
                    seriesConfirmed -= beforeArtist.seriesConfirmed.toSet()
                    merchInferred -= beforeArtist.merchInferred.toSet()
                    merchConfirmed -= beforeArtist.merchConfirmed.toSet()

                    val beforeImageNames = beforeArtist.images.map { it.name }.toSet()
                    images.removeIf { it.name in beforeImageNames }
                }
                seriesInferred.retainAll(latestArtist.seriesInferred.toSet())
                seriesConfirmed.retainAll(latestArtist.seriesConfirmed.toSet())
                merchInferred.retainAll(latestArtist.merchInferred.toSet())
                merchConfirmed.retainAll(latestArtist.merchConfirmed.toSet())
                images.retainAll { it.name in latestImageNames }
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
        latestImageNames: Set<String>,
        before: DataYearDiffs,
        current: DataYearSnapshotData,
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
                val images = afterRally.images.toMutableList()
                val beforeRallyImageNames = beforeRally?.images?.map { it.name }?.toSet().orEmpty()
                images.removeAll { it.name in beforeRallyImageNames }
                images.retainAll { it.name in latestImageNames }
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

    private fun diffSeries(before: TagDiffs, current: TagSnapshotData): SeriesDiff? {
        val diff = current.seriesIds - before.latestSeriesIds
        if (diff.isEmpty()) return null
        return SeriesDiff(date = current.date, diff)
    }

    private fun diffMerch(before: TagDiffs, current: TagSnapshotData): MerchDiff? {
        val diff = current.merchIds - before.latestMerchIds
        if (diff.isEmpty()) return null
        return MerchDiff(date = current.date, diff)
    }

    private data class SnapshotFile(
        val timestamp: Instant,
        val date: LocalDate,
        val file: File,
    )

    private data class DataYearSnapshotData(
        val timestamp: Instant,
        val date: LocalDate,
        val artists: List<ArtistEntry>,
        val rallies: List<StampRallyEntryAnimeExpo2026>,
    )

    private data class TagSnapshotData(
        val timestamp: Instant,
        val date: LocalDate,
        val seriesIds: Set<String>,
        val merchIds: Set<String>,
    )

    private data class DataYearDiffs(
        val artistDiffs: List<ArtistDiff> = emptyList(),
        val rallyDiffs: List<StampRallyDiff> = emptyList(),
        val latestArtists: List<ArtistEntry> = emptyList(),
        val latestRallies: List<StampRallyEntryAnimeExpo2026> = emptyList(),
    )

    private data class TagDiffs(
        val seriesDiffs: List<SeriesDiff> = emptyList(),
        val merchDiffs: List<MerchDiff> = emptyList(),
        val latestSeriesIds: Set<String> = emptySet(),
        val latestMerchIds: Set<String> = emptySet(),
    )

    private data class ArtistEntry(
        val id: String,
        val booth: String?,
        val name: String,
        val seriesInferred: List<String>,
        val seriesConfirmed: List<String>,
        val merchInferred: List<String>,
        val merchConfirmed: List<String>,
        val images: List<DatabaseImage>,
    ) {
        constructor(entry: ArtistEntryAnimeExpo2026) : this(
            id = entry.id,
            booth = entry.booth,
            name = entry.name,
            seriesInferred = entry.seriesInferred,
            seriesConfirmed = entry.seriesConfirmed,
            merchInferred = entry.merchInferred,
            merchConfirmed = entry.merchConfirmed,
            images = entry.images,
        )

        constructor(entry: ArtistEntryAnimeNyc2026) : this(
            id = entry.id,
            booth = entry.booth,
            name = entry.name,
            seriesInferred = entry.seriesInferred,
            seriesConfirmed = entry.seriesConfirmed,
            merchInferred = entry.merchInferred,
            merchConfirmed = entry.merchConfirmed,
            images = entry.images,
        )
    }
}
