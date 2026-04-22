
import com.sksamuel.scrimage.webp.CWebpHandler
import com.sksamuel.scrimage.webp.WebpImageReader
import org.gradle.api.logging.Logger
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream

internal object ImageUtils {
    val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "bmp", "webp")
    const val RESIZE_TARGET = 1500
    const val WEBP_TARGET_QUALITY = 80
    const val WEBP_METHOD = 6

    val cwebpPath by lazy {
        val field = CWebpHandler::class.java.getDeclaredField("binary")
        field.trySetAccessible()
        field.get(null) as Path
    }

    fun parseScaledImageWidthHeight(
        logger: Logger,
        imageCacheDir: File,
        file: File,
    ): Triple<Int, Int, Boolean> = try {
        file.inputStream().use {
            FileCacheImageInputStream(it, imageCacheDir).use {
                val reader = ImageIO.getImageReaders(it).asSequence().firstOrNull()
                    ?: ImageIO.getImageReadersByMIMEType("image/${file.extension}").asSequence().firstOrNull()
                try {
                    val imageWidth: Int
                    val imageHeight: Int
                    if (reader == null) {
                        val imageReader = WebpImageReader()
                        val image = imageReader.read(file.readBytes())
                        imageWidth = image.width
                        imageHeight = image.height
                    } else {
                        reader.setInput(it)
                        imageWidth = reader.getWidth(reader.minIndex)
                        imageHeight = reader.getHeight(reader.minIndex)
                    }

                    val width: Int
                    val height: Int
                    val resized: Boolean
                    if (imageWidth > imageHeight && imageWidth > RESIZE_TARGET) {
                        width = RESIZE_TARGET
                        height =
                            (RESIZE_TARGET.toFloat() / imageWidth * imageHeight).toInt()
                        resized = true
                    } else if (imageHeight >= imageWidth && imageHeight > RESIZE_TARGET) {
                        width =
                            (RESIZE_TARGET.toFloat() / imageHeight * imageWidth).toInt()
                        height = RESIZE_TARGET
                        resized = true
                    } else {
                        width = imageWidth
                        height = imageHeight
                        resized = false
                    }
                    Triple(width, height, resized)
                } finally {
                    reader?.dispose()
                }
            }
        }
    } catch (t: Throwable) {
        logger.error("Failed to read $file", t)
        throw t
    }

    fun hash(file: File) = Utils.hash(
        file = file,
        RESIZE_TARGET,
        WEBP_METHOD,
        WEBP_TARGET_QUALITY
    ).toString()

    fun compressAndRename(
        logger: Logger,
        input: File,
        resized: Boolean,
        width: Int,
        height: Int,
        target: File,
    ) {
        logger.lifecycle("Compressing $input")
        val params = mutableListOf(
            cwebpPath.toAbsolutePath().toString(),
            "-af",
            "-q",
            WEBP_TARGET_QUALITY.toString(),
            "-m",
            WEBP_METHOD.toString(),
            "-noalpha",
            input.absolutePath,
            "-o",
            target.absolutePath,
        )
        if (resized) {
            params += "-resize"
            params += width.toString()
            params += height.toString()
        }
        val success = ProcessBuilder(params)
            .redirectErrorStream(true)
            .start()
            .waitFor(30, TimeUnit.SECONDS)
        if (!success) {
            throw IllegalStateException("Failed to compress $input")
        }
    }
}
