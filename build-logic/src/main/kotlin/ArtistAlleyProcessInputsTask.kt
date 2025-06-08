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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream
import javax.inject.Inject

@CacheableTask
abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    companion object {
        private const val PACKAGE_NAME = "com.thekeeperofpie.artistalleydatabase.generated"
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "bmp")
        private const val RESIZE_TARGET = 1200
        private const val WEBP_TARGET_QUALITY = 80
        private const val WEBP_METHOD = 6
        private const val COMPOSE_FILES_CHUNK_SIZE = 50
    }

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSource: DirectoryProperty

    init {
        inputFolder.convention(layout.projectDirectory.dir("inputs"))
        outputResources.convention(layout.buildDirectory.dir("generated/composeResources"))
        outputSource.convention(layout.buildDirectory.dir("generated/source"))
    }

    private val composeFileType = ClassName(PACKAGE_NAME, "ComposeFile")
    private val composeFolderType = ClassName(PACKAGE_NAME, "ComposeFile.Folder")
    private val listComposeFileType =
        List::class.asClassName().parameterizedBy(composeFileType)
    private val nullableIntType = Int::class.asTypeName().copy(nullable = true)

    @TaskAction
    fun process() =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1).use {
            val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
            val dispatcher = it.asCoroutineDispatcher()
            runBlocking(dispatcher) {
                // Copy over preserved pre-processed images
                listOf("2023", "2024", "2025").forEach {
                    val processed = inputFolder.dir("$it/processed").get().asFile
                    if (processed.exists()) {
                        val output = outputResources.dir("files/$it").get().asFile
                            .apply { mkdirs() }
                        processed.copyRecursively(
                            output,
                            overwrite = false,
                            onError = { file, exception ->
                                if (exception is FileAlreadyExistsException) {
                                    OnErrorAction.SKIP
                                } else {
                                    OnErrorAction.TERMINATE
                                }
                            },
                        )
                    }
                }

                val catalogs2023 = "catalogs2023" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2023/catalogs",
                    transformName = { it },
                )
                val rallies2023 = "rallies2023" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2023/rallies",
                    transformName = {
                        val parts = it.split("-").map { it.trim() }
                        "${parts[1]}${parts[0]}${parts[2]}"
                    },
                )
                val catalogs2024 = "catalogs2024" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2024/catalogs",
                    transformName = { it.substringBefore(" -") },
                )
                val rallies2024 = "rallies2024" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2024/rallies",
                    transformName = { it.replace(" - ", "").replace("'", "_") },
                )
                val catalogs2025 = "catalogs2025" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2025/catalogs",
                    transformName = { it.substringBefore(" -") },
                )
                val rallies2025 = "rallies2025" to processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2025/rallies",
                    transformName = { it.replace(" - ", "").replace("'", "_") },
                )

                buildComposeFiles(
                    catalogs2023,
                    rallies2023,
                    catalogs2024,
                    rallies2024,
                    catalogs2025,
                    rallies2025,
                )
            }
        }

    private suspend fun CoroutineScope.processFolder(
        imageCacheDir: File,
        path: String,
        transformName: (String) -> String,
    ): List<CatalogFolder> {
        val input = inputFolder.dir(path).get().asFile
        val output = outputResources.dir("files/$path").get().asFile
        return processFolders(
            imageCacheDir = imageCacheDir,
            inputFolder = input,
            outputFolder = output,
            transformName = transformName,
        )
    }

    private suspend fun CoroutineScope.processFolders(
        imageCacheDir: File,
        inputFolder: File,
        outputFolder: File,
        transformName: (String) -> String,
    ): List<CatalogFolder> {
        val folders = inputFolder.listFiles()
            .orEmpty()
            .flatMap { it.listFiles().filter { it.isDirectory }.ifEmpty { listOf(it) } }
            .map {
                async {
                    val images = it.listFiles()
                        .orEmpty()
                        .filter { it.isFile && it.extension in IMAGE_EXTENSIONS }
                        .sorted()
                        .mapIndexed { index, file ->
                            val (width, height, resized) = parseScaledImageWidthHeight(
                                imageCacheDir = imageCacheDir,
                                file = file
                            )
                            val hash = Utils.hash(
                                file = file,
                                RESIZE_TARGET,
                                WEBP_METHOD,
                                WEBP_TARGET_QUALITY
                            )
                            CatalogFolder.Image(
                                file = file,
                                width = width,
                                height = height,
                                resized = resized,
                                index = index,
                                hash = hash.toString(),
                            )
                        }
                    val name = transformName(it.name)
                    CatalogFolder(name, it, images)
                }
            }
            .awaitAll()

        val retainFolderNames = folders.map { it.name }.toSet()
        outputFolder.listFiles()
            .orEmpty()
            .filter { it.name !in retainFolderNames }
            .forEach(File::deleteRecursively)

        folders.map { catalogFolder ->
            async {
                val images = catalogFolder.images
                val catalogOutputFolder =
                    outputFolder.resolve(catalogFolder.name).apply { mkdirs() }

                val imagesWithOutput = images.map {
                    it to catalogOutputFolder.resolve(it.name)
                }

                val retainFileNames = imagesWithOutput.map { it.second.name }.toSet()
                catalogOutputFolder.listFiles()
                    .orEmpty()
                    .filter { it.name !in retainFileNames }
                    .forEach(File::deleteRecursively)

                imagesWithOutput.filter { !it.second.exists() }
            }
        }
            .awaitAll()
            .flatten()
            .map { (image, output) ->
                async {
                    compressAndRename(image, output)
                }
            }
            .awaitAll()

        return folders
    }

    private fun compressAndRename(image: CatalogFolder.Image, target: File) {
        val input = image.file
        logger.lifecycle("Compressing $input")
        val params = mutableListOf(
            "cwebp",
            "-af",
            "-q",
            WEBP_TARGET_QUALITY.toString(),
            "-m",
            WEBP_METHOD.toString(),
            input.absolutePath,
            "-o",
            target.absolutePath,
        )
        if (image.resized) {
            params += "-resize"
            params += image.width.toString()
            params += image.height.toString()
        }
        val success = ProcessBuilder(params)
            .redirectErrorStream(true)
            .start()
            .waitFor(30, TimeUnit.SECONDS)
        if (!success) {
            throw IllegalStateException("Failed to compress $input")
        }
    }

    private fun buildComposeFiles(
        vararg folders: Pair<String, List<CatalogFolder>>,
    ) {
        FileSpec.builder(PACKAGE_NAME, "ComposeFiles")
            .apply {
                addType(accessorType(this, *folders))
            }
            .addType(folderType())
            .build()
            .writeTo(outputSource.asFile.get())
    }

    private fun accessorType(
        fileSpec: FileSpec.Builder,
        vararg folders: Pair<String, List<CatalogFolder>>,
    ) = TypeSpec.objectBuilder("ComposeFiles")
        .apply {
            folders.forEach { (name, folders) ->
                addChunkedFolder(fileSpec, name, folders)
            }
        }
        .build()

    private fun TypeSpec.Builder.addChunkedFolder(
        fileSpec: FileSpec.Builder,
        name: String,
        folders: List<CatalogFolder>,
    ) = apply {
        val chunks = folders.chunked(COMPOSE_FILES_CHUNK_SIZE)
            .map {
                CodeBlock.Builder()
                    .addStatement("listOf(")
                    .apply {
                        it.forEach { folder ->
                            add(
                                """
                                ComposeFile.Folder(
                                    name = %S,
                                    files = listOf(
                                """.trimIndent(),
                                folder.name,
                            )
                            folder.images.forEach { image ->
                                addStatement(
                                    "ComposeFile.Image(name = %S, width = %L, height = %L),",
                                    image.name,
                                    image.width,
                                    image.height,
                                )
                            }
                            addStatement("),),")
                        }
                    }
                    .addStatement(")")
                    .build()
            }
        chunks.forEachIndexed { index, chunk ->
            fileSpec.addType(
                TypeSpec.objectBuilder("$name$index".capitalized())
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
            PropertySpec.builder(name, composeFolderType)
                .initializer(
                    CodeBlock.builder()
                        .apply {
                            add(
                                """
                                    ComposeFile.Folder(
                                        name = %S,
                                        files = 
                                """.trimIndent(), name
                            )
                            if (chunks.isEmpty()) {
                                add("emptyList()")
                            } else {
                                chunks.indices.forEach { index ->
                                    add("$name$index".capitalized() + ".files ")
                                    if (index != chunks.lastIndex) add(" + ")
                                }
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

    private fun parseFile(file: File, name: String = file.name): ComposeFile =
        if (file.isDirectory) {
            ComposeFile(
                name = name,
                file = file,
                files = file.listFiles().orEmpty().sorted().mapIndexed { index, child ->
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

    private fun parseScaledImageWidthHeight(
        imageCacheDir: File,
        file: File,
    ): Triple<Int, Int, Boolean> =
        file.extension
            .let(ImageIO::getImageReadersBySuffix)
            .asSequence()
            .firstNotNullOf { reader ->
                try {
                    file.inputStream().use {
                        FileCacheImageInputStream(it, imageCacheDir).use {
                            reader.setInput(it)
                            val imageWidth = reader.getWidth(reader.minIndex)
                            val imageHeight = reader.getHeight(reader.minIndex)

                            val width: Int
                            val height: Int
                            val resized: Boolean
                            if (imageWidth > imageHeight && imageWidth > RESIZE_TARGET) {
                                width = RESIZE_TARGET
                                height =
                                    (RESIZE_TARGET.toFloat() / imageWidth * imageHeight).toInt()
                                resized = true
                            } else if (imageHeight >= imageWidth && imageHeight > RESIZE_TARGET) {
                                width = (RESIZE_TARGET.toFloat() / imageHeight * imageWidth).toInt()
                                height = RESIZE_TARGET
                                resized = true
                            } else {
                                width = imageWidth
                                height = imageHeight
                                resized = false
                            }
                            Triple(width, height, resized)
                        }
                    }
                } catch (_: Throwable) {
                    null
                } finally {
                    reader.dispose()
                }
            }

    data class ComposeFile(
        val name: String,
        val file: File,
        val files: List<ComposeFile>,
    )

    data class CatalogFolder(
        val name: String,
        val folder: File,
        val images: List<Image>,
    ) {
        data class Image(
            val file: File,
            val width: Int,
            val height: Int,
            val resized: Boolean,
            val index: Int,
            val hash: String,
            val name: String = "${index.toString().padStart(2, '0')}-$hash.webp",
        )
    }
}
