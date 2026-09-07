package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.WebImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageFileData
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class FormImageUploader(
    buildConfig: BuildConfig,
    private val formDatabase: AlleyFormDatabase,
    httpClient: HttpClient,
) : WebImageUploader(buildConfig, httpClient) {

    override suspend fun fetchUploadImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        artistImageData: List<ImageFileData>,
        stampRallyIdsToImageData: Map<String, List<ImageFileData>>,
    ): Response = when (val response = formDatabase.fetchUploadImageUrls(
        dataYear = dataYear,
        artistId = artistId!!,
        artistImageData = artistImageData,
        stampRallyIdsToImageData = stampRallyIdsToImageData,
    )) {
        is BackendFormRequest.UploadImageUrls.Response.Failed -> Response.Failed(response.errorMessage)
        is BackendFormRequest.UploadImageUrls.Response.Success -> Response.Success(
            artistUrls = response.artistUrls,
            stampRallyUrls = response.stampRallyUrls,
        )
    }
}
