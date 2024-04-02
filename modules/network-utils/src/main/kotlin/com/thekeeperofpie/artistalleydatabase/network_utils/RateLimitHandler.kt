package com.thekeeperofpie.artistalleydatabase.network_utils

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
object RateLimitHandler {

    val apolloHttpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain,
        ): HttpResponse {
            val response = chain.proceed(request)
            if (response.statusCode == 429) {
                val retryAfter =
                    response.headers.find { it.name == "retry-after" }?.value?.toIntOrNull()?.seconds
                if (retryAfter != null) {
                    delay(retryAfter)
                    return chain.proceed(request.newBuilder().build())
                }
            }
            return response
        }
    }
}
