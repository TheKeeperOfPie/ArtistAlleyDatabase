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
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream
import javax.inject.Inject

abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    companion object {
        private const val PACKAGE_NAME = "com.thekeeperofpie.artistalleydatabase.generated"
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "bmp")
        private const val RESIZE_TARGET = 800
        private const val WEBP_TARGET_QUALITY = 15
        private const val WEBP_METHOD = 6
        private const val COMPOSE_FILES_CHUNK_SIZE = 50
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
    private val composeFolderType = ClassName(PACKAGE_NAME, "ComposeFile.Folder")
    private val listComposeFileType =
        List::class.asClassName().parameterizedBy(composeFileType)
    private val nullableIntType = Int::class.asTypeName().copy(nullable = true)

    @TaskAction
    fun process() {
        outputResources.dir("files/catalogs").get().asFile.deleteRecursively()
        outputResources.dir("files/rallies").get().asFile.deleteRecursively()

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
            .apply {
                addType(accessorType(this, imageCacheDir))
            }
            .addType(folderType())
            .build()
            .writeTo(outputSource.asFile.get())
    }

    private fun accessorType(fileSpec: FileSpec.Builder, imageCacheDir: File): TypeSpec {
        val folders = parseFolders()
        val catalogs = folders.first { it.name == "catalogs" }
        val rallies = folders.first { it.name == "rallies" }
        return TypeSpec.objectBuilder("ComposeFiles")
            .addChunkedFolder(fileSpec, imageCacheDir, catalogs)
            .addChunkedFolder(fileSpec, imageCacheDir, rallies)
            .build()
    }

    private fun TypeSpec.Builder.addChunkedFolder(
        fileSpec: FileSpec.Builder,
        imageCacheDir: File,
        folder: ComposeFile,
    ) = apply {
        val chunks = folder.files.chunked(COMPOSE_FILES_CHUNK_SIZE)
            .map {
                CodeBlock.Builder()
                    .apply {
                        add("listOf(")
                        it.forEach { appendFileCode(imageCacheDir, it, 1) }
                        add(")")
                    }
                    .build()
            }
        chunks.forEachIndexed { index, chunk ->
            fileSpec.addType(
                TypeSpec.objectBuilder("${folder.name}$index".capitalized())
                    .addProperty(
                        PropertySpec.builder("files", listComposeFileType)
                            .initializer(
                                CodeBlock.builder()
                                    .add(chunk)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        }

        addProperty(
            PropertySpec.builder(folder.name, composeFolderType)
                .initializer(
                    CodeBlock.builder()
                        .apply {
                            add(
                                """
                                    ComposeFile.Folder(
                                        name = %S,
                                        files = 
                                """.trimIndent(), folder.name
                            )
                            chunks.indices.forEach { index ->
                                add("${folder.name}$index".capitalized() + ".files ")
                                if (index != chunks.lastIndex) add(" + ")
                            }
                            add(")")
                        }
                        .build()
                )
                .build()
        )
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
        add("),\n)")
        if (level > 0) {
            add(",\n")
        }
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
}
