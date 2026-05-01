package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformType
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageFileData
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageUploadUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.PresignedImageUrl
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.asBytes
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
import kotlin.uuid.Uuid

abstract class ImageUploader(private val httpClient: HttpClient) {
    suspend fun uploadImages(
        dataYear: DataYear,
        artistId: Uuid?,
        artistImages: List<EditImage>,
        stampRallyImages: Map<String, List<EditImage>>,
    ): UploadResult {
        when (val validateResult = validateInput(artistImages, stampRallyImages)) {
            ValidateResult.Empty -> return UploadResult.Empty
            is ValidateResult.Error -> return UploadResult.Error(validateResult.message)
            ValidateResult.Success -> Unit
        }

        val (preparedArtistImages, preparedStampRallyImages) =
            when (val prepareResult = prepareImages(artistImages, stampRallyImages)) {
                is PrepareResult.Error -> return UploadResult.Error(prepareResult.message)
                is PrepareResult.Success -> prepareResult
            }

        val localImageToUploadUrl = requestPresignedUrls(
            dataYear = dataYear,
            artistId = artistId,
            preparedArtistImages = preparedArtistImages,
            preparedStampRallyImages = preparedStampRallyImages,
        )

        val uploadedImages = mutableMapOf<EditImage, EditImage>()

        val artistCatalogImages = if (artistId == null) {
            emptyList()
        } else {
            preparedArtistImages.map {
                when (val uploadResult = uploadImage(localImageToUploadUrl, it)) {
                    is UploadImageResult.Error -> return UploadResult.Error(
                        uploadResult.message,
                        uploadedImages
                    )
                    is UploadImageResult.NotUploaded -> uploadResult.catalogImage
                    is UploadImageResult.Success -> {
                        val localImage = uploadResult.localImage
                        val catalogImage = DatabaseImage(uploadResult.finalKey, localImage.width, localImage.height)
                        uploadedImages[localImage] = ImageUtils.toEditImage(catalogImage)
                        catalogImage
                    }
                }
            }
        }

        val stampRallyCatalogImages = preparedStampRallyImages.mapValues {
            it.value.map {
                when (val uploadResult = uploadImage(localImageToUploadUrl, it)) {
                    is UploadImageResult.Error -> return UploadResult.Error(
                        uploadResult.message,
                        uploadedImages
                    )
                    is UploadImageResult.NotUploaded -> uploadResult.catalogImage
                    is UploadImageResult.Success -> {
                        val localImage = uploadResult.localImage
                        val catalogImage = DatabaseImage(uploadResult.finalKey, localImage.width, localImage.height)
                        uploadedImages[localImage] = ImageUtils.toEditImage(catalogImage)
                        catalogImage
                    }
                }
            }
        }

        return UploadResult.Success(artistCatalogImages, stampRallyCatalogImages, uploadedImages)
    }

    protected abstract suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        localImages: List<PrepareImageResult.Success>,
        stampRallyIdsToLocalImages: Map<String, List<PrepareImageResult.Success>>,
    ): Map<EditImage.LocalImage, PresignedImageUrl>

    protected abstract suspend fun compressImage(bytes: ByteArray): ByteArray

    private fun validateInput(
        artistImages: List<EditImage>,
        stampRallyImages: Map<String, List<EditImage>>,
    ): ValidateResult {
        return if (artistImages.isEmpty() && stampRallyImages.isEmpty()) {
            ValidateResult.Empty
        } else if (artistImages.size > ImageUploadUtils.MAX_ARTIST_UPLOAD_COUNT) {
            ValidateResult.Error("Only ${ImageUploadUtils.MAX_ARTIST_UPLOAD_COUNT} images are allowed")
        } else if (stampRallyImages.any { it.value.size > ImageUploadUtils.MAX_STAMP_RALLY_UPLOAD_COUNT }) {
            // TODO: Specify the stamp rally
            ValidateResult.Error("Only ${ImageUploadUtils.MAX_STAMP_RALLY_UPLOAD_COUNT} images are allowed")
        } else {
            ValidateResult.Success
        }
    }

    private suspend fun prepareImages(
        artistImages: List<EditImage>,
        stampRallyImages: Map<String, List<EditImage>>,
    ): PrepareResult {
        val preparedArtistImages = artistImages.map {
            if (it is EditImage.LocalImage) {
                val file = PlatformImageCache[it.key]
                    ?: return PrepareResult.Error("Failed to read image ${it.name}")
                val compressedResult = compressToBytes(it, file)
                    ?: return PrepareResult.Error("Failed to compress image ${it.name}")
                compressedResult
            } else {
                PrepareImageResult.Ignore(it)
            }
        }

        val preparedStampRallyImages = stampRallyImages.mapValues {
            it.value.map {
                if (it is EditImage.LocalImage) {
                    val file = PlatformImageCache[it.key]
                        ?: return PrepareResult.Error("Failed to read image ${it.name}")
                    val compressedResult = compressToBytes(it, file)
                        ?: return PrepareResult.Error("Failed to compress image ${it.name}")
                    compressedResult
                } else {
                    PrepareImageResult.Ignore(it)
                }
            }
        }

        return PrepareResult.Success(preparedArtistImages, preparedStampRallyImages)
    }

    private suspend fun compressToBytes(
        editImage: EditImage.LocalImage,
        platformFile: PlatformFile,
    ): PrepareImageResult.Success? {
        var size = platformFile.size().asBytes()
        return if (size < ImageUtils.MAX_UPLOAD_SIZE) {
            PrepareImageResult.Success.ImageFile(editImage, platformFile)
        } else {
            var count = 0
            var imageBytes = platformFile.readBytes()
            while (size > ImageUtils.MAX_UPLOAD_SIZE && count < 3) {
                imageBytes = compressImage(imageBytes)
                if (imageBytes.size.asBytes() == size) {
                    ConsoleLogger.log("Failed to compress ${platformFile.name}")
                    return null
                }
                size = imageBytes.size.asBytes()
                count++
            }
            PrepareImageResult.Success.CompressedBytes(editImage, imageBytes)
        }
    }

    private suspend fun requestPresignedUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        preparedArtistImages: List<PrepareImageResult>,
        preparedStampRallyImages: Map<String, List<PrepareImageResult>>,
    ): Map<EditImage.LocalImage, PresignedImageUrl> = getPresignedImageUrls(
        dataYear = dataYear,
        artistId = artistId,
        localImages = preparedArtistImages.filterIsInstance<PrepareImageResult.Success>(),
        stampRallyIdsToLocalImages = preparedStampRallyImages.mapValues {
            it.value.filterIsInstance<PrepareImageResult.Success>()
        },
    )

    private suspend fun uploadImage(
        localImageToUploadUrl: Map<EditImage.LocalImage, PresignedImageUrl>,
        result: PrepareImageResult,
    ): UploadImageResult {
        val (localImage, bytes) = when (result) {
            is PrepareImageResult.Ignore ->
                return UploadImageResult.NotUploaded(result.original.toCatalogImage())
            is PrepareImageResult.Success.CompressedBytes -> result.original to result.bytes
            is PrepareImageResult.Success.ImageFile -> result.original to result.file.readBytes()
        }
        val uploadUrl = localImageToUploadUrl[localImage]
            ?: if (PlatformSpecificConfig.type == PlatformType.DESKTOP) {
                return UploadImageResult.NotUploaded(result.original.toCatalogImage())
            } else {
                return UploadImageResult.Error(
                    "Failed to get presigned URL for ${localImage.key} ${localImage.name}",
                )
            }
        val response = httpClient.put(uploadUrl.url) {
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }
        val status = response.status
        if (status != HttpStatusCode.OK) {
            return UploadImageResult.Error(
                "Failed to upload ${localImage.key} ${localImage.name}: $status, ${response.bodyAsText()}",
            )
        }

        return UploadImageResult.Success(localImage, uploadUrl.key)
    }

    private sealed interface ValidateResult {
        data object Empty : ValidateResult
        data class Error(val message: String) : ValidateResult
        data object Success : ValidateResult
    }

    private sealed interface PrepareResult {
        data class Error(val message: String) : PrepareResult
        data class Success(
            val preparedArtistImages: List<PrepareImageResult>,
            val preparedStampRallyImages: Map<String, List<PrepareImageResult>>,
        ) : PrepareResult
    }

    sealed interface PrepareImageResult {
        val original: EditImage

        data class Ignore(override val original: EditImage) : PrepareImageResult
        sealed interface Success : PrepareImageResult {
            override val original: EditImage.LocalImage
            fun toImageFileData() = ImageFileData(
                id = original.id,
                extension = original.extension
            )

            data class ImageFile(
                override val original: EditImage.LocalImage,
                val file: PlatformFile,
            ) : Success

            // TODO: Any concern about too much memory usage storing these as temp bytes in memory?
            data class CompressedBytes(
                override val original: EditImage.LocalImage,
                val bytes: ByteArray,
            ) : Success {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other == null || this::class != other::class) return false
                    other as CompressedBytes
                    return bytes.contentEquals(other.bytes)
                }

                override fun hashCode(): Int {
                    return bytes.contentHashCode()
                }
            }
        }
    }

    private sealed interface UploadImageResult {
        data class NotUploaded(val catalogImage: DatabaseImage) : UploadImageResult
        data class Error(val message: String) : UploadImageResult
        data class Success(
            val localImage: EditImage.LocalImage,
            val finalKey: String,
        ) : UploadImageResult
    }

    sealed interface UploadResult {
        data object Empty : UploadResult
        data class Error(
            val message: String,
            val alreadyUploadedImages: Map<EditImage, EditImage> = emptyMap(),
        ) : UploadResult

        data class Success(
            val artistCatalogImages: List<DatabaseImage>,
            val stampRallyCatalogImages: Map<String, List<DatabaseImage>>,
            val uploadedImages: Map<EditImage, EditImage>,
        ) : UploadResult
    }
}
