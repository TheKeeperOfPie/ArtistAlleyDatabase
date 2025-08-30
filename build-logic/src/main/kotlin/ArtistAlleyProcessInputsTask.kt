
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
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream
import javax.inject.Inject

@CacheableTask
abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "bmp", "webp")
        private const val RESIZE_TARGET = 1200
        private const val WEBP_TARGET_QUALITY = 80
        private const val WEBP_METHOD = 6

        internal fun parseScaledImageWidthHeight(
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

    @Suppress("NewApi")
    @TaskAction
    fun process() =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1).use {
            val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
            val dispatcher = it.asCoroutineDispatcher()
            runBlocking(dispatcher) {
                // Copy over preserved pre-processed images
                listOf("2023", "2024", "2025", "animeNyc2024", "animeNyc2025").forEach {
                    val processed = inputFolder.dir("$it/processed").get().asFile
                    if (processed.exists()) {
                        val output = outputResources.dir("files/$it").get().asFile
                            .apply { mkdirs() }
                        processed.copyRecursively(
                            output,
                            overwrite = false,
                            onError = { _, exception ->
                                if (exception is FileAlreadyExistsException) {
                                    OnErrorAction.SKIP
                                } else {
                                    OnErrorAction.TERMINATE
                                }
                            },
                        )
                    }
                }

                val transformCatalogId: (String) -> String = {
                    it.split("-")
                        .takeLast(5)
                        .joinToString(separator = "-")
                        .trim()
                        .also { UUID.fromString(it) }
                }

                val transformCatalogName: (String) -> String = {
                    val booth = it.substringBefore("-").trim()
                    val uuid = transformCatalogId(it)
                    "$booth - $uuid"
                }

                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2023/catalogs",
                    transformName = transformCatalogName,
                    transformId = transformCatalogId,
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2023/rallies",
                    transformName = {
                        val parts = it.split("-").map { it.trim() }
                        "${parts[1]}${parts[0]}${parts[2]}"
                    },
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2024/catalogs",
                    transformName = transformCatalogName,
                    transformId = transformCatalogId,
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2024/rallies",
                    transformName = { it.replace(" - ", "").replace("'", "_") },
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2025/catalogs",
                    transformName = transformCatalogName,
                    transformId = transformCatalogId,
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "2025/rallies",
                    transformName = { it.replace(" - ", "").replace("'", "_") },
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "animeNyc2024/catalogs",
                    transformName = transformCatalogName,
                    transformId = transformCatalogId,
                )
                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "animeNyc2025/catalogs",
                    transformName = transformCatalogName,
                    transformId = transformCatalogId,
                )
            }
        }

    private suspend fun CoroutineScope.processFolder(
        imageCacheDir: File,
        path: String,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
    ): List<CatalogFolder> {
        val input = inputFolder.dir(path).get().asFile
        val output = outputResources.dir("files/$path").get().asFile
        return processFolders(
            imageCacheDir = imageCacheDir,
            inputFolder = input,
            outputFolder = output,
            transformName = transformName,
            transformId = transformId,
        )
    }

    private suspend fun CoroutineScope.processFolders(
        imageCacheDir: File,
        inputFolder: File,
        outputFolder: File,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
    ): List<CatalogFolder> {
        val folders = inputFolder.listFiles()
            .orEmpty()
            .flatMap { it.listFiles().filter { it.isDirectory }.ifEmpty { listOf(it) } }
            .map {
                async {
                    val images = it.listFiles()
                        .orEmpty()
                        .filter { file ->
                            file.isFile && IMAGE_EXTENSIONS.any {
                                it.equals(file.extension, ignoreCase = true)
                            }
                        }
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
                    val id = transformId(it.name)
                    CatalogFolder(id, name, it, images)
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

    data class CatalogFolder(
        val id: String,
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
