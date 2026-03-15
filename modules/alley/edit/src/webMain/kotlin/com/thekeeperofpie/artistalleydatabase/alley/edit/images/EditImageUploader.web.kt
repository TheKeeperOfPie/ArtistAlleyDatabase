package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageFileData
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class EditImageUploader(
    private val editDatabase: AlleyEditDatabase,
    httpClient: HttpClient,
) : WebImageUploader(httpClient) {

    override suspend fun fetchUploadImageUrls(
        dataYear: DataYear,
        artistId: Uuid?,
        artistImageData: List<ImageFileData>,
        stampRallyIdsToImageData: Map<String, List<ImageFileData>>,
    ): Response = when (val response = editDatabase.fetchUploadImageUrls(
        dataYear = dataYear,
        artistId = artistId,
        artistImageData = artistImageData,
        stampRallyIdsToImageData = stampRallyIdsToImageData,
    )) {
        is BackendRequest.UploadImageUrls.Response.Failed -> Response.Failed(response.errorMessage)
        is BackendRequest.UploadImageUrls.Response.Success -> Response.Success(
            artistUrls = response.artistUrls,
            stampRallyUrls = response.stampRallyUrls,
        )
    }
}
