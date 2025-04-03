package com.thekeeperofpie.artistalleydatabase.anilist.data

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain

object AniListResponseCodeCoercingInterceptor : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain,
    ): HttpResponse {
        val initialResponse = chain.proceed(request)
        // Coerce all responses to 200 so that error parsing works as expected
        // https://api.akeneo.com/graphql/error-codes.html#status-and-error-codes
        return HttpResponse.Builder(200)
            .apply {
                addHeaders(initialResponse.headers)
                initialResponse.body?.let(::body)
            }
            .build()
    }
}
