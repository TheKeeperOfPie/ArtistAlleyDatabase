package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.thekeeperofpie.artistalleydatabase.anilist.AniListCache
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.addLoggingInterceptors
import okhttp3.OkHttpClient

class AuthedAniListApi(
    cache: AniListCache,
    oAuthStore: AniListOAuthStore
) {

    companion object {
        private const val TAG = "AniListApi"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(
            DefaultHttpEngine(
                OkHttpClient.Builder()
                    .cache(cache.cache)
                    .addInterceptor(oAuthStore)
                    .addLoggingInterceptors(TAG)
                    .build()
            )
        )
        .addLoggingInterceptors(TAG)
        .build()
}
