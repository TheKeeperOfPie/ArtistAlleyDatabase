package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformType
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.asBytes
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlin.uuid.Uuid

abstract class ImageUploader(
    private val dispatchers: CustomDispatchers,
    private val httpClient: HttpClient,
) {
    fun uploadImages(
        dataYear: DataYear,
        artistId: Uuid,
        images: List<EditImage>,
    ): Flow<UploadResult> {
        val imagesToUpload = images.filterIsInstance<EditImage.LocalImage>()
        if (imagesToUpload.isEmpty()) {
            return flowOf(UploadResult(images = images, errors = emptyMap(), finished = true))
        } else if (imagesToUpload.size > ImageUtils.MAX_UPLOAD_COUNT) {
            return flowOf(UploadResult(images = images, errors = imagesToUpload.associateWith {
                "Only ${ImageUtils.MAX_UPLOAD_COUNT} images are allowed"
            }, finished = true))
        }

        return flow {
            var result = UploadResult(images, emptyMap())
            suspend fun updateResult(
                images: List<EditImage>? = null,
                errors: Map<EditImage, String>? = null,
                finished: Boolean? = null,
            ) {
                result = result.copy(
                    images = images ?: result.images,
                    errors = errors ?: result.errors,
                    finished = finished ?: result.finished,
                )
                emit(result)
            }

            try {
                val presignedUrls = getPresignedImageUrls(dataYear, artistId, imagesToUpload)
                imagesToUpload.forEach { imageToUpload ->
                    val platformFile = PlatformImageCache[imageToUpload.key]
                    if (platformFile == null) {
                        updateResult(errors = result.errors + (imageToUpload to "Failed to read image"))
                        return@forEach
                    }
                    val imageUrl = presignedUrls[imageToUpload]
                    if (imageUrl == null) {
                        if (PlatformSpecificConfig.type == PlatformType.DESKTOP) {
                            return@forEach
                        } else {
                            requireNotNull(imageUrl) {
                                "Failed to get presigned URL for ${imageToUpload.key} ${platformFile.name}"
                            }
                        }
                    }
                    var size = platformFile.size().asBytes()
                    val bytes = if (size < ImageUtils.MAX_UPLOAD_SIZE) {
                        platformFile.readBytes()
                    } else {
                        var bytes: ByteArray? = null
                        var count = 0
                        while (size > ImageUtils.MAX_UPLOAD_SIZE && count < 3) {
                            val imageBytes = compressImage(file = platformFile)
                            bytes = imageBytes
                            if (imageBytes.size.asBytes() == size) {
                                ConsoleLogger.log("Failed to compress ${platformFile.name}")
                                break
                            }
                            size = imageBytes.size.asBytes()
                            count++
                        }
                        bytes
                    }
                    if (bytes == null) {
                        updateResult(errors = result.errors + (imageToUpload to "Failed to read image"))
                        return@forEach
                    }

                    val response = httpClient.put(imageUrl) {
                        contentType(ContentType.Application.OctetStream)
                        setBody(bytes)
                    }
                    val status = response.status
                    if (status != HttpStatusCode.OK) {
                        val errorMessage =
                            "Failed to upload image: $status, ${response.bodyAsText()}"
                        updateResult(errors = result.errors + (imageToUpload to errorMessage))
                        return@forEach
                    }
                    updateResult(
                        images = result.images.map {
                            if (imageToUpload == it) {
                                imageFromIdAndKey(
                                    original = it,
                                    dataYear = dataYear,
                                    artistId = artistId,
                                    platformFile = platformFile,
                                    id = imageToUpload.id,
                                )
                            } else {
                                it
                            }
                        },
                    )
                }
                updateResult(finished = true)
            } catch (t: Throwable) {
                updateResult(errors = imagesToUpload.associateWith {
                    t.message.orEmpty().ifEmpty { t.stackTraceToString() }
                })
            }
        }.flowOn(dispatchers.io)
    }

    protected abstract suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid,
        localImages: List<EditImage.LocalImage>,
    ): Map<EditImage.LocalImage, String>

    protected abstract fun imageFromIdAndKey(
        original: EditImage,
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage

    protected abstract suspend fun compressImage(file: PlatformFile): ByteArray

    data class UploadResult(
        val images: List<EditImage>,
        val errors: Map<EditImage, String>,
        val finished: Boolean = false,
    )

    protected companion object {
        const val IMAGES_URL = BuildKonfig.imagesUrl
    }
}
