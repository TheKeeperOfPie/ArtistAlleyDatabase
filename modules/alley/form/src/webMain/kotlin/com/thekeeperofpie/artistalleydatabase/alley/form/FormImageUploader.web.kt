package com.thekeeperofpie.artistalleydatabase.alley.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.form.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import kotlinx.browser.window
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.khronos.webgl.toUByteArray
import org.w3c.dom.CanvasImageSource
import org.w3c.dom.HIGH
import org.w3c.dom.ImageBitmapOptions
import org.w3c.dom.ResizeQuality
import org.w3c.files.Blob
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.Promise
import kotlin.js.js
import kotlin.js.set
import kotlin.js.unsafeCast
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class FormImageUploader(
    dispatchers: CustomDispatchers,
    private val formDatabase: AlleyFormDatabase,
    httpClient: HttpClient,
) : ImageUploader(dispatchers, httpClient) {
    private val canvas by lazy {
        OffscreenCanvas(width = 1, height = 1)
    }

    override suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid,
        localImages: List<EditImage.LocalImage>,
    ): Map<EditImage.LocalImage, String> {
        val presignedUrls = formDatabase.fetchUploadImageUrls(
            dataYear = dataYear,
            artistId = artistId,
            imageData = localImages.map {
                BackendFormRequest.UploadImageUrls.ImageData(
                    id = it.id,
                    extension = it.extension
                )
            },
        )

        return if (BuildKonfig.isWasmDebug) {
            localImages.associateWith {
                val id = it.id
                val key = EditImage.NetworkImage.makePrefix(dataYear, artistId.toString()) +
                        "/$id.${it.extension}"
                "${window.origin}/form/api/uploadImage/$key".also {
                    ConsoleLogger.log("Redirecting image upload from ${presignedUrls[id]} to $it")
                }
            }
        } else {
            localImages.associateWith { presignedUrls[it.id]!! }
        }
    }

    override fun imageFromIdAndKey(
        original: EditImage,
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage {
        val key = EditImage.NetworkImage.makeKey(dataYear, artistId, id, platformFile)
        return EditImage.NetworkImage(
            uri = Uri.parse(
                IMAGES_URL.ifBlank { "${window.origin}/edit/api/image" } + "/$key"
            ),
            key = key,
        )
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun compressImage(file: PlatformFile): ByteArray {
        val imageBitmap = file.toImageBitmap()
        canvas.width = imageBitmap.width
        canvas.height = imageBitmap.height
        val bytes = file.readBytes()
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
