package com.thekeeperofpie.artistalleydatabase.alley.edit

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

@OptIn(ExperimentalWasmJsInterop::class)
actual fun credentialsHttpClient() = HttpClient(Js) {
    engine {
        configureRequest {
            credentials = "include".toJsString()
        }
    }
}
