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
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.ktor.client.HttpClient
import kotlinx.browser.window
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class FormImageUploader(
    dispatchers: CustomDispatchers,
    private val formDatabase: AlleyFormDatabase,
    httpClient: HttpClient,
) : ImageUploader(dispatchers, httpClient) {
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
        val key = EditImage.NetworkImage.makePrefix(dataYear, artistId.toString()) +
                "/$id.${platformFile.extension}"
        return EditImage.NetworkImage(
            uri = Uri.parse(
                IMAGES_URL.ifBlank { "${window.origin}/edit/api/image" } + "/$key"
            ),
            id = id,
        )
    }
}
