package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
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
import kotlinx.coroutines.withContext
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
        }

        return flow {
            withContext(dispatchers.io) {
                var result = UploadResult(images, emptyMap())
                suspend fun updateResult(
                    images: List<EditImage> = result.images,
                    errors: Map<EditImage, String> = result.errors,
                    finished: Boolean = result.finished,
                ) {
                    result = result.copy(images = images, errors = errors, finished = finished)
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
                        val bytes = platformFile.readBytes()
                        val imageUrl = presignedUrls[imageToUpload]!!
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
                    updateResult(errors = imagesToUpload.associateWith { t.message.orEmpty() })
                }
            }
        }
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

    data class UploadResult(
        val images: List<EditImage>,
        val errors: Map<EditImage, String>,
        val finished: Boolean = false,
    )

    protected companion object {
        const val IMAGES_URL = BuildKonfig.imagesUrl
    }
}
