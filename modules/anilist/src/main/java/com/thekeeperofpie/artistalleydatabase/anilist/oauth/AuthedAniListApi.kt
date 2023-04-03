package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.util.Log
import com.anilist.AuthedUserQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListSort
import com.anilist.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListCache
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.addLoggingInterceptors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class AuthedAniListApi(
    scopedApplication: ScopedApplication,
    cache: AniListCache,
    oAuthStore: AniListOAuthStore,
    networkSettings: NetworkSettings,
) {
    companion object {
        private const val TAG = "AuthedAniListApi"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(
            DefaultHttpEngine(
                OkHttpClient.Builder()
                    .cache(cache.cache)
                    .addInterceptor(oAuthStore)
                    .addLoggingInterceptors(TAG, networkSettings)
                    .build()
            )
        )
        .addLoggingInterceptors(TAG, networkSettings)
        .build()

    val authedUser = MutableStateFlow<AuthedUserQuery.Data.Viewer?>(null)

    init {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            oAuthStore.authToken
                .map {
                    try {
                        viewer()
                    } catch (e: Exception) {
                        Log.d(TAG, "Error loading authed user")
                        null
                    }
                }
                .collectLatest(authedUser::emit)
        }
    }

    private suspend fun viewer() = query(AuthedUserQuery())?.viewer

    suspend fun userMediaList(
        userId: Int,
        type: MediaType = MediaType.ANIME,
        vararg sort: MediaListSort = emptyArray()
    ) =
        query(
            UserMediaListQuery(
                userId = userId,
                type = type,
                sort = Optional.presentIfNotNull(sort.toList().ifEmpty { null })
            )
        )?.mediaListCollection

    private suspend fun <D : Query.Data> query(query: Query<D>) =
        apolloClient.query(query).execute().data
}
