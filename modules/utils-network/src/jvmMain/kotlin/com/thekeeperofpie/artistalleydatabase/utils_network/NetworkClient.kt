package com.thekeeperofpie.artistalleydatabase.utils_network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import it.skrape.fetcher.BrowserFetcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class NetworkClient(
    cache: Cache?,
    authProviders: @JvmSuppressWildcards Map<String, NetworkAuthProvider>,
    isConnected: () -> Boolean,
    interceptors: List<Interceptor>,
) {
    val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val authHeader = authProviders[request.url.host]?.authHeader
            if (authHeader == null) {
                chain.proceed(request)
            } else {
                request.newBuilder()
                    .addHeader("Authorization", authHeader)
                    .build()
                    .let(chain::proceed)
            }
        })
        .dns { hostname ->
            // Manually timeout DNS because OkHttp doesn't support that by default
            runBlocking {
                if (!isConnected()) {
                    throw IllegalStateException("No active connection")
                }
                withTimeout(5.seconds) {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
        }
        .apply { interceptors.forEach(::addInterceptor) }
        .addInterceptor { chain ->
            // TODO: Improve this mechanism, move the failure to a lower level?
            if (isConnected()) {
                chain.proceed(chain.request())
            } else {
                chain.withConnectTimeout(1, TimeUnit.SECONDS)
                    .withReadTimeout(1, TimeUnit.SECONDS)
                    .withWriteTimeout(1, TimeUnit.SECONDS)
                    .proceed(chain.request())
            }
        }
        .build()

    val httpClient = HttpClient(OkHttp) {
        engine {
            preconfigured = okHttpClient
        }
    }

    val webScraper = object : WebScraper {
        override fun get(url: String): WebScraper.Result {
            val result = BrowserFetcher.fetch(
                BrowserFetcher.requestBuilder.apply { this.url = url }
            )
            return WebScraper.Result(
                result.headers["Location"] ?: result.baseUri,
                result.responseBody
            )
        }
    }
}
