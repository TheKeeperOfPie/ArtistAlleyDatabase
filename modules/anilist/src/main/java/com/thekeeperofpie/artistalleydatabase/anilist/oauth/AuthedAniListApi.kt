package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.util.Log
import com.anilist.AiringScheduleQuery
import com.anilist.AuthedUserQuery
import com.anilist.CharacterAdvancedSearchQuery
import com.anilist.CharacterAndMediasPaginationQuery
import com.anilist.CharacterAndMediasQuery
import com.anilist.CharacterDetailsQuery
import com.anilist.DeleteMediaEntryMutation
import com.anilist.GenresQuery
import com.anilist.HomeAnimeQuery
import com.anilist.HomeMangaQuery
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.MediaAndCharactersPaginationQuery
import com.anilist.MediaAndCharactersQuery
import com.anilist.MediaAndRecommendationsPaginationQuery
import com.anilist.MediaAndRecommendationsQuery
import com.anilist.MediaAndReviewsPaginationQuery
import com.anilist.MediaAndReviewsQuery
import com.anilist.MediaByIdsQuery
import com.anilist.MediaDetailsQuery
import com.anilist.MediaListEntryQuery
import com.anilist.MediaTagsQuery
import com.anilist.MediaTitlesAndImagesQuery
import com.anilist.RateReviewMutation
import com.anilist.ReviewDetailsQuery
import com.anilist.SaveMediaEntryEditMutation
import com.anilist.StaffAndCharactersPaginationQuery
import com.anilist.StaffAndCharactersQuery
import com.anilist.StaffDetailsCharacterMediaPaginationQuery
import com.anilist.StaffDetailsQuery
import com.anilist.StaffDetailsStaffMediaPaginationQuery
import com.anilist.StaffSearchQuery
import com.anilist.ToggleFollowMutation
import com.anilist.UserByIdQuery
import com.anilist.UserMediaListQuery
import com.anilist.UserSearchQuery
import com.anilist.UserSocialActivityQuery
import com.anilist.UserSocialFollowersQuery
import com.anilist.UserSocialFollowingQuery
import com.anilist.type.ActivitySort
import com.anilist.type.AiringSort
import com.anilist.type.CharacterSort
import com.anilist.type.FuzzyDateInput
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSort
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.RecommendationSort
import com.anilist.type.ReviewRating
import com.anilist.type.ReviewSort
import com.anilist.type.StaffSort
import com.anilist.type.UserSort
import com.apollographql.apollo3.ApolloClient
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
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthedAniListApi(
    private val scopedApplication: ScopedApplication,
    private val oAuthStore: AniListOAuthStore,
    networkSettings: NetworkSettings,
    private val okHttpClient: OkHttpClient,
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

    private suspend fun viewer() = query(AuthedUserQuery()).viewer

    suspend fun userMediaList(
        userId: Int,
        type: MediaType,
    ) =
        query(
            UserMediaListQuery(
                userId = userId,
                type = type,
            )
        ).mediaListCollection

    suspend fun searchMedia(
        query: String,
        mediaType: MediaType,
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
    ): MediaAdvancedSearchQuery.Data {
        val sortParam =
            if (query.isEmpty() && sort?.size == 1 && sort.contains(MediaSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by TRENDING_DESC
                Optional.Present(listOf(MediaSort.TRENDING_DESC))
            } else {
                Optional.presentIfNotNull(listOf(MediaSort.SEARCH_MATCH) + sort.orEmpty())
            }

        return query(
            MediaAdvancedSearchQuery(
                search = Optional.presentIfNotNull(query.ifEmpty { null }),
                page = Optional.Present(page),
                perPage = Optional.Present(perPage),
                type = mediaType,
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

    suspend fun mediaDetails(id: String) = query(MediaDetailsQuery(id.toInt())).media!!

    suspend fun mediaTitlesAndImages(mediaIds: List<Int>) =
        query(MediaTitlesAndImagesQuery(ids = Optional.present(mediaIds)))
            .page?.media?.filterNotNull().orEmpty()

    suspend fun mediaListEntry(id: String) = query(MediaListEntryQuery(id.toInt()))

    suspend fun deleteMediaListEntry(id: String) =
        apolloClient.mutation(DeleteMediaEntryMutation(id = id.toInt()))
            .execute().dataOrThrow()

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
    ).execute().dataOrThrow().saveMediaListEntry!!

    suspend fun mediaByIds(ids: List<Int>) =
        query(MediaByIdsQuery(ids = Optional.present(ids)))
            .page?.media?.filterNotNull()
            .orEmpty()

    suspend fun searchCharacters(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        isBirthday: Boolean? = null,
        sort: List<CharacterSort>? = null,
    ): CharacterAdvancedSearchQuery.Data {
        val sortParam =
            if (query.isEmpty() && sort?.size == 1 && sort.contains(CharacterSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by FAVOURITES_DESC
                Optional.Present(listOf(CharacterSort.FAVOURITES_DESC))
            } else {
                Optional.presentIfNotNull(listOf(CharacterSort.SEARCH_MATCH) + sort.orEmpty())
            }
        return query(
            CharacterAdvancedSearchQuery(
                search = Optional.presentIfNotNull(query.ifEmpty { null }),
                page = Optional.Present(page),
                perPage = Optional.Present(perPage),
                isBirthday = Optional.presentIfNotNull(isBirthday),
                sort = sortParam,
            )
        )
    }

    suspend fun characterDetails(id: String) = query(CharacterDetailsQuery(id.toInt()))
        .character!!

    suspend fun searchStaff(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        isBirthday: Boolean? = null,
        sort: List<StaffSort>? = null,
    ): StaffSearchQuery.Data {
        val sortParam =
            if (query.isEmpty() && sort?.size == 1 && sort.contains(StaffSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by FAVOURITES_DESC
                Optional.Present(listOf(StaffSort.FAVOURITES_DESC))
            } else {
                Optional.presentIfNotNull(listOf(StaffSort.SEARCH_MATCH) + sort.orEmpty())
            }
        return query(
            StaffSearchQuery(
                search = Optional.presentIfNotNull(query.ifEmpty { null }),
                page = Optional.Present(page),
                perPage = Optional.Present(perPage),
                isBirthday = Optional.presentIfNotNull(isBirthday),
                sort = sortParam,
            )
        )
    }

    suspend fun staffDetails(id: String) = query(StaffDetailsQuery(id.toInt())).staff!!

    suspend fun staffDetailsCharacterMediaPagination(id: String, page: Int) =
        query(
            StaffDetailsCharacterMediaPaginationQuery(
                id = id.toInt(),
                page = page
            )
        ).staff?.characterMedia!!

    suspend fun staffDetailsStaffMediaPagination(id: String, page: Int) =
        query(
            StaffDetailsStaffMediaPaginationQuery(
                id = id.toInt(),
                page = page
            )
        ).staff?.staffMedia!!

    suspend fun searchUsers(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<UserSort>? = null,
    ): UserSearchQuery.Data {
        val sortParam =
            if (query.isEmpty() && sort?.size == 1 && sort.contains(UserSort.SEARCH_MATCH)) {
                // On a default, empty search, sort by WATCHED_TIME_DESC
                Optional.Present(listOf(UserSort.WATCHED_TIME_DESC))
            } else {
                Optional.presentIfNotNull(listOf(UserSort.SEARCH_MATCH) + sort.orEmpty())
            }
        return query(
            UserSearchQuery(
                search = Optional.presentIfNotNull(query.ifEmpty { null }),
                page = Optional.Present(page),
                perPage = Optional.Present(perPage),
                sort = sortParam,
            )
        )
    }

    suspend fun user(id: String) = query(UserByIdQuery(id.toInt())).user

    fun logOut() {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            try {
                // TODO: This doesn't actually work right now, API doesn't support revoking
                // TODO: Notify user that to really log out, they need to go to website
                val result = executeLogout()?.body?.string()
                Log.d(TAG, "Logging out response: $result")
            } catch (e: Exception) {
                Log.d(TAG, "Error logging out", e)
            }

            oAuthStore.clearAuthToken()
        }
    }

    private suspend fun executeLogout() = suspendCancellableCoroutine {
        val authHeader = oAuthStore.authHeader
        if (authHeader == null || oAuthStore.authToken.value == null) {
            it.resume(null)
            return@suspendCancellableCoroutine
        }
        val call = okHttpClient.newCall(
            Request.Builder()
                .url(AniListUtils.GRAPHQL_API_URL)
                .addHeader("Authorization", authHeader)
                .post("""{"query":"mutation{Logout}","variables":{}}""".toRequestBody())
                .build()
        )
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                it.resume(response)
            }
        })

        it.invokeOnCancellation {
            call.cancel()
        }
    }

    suspend fun homeAnime(
        perPage: Int = 10,
    ): HomeAnimeQuery.Data {
        val currentSeasonYear = AniListUtils.getCurrentSeasonYear()
        val nextSeasonYear = AniListUtils.getNextSeasonYear(currentSeasonYear)
        val lastSeasonYear = AniListUtils.getPreviousSeasonYear(currentSeasonYear)
        return query(
            HomeAnimeQuery(
                currentSeason = currentSeasonYear.first,
                currentYear = currentSeasonYear.second,
                lastSeason = lastSeasonYear.first,
                lastYear = lastSeasonYear.second,
                nextSeason = nextSeasonYear.first,
                nextYear = nextSeasonYear.second,
                perPage = perPage,
            )
        )
    }

    suspend fun homeManga(perPage: Int = 10) = query(
        HomeMangaQuery(perPage = perPage)
    )

    suspend fun airingSchedule(
        date: LocalDate,
        sort: AiringSort,
        perPage: Int,
        page: Int,
    ): AiringScheduleQuery.Data {
        val startTime = date.atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond - 1
        val endTime = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond
        return query(
            AiringScheduleQuery(
                startTime = startTime.toInt(),
                endTime = endTime.toInt(),
                perPage = perPage,
                page = page,
                sort = listOf(sort),
            )
        )
    }

    suspend fun toggleFollow(userId: Int) =
        apolloClient.mutation(ToggleFollowMutation(userId)).execute().dataOrThrow().toggleFollow

    suspend fun userSocialFollowers(userId: Int, page: Int, perPage: Int = 10) =
        query(UserSocialFollowersQuery(userId = userId, perPage = perPage, page = page))

    suspend fun userSocialFollowing(userId: Int, page: Int, perPage: Int = 10) =
        query(UserSocialFollowingQuery(userId = userId, perPage = perPage, page = page))

    suspend fun userSocialActivity(
        isFollowing: Boolean,
        page: Int,
        perPage: Int = 10,
        sort: List<ActivitySort> = listOf(ActivitySort.PINNED, ActivitySort.ID_DESC),
        userIdNot: Int? = null,
    ) = query(
        UserSocialActivityQuery(
            isFollowing = isFollowing,
            sort = sort,
            perPage = perPage,
            page = page,
            userIdNotIn = Optional.presentIfNotNull(userIdNot?.let(::listOf))
        )
    )

    suspend fun mediaAndCharacters(mediaId: String, charactersPerPage: Int = 10) = query(
        MediaAndCharactersQuery(
            mediaId = mediaId.toInt(),
            charactersPerPage = charactersPerPage,
        )
    ).media

    suspend fun mediaAndCharactersPage(
        mediaId: String,
        page: Int,
        charactersPerPage: Int = 10,
    ) = query(
        MediaAndCharactersPaginationQuery(
            mediaId = mediaId.toInt(),
            page = page,
            charactersPerPage = charactersPerPage,
        )
    )

    suspend fun mediaAndReviews(
        mediaId: String,
        sort: ReviewSort,
        reviewsPerPage: Int = 10,
    ) = query(
        MediaAndReviewsQuery(
            mediaId = mediaId.toInt(),
            sort = listOf(sort),
            reviewsPerPage = reviewsPerPage,
        )
    ).media

    suspend fun mediaAndReviewsPage(
        mediaId: String,
        sort: ReviewSort,
        page: Int,
        reviewsPerPage: Int = 10,
    ) = query(
        MediaAndReviewsPaginationQuery(
            mediaId = mediaId.toInt(),
            sort = listOf(sort),
            page = page,
            reviewsPerPage = reviewsPerPage,
        )
    )

    suspend fun reviewDetails(reviewId: String) = query(ReviewDetailsQuery(reviewId.toInt())).review

    suspend fun rateReview(reviewId: String, rating: ReviewRating) =
        apolloClient.mutation(RateReviewMutation(id = reviewId.toInt(), rating = rating)).execute()
            .dataOrThrow().rateReview.userRating

    suspend fun mediaAndRecommendations(
        mediaId: String,
        sort: RecommendationSort,
        recommendationsPerPage: Int = 10,
    ) = query(
        MediaAndRecommendationsQuery(
            mediaId = mediaId.toInt(),
            sort = listOf(sort),
            recommendationsPerPage = recommendationsPerPage,
        )
    ).media

    suspend fun mediaAndRecommendationsPage(
        mediaId: String,
        sort: RecommendationSort,
        page: Int,
        recommendationsPerPage: Int = 10,
    ) = query(
        MediaAndRecommendationsPaginationQuery(
            mediaId = mediaId.toInt(),
            sort = listOf(sort),
            page = page,
            recommendationsPerPage = recommendationsPerPage,
        )
    )

    suspend fun characterAndMedias(
        characterId: String,
        sort: MediaSort,
        mediasPerPage: Int = 10,
    ) = query(
        CharacterAndMediasQuery(
            characterId = characterId.toInt(),
            sort = listOf(sort),
            mediasPerPage = mediasPerPage,
        )
    ).character

    suspend fun characterAndMediasPage(
        characterId: String,
        sort: MediaSort,
        page: Int,
        mediasPerPage: Int = 10,
    ) = query(
        CharacterAndMediasPaginationQuery(
            characterId = characterId.toInt(),
            sort = listOf(sort),
            page = page,
            mediasPerPage = mediasPerPage,
        )
    )

    suspend fun staffAndCharacters(
        staffId: String,
        sort: List<CharacterSort>,
        charactersPerPage: Int = 5
    ) = query(
        StaffAndCharactersQuery(
            staffId = staffId.toInt(),
            sort = sort,
            charactersPerPage = charactersPerPage,
        )
    ).staff

    suspend fun staffAndCharactersPage(
        staffId: String,
        sort: List<CharacterSort>,
        page: Int,
        charactersPerPage: Int = 5,
    ) = query(
        StaffAndCharactersPaginationQuery(
            staffId = staffId.toInt(),
            sort = sort,
            page = page,
            charactersPerPage = charactersPerPage,
        )
    )

    private suspend fun <D : Query.Data> query(query: Query<D>) =
        apolloClient.query(query).execute().dataOrThrow()
}
