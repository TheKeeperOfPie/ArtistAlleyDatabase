package com.thekeeperofpie.artistalleydatabase.utils_network

import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.HttpEngine
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@Inject
@SingleIn(AppScope::class)
actual class NetworkClient {
    actual val httpClient = HttpClient { configure() }
    actual val httpEngine: HttpEngine = DefaultHttpEngine()
}
