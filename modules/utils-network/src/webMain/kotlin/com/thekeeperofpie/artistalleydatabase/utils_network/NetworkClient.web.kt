package com.thekeeperofpie.artistalleydatabase.utils_network

import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.HttpEngine
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Inject

@Inject
@SingletonScope
actual class NetworkClient {
    actual val httpClient = HttpClient()
    actual val httpEngine: HttpEngine = DefaultHttpEngine()
}
