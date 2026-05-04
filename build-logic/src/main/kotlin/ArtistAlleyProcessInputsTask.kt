import ImageUtils.parseScaledImageWidthHeight
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    abstract val imagesAnimeExpo2023Folder: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val imagesAnimeExpo2024Folder: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val imagesAnimeExpo2025Folder: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val imagesAnimeNyc2025Folder: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesAnimeExpo2023: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesAnimeExpo2024: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesAnimeExpo2025: DirectoryProperty

    @get:OutputDirectory
    abstract val outputImagesAnimeNyc2025: DirectoryProperty

    @get:OutputDirectory
    abstract val outputSource: DirectoryProperty

    init {
        val projectDirectory = layout.projectDirectory
        imagesAnimeExpo2023Folder.convention(projectDirectory.dir("images/2023"))
        imagesAnimeExpo2024Folder.convention(projectDirectory.dir("images/2024"))
        imagesAnimeExpo2025Folder.convention(projectDirectory.dir("images/2025"))
        imagesAnimeNyc2025Folder.convention(projectDirectory.dir("images/animeNyc2025"))

        val buildDirectory = layout.buildDirectory
        outputImagesAnimeExpo2023.convention(buildDirectory.dir("generated/composeResources/files/images/2023"))
        outputImagesAnimeExpo2024.convention(buildDirectory.dir("generated/composeResources/files/images/2024"))
        outputImagesAnimeExpo2025.convention(buildDirectory.dir("generated/composeResources/files/images/2025"))
        outputImagesAnimeNyc2025.convention(buildDirectory.dir("generated/composeResources/files/images/animeNyc2025"))
        outputSource.convention(buildDirectory.dir("generated/source"))
    }

    @Suppress("NewApi")
    @TaskAction
    fun process() {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1).use {
            val imageCacheDir = temporaryDir.resolve("imageCache").apply(File::mkdirs)
            val dispatcher = it.asCoroutineDispatcher()
            runBlocking(dispatcher) {
                // Copy over preserved pre-processed images
                listOf(
                    imagesAnimeExpo2023Folder to outputImagesAnimeExpo2023,
                    imagesAnimeExpo2024Folder to outputImagesAnimeExpo2024,
                    imagesAnimeExpo2025Folder to outputImagesAnimeExpo2025,
                    imagesAnimeNyc2025Folder to outputImagesAnimeNyc2025,
                ).forEach { (input, output) ->
                    val processed = input.dir("processed").get().asFile
                    if (processed.exists()) {
                        processed.copyRecursively(
                            output.get().asFile,
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

                trackStage("AnimeExpo2023Catalogs") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2023Folder.dir("catalogs").get().asFile,
                        outputImagesAnimeExpo2023.dir("catalogs").get().asFile,
                        transformName = transformCatalogName,
                        transformId = transformCatalogId,
                    )
                }
                trackStage("AnimeExpo2023Rallies") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2023Folder.dir("rallies").get().asFile,
                        outputImagesAnimeExpo2023.dir("rallies").get().asFile,
                        transformName = {
                            val parts = it.split("-").map { it.trim() }
                            "${parts[1]}${parts[0]}${parts[2]}"
                        },
                    )
                }
                trackStage("AnimeExpo2024Catalogs") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2024Folder.dir("catalogs").get().asFile,
                        outputImagesAnimeExpo2024.dir("catalogs").get().asFile,
                        transformName = transformCatalogName,
                        transformId = transformCatalogId,
                    )
                }
                trackStage("AnimeExpo2024Rallies") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2024Folder.dir("rallies").get().asFile,
                        outputImagesAnimeExpo2024.dir("rallies").get().asFile,
                        transformName = { it.replace(" - ", "").replace("'", "_") },
                    )
                }
                trackStage("AnimeExpo2025Catalogs") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2025Folder.dir("catalogs").get().asFile,
                        outputImagesAnimeExpo2025.dir("catalogs").get().asFile,
                        transformName = transformCatalogName,
                        transformId = transformCatalogId,
                    )
                }
                trackStage("AnimeExpo2025Rallies") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeExpo2025Folder.dir("rallies").get().asFile,
                        outputImagesAnimeExpo2025.dir("rallies").get().asFile,
                        transformName = { it.replace(" - ", "").replace("'", "_") },
                    )
                }
                trackStage("AnimeNyc2025Catalogs") {
                    processFolder(
                        imageCacheDir = imageCacheDir,
                        imagesAnimeNyc2025Folder.dir("catalogs").get().asFile,
                        outputImagesAnimeNyc2025.dir("catalogs").get().asFile,
                        transformName = transformCatalogName,
                        transformId = transformCatalogId,
                    )
                }
            }
        }
    }

    private suspend fun processFolder(
        imageCacheDir: File,
        inputFolder: File,
        outputFolder: File,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
        transformImageName: (index: Int, hash: String, name: String) -> String = { index, hash, _ ->
            "${index.toString().padStart(2, '0')}-$hash.webp"
        },
    ): List<CatalogFolder> {
        return processFolders(
            imageCacheDir = imageCacheDir,
            inputFolder = inputFolder,
            outputFolder = outputFolder,
            transformName = transformName,
            transformId = transformId,
            transformImageName = transformImageName,
        )
    }

    private suspend fun processFolders(
        imageCacheDir: File,
        inputFolder: File,
        outputFolder: File,
        transformName: (String) -> String,
        transformId: (String) -> String = transformName,
        transformImageName: (index: Int, hash: String, name: String) -> String,
    ): List<CatalogFolder> = coroutineScope {
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

        folders
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
