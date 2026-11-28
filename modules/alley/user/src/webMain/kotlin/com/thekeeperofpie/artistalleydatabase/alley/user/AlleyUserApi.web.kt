package com.thekeeperofpie.artistalleydatabase.alley.user

import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendUserRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.browser.window

@Inject
actual class AlleyUserApi(private val ktorClient: HttpClient) {
    actual suspend fun exchangeAuthCode(authCode: String): String? {
        val response = ktorClient.post(window.origin + "/user/api/auth") {
            contentType(ContentType.Text.Plain)
            setBody(authCode)
        }
        if (response.status != HttpStatusCode.OK) {
            // TODO: Surface errors
            return null
        }
        return response.bodyAsText()
    }

    // TODO: Remove, just rely on sync to revoke authorization
    suspend fun checkAuth() {
        val response = ktorClient.post(window.origin + "/user/api/check")
        if (response.status == HttpStatusCode.Forbidden) {
            ktorClient.post(window.origin + "/user/api/refresh")
        }
    }

    suspend fun sync(payload: UserDataSyncPayload): Pair<HttpStatusCode, BackendUserRequest.Sync.Response> =
        sendRequest(BackendUserRequest.Sync(payload))

    private suspend inline fun <reified Request, reified Response> sendRequest(
        request: Request,
    ): Pair<HttpStatusCode, Response> where Request : BackendUserRequest, Request : BackendUserRequest.WithResponse<Response> {
        val response = ktorClient.post(window.origin + "/user/api") {
            contentType(ContentType.Application.Json)
            setBody<BackendUserRequest>(request)
        }
        return response.status to response.body<Response>()
    }
}
