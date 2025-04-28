import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.build_logic.BuildLogicDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link.Companion.parse
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link.Type
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.serialization.json.Json
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
import javax.inject.Inject

@CacheableTask
abstract class ArtistAlleyDatabaseTask : DefaultTask() {

    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
        private const val SERIES_CSV_NAME = "series.csv"
        private const val MERCH_CSV_NAME = "merch.csv"
        private const val DATABASE_CHUNK_SIZE = 50
    }

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputsDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    init {
        inputsDirectory.convention(layout.projectDirectory.dir("inputs"))
        outputResources.convention(layout.buildDirectory.dir("generated/composeResources"))
    }

    private val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
        override fun decode(databaseValue: String) =
            DataYear.entries.first { it.serializedName == databaseValue }

        override fun encode(value: DataYear) = value.serializedName
    }

    @TaskAction
    fun process() {
        val dbFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        if (dbFile.exists() && !dbFile.delete()) {
            throw IllegalStateException(
                "Failed to delete $dbFile, manually delete to re-process inputs"
            )
        } else {
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            BuildLogicDatabase.Schema.create(driver)
            val database = BuildLogicDatabase(
                driver = driver,
                artistEntry2023Adapter = ArtistEntry2023.Adapter(
                    artistNamesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                ),
                artistEntry2024Adapter = ArtistEntry2024.Adapter(
                    linksAdapter = listStringAdapter,
                    storeLinksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                    seriesInferredAdapter = listStringAdapter,
                    seriesConfirmedAdapter = listStringAdapter,
                    merchInferredAdapter = listStringAdapter,
                    merchConfirmedAdapter = listStringAdapter,
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
                ),
                stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
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
                )
            )

            val seriesConnections = mutableMapOf<Pair<String, String>, ArtistSeriesConnection>()
            val merchConnections = mutableMapOf<Pair<String, String>, ArtistMerchConnection>()

            val artists2023 = parseArtists2023(database)

            val (artists2024, seriesConnections2024, merchConnections2024) =
                parseArtists2024(database)
            seriesConnections2024.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnections2024.forEach { merchConnections.addMerchConnection(it) }

            val (artists2025, seriesConnections2025, merchConnections2025) =
                parseArtists2025(database, artists2023, artists2024)
            seriesConnections2025.forEach { seriesConnections.addSeriesConnection(it) }
            merchConnections2025.forEach { merchConnections.addMerchConnection(it) }

            val mutationQueries = database.mutationQueries
            seriesConnections.values.forEach(mutationQueries::insertSeriesConnection)
            merchConnections.values.forEach(mutationQueries::insertMerchConnection)

            parseSeries(database, seriesConnections)
            parseMerch(database, merchConnections)

            parseStampRallies(artists2023, artists2024, artists2025, database)

            runBlocking {
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

            driver.closeConnection(driver.getConnection())
            driver.close()

            dbFile.copyTo(
                outputResources.file("files/database.sqlite").get().asFile,
                overwrite = true,
            )
            val hash = Utils.hash(dbFile)

            dbFile.delete()

            outputResources.file("files/databaseHash.txt").get().asFile.writeText(hash.toString())
        }
    }

    private fun parseArtists2023(database: BuildLogicDatabase): List<ArtistEntry2023> {
        val artistsCsv2023 = inputsDirectory.file("2023/$ARTISTS_CSV_NAME").get()
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

                    val driveLink = it["Drive"]

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

    private fun parseArtists2024(database: BuildLogicDatabase): Triple<List<ArtistEntry2024>, MutableList<ArtistSeriesConnection>, MutableList<ArtistMerchConnection>> {
        val seriesConnections = mutableListOf<ArtistSeriesConnection>()
        val merchConnections = mutableListOf<ArtistMerchConnection>()
        val artistsCsv2024 = inputsDirectory.file("2024/$ARTISTS_CSV_NAME").get()
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
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                state2024 = 1,
                                state2025 = 0,
                            )
                        }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                state2024 = 2,
                                state2025 = 0,
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                state2024 = 1,
                                state2025 = 0,
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                state2024 = 2,
                                state2025 = 0,
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

                    val commaRegex = Regex(",\\s?")

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
                        commissionOnsite = commissions.contains("On-site"),
                        commissionOnline = commissions.contains("Online") ||
                                commissions.any {
                                    it.contains("http", ignoreCase = true) &&
                                            !it.contains("vgen.co", ignoreCase = true)
                                },
                        commissionVGen = commissions.any {
                            it.contains("vgen.co", ignoreCase = true)
                        },
                        commissionOther = commissions.filterNot {
                            it.contains("On-site", ignoreCase = true) ||
                                    it.contains("Online", ignoreCase = true) ||
                                    it.contains("http", ignoreCase = true) ||
                                    it.contains("vgen.co", ignoreCase = true)
                        }.isNotEmpty(),
                        counter = counter++,
                    )

                    val seriesConnectionsInferred = seriesInferred.map {
                        ArtistSeriesConnection(
                            artistId = id,
                            seriesId = it,
                            state2024 = 0,
                            state2025 = 1,
                        )
                    }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                state2024 = 0,
                                state2025 = 2,
                            )
                        }

                    val merchConnectionsInferred = merchInferred
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                state2024 = 0,
                                state2025 = 1,
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                state2024 = 0,
                                state2025 = 2,
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

    private fun parseSeries(
        database: BuildLogicDatabase,
        seriesConnections: MutableMap<Pair<String, String>, ArtistSeriesConnection>,
    ) {
        val mutationQueries = database.mutationQueries
        val seriesCsv = inputsDirectory.file(SERIES_CSV_NAME).get()
        open(seriesCsv).use {
            read(it)
                .map {
                    // Validated, Series, Notes, AniList ID, AniList Type, Source Type, English,
                    // Romaji, Native, Preferred, Wikipedia ID, External Link,
                    val validated = it["Validated"] == "DONE"
                    val id = it["Series"]!!
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
                    SeriesEntry(
                        id = id,
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
                        has2024 = seriesConnections.any { it.value.seriesId == id && it.value.state2024 > 0 },
                        has2025 = seriesConnections.any { it.value.seriesId == id && it.value.state2025 > 0 },
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertSeries)
                    }
                }
        }
    }

    private fun parseMerch(
        database: BuildLogicDatabase,
        merchConnections: MutableMap<Pair<String, String>, ArtistMerchConnection>,
    ) {
        val mutationQueries = database.mutationQueries
        val merchCsv = inputsDirectory.file(MERCH_CSV_NAME).get()
        open(merchCsv).use {
            read(it)
                .map {
                    // Merch, Notes
                    val name = it["Merch"]!!
                    val notes = it["Notes"]
                    val categories = it["Categories"]
                    MerchEntry(
                        name = name,
                        notes = notes,
                        categories = categories,
                        has2024 = merchConnections.any { it.value.merchId == name && it.value.state2024 > 0 },
                        has2025 = merchConnections.any { it.value.merchId == name && it.value.state2025 > 0 },
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertMerch)
                    }
                }
        }
    }

    private fun parseStampRallies(
        artists2023: List<ArtistEntry2023>,
        artists2024: List<ArtistEntry2024>,
        artists2025: List<ArtistEntry2025>,
        database: BuildLogicDatabase,
    ) {
        val mutationQueries = database.mutationQueries
//        val stampRalliesCsv2025 = inputsDirectory.file("2025/$STAMP_RALLIES_CSV_NAME").get()
        val stampRalliesCsv2024 = inputsDirectory.file("2024/$STAMP_RALLIES_CSV_NAME").get()
        val stampRalliesCsv2023 = inputsDirectory.file("2023/$STAMP_RALLIES_CSV_NAME").get()

        val boothToArtist2023 = artists2023.associate { it.booth to it }
        val boothToArtist2024 = artists2024.associate { it.booth to it }
//        val boothToArtist2025 = artists2025.associate { it.booth to it }

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
                    val tableMin = it["Table Min"]!!.let {
                        when {
                            it.equals("Free", ignoreCase = true) -> 0
                            it.equals("Any", ignoreCase = true) -> 1
                            else -> it.removePrefix("$").toIntOrNull()
                        }
                    }
                    val totalCost = it["Total"]?.removePrefix("$")?.toIntOrNull()
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
                        tableMin = tableMin?.toLong(),
                        totalCost = (if (tableMin == 0) 0 else totalCost)?.toLong(),
                        prizeLimit = prizeLimit?.toLong(),
                        notes = notes,
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

    private fun open(file: RegularFile) = file.asFile.inputStream().asSource().buffered()

    private fun read(source: Source): Sequence<Map<String, String>> {
        val header = source.readLine()!!
        val columnNames = header.split(",").map { it.removePrefix("\"").removeSuffix("\"") }
        val columnCount = columnNames.size
        return sequence {
            val buffer = Buffer()
            while (!source.exhausted()) {
                var fieldIndex = 0
                val map = mutableMapOf<String, String>()
                buffer.clear()
                val commaByte = ','.code.toByte()
                val quoteByte = '"'.code.toByte()
                val newLineByte = '\n'.code.toByte()
                var insideQuote = false
                while (fieldIndex < columnCount && !source.exhausted()) {
                    when (val byte = source.readByte()) {
                        quoteByte -> insideQuote = !insideQuote
                        commaByte,
                        newLineByte,
                            -> {
                            if (insideQuote) {
                                buffer.writeByte(byte)
                            } else {
                                map[columnNames[fieldIndex]] = buffer.readString()
                                fieldIndex++
                            }
                        }
                        else -> buffer.writeByte(byte)
                    }
                }

                yield(map)
            }

            buffer.close()
        }
    }

    fun MutableMap<Pair<String, String>, ArtistSeriesConnection>.addSeriesConnection(
        seriesConnection: ArtistSeriesConnection,
    ) {
        val idPair = seriesConnection.let { it.artistId to it.seriesId }
        val existing = this[idPair]
        if (existing == null) {
            this[idPair] = seriesConnection
        } else {
            this[idPair] = existing.copy(
                state2024 = existing.state2024.coerceAtLeast(seriesConnection.state2024),
                state2025 = existing.state2025.coerceAtLeast(seriesConnection.state2025),
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
                state2024 = existing.state2024.coerceAtLeast(merchConnection.state2024),
                state2025 = existing.state2025.coerceAtLeast(merchConnection.state2025),
            )
        }
    }
}
