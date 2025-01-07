import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_merch_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Artist_series_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Merch_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Series_entries
import com.thekeeperofpie.artistalleydatabase.alley.app.Stamp_rally_artist_connections
import com.thekeeperofpie.artistalleydatabase.alley.app.Stamp_rally_entries
import com.thekeeperofpie.artistalleydatabase.build_logic.BuildLogicDatabase
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream
import javax.inject.Inject

abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    companion object {
        private const val ARTISTS_CSV_NAME = "artists.csv"
        private const val STAMP_RALLIES_CSV_NAME = "rallies.csv"
        private const val SERIES_CSV_NAME = "series.csv"
        private const val MERCH_CSV_NAME = "merch.csv"
        private const val CHUNK_SIZE = 50
        private const val PACKAGE_NAME = "com.thekeeperofpie.artistalleydatabase.generated"
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "bmp")
        private const val RESIZE_TARGET = 800
        private const val WEBP_TARGET_QUALITY = 15
        private const val WEBP_METHOD = 6
    }

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputDirectory
    abstract val inputFolder: DirectoryProperty

    @get:InputDirectory
    abstract val commonMainResourcesFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSource: DirectoryProperty

    init {
        inputFolder.convention(layout.projectDirectory.dir("inputs"))
        commonMainResourcesFolder.convention(layout.projectDirectory.dir("src/commonMain/composeResources"))
        outputResources.convention(layout.buildDirectory.dir("generated/composeResources"))
        outputSource.convention(layout.buildDirectory.dir("generated/source"))
    }

    private val composeFileType = ClassName(PACKAGE_NAME, "ComposeFile")
    private val listComposeFileType =
        List::class.asClassName().parameterizedBy(composeFileType)
    private val nullableIntType = Int::class.asTypeName().copy(nullable = true)

    @TaskAction
    fun process() {
        outputResources.get().asFile.deleteRecursively()

        val dbFile = temporaryDir.resolve("artistAlleyDatabase.sqlite")
        if (dbFile.exists() && !dbFile.delete()) {
            println("Failed to delete $dbFile, manually delete to re-process inputs")
        } else {
            val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
            BuildLogicDatabase.Schema.create(driver)
            val database = BuildLogicDatabase(driver)

            parseArtists(database)
            parseTags(database)
            parseStampRallies(database)

            driver.closeConnection(driver.getConnection())
            driver.close()

            dbFile.copyTo(
                outputResources.file("files/database.sqlite").get().asFile,
                overwrite = true,
            )
            dbFile.delete()
        }

        val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)

        copyInputFiles()
        buildComposeFiles(imageCacheDir)
    }

    private fun copyInputFiles() {
        FileUtils.copyDirectory(
            commonMainResourcesFolder.asFile.get(),
            outputResources.asFile.get()
        )

        // Catalogs/rallies strip out "-" because CMP uses that as a type qualifier
        val catalogsInput = inputFolder.dir("catalogs").get().asFile
        val catalogsOutput = outputResources.dir("files/catalogs").get().asFile
        catalogsInput.listFiles().forEach {
            FileUtils.copyDirectory(it, catalogsOutput.resolve(it.name.substringBefore(" -")))
        }

        val ralliesInput = inputFolder.dir("rallies").get().asFile
        val ralliesOutput = outputResources.dir("files/rallies").get().asFile
        ralliesInput.listFiles().forEach {
            FileUtils.copyDirectory(
                it, ralliesOutput.resolve(
                    it.name
                        .replace(" - ", "")
                        .replace("'", "_")
                )
            )
        }

        compressAndRenameImages(catalogsOutput)
        compressAndRenameImages(ralliesOutput)
    }

    /**
     * Compresses and renames files to their index in their parent, to shrink paths and line up with
     * how they were parsed by [parseFolders].
     */
    private fun compressAndRenameImages(dir: File) {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1).use {
            runBlocking {
                withContext(it.asCoroutineDispatcher()) {
                    dir.listFiles()
                        .filter { it.isDirectory }
                        .flatMap {
                            it.listFiles()
                                .filter { it.isFile && it.extension in IMAGE_EXTENSIONS }
                                .sorted()
                                .withIndex()
                        }
                        .map { (index, file) ->
                            async {
                                var image = ImageIO.read(file)
                                var width: Int? = null
                                var height: Int? = null
                                if (image.width > image.height && image.width > RESIZE_TARGET) {
                                    logger.lifecycle("Resizing $file")
                                    width = RESIZE_TARGET
                                    height = 0
                                } else if (image.height >= image.width && image.height > RESIZE_TARGET) {
                                    logger.lifecycle("Resizing $file")
                                    width = 0
                                    height = RESIZE_TARGET
                                }
                                logger.lifecycle("Compressing $file")
                                val output = file.resolveSibling("$index.webp")
                                val params = mutableListOf(
                                    "cwebp",
                                    "-af",
                                    "-q",
                                    WEBP_TARGET_QUALITY.toString(),
                                    "-m",
                                    WEBP_METHOD.toString(),
                                    file.absolutePath,
                                    "-o",
                                    output.absolutePath,
                                )
                                if (width != null) {
                                    params += "-resize"
                                    params += width.toString()
                                    params += height.toString()
                                }
                                val success = ProcessBuilder(params)
                                    .redirectErrorStream(true)
                                    .start()
                                    .waitFor(30, TimeUnit.SECONDS)
                                if (!success) {
                                    throw IllegalStateException("Failed to compress $file")
                                }
                                file.delete()
                            }
                        }
                        .awaitAll()
                }
            }
        }
    }

    private fun buildComposeFiles(imageCacheDir: File) {
        FileSpec.builder(PACKAGE_NAME, "ComposeFiles")
            .addType(accessorType(imageCacheDir))
            .addType(folderType())
            .build()
            .writeTo(outputSource.asFile.get())
    }

    private fun accessorType(imageCacheDir: File): TypeSpec {
        val folders = parseFolders()
        return TypeSpec.objectBuilder("ComposeFiles")
            .addProperty(
                PropertySpec.builder("root", listComposeFileType)
                    .initializer(
                        CodeBlock.builder()
                            .apply {
                                add("listOf(\n")
                                folders.forEach {
                                    appendFileCode(imageCacheDir, it, 1)
                                }
                                add(")")
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun folderType() = TypeSpec.interfaceBuilder("ComposeFile")
        .addModifiers(KModifier.SEALED)
        .addType(
            TypeSpec.classBuilder("Folder")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(composeFileType)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("name", String::class)
                        .addParameter("files", listComposeFileType)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class)
                        .initializer("name")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("files", listComposeFileType)
                        .initializer("files")
                        .build()
                )
                .build()
        )
        .addType(
            TypeSpec.classBuilder("File")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(composeFileType)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("name", String::class)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class)
                        .initializer("name")
                        .build()
                )
                .build()
        )
        .addType(
            TypeSpec.classBuilder("Image")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(composeFileType)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("name", String::class)
                        .addParameter("width", nullableIntType)
                        .addParameter("height", nullableIntType)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class)
                        .initializer("name")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("width", nullableIntType)
                        .initializer("width")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("height", nullableIntType)
                        .initializer("height")
                        .build()
                )
                .build()
        )
        .build()

    private fun parseFolders() =
        outputResources.dir("files").get().asFile.listFiles().map(::parseFile)

    private fun parseFile(file: File, name: String = file.name): ComposeFile =
        if (file.isDirectory) {
            ComposeFile(
                name = name,
                file = file,
                files = file.listFiles().sorted().mapIndexed { index, child ->
                    parseFile(
                        file = child,
                        name = if (child.isDirectory) {
                            child.name
                        } else {
                            "$index.${child.extension}"
                        }
                    )
                },
            )
        } else {
            ComposeFile(name, file, emptyList())
        }

    private fun CodeBlock.Builder.appendFileCode(
        imageCacheDir: File,
        file: ComposeFile,
        level: Int,
    ) {
        if (file.files.isEmpty()) {
            if (file.name.endsWith(".jpg")
                || file.name.endsWith(".png")
                || file.name.endsWith(".webp")
            ) {
                val (width, height) = parseImageWidthHeight(imageCacheDir, file)
                addStatement(
                    "ComposeFile.Image(name = %S, width = %L, height = %L),",
                    file.name,
                    width,
                    height,
                )
                return
            }
            addStatement("ComposeFile.File(name = %S),", file.name)
            return
        }
        addStatement("ComposeFile.Folder(")
        addStatement("name = %S,", file.name)
        addStatement("files = listOf(")
        file.files.forEach {
            appendFileCode(imageCacheDir, it, level + 1)
        }
        add("),\n),\n")
    }

    private fun parseImageWidthHeight(imageCacheDir: File, file: ComposeFile): Pair<Int?, Int?> {
        file.file.extension
            .let(ImageIO::getImageReadersBySuffix)
            .forEach { reader ->
                try {
                    file.file.inputStream().use {
                        FileCacheImageInputStream(it, imageCacheDir).use {
                            reader.setInput(it)
                            val width = reader.getWidth(reader.minIndex)
                            val height = reader.getHeight(reader.minIndex)
                            return width to height
                        }
                    }
                } finally {
                    reader.dispose()
                }
            }

        return null to null
    }

    data class ComposeFile(
        val name: String,
        val file: File,
        val files: List<ComposeFile>,
    )

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
