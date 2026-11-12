package com.thekeeperofpie.artistalleydatabase.alley.user

import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
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
}
