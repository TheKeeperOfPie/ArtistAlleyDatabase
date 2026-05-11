package com.thekeeperofpie.artistalleydatabase

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.thekeeperofpie.artistalleydatabase.utils_network.ApolloRateLimitUtils
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface ApplicationVariantComponent {


    /**
     * Introduces the release rate limiter. Unfortunately it's difficult to intercept rate limits while
     * also surfacing the errors to the debug drawer, so on debug builds the drawer controller includes
     * its own rate limiting. That won't exist on release builds, so this introduces a replacement
     * without the debug functionality.
     */
    @SingleIn(AppScope::class)
    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor(): HttpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain) =
            ApolloRateLimitUtils.rateLimit(request, chain)
    }
}
