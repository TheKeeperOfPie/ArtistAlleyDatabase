package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyFormRemoteDatabase(
    private val dispatchers: CustomDispatchers,
    private val ktorClient: HttpClient,
) {
    actual suspend fun loadArtist(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
    ): ArtistDatabaseEntry.Impl? =
        sendRequest(BackendFormRequest.Artist(dataYear, artistId), privateKey)

    private suspend inline fun <reified Request, reified Response> sendRequest(
        request: Request,
        privateKey: String,
    ): Response? where Request : BackendFormRequest, Request : BackendFormRequest.WithResponse<Response> {
        val signature = AlleyCryptography.signRequest<BackendFormRequest>(privateKey = privateKey, payload = request)
        // TODO: Use /form directly
        val response = ktorClient.post(window.origin + "/database/form") {
            headers {
                set(AlleyCryptography.SIGNATURE_HEADER_KEY, signature)
            }
            contentType(ContentType.Application.Json)
            setBody<BackendFormRequest>(request)
        }
        if (response.status != HttpStatusCode.OK) {
            // TODO: Surface errors
            return null
        }
        return response.body<Response>()
    }
}
