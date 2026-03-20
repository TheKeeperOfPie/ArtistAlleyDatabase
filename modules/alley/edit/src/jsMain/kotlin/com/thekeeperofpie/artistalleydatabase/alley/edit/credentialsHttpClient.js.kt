package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import io.ktor.client.HttpClient
import io.ktor.client.fetchOptions
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials

private val imagesHost = Uri.parseOrNull(AlleyUtils.prodImagesUrl)?.host

actual fun credentialsHttpClient() = HttpClient().apply {
    plugin(HttpSend).intercept {
        if (it.url.host == imagesHost) {
            it.fetchOptions {
                credentials = RequestCredentials.INCLUDE
            }
        }
        execute(it)
    }
}
