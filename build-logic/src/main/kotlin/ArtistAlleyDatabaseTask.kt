import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.build_logic.BuildLogicDatabase
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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

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

    @get:InputFile
    abstract val artistsCsv2024: RegularFileProperty

    @get:InputFile
    abstract val artistsCsv2025: RegularFileProperty

    @get:InputFile
    abstract val stampRalliesCsv2024: RegularFileProperty

    @get:InputFile
    abstract val stampRalliesCsv2025: RegularFileProperty

    @get:InputFile
    abstract val seriesCsv: RegularFileProperty

    @get:InputFile
    abstract val merchCsv: RegularFileProperty

    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    init {
        artistsCsv2024.convention(layout.projectDirectory.file("inputs/2024/$ARTISTS_CSV_NAME"))
        artistsCsv2025.convention(layout.projectDirectory.file("inputs/2025/$ARTISTS_CSV_NAME"))
        stampRalliesCsv2024.convention(layout.projectDirectory.file("inputs/2024/$STAMP_RALLIES_CSV_NAME"))
        stampRalliesCsv2025.convention(layout.projectDirectory.file("inputs/2025/$STAMP_RALLIES_CSV_NAME"))
        seriesCsv.convention(layout.projectDirectory.file("inputs/$SERIES_CSV_NAME"))
        merchCsv.convention(layout.projectDirectory.file("inputs/$MERCH_CSV_NAME"))
        outputResources.convention(layout.buildDirectory.dir("generated/composeResources"))
    }

    private val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    @TaskAction
    fun process() {
        val dbFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        if (dbFile.exists() && !dbFile.delete()) {
            println("Failed to delete $dbFile, manually delete to re-process inputs")
        } else {
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            BuildLogicDatabase.Schema.create(driver)
            val database = BuildLogicDatabase(
                driver = driver,
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
                stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
            )

            val (seriesConnections, merchConnections) = parseArtists(database)
            parseTags(database, seriesConnections, merchConnections)
            parseStampRallies(database)

            val ftsTables = listOf(
                "artistEntry2024_fts",
                "artistEntry2025_fts",
                "stampRallyEntry2024_fts",
                "stampRallyEntry2025_fts",
                "seriesEntry_fts",
                "merchEntry_fts",
            )

            ftsTables.forEach {
                driver.execute(null, "INSERT INTO $it($it) VALUES('rebuild');", 0, null)
                driver.execute(null, "INSERT INTO $it($it) VALUES('optimize');", 0, null)
            }

            driver.execute(null, "VACUUM;", 0, null)
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

    private fun parseArtists(database: BuildLogicDatabase): Pair<MutableMap<Pair<String, String>, ArtistSeriesConnection>, MutableMap<Pair<String, String>, ArtistMerchConnection>> {
        val seriesConnections = mutableMapOf<Pair<String, String>, ArtistSeriesConnection>()
        val merchConnections = mutableMapOf<Pair<String, String>, ArtistMerchConnection>()

        fun addSeriesConnection(seriesConnection: ArtistSeriesConnection) {
            val idPair = seriesConnection.let { it.artistId to it.seriesId }
            val existing = seriesConnections[idPair]
            if (existing == null) {
                seriesConnections[idPair] = seriesConnection
            } else {
                seriesConnections[idPair] = existing.copy(
                    has2024 = existing.has2024 || seriesConnection.has2024,
                    has2025 = existing.has2025 || seriesConnection.has2025,
                )
            }
        }

        fun addMerchConnection(merchConnection: ArtistMerchConnection) {
            val idPair = merchConnection.let { it.artistId to it.merchId }
            val existing = merchConnections[idPair]
            if (existing == null) {
                merchConnections[idPair] = merchConnection
            } else {
                merchConnections[idPair] = existing.copy(
                    has2024 = existing.has2024 || merchConnection.has2024,
                    has2025 = existing.has2025 || merchConnection.has2025,
                )
            }
        }

        open(artistsCsv2024).use {
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
                    val seriesInferred = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    val merchInferred = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)

                    val notes = it["Notes"]

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

                    val seriesConnectionsInferred =
                        (seriesInferred - seriesConfirmed.toSet())
                            .map {
                                ArtistSeriesConnection(
                                    artistId = id,
                                    seriesId = it,
                                    confirmed = false,
                                    has2024 = true,
                                    has2025 = false,
                                )
                            }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                confirmed = true,
                                has2024 = true,
                                has2025 = false,
                            )
                        }

                    val merchConnectionsInferred = (merchInferred - merchConfirmed.toSet())
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                confirmed = false,
                                has2024 = true,
                                has2025 = false,
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                confirmed = true,
                                has2024 = true,
                                has2025 = false,
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .forEach {
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtist2024)
                    }
                    it.flatMap { it.second }.forEach(::addSeriesConnection)
                    it.flatMap { it.third }.forEach(::addMerchConnection)
                }
        }

        open(artistsCsv2025).use {
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

                    val newLineRegex = Regex("\n\\s?")
                    val links = it["Links"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val storeLinks = it["Store"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val catalogLinks = it["Catalog / table"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                    val driveLink = it["Drive"]

                    val commaRegex = Regex(",\\s?")
                    val seriesInferred = it["Series - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    val merchInferred = it["Merch - Inferred"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)

                    val seriesConfirmed = it["Series - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)
                    val merchConfirmed = it["Merch - Confirmed"].orEmpty().split(commaRegex)
                        .filter(String::isNotBlank)

                    val notes = it["Notes"]
                    val commissions = it["Commissions"].orEmpty().split(newLineRegex)
                        .filter(String::isNotBlank)
                        .sorted()

                    val artistEntry = ArtistEntry2025(
                        id = id,
                        booth = booth?.takeIf { it.length == 3 },
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
                        commissions = commissions,
                        counter = counter++,
                    )

                    val seriesConnectionsInferred =
                        (seriesInferred - seriesConfirmed.toSet())
                            .map {
                                ArtistSeriesConnection(
                                    artistId = id,
                                    seriesId = it,
                                    confirmed = false,
                                    has2024 = false,
                                    has2025 = true,
                                )
                            }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            ArtistSeriesConnection(
                                artistId = id,
                                seriesId = it,
                                confirmed = true,
                                has2024 = false,
                                has2025 = true,
                            )
                        }

                    val merchConnectionsInferred = (merchInferred - merchConfirmed.toSet())
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                confirmed = false,
                                has2024 = false,
                                has2025 = true,
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            ArtistMerchConnection(
                                artistId = id,
                                merchId = it,
                                confirmed = true,
                                has2024 = false,
                                has2025 = true,
                            )
                        }

                    Triple(
                        artistEntry,
                        seriesConnectionsInferred + seriesConnectionsConfirmed,
                        merchConnectionsInferred + merchConnectionsConfirmed,
                    )
                }
                .chunked(100)
                .forEach {
                    val artists = it.map { it.first }
                    val mutationQueries = database.mutationQueries
                    mutationQueries.transaction {
                        artists.forEach(mutationQueries::insertArtist2025)
                    }
                    it.flatMap { it.second }.forEach(::addSeriesConnection)
                    it.flatMap { it.third }.forEach(::addMerchConnection)
                }
        }
        val mutationQueries = database.mutationQueries
        seriesConnections.values.forEach(mutationQueries::insertSeriesConnection)
        merchConnections.values.forEach(mutationQueries::insertMerchConnection)

        return seriesConnections to merchConnections
    }

    private fun parseTags(
        database: BuildLogicDatabase,
        seriesConnections: MutableMap<Pair<String, String>, ArtistSeriesConnection>,
        merchConnections: MutableMap<Pair<String, String>, ArtistMerchConnection>,
    ) {
        val mutationQueries = database.mutationQueries
        open(seriesCsv).use {
            read(it)
                .map {
                    // Series, Notes
                    val name = it["Series"]!!
                    val notes = it["Notes"]
                    SeriesEntry(
                        name = name,
                        notes = notes,
                        has2024 = seriesConnections.any { it.value.seriesId == name && it.value.has2024 },
                        has2025 = seriesConnections.any { it.value.seriesId == name && it.value.has2025 },
                    )
                }
                .chunked(DATABASE_CHUNK_SIZE)
                .forEach {
                    mutationQueries.transaction {
                        it.forEach(mutationQueries::insertSeries)
                    }
                }
        }
        open(merchCsv).use {
            read(it)
                .map {
                    // Merch, Notes
                    val name = it["Merch"]!!
                    val notes = it["Notes"]
                    MerchEntry(
                        name = name,
                        notes = notes,
                        has2024 = merchConnections.any { it.value.merchId == name && it.value.has2024 },
                        has2025 = merchConnections.any { it.value.merchId == name && it.value.has2025 },
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

    private fun parseStampRallies(database: BuildLogicDatabase) {
        val mutationQueries = database.mutationQueries
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

                    val stampRallyId = "$hostTable-$theme"
                    val connections = tables
                        .filter { it.contains("-") }
                        .map { it.substringBefore("-") }
                        .map(String::trim)
                        .map { StampRallyArtistConnection(stampRallyId, it) }

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

        open(stampRalliesCsv2025).use {
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

                    val stampRallyId = "$hostTable-$theme"
                    val connections = tables
                        .filter { it.contains("-") }
                        .map { it.substringBefore("-") }
                        .map(String::trim)
                        .map { StampRallyArtistConnection(stampRallyId, it) }

                    StampRallyEntry2025(
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
                        stampRallies.forEach(mutationQueries::insertStampRally2025)
                        artistConnections.forEach(mutationQueries::insertArtistConnection)
                    }
                }
        }
    }

    private fun open(file: RegularFileProperty) =
        file.get().asFile.inputStream().asSource().buffered()

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
}
