package com.thekeeperofpie.artistalleydatabase

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.thekeeperofpie.artistalleydatabase.network_utils.RateLimitUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/**
 * Introduces the release rate limiter. Unfortunately it's difficult to intercept rate limits while
 * also surfacing the errors to the debug drawer, so on debug builds the drawer controller includes
 * its own rate limiting. That won't exist on release builds, so this introduces a replacement
 * without the debug functionality.
 */
@Module
@InstallIn(SingletonComponent::class)
class ReleaseNetworkModule {

    @Singleton
    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor() = object : HttpInterceptor {
        override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain) =
            RateLimitUtils.rateLimit(request, chain)
    }
}
