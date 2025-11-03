package com.thekeeperofpie.artistalleydatabase.utils_network

import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

interface NetworkComponent {

    @Provides
    fun provideWebScraper(networkClient: NetworkClient): WebScraper = networkClient.webScraper

    @Provides
    fun provideHttpClient(networkClient: NetworkClient): HttpClient = networkClient.httpClient
}
