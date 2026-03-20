package com.thekeeperofpie.artistalleydatabase.alley.edit

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun credentialsHttpClient() = HttpClient(Js) {
    HttpClient()
    engine {
        configureRequest {
            credentials = "include"
        }
    }
}
