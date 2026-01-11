@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import coil3.Bitmap
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.decode.SkiaImageDecoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.maxBitmapSize
import coil3.size.Precision
import coil3.util.component1
import coil3.util.component2
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import kotlinx.browser.window
import okio.use
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasImageSource
import org.w3c.dom.HIGH
import org.w3c.dom.ImageBitmap
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.ImageData
import org.w3c.dom.ResizeQuality
import org.w3c.files.Blob
import kotlin.collections.indices
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsBoolean
import kotlin.js.js
import kotlin.js.set
import kotlin.js.toBoolean
import kotlin.time.measureTimedValue

private val offscreenCanvasWrapper by lazy { OffscreenCanvasWrapper.create() }

/**
 * Attempt to improve image loading speed, ported from
 * https://github.com/coil-kt/coil/issues/3226#issuecomment-3556608163
 */
class ImageWithDimensionsDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    companion object {
        fun create(
            result: SourceFetchResult,
            options: Options,
            @Suppress("unused")
            imageLoader: ImageLoader,
        ): Decoder? {
            val source = result.source
            return if (source.metadata is ImageWithDimensionsFetcher.ImageMetadata) {
                ImageWithDimensionsDecoder(source, options)
            } else {
                null
            }
        }
    }

    override suspend fun decode(): DecodeResult {
        val imageMetadata = source.metadata as? ImageWithDimensionsFetcher.ImageMetadata
            ?: return SkiaImageDecoder(source, options).decode().also {
                ConsoleLogger.log("Failed to get ImageMetadata")
            }

        val offscreenCanvasWrapper =
            offscreenCanvasWrapper ?: return SkiaImageDecoder(source, options).decode().also {
                ConsoleLogger.log("Failed to get offscreenCanvasWrapper")
            }
        val blob: Blob = source.source()
            .use { it.readByteArray() }
            .asBlob()

        val (value, time) = measureTimedValue {
            val srcWidth = imageMetadata.width
            val srcHeight = imageMetadata.height

            val (dstWidth, dstHeight) = DecodeUtils.computeDstSize(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                targetSize = options.size,
                scale = options.scale,
                maxSize = options.maxBitmapSize,
            )

            var multiplier = DecodeUtils.computeSizeMultiplier(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                dstWidth = dstWidth,
                dstHeight = dstHeight,
                scale = options.scale,
            )

            if (options.precision == Precision.INEXACT) {
                multiplier = multiplier.coerceAtMost(1.0)
            }

            val outWidth = (multiplier * srcWidth).toInt()
            val outHeight = (multiplier * srcHeight).toInt()

            val (imageBitmap, imageBitmapTime) = measureTimedValue {
                window.createImageBitmap(
                    image = blob,
                    options = ImageBitmapOptions(
                        resizeWidth = outWidth,
                        resizeHeight = outHeight,
                        resizeQuality = ResizeQuality.Companion.HIGH
                    ),
                ).await()
            }

            val bitmap = convertImageBitmapToSkiaBitmap(offscreenCanvasWrapper, imageBitmap)
            imageBitmap.close()
            val isSampled = outWidth < srcWidth || outHeight < srcHeight

            imageBitmapTime to DecodeResult(
                image = bitmap.asImage(),
                isSampled = isSampled,
            )
        }
        ConsoleLogger.log("convert bitmap timing: ${time - value.first}")

        return value.second
    }

    private fun convertImageBitmapToSkiaBitmap(
        wrapper: OffscreenCanvasWrapper,
        imageBitmap: ImageBitmap,
    ): Bitmap {
        wrapper.canvas.width = imageBitmap.width
        wrapper.canvas.height = imageBitmap.height
        wrapper.context.drawImage(imageBitmap, 0f, 0f)
        val imageData = wrapper.context.getImageData(
            sx = 0f,
            sy = 0f,
            sw = wrapper.canvas.width.toFloat(),
            sh = wrapper.canvas.height.toFloat()
        )
        wrapper.clear()
        return convertImageDataToSkiaBitmap(imageData)
    }

    private fun convertImageDataToSkiaBitmap(imageData: ImageData): Bitmap {
        val width = imageData.width
        val height = imageData.height
        val pixelData: Uint8ClampedArray = imageData.data

        val bitmap = coil3.Bitmap()
        val imageInfo = ImageInfo(
            width = width,
            height = height,
            colorType = ColorType.RGBA_8888,
            alphaType = ColorAlphaType.UNPREMUL
        )
        val pixels = pixelData.toByteArray()
        bitmap.installPixels(imageInfo, pixels, width * 4)
        bitmap.setImmutable()

        return bitmap
    }

    private fun ByteArray.asBlob(): Blob {
        val uint8 = this.toUint8Array()
        val jsParts = JsArray<JsAny?>()
        jsParts[0] = uint8
        return Blob(jsParts)
    }

    private fun ByteArray.toUint8Array(): Uint8Array {
        val result = Uint8Array(this.size)
        for (i in this.indices) {
            result[i] = ((this[i].toInt() and 0xFF).toByte())
        }
        return result
    }

    private fun Uint8ClampedArray.toByteArray(): ByteArray {
        return ByteArray(this.length) { i ->
            this@toByteArray[i]
        }
    }
}

private class OffscreenCanvasWrapper(
    val canvas: OffscreenCanvas,
) {
    companion object {
        fun create(): OffscreenCanvasWrapper? {
            return try {
                if (!offscreenCanvasAvailable().toBoolean()) return null
                val canvas = OffscreenCanvas(width = 1, height = 1)
                OffscreenCanvasWrapper(canvas)
            } catch (_: Throwable) {
                null
            }
        }
    }

    val context =
        canvas.getContext(contextId = "2d", contextAttributes = contextAttributes())!!

    fun clear() {
        context.clearRect(
            x = 0f,
            y = 0f,
            width = canvas.width.toFloat(),
            height = canvas.height.toFloat(),
        )
    }
}

private fun contextAttributes(): JsAny = js("({ willReadFrequently: true })")

external class OffscreenCanvas(width: Int, height: Int) {
    fun getContext(contextId: String, contextAttributes: JsAny?): OffscreenCanvasRenderingContext2D?
    var width: Int
    var height: Int
}

external interface OffscreenCanvasRenderingContext2D {
    fun drawImage(image: CanvasImageSource, dx: Float, dy: Float)
    fun getImageData(sx: Float, sy: Float, sw: Float, sh: Float): ImageData
    fun clearRect(x: Float, y: Float, width: Float, height: Float)
}

private fun offscreenCanvasAvailable(): JsBoolean =
    js("typeof OffscreenCanvas !== 'undefined'")
