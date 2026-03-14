package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.HttpClient
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class FormImageUploader(httpClient: HttpClient) : ImageUploader(httpClient) {
    override suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid,
        localImages: List<PrepareImageResult.Success>,
        stampRallyIdsToLocalImages: Map<String, List<PrepareImageResult.Success>>,
    ): Map<EditImage.LocalImage, String> = emptyMap()

    override fun imageFromIdAndKey(
        original: EditImage,
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage = original

    override suspend fun compressImage(bytes: ByteArray): ByteArray = bytes
}
