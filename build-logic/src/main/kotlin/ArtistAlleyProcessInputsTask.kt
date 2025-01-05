import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_merch_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_series_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Merch_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Series_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Stamp_rally_artist_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Stamp_rally_entries
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
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
        private const val SERIES_CSV_NAME = "series.csv"
        private const val MERCH_CSV_NAME = "merch.csv"
        private const val CHUNK_SIZE = 50
    }

    @get:InputDirectory
    abstract val inputFolder: DirectoryProperty

//    @get:InputDirectory
//    abstract val commonMainResourcesFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        inputFolder.convention(project.layout.projectDirectory.dir("inputs"))
//        commonMainResourcesFolder.convention(project.layout.projectDirectory.dir("src/commonMain/composeResources"))
        outputFolder.convention(project.layout.buildDirectory.dir("generated/composeResources"))
    }

    @TaskAction
    fun process() {
        // CMP resources only supports 1 composeResources directory, so copy over source files
        // TODO: Not needed since manual copy-paste is necessary anyways
//        FileUtils.copyDirectory(commonMainResourcesFolder.asFile.get(), outputFolder.asFile.get())

        val dbFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        dbFile.delete()
        dbFile.createNewFile()

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        BuildLogicDatabase.Schema.create(driver)
        val database = BuildLogicDatabase(driver)

        parseArtists(database)
        parseTags(database)
        parseStampRallies(database)

        driver.close()
        dbFile.copyTo(outputFolder.file("files/database.sqlite").get().asFile, overwrite = true)
    }

    private fun parseArtists(database: BuildLogicDatabase) {
        val json = Json.Default
        open(ARTISTS_CSV_NAME).use {
            var counter = 1L
            read(it)
                .map {
                    // Booth,Artist,Summary,Links,Store,Catalog / table,
                    // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                    // Merch - Confirmed,Drive,Catalog images
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

                    val artistEntry = Artist_entries(
                        id = booth,
                        booth = booth,
                        name = artist,
                        summary = summary,
                        links = links.let(json::encodeToString),
                        storeLinks = storeLinks.let(json::encodeToString),
                        catalogLinks = catalogLinks.let(json::encodeToString),
                        driveLink = driveLink,
                        seriesInferred = seriesInferred.let(json::encodeToString),
                        seriesConfirmed = seriesConfirmed.let(json::encodeToString),
                        merchInferred = merchInferred.let(json::encodeToString),
                        merchConfirmed = merchConfirmed.let(json::encodeToString),
                        notes = notes,
                        counter = counter++,
                        favorite = false,
                        ignored = false,
                    )

                    val seriesConnectionsInferred =
                        (seriesInferred - seriesConfirmed.toSet())
                            .map {
                                Artist_series_connections(
                                    artistId = booth,
                                    seriesId = it,
                                    confirmed = false
                                )
                            }
                    val seriesConnectionsConfirmed = seriesConfirmed
                        .map {
                            Artist_series_connections(
                                artistId = booth,
                                seriesId = it,
                                confirmed = true
                            )
                        }
                    val seriesConnections =
                        seriesConnectionsInferred + seriesConnectionsConfirmed

                    val merchConnectionsInferred = (merchInferred - merchConfirmed.toSet())
                        .map {
                            Artist_merch_connections(
                                artistId = booth,
                                merchId = it,
                                confirmed = false
                            )
                        }
                    val merchConnectionsConfirmed = merchConfirmed
                        .map {
                            Artist_merch_connections(
                                artistId = booth,
                                merchId = it,
                                confirmed = true
                            )
                        }
                    val merchConnections =
                        merchConnectionsInferred + merchConnectionsConfirmed

                    Triple(artistEntry, seriesConnections, merchConnections)
                }
                .chunked(100)
                .forEach {
                    val artists = it.map { it.first }
                    val seriesConnections = it.flatMap { it.second }
                    val merchConnections = it.flatMap { it.third }
                    val artistEntryQueries = database.artistEntryQueries
                    artistEntryQueries.transaction {
                        artists.forEach(artistEntryQueries::insert)
                        seriesConnections.forEach(artistEntryQueries::insertSeriesConnection)
                        merchConnections.forEach(artistEntryQueries::insertMerchConnection)
                    }
                }
        }
    }

    private fun parseTags(database: BuildLogicDatabase) {
        val tagEntryQueries = database.tagEntryQueries
        open(SERIES_CSV_NAME).use {
            read(it)
                .map {
                    // Series, Notes
                    val name = it["Series"]!!
                    val notes = it["Notes"]
                    Series_entries(name = name, notes = notes)
                }
                .chunked(CHUNK_SIZE)
                .forEach {
                    tagEntryQueries.transaction {
                        it.forEach(tagEntryQueries::insertSeries)
                    }
                }
        }
        open(MERCH_CSV_NAME).use {
            read(it)
                .map {
                    // Merch, Notes
                    val name = it["Merch"]!!
                    val notes = it["Notes"]
                    Merch_entries(name = name, notes = notes)
                }
                .chunked(CHUNK_SIZE)
                .forEach {
                    tagEntryQueries.transaction {
                        it.forEach(tagEntryQueries::insertMerch)
                    }
                }
        }
    }

    private fun parseStampRallies(database: BuildLogicDatabase) {
        val json = Json.Default
        val stampRallyEntryQueries = database.stampRallyEntryQueries
        open(STAMP_RALLIES_CSV_NAME).use {
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
                        .map { Stamp_rally_artist_connections(stampRallyId, it) }

                    Stamp_rally_entries(
                        id = stampRallyId,
                        fandom = theme,
                        tables = tables.let(json::encodeToString),
                        hostTable = hostTable,
                        links = links.let(json::encodeToString),
                        tableMin = tableMin?.toLong(),
                        totalCost = (if (tableMin == 0) 0 else totalCost)?.toLong(),
                        prizeLimit = prizeLimit?.toLong(),
                        notes = notes,
                        counter = counter++,
                        favorite = false,
                        ignored = false,
                    ) to connections
                }
                .chunked(CHUNK_SIZE)
                .forEach {
                    val stampRallies = it.map { it.first }
                    val artistConnections = it.flatMap { it.second }
                    stampRallyEntryQueries.transaction {
                        stampRallies.forEach(stampRallyEntryQueries::insert)
                        artistConnections.forEach(stampRallyEntryQueries::insertArtistConnection)
                    }
                }
        }
    }

    private fun open(name: String) =
        inputFolder.file(name).get().asFile.inputStream().asSource().buffered()

    private fun read(source: Source): Sequence<Map<String, String>> {
        val header = source.readLine()!!
        val columnNames = header.split(",")
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
