package com.thekeeperofpie.artistalleydatabase

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.unity.UnityComponent
import com.thekeeperofpie.artistalleydatabase.play.PlayComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.ApolloRateLimitUtils
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface VariantComponent : PlayComponent, UnityComponent {


    /**
     * Introduces the release rate limiter. Unfortunately it's difficult to intercept rate limits while
     * also surfacing the errors to the debug drawer, so on debug builds the drawer controller includes
     * its own rate limiting. That won't exist on release builds, so this introduces a replacement
     * without the debug functionality.
     */
    @SingletonScope
    @Provides
    @IntoSet
    fun provideApolloHttpInterceptor() = object : HttpInterceptor {
        override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain) =
            ApolloRateLimitUtils.rateLimit(request, chain)
    }
}
