package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
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
import kotlinx.coroutines.withContext
import kotlin.time.Clock

@SingleIn(AppScope::class)
@Inject
actual class AlleyFormRemoteDatabase(
    private val dispatchers: CustomDispatchers,
    private val ktorClient: HttpClient,
) {
    actual suspend fun loadArtist(
        dataYear: DataYear,
    ): BackendFormRequest.Artist.Response? = withContext(dispatchers.io) {
        val accessKey = ArtistFormAccessKey.key ?: return@withContext null
        sendRequest(BackendFormRequest.Artist(dataYear), accessKey)
    }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        beforeArtist: ArtistDatabaseEntry.Impl,
        afterArtist: ArtistDatabaseEntry.Impl,
        beforeStampRallies: List<StampRallyDatabaseEntry>,
        afterStampRallies: List<StampRallyDatabaseEntry>,
        formNotes: String,
    ): BackendFormRequest.ArtistSave.Response = withContext(dispatchers.io) {
        val accessKey = ArtistFormAccessKey.key
            ?: return@withContext BackendFormRequest.ArtistSave.Response.Failed("Invalid access key")
        val nonce =
            sendRequest(
                request = BackendFormRequest.Nonce(Clock.System.now()),
                accessKey = accessKey,
            ) ?: return@withContext BackendFormRequest.ArtistSave.Response.Failed(
                "Failed to generate nonce, check system clock"
            )

        sendRequest(
            request = BackendFormRequest.ArtistSave(
                nonce = nonce,
                dataYear = dataYear,
                beforeArtist = beforeArtist,
                afterArtist = afterArtist,
                beforeStampRallies = beforeStampRallies,
                afterStampRallies = afterStampRallies,
                formNotes = formNotes,
            ),
            accessKey = accessKey,
        ) ?: BackendFormRequest.ArtistSave.Response.Failed(
            "Failed to save artist, check system clock"
        )
    }

    private suspend inline fun <reified Request, reified Response> sendRequest(
        request: Request,
        accessKey: String,
    ): Response? where Request : BackendFormRequest, Request : BackendFormRequest.WithResponse<Response> {
        val signature = AlleyCryptography.signRequest<BackendFormRequest>(
            privateKey = accessKey,
            payload = request,
        )
        val response = ktorClient.post(window.origin + "/form/api") {
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
