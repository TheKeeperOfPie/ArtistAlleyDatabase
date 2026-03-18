package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageFileData
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageUploadUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.PresignedImageUrl
import com.thekeeperofpie.artistalleydatabase.alley.models.makeArtistKey
import com.thekeeperofpie.artistalleydatabase.alley.models.makeStampRallyKey
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import io.ktor.client.HttpClient
import kotlinx.browser.window
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.khronos.webgl.toUByteArray
import org.w3c.dom.CanvasImageSource
import org.w3c.dom.HIGH
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.ResizeQuality
import org.w3c.files.Blob
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.associate
import kotlin.collections.associateWith
import kotlin.collections.emptyMap
import kotlin.collections.flatMap
import kotlin.collections.forEachIndexed
import kotlin.collections.map
import kotlin.collections.mapValues
import kotlin.collections.plus
import kotlin.collections.toByteArray
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.Promise
import kotlin.js.js
import kotlin.js.set
import kotlin.js.unsafeCast
import kotlin.uuid.Uuid

abstract class WebImageUploader(httpClient: HttpClient) : ImageUploader(httpClient) {
    private val canvas by lazy {
        OffscreenCanvas(width = 1, height = 1)
    }

    abstract suspend fun fetchUploadImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        artistImageData: List<ImageFileData>,
        stampRallyIdsToImageData: Map<String, List<ImageFileData>>,
    ): Response

    final override suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        localImages: List<PrepareImageResult.Success>,
        stampRallyIdsToLocalImages: Map<String, List<PrepareImageResult.Success>>,
    ): Map<EditImage.LocalImage, PresignedImageUrl> {
        val response = fetchUploadImageUrls(
            dataYear = dataYear,
            artistId = artistId,
            artistImageData = localImages.map { it.toImageFileData() },
            stampRallyIdsToImageData = stampRallyIdsToLocalImages.mapValues {
                it.value.map { it.toImageFileData() }
            },
        )
        return when (response) {
            is Response.Failed -> emptyMap()
            is Response.Success -> {
                @Suppress("SimplifyBooleanWithConstants")
                val artistUrls = if (BuildKonfig.isWasmDebug && artistId != null) {
                    localImages.associateWith {
                        val key = ImageUploadUtils.makeArtistKey(
                            dataYear = dataYear,
                            artistId = artistId,
                            imageId = Uuid.random(),
                            extension = it.original.extension,
                        )
                        val url = "${window.origin}/form/api/uploadImage/$key"
                        ConsoleLogger.log("Redirecting image upload from ${response.artistUrls[it.original.id]} to $url")
                        PresignedImageUrl(key = key, url = url)
                    }
                } else {
                    localImages.associateWith { response.artistUrls[it.original.id]!! }
                }

                val stampRallyUrls = if (BuildKonfig.isWasmDebug) {
                    stampRallyIdsToLocalImages.mapValues {
                        val stampRallyId = it.key
                        it.value.associateWith {
                            val key = ImageUploadUtils.makeStampRallyKey(
                                dataYear = dataYear,
                                stampRallyId = stampRallyId,
                                imageId = Uuid.random(),
                                extension = it.original.extension,
                            )
                            val url = "${window.origin}/form/api/uploadImage/$key"
                            ConsoleLogger.log("Redirecting image upload from ${response.stampRallyUrls[stampRallyId]!![it.original.id]} to $url")
                            PresignedImageUrl(key = key, url = url)
                        }
                    }
                } else {
                    stampRallyIdsToLocalImages.mapValues {
                        val stampRallyId = it.key
                        it.value.associateWith { response.stampRallyUrls[stampRallyId]!![it.original.id]!! }
                    }
                }

                (artistUrls.entries + stampRallyUrls.flatMap { it.value.entries })
                    .associate { it.key.original to it.value }
            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun compressImage(bytes: ByteArray): ByteArray {
        val imageBitmap = bytes.decodeToImageBitmap()
        canvas.width = imageBitmap.width
        canvas.height = imageBitmap.height
        val context = canvas.getContext("2d", OffscreenCanvasAttributes(true))
            ?: return bytes
        val webBitmap = window.createImageBitmap(
            image = bytes.toBlob(),
            options = ImageBitmapOptions(
                resizeWidth = imageBitmap.width,
                resizeHeight = imageBitmap.height,
                resizeQuality = ResizeQuality.HIGH,
            ),
        ).await()
        return try {
            context.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
            context.drawImage(webBitmap, 0.0, 0.0)
            canvas.convertToBlob(ConvertToBlobOptions(type = "image/jpeg", quality = 0.8))
                .await()
                .unsafeCast<BlobWithByteArray>()
                .bytes()
                .await()
                .toUByteArray()
                .toByteArray()
        } catch (t: Throwable) {
            t.printStackTrace()
            bytes
        } finally {
            webBitmap.close()
        }
    }

    private fun ByteArray.toBlob(): Blob {
        val array = Uint8Array(size)
        forEachIndexed { index, byte ->
            array[index] = byte
        }
        val blobParts = JsArray<JsAny?>()
        blobParts[0] = array
        return Blob(blobParts)
    }

    sealed interface Response {
        data class Success(
            val artistUrls: Map<Uuid, PresignedImageUrl>,
            val stampRallyUrls: Map<String, Map<Uuid, PresignedImageUrl>>,
        ) : Response

        data class Failed(val errorMessage: String) : Response
    }
}

private external interface BlobWithByteArray : JsAny {
    fun bytes(): Promise<Uint8Array>
}

private external class OffscreenCanvas(width: Int, height: Int) {
    fun getContext(
        contextType: String,
        contextAttributes: OffscreenCanvasAttributes,
    ): OffscreenCanvasRenderingContext2D?

    fun convertToBlob(options: ConvertToBlobOptions): Promise<Blob>

    var width: Int
    var height: Int
}

private external interface OffscreenCanvasAttributes {
    val willReadFrequently: Boolean?
}

@Suppress("unused")
private fun OffscreenCanvasAttributes(willReadFrequently: Boolean): OffscreenCanvasAttributes =
    js("({ willReadFrequently: willReadFrequently })")

private external interface ConvertToBlobOptions {
    var type: String
    var quality: Double
}

@Suppress("unused")
private fun ConvertToBlobOptions(type: String, quality: Double): ConvertToBlobOptions =
    js("({ type: type, quality: quality })")

private external interface OffscreenCanvasRenderingContext2D {
    fun drawImage(image: CanvasImageSource, dx: Double, dy: Double)
    fun clearRect(x: Double, y: Double, w: Double, h: Double)
}
