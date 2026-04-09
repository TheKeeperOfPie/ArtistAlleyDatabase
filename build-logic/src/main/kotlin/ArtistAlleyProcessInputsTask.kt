
import ImageUtils.parseScaledImageWidthHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject

@CacheableTask
abstract class ArtistAlleyProcessInputsTask : DefaultTask() {

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputsFolder: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val imagesFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImages: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSource: DirectoryProperty

    init {
        inputsFolder.convention(layout.projectDirectory.dir("inputs"))
        imagesFolder.convention(layout.projectDirectory.dir("images"))
        outputImages.convention(layout.buildDirectory.dir("generated/composeResources/files/images"))
        outputSource.convention(layout.buildDirectory.dir("generated/source"))
    }

    @Suppress("NewApi")
    @TaskAction
    fun process() {
        if (!inputsFolder.get().asFile.exists()) return
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1).use {
            val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
            val dispatcher = it.asCoroutineDispatcher()
            runBlocking(dispatcher) {
                // Copy over preserved pre-processed images
                listOf(
                    "2023",
                    "2024",
                    "2025",
                    "animeExpo2026",
                    "animeNyc2024",
                    "animeNyc2025"
                ).forEach {
                    val processed = imagesFolder.dir("$it/processed").get().asFile
                    if (processed.exists()) {
                        val output = outputImages.dir(it).get().asFile.apply { mkdirs() }
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

                processFolder(
                    imageCacheDir = imageCacheDir,
                    path = "animeExpo2026/catalogs",
                    transformName = { it },
                    transformImageName = { index, hash, name ->
                        "${index.toString().padStart(2, '0')}-$name-$hash.webp"
                    },
                )
            }
        }
    }

    private suspend fun CoroutineScope.processFolder(
        imageCacheDir: File,
        path: String,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
        transformImageName: (index: Int, hash: String, name: String) -> String = { index, hash, _ ->
            "${index.toString().padStart(2, '0')}-$hash.webp"
        },
    ): List<CatalogFolder> {
        val input = imagesFolder.dir(path).get().asFile
        val output = outputImages.dir(path).get().asFile
        return processFolders(
            imageCacheDir = imageCacheDir,
            inputFolder = input,
            outputFolder = output,
            transformName = transformName,
            transformId = transformId,
            transformImageName = transformImageName,
        )
    }

    private suspend fun CoroutineScope.processFolders(
        imageCacheDir: File,
        inputFolder: File,
        outputFolder: File,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
        transformImageName: (index: Int, hash: String, name: String) -> String,
    ): List<CatalogFolder> {
        val folders = inputFolder.listFiles()
            .orEmpty()
            .flatMap { it.listFiles().filter { it.isDirectory }.ifEmpty { listOf(it) } }
            .map {
                async {
                    val images = it.listFiles()
                        .orEmpty()
                        .filter { file ->
                            file.isFile && ImageUtils.IMAGE_EXTENSIONS.any {
                                it.equals(file.extension, ignoreCase = true)
                            }
                        }
                        .sorted()
                        .mapIndexed { index, file ->
                            val (width, height, resized) = parseScaledImageWidthHeight(
                                logger = logger,
                                imageCacheDir = imageCacheDir,
                                file = file
                            )
                            val hash = ImageUtils.hash(file)
                            CatalogFolder.Image(
                                file = file,
                                width = width,
                                height = height,
                                resized = resized,
                                index = index,
                                hash = hash,
                                name = transformImageName(index, hash, file.name)
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
                    ImageUtils.compressAndRename(
                        logger = logger,
                        input = image.file,
                        resized = image.resized,
                        width = image.width,
                        height = image.height,
                        target = output,
                    )
                }
            }
            .awaitAll()

        return folders
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
            val name: String,
        )
    }
}
