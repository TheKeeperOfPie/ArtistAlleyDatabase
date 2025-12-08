package com.thekeeperofpie.artistalleydatabase.utils_network

import com.apollographql.apollo3.network.http.HttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.serialization.json.Json

expect class NetworkClient {
    val httpClient: HttpClient
    val httpEngine: HttpEngine
}

fun HttpClientConfig<*>.configure() {
    install(ContentNegotiation) {
        jsonIo(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
}
