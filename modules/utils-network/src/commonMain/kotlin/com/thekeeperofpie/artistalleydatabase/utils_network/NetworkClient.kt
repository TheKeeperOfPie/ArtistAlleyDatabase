package com.thekeeperofpie.artistalleydatabase.utils_network

import com.apollographql.apollo3.network.http.HttpEngine
import io.ktor.client.HttpClient

expect class NetworkClient {
    val httpClient: HttpClient
    val httpEngine: HttpEngine
}
