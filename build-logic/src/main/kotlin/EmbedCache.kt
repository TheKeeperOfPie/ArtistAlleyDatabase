
import ImageUtils.parseScaledImageWidthHeight
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parseInputStream
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.logging.Logger
import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds

private val DIMENSION_RANGE = 30..4000
private val REQUEST_THROTTLE = 2.seconds

internal class EmbedCache(
    private val logger: Logger,
    inputFolder: File,
    private val outputJsonFile: File,
    private val workingImagesFolder: File,
) {
    private val cache: MutableMap<String, EmbedImage?>
    private val ignored: MutableSet<String>
    private val json = Json { prettyPrint = true }

    init {
        val initialFile = inputFolder.resolve("embeds.json")
        if (initialFile.exists()) {
            val embeds = initialFile.inputStream()
                .use { json.decodeFromStream<Embeds>(it) }
            cache = embeds.images
                .mapValues {
                    val value = it.value ?: return@mapValues null
                    if (value.failureReason == null &&
                        value.width != null && value.height != null &&
                        (value.width !in DIMENSION_RANGE || value.height !in DIMENSION_RANGE)
                    ) {
                        value.copy(
                            fileName = null,
                            fileHash = null,
                            width = null,
                            height = null,
                            failureReason = EmbedFailureReason.DIMENSIONS,
                        )
                    } else {
                        value
                    }
                }
                .toMutableMap()
            ignored = embeds.ignored.toMutableSet()
        } else {
            cache = mutableMapOf()
            ignored = mutableSetOf()
        }
        logger.lifecycle("Loaded ${cache.size} embeds from cache at $initialFile")

        val inputImagesFolder = inputFolder.resolve("images")
        val missingImages = inputImagesFolder.list()?.toSet().orEmpty() -
                workingImagesFolder.list()?.toSet().orEmpty()
        missingImages.forEach {
            inputImagesFolder.resolve(it)
                .copyTo(workingImagesFolder.resolve(it))
        }
    }

    suspend fun getEmbedCatalogImage(link: String): Pair<String, CatalogImage>? {
        val targetLink = link.lowercase()
        if (cache.contains(targetLink)) {
            val cached = cache[targetLink]
            if (ignored.contains(cached?.link)) return null
            if (cached != null) {
                if (cached.failureReason == null && (cached.fileName == null ||
                            !workingImagesFolder.resolve(cached.fileName).exists())
                ) {
                    delay(REQUEST_THROTTLE)
                    val fetchResult = fetchEmbedImage(cached.link)
                    val embedImage = EmbedImage(link = cached.link, fetchResult = fetchResult)
                    cache[targetLink] = embedImage
                    return embedImage.linkAndCatalogImage
                }
            }
            return cached?.linkAndCatalogImage
        }

        delay(REQUEST_THROTTLE)
        logger.lifecycle("Fetching embed for $link")
        val metadata = try {
            Jsoup.connect(link)
                .userAgent("OpenGraph-Bot")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .followRedirects(true)
                .execute()
                .bodyStream()
                .use {
                    parseMetaData(element = Ksoup.parseInputStream(it, link))
                }
        } catch (_: Throwable) {
            null
        }

        val image = when {
            metadata?.twitterImage != null -> {
                Triple(
                    metadata.twitterImage,
                    metadata.twitterImageWidth,
                    metadata.twitterImageHeight,
                )
            }
            metadata?.ogImage != null -> {
                Triple(
                    metadata.ogImage,
                    metadata.ogImageWidth,
                    metadata.ogImageHeight,
                )
            }
            else -> null
        }
        if (image == null || image.first.isBlank()) {
            cache[targetLink] = null
            return null
        }
        val (imageLink, width, height) = image
        if (width != null && height != null &&
            (width !in DIMENSION_RANGE || height !in DIMENSION_RANGE)
        ) {
            val embedImage = EmbedImage(
                link = imageLink,
                fetchResult = FetchResult(failureReason = EmbedFailureReason.DIMENSIONS),
            )
            cache[targetLink] = embedImage
            return null
        }

        // Fetch first before checking ignored to ensure value is cached
        val fetchResult = fetchEmbedImage(imageLink)
        if (ignored.contains(imageLink)) return null

        val embedImage = EmbedImage(link = imageLink, fetchResult = fetchResult)
        cache[targetLink] = embedImage
        return embedImage.linkAndCatalogImage
    }

    suspend fun finalizeCache(
        scope: CoroutineScope,
        imageCacheDir: File,
        embedImagesOutputFolder: File,
    ) {
        outputJsonFile.resolve("embeds.json").outputStream().use {
            it.writer().use {
                it.write(
                    json.encodeToString<Embeds>(
                        Embeds(
                            cache.toSortedMap(),
                            ignored.toSortedSet(),
                        )
                    )
                )
            }
        }
        val expectedFiles = cache.map { (_, embed) ->
            embed?.resourceFileName.takeUnless { ignored.contains(embed?.link) }
        }.toSet()
        embedImagesOutputFolder.list()
            .filterNot(expectedFiles::contains)
            .forEach {
                logger.lifecycle("Deleting unexpected embed $it")
                embedImagesOutputFolder.resolve(it).delete()
            }
        cache.values
            .filterNotNull()
            .filterNot { ignored.contains(it.link) }
            .map {
                scope.async {
                    val imageFile = it.fileName?.let(workingImagesFolder::resolve)
                    if (imageFile?.exists() == true) {
                        val targetFile = embedImagesOutputFolder.resolve(it.resourceFileName)
                        if (!targetFile.exists()) {
                            val (width, height, resized) = parseScaledImageWidthHeight(
                                logger = logger,
                                imageCacheDir = imageCacheDir,
                                file = imageFile,
                            )
                            ImageUtils.compressAndRename(
                                logger = logger,
                                input = imageFile,
                                resized = resized,
                                width = width,
                                height = height,
                                target = targetFile,
                            )
                        }
                    }
                }
            }
            .awaitAll()
    }

    private suspend fun fetchEmbedImage(imageLink: String) = withContext(Dispatchers.IO) {
        val fileName = "${imageLink.hashCode()}.webp"
        val outputFile = workingImagesFolder.resolve(fileName)
        if (outputFile.exists()) {
            val image = ImageIO.read(outputFile)
            return@withContext if (image == null) {
                logger.lifecycle("Failed to read $outputFile")
                FetchResult(failureReason = EmbedFailureReason.READ)
            } else if (image.width !in DIMENSION_RANGE || image.height !in DIMENSION_RANGE) {
                FetchResult(failureReason = EmbedFailureReason.DIMENSIONS)
            } else {
                FetchResult(
                    fileName = fileName,
                    fileHash = ImageUtils.hash(outputFile),
                    width = image.width,
                    height = image.height,
                    failureReason = null,
                )
            }
        }

        logger.lifecycle("Fetching embed image for $imageLink")
        val image = try {
            ImageIO.read(URI(imageLink).toURL())
        } catch (t: Throwable) {
            logger.lifecycle("Failed to fetch embed for $fileName: $imageLink", t)
            return@withContext FetchResult(failureReason = EmbedFailureReason.FETCH)
        }

        try {
            ImageIO.write(image, "webp", outputFile)
        } catch (t: Throwable) {
            logger.lifecycle("Failed to write embed for $fileName: $imageLink", t)
            return@withContext FetchResult(failureReason = EmbedFailureReason.WRITE)
        }

        FetchResult(
            fileName = fileName,
            fileHash = ImageUtils.hash(outputFile),
            width = image.width,
            height = image.height,
        )
    }

    private fun parseMetaData(element: Element): EmbedMetadata {
        val document = (element as? Document)?.headOrNull() ?: element
        return EmbedMetadata(
            ogImage = document.selectFirst("meta[property=og:image]")
                ?.attr("content"),
            ogImageWidth = document.selectFirst("meta[property=og:image:width]")
                ?.attr("content")
                ?.toIntOrNull(),
            ogImageHeight = document.selectFirst("meta[property=og:image:height]")
                ?.attr("content")
                ?.toIntOrNull(),
            twitterImage = document.selectFirst("meta[name=twitter:image]")
                ?.attr("content"),
            twitterImageWidth = document.selectFirst("meta[property=twitter:image:width]")
                ?.attr("content")
                ?.toIntOrNull(),
            twitterImageHeight = document.selectFirst("meta[property=twitter:image:height]")
                ?.attr("content")
                ?.toIntOrNull(),
        )
    }

    @Serializable
    data class EmbedMetadata(
        val ogImage: String?,
        val ogImageWidth: Int?,
        val ogImageHeight: Int?,
        val twitterImage: String?,
        val twitterImageWidth: Int?,
        val twitterImageHeight: Int?,
    )

    @Serializable
    data class EmbedImage(
        val link: String,
        val fileName: String?,
        val fileHash: String?,
        val width: Int?,
        val height: Int?,
        val failureReason: EmbedFailureReason? = null,
    ) {
        internal constructor(link: String, fetchResult: FetchResult) : this(
            link = link,
            fileName = fetchResult.fileName,
            fileHash = fetchResult.fileHash,
            width = fetchResult.width,
            height = fetchResult.height,
            failureReason = fetchResult.failureReason,
        )

        val resourceFileName =
            "embed-${fileName?.substringBeforeLast(".")}-${fileHash}.webp"

        val linkAndCatalogImage =
            fileName?.let { link to CatalogImage(name = resourceFileName, width = width, height = height) }
    }

    @Serializable
    data class FetchResult(
        val fileName: String?,
        val fileHash: String?,
        val width: Int?,
        val height: Int?,
        val failureReason: EmbedFailureReason? = null,
    ) {
        constructor(failureReason: EmbedFailureReason) : this(
            fileName = null,
            fileHash = null,
            width = null,
            height = null,
            failureReason = failureReason,
        )
    }

    enum class EmbedFailureReason {
        FETCH, READ, WRITE, DIMENSIONS, EXCLUDED
    }

    @Serializable
    private data class Embeds(
        val images: Map<String, EmbedImage?>,
        val ignored: Set<String>,
    )
}
