package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.models.PresignedImageUrl
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class FormImageUploader(httpClient: HttpClient) : ImageUploader(httpClient) {
    override suspend fun getPresignedImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        localImages: List<PrepareImageResult.Success>,
        stampRallyIdsToLocalImages: Map<String, List<PrepareImageResult.Success>>,
    ): Map<EditImage.LocalImage, PresignedImageUrl> = emptyMap()

    override suspend fun compressImage(bytes: ByteArray): ByteArray = bytes
}
