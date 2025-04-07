package com.thekeeperofpie.artistalleydatabase.utils_network

import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides

interface NetworkComponent {

    val NetworkClient.bindAsWebScraper: WebScraper
        @Provides get() = webScraper
    val NetworkClient.bindAsHttpClient: HttpClient
        @Provides get() = httpClient
}
