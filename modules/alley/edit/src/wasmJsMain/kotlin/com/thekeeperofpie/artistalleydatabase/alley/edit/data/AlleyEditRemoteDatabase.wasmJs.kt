package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val ktorClient: HttpClient,
) {
    // TODO: Error handling
    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        withContext(PlatformDispatchers.IO) {
            sendRequest(BackendRequest.Artist(artistId))
        }

    actual suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary> =
        withContext(PlatformDispatchers.IO) {
            sendRequest(BackendRequest.Artists).orEmpty()
        }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): ArtistSave.Response.Result =
        withContext(PlatformDispatchers.IO) {
            try {
                sendRequest(ArtistSave.Request(initial = initial, updated = updated))!!.result
            } catch (t: Throwable) {
                t.printStackTrace()
                ArtistSave.Response.Result.Failed(t)
            }
        }

    actual suspend fun listImages(dataYear: DataYear, artistId: Uuid): List<EditImage> =
        withContext(PlatformDispatchers.IO) {
            try {
                sendRequest(
                    ListImages.Request(
                        EditImage.NetworkImage.makePrefix(
                            dataYear,
                            artistId
                        )
                    )
                )!!
                    .idsAndKeys
                    .map { imageFromIdAndKey(it.first, it.second) }
            } catch (t: Throwable) {
                t.printStackTrace()
                emptyList()
            }
        }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage = withContext(PlatformDispatchers.IO) {
        val key = EditImage.NetworkImage.makePrefix(dataYear, artistId) +
                "/$id.${platformFile.extension}"
        val bytes = platformFile.readBytes()

        ktorClient.put(window.origin + "/database/uploadImage/$key") {
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }

        imageFromIdAndKey(id, key)
    }

    // TODO: Cache this and rely on manual refresh to avoid extra row reads
    actual suspend fun loadSeries(): List<SeriesInfo> =
        withContext(PlatformDispatchers.IO) {
            sendRequest(BackendRequest.Series).orEmpty()
        }

    actual suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): SeriesSave.Response.Result =
        withContext(PlatformDispatchers.IO) {
            try {
                sendRequest(SeriesSave.Request(initial = initial, updated = updated))!!.result
            } catch (t: Throwable) {
                t.printStackTrace()
                SeriesSave.Response.Result.Failed(t)
            }
        }

    // TODO: Cache this and rely on manual refresh to avoid extra row reads
    actual suspend fun loadMerch(): List<MerchInfo> =
        withContext(PlatformDispatchers.IO) {
            sendRequest(BackendRequest.Merch).orEmpty()
        }

    actual suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): MerchSave.Response.Result =
        withContext(PlatformDispatchers.IO) {
            try {
                sendRequest(MerchSave.Request(initial = initial, updated = updated))!!.result
            } catch (t: Throwable) {
                t.printStackTrace()
                MerchSave.Response.Result.Failed(t)
            }
        }

    private fun imageFromIdAndKey(id: Uuid, key: String) = EditImage.NetworkImage(
        uri = Uri.parse(
            BuildKonfig.imagesUrl.ifBlank { "${window.origin}/database/image" } + "/$key"
        ),
        id = id,
    )

    private suspend inline fun <reified Request, reified Response> sendRequest(
        request: Request,
    ): Response? where Request : BackendRequest, Request : BackendRequest.WithResponse<Response> {
        ConsoleLogger.log("sendRequest = $request")
        val response = ktorClient.post(window.origin + "/database") {
            contentType(ContentType.Application.Json)
            setBody<BackendRequest>(request)
        }
        ConsoleLogger.log("response = $response")
        if (response.status != HttpStatusCode.OK) {
            // TODO: Surface errors
            return null
        }
        return response.body<Response>()
    }
}
