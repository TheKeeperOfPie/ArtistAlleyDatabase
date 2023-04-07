package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.util.Log
import com.anilist.AuthedUserQuery
import com.anilist.GenresQuery
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListSort
import com.anilist.type.MediaSort
import com.anilist.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
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

    private suspend fun viewer() = query(AuthedUserQuery()).data?.viewer

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
        ).data?.mediaListCollection

    suspend fun searchMedia(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        vararg sort: MediaSort = emptyArray(),
        genreIn: List<String>,
        genreNotIn: List<String>,
    ): ApolloResponse<MediaAdvancedSearchQuery.Data> {
        val sortParam =
            if (query.isEmpty() && sort.size == 1 && sort.contains(MediaSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by TRENDING_DESC
                Optional.Present(listOf(MediaSort.TRENDING_DESC))
            } else {
                Optional.presentIfNotNull(sort.toList().ifEmpty { null })
            }

        return query(
            MediaAdvancedSearchQuery(
                search = Optional.presentIfNotNull(query.ifEmpty { null }),
                page = Optional.Present(page),
                perPage = Optional.Present(perPage),
                type = MediaType.ANIME,
                sort = sortParam,
                genreIn = Optional.presentIfNotNull(genreIn.ifEmpty { null }),
                genreNotIn = Optional.presentIfNotNull(genreNotIn.ifEmpty { null }),
            )
        )
    }

    suspend fun genres() = query(GenresQuery())

    private suspend fun <D : Query.Data> query(query: Query<D>) =
        apolloClient.query(query).execute()
}
