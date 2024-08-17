package com.thekeeperofpie.artistalleydatabase.utils_network

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object ApolloRateLimitUtils {

    suspend fun rateLimit(
        request: HttpRequest,
        chain: HttpInterceptorChain,
        onRetry: (suspend (Attempt) -> Unit)? = null,
    ): HttpResponse {
        var attempt = chain.attempt(request)
        attempt.retryAfter?.let {
            onRetry?.invoke(attempt)
            // First try with a third the wait time in case it's caused by the burst limiter
            attempt = chain.attempt(request, it / 3)
        }
        attempt.retryAfter?.let {
            onRetry?.invoke(attempt)
            // Buffer with 1 second to ensure it reaches the rate limit reset threshold
            attempt = chain.attempt(request, it + 1.seconds)
        }
        return attempt.response
    }

    private suspend fun HttpInterceptorChain.attempt(
        request: HttpRequest,
        delay: Duration? = null,
    ): Attempt {
        val newRequest = if (delay == null) {
            request
        } else {
            delay(delay)
            request.newBuilder().build()
        }
        val response = proceed(newRequest)
        val retryAfter = response.takeIf { it.statusCode == 429 }
            ?.headers
            ?.find { it.name == "retry-after" }
            ?.value
            ?.toIntOrNull()
            ?.seconds
        return Attempt(response, retryAfter)
    }

    data class Attempt(
        val response: HttpResponse,
        val retryAfter: Duration?,
    )
}
