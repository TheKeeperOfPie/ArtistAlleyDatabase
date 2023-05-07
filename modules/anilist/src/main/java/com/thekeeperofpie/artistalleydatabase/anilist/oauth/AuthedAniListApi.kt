package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.util.Log
import com.anilist.AuthedUserQuery
import com.anilist.DeleteMediaEntryMutation
import com.anilist.GenresQuery
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.MediaDetailsQuery
import com.anilist.MediaTagsQuery
import com.anilist.SaveMediaEntryEditMutation
import com.anilist.UserByIdQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.FuzzyDateInput
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListSort
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSort
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.addLoggingInterceptors
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.time.LocalDate

class AuthedAniListApi(
    scopedApplication: ScopedApplication,
    oAuthStore: AniListOAuthStore,
    networkSettings: NetworkSettings,
    okHttpClient: OkHttpClient,
) {
    companion object {
        private const val TAG = "AuthedAniListApi"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(DefaultHttpEngine(okHttpClient))
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
        sort: List<MediaListSort>? = null,
    ) =
        query(
            UserMediaListQuery(
                userId = userId,
                type = type,
                sort = Optional.present(sort?.ifEmpty { listOf(MediaListSort.FINISHED_ON_DESC) }),
            )
        ).data?.mediaListCollection

    suspend fun searchMedia(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        genreIn: List<String>,
        genreNotIn: List<String>,
        tagIn: List<String>,
        tagNotIn: List<String>,
        statusIn: List<MediaStatus>,
        statusNotIn: List<MediaStatus>,
        formatIn: List<MediaFormat>,
        formatNotIn: List<MediaFormat>,
        showAdult: Boolean,
        onList: Boolean?,
        season: MediaSeason?,
        seasonYear: Int?,
        startDateGreater: Int?,
        startDateLesser: Int?,
        averageScoreGreater: Int?,
        averageScoreLesser: Int?,
        episodesGreater: Int?,
        episodesLesser: Int?,
        sourcesIn: List<MediaSource>?,
        minimumTagRank: Int?,
    ): ApolloResponse<MediaAdvancedSearchQuery.Data> {
        val sortParam =
            if (query.isEmpty() && sort?.size == 1 && sort.contains(MediaSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by TRENDING_DESC
                Optional.Present(listOf(MediaSort.TRENDING_DESC))
            } else {
                Optional.presentIfNotNull(sort?.ifEmpty { null })
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
                tagIn = Optional.presentIfNotNull(tagIn.ifEmpty { null }),
                tagNotIn = Optional.presentIfNotNull(tagNotIn.ifEmpty { null }),
                statusIn = Optional.presentIfNotNull(statusIn.ifEmpty { null }),
                statusNotIn = Optional.presentIfNotNull(statusNotIn.ifEmpty { null }),
                formatIn = Optional.presentIfNotNull(formatIn.ifEmpty { null }),
                formatNotIn = Optional.presentIfNotNull(formatNotIn.ifEmpty { null }),
                isAdult = Optional.presentIfNotNull(if (showAdult) null else false),
                onList = Optional.presentIfNotNull(onList),
                season = Optional.presentIfNotNull(season),
                seasonYear = Optional.presentIfNotNull(seasonYear),
                startDateGreater = Optional.presentIfNotNull(startDateGreater),
                startDateLesser = Optional.presentIfNotNull(startDateLesser),
                averageScoreGreater = Optional.presentIfNotNull(averageScoreGreater),
                averageScoreLesser = Optional.presentIfNotNull(averageScoreLesser),
                episodesGreater = Optional.presentIfNotNull(episodesGreater),
                episodesLesser = Optional.presentIfNotNull(episodesLesser),
                sourceIn = Optional.presentIfNotNull(sourcesIn?.ifEmpty { null }),
                minimumTagRank = Optional.presentIfNotNull(minimumTagRank),
            )
        )
    }

    suspend fun genres() = query(GenresQuery())

    suspend fun tags() = query(MediaTagsQuery())

    suspend fun mediaDetails(id: String) = query(MediaDetailsQuery(id.toInt()))

    suspend fun deleteMediaListEntry(id: String) =
        apolloClient.mutation(DeleteMediaEntryMutation(id = id.toInt()))
            .execute().dataAssertNoErrors

    suspend fun saveMediaListEntry(
        id: String?,
        mediaId: String,
        type: MediaType?,
        status: MediaListStatus?,
        scoreRaw: Int,
        progress: Int,
        repeat: Int,
        priority: Int,
        private: Boolean,
        startedAt: LocalDate?,
        completedAt: LocalDate?,
        hiddenFromStatusLists: Boolean?,
    ) = apolloClient.mutation(
        SaveMediaEntryEditMutation(
            id = Optional.presentIfNotNull(id?.toIntOrNull()),
            mediaId = mediaId.toInt(),
            status = Optional.present(status),
            scoreRaw = Optional.presentIfNotNull(scoreRaw),
            progress = Optional.presentIfNotNull(progress.takeIf { type == MediaType.ANIME }),
            progressVolumes = Optional.presentIfNotNull(
                progress.takeIf { type != MediaType.ANIME }
            ),
            repeat = Optional.presentIfNotNull(repeat),
            priority = Optional.presentIfNotNull(priority),
            private = Optional.presentIfNotNull(private),
            startedAt = Optional.present(startedAt?.let {
                FuzzyDateInput(
                    year = Optional.present(it.year),
                    month = Optional.present(it.monthValue),
                    day = Optional.present(it.dayOfMonth),
                )
            }),
            completedAt = Optional.present(completedAt?.let {
                FuzzyDateInput(
                    year = Optional.present(it.year),
                    month = Optional.present(it.monthValue),
                    day = Optional.present(it.dayOfMonth),
                )
            }),
            hiddenFromStatusLists = Optional.presentIfNotNull(hiddenFromStatusLists),
        )
    ).execute().dataAssertNoErrors.saveMediaListEntry!!

    suspend fun user(id: String) = query(UserByIdQuery(id.toInt())).dataAssertNoErrors.user

    private suspend fun <D : Query.Data> query(query: Query<D>) =
        apolloClient.query(query).execute()
}
