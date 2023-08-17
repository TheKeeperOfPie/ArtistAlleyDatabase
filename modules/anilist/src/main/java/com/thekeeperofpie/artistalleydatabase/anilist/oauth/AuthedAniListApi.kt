@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.util.Log
import com.anilist.ActivityDetailsQuery
import com.anilist.ActivityDetailsRepliesQuery
import com.anilist.AiringScheduleQuery
import com.anilist.AuthedUserQuery
import com.anilist.CharacterAdvancedSearchQuery
import com.anilist.CharacterAndMediasPaginationQuery
import com.anilist.CharacterAndMediasQuery
import com.anilist.CharacterDetailsMediaPageQuery
import com.anilist.CharacterDetailsQuery
import com.anilist.DeleteActivityMutation
import com.anilist.DeleteActivityReplyMutation
import com.anilist.DeleteForumThreadCommentMutation
import com.anilist.DeleteMediaListEntryMutation
import com.anilist.ForumRootQuery
import com.anilist.ForumThreadCommentQuery
import com.anilist.ForumThreadDetailsQuery
import com.anilist.ForumThreadSearchQuery
import com.anilist.ForumThread_CommentsQuery
import com.anilist.GenresQuery
import com.anilist.HomeAnime2Query
import com.anilist.HomeAnimeQuery
import com.anilist.HomeManga2Query
import com.anilist.HomeMangaQuery
import com.anilist.LicensorsQuery
import com.anilist.MediaActivityPageQuery
import com.anilist.MediaActivityQuery
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.MediaAndCharactersPaginationQuery
import com.anilist.MediaAndCharactersQuery
import com.anilist.MediaAndRecommendationsPaginationQuery
import com.anilist.MediaAndRecommendationsQuery
import com.anilist.MediaAndReviewsPaginationQuery
import com.anilist.MediaAndReviewsQuery
import com.anilist.MediaByIdsQuery
import com.anilist.MediaDetailsCharactersPageQuery
import com.anilist.MediaDetailsQuery
import com.anilist.MediaDetailsStaffPageQuery
import com.anilist.MediaListEntryQuery
import com.anilist.MediaTagsQuery
import com.anilist.MediaTitlesAndImagesQuery
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.NotificationsQuery
import com.anilist.RateReviewMutation
import com.anilist.ReviewDetailsQuery
import com.anilist.SaveActivityReplyMutation
import com.anilist.SaveForumThreadCommentMutation
import com.anilist.SaveMediaListEntryMutation
import com.anilist.SaveRecommendationRatingMutation
import com.anilist.StaffAndCharactersPaginationQuery
import com.anilist.StaffAndCharactersQuery
import com.anilist.StaffDetailsCharacterMediaPaginationQuery
import com.anilist.StaffDetailsCharactersPageQuery
import com.anilist.StaffDetailsQuery
import com.anilist.StaffDetailsStaffMediaPaginationQuery
import com.anilist.StaffSearchQuery
import com.anilist.StudioMediasPaginationQuery
import com.anilist.StudioMediasQuery
import com.anilist.StudioSearchQuery
import com.anilist.ToggleActivityLikeMutation
import com.anilist.ToggleActivityReplyLikeMutation
import com.anilist.ToggleActivityReplyLikeMutation.Data.ToggleLikeV2.Companion.asActivityReply
import com.anilist.ToggleActivitySubscribeMutation
import com.anilist.ToggleAnimeFavoriteMutation
import com.anilist.ToggleCharacterFavoriteMutation
import com.anilist.ToggleCharacterResultQuery
import com.anilist.ToggleFollowMutation
import com.anilist.ToggleForumThreadCommentLikeMutation
import com.anilist.ToggleForumThreadCommentLikeMutation.Data.ToggleLikeV2.Companion.asThreadComment
import com.anilist.ToggleForumThreadLikeMutation
import com.anilist.ToggleForumThreadLikeMutation.Data.ToggleLikeV2.Companion.asThread
import com.anilist.ToggleForumThreadSubscribeMutation
import com.anilist.ToggleMangaFavoriteMutation
import com.anilist.ToggleMediaResultQuery
import com.anilist.ToggleStaffFavoriteMutation
import com.anilist.ToggleStaffResultQuery
import com.anilist.ToggleStudioFavoriteMutation
import com.anilist.ToggleStudioResultQuery
import com.anilist.UnreadNotificationCountQuery
import com.anilist.UserByIdQuery
import com.anilist.UserDetailsAnimePageQuery
import com.anilist.UserDetailsCharactersPageQuery
import com.anilist.UserDetailsMangaPageQuery
import com.anilist.UserDetailsStaffPageQuery
import com.anilist.UserDetailsStudiosPageQuery
import com.anilist.UserMediaListQuery
import com.anilist.UserSearchQuery
import com.anilist.UserSocialActivityQuery
import com.anilist.UserSocialFollowersQuery
import com.anilist.UserSocialFollowingQuery
import com.anilist.type.ActivitySort
import com.anilist.type.ActivityType
import com.anilist.type.AiringSort
import com.anilist.type.CharacterSort
import com.anilist.type.ExternalLinkMediaType
import com.anilist.type.FuzzyDateInput
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSort
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.NotificationType
import com.anilist.type.RecommendationRating
import com.anilist.type.RecommendationSort
import com.anilist.type.ReviewRating
import com.anilist.type.ReviewSort
import com.anilist.type.StaffSort
import com.anilist.type.StudioSort
import com.anilist.type.ThreadSort
import com.anilist.type.UserSort
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.addLoggingInterceptors
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class AuthedAniListApi(
    private val scopedApplication: ScopedApplication,
    private val oAuthStore: AniListOAuthStore,
    networkSettings: NetworkSettings,
    aniListSettings: AniListSettings,
    private val okHttpClient: OkHttpClient,
) {
    companion object {
        private const val TAG = "AuthedAniListApi"
        private const val MEMORY_CACHE_BYTE_SIZE = 100 * 1024 * 1024 // 100 MB
    }

    private val memoryThenDiskCache = MemoryCacheFactory(MEMORY_CACHE_BYTE_SIZE)
        .apply {
            if (networkSettings.enableNetworkCaching.value) {
                chain(
                    SqlNormalizedCacheFactory(
                        scopedApplication.app,
                        "apollo.db",
                        useNoBackupDirectory = true,
                    )
                )
            }
        }
    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(DefaultHttpEngine(okHttpClient))
        .addLoggingInterceptors(TAG, networkSettings)
        .normalizedCache(memoryThenDiskCache, writeToCacheAsynchronously = true)
        .build()

    val authedUser = MutableStateFlow<AniListViewer?>(null)

    init {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            oAuthStore.authToken
                .flatMapLatest {
                    if (it == null) {
                        flowOf(null)
                    } else {
                        queryCacheAndNetwork(AuthedUserQuery())
                            .mapNotNull { it.result?.viewer }
                            .catch { Log.d(TAG, "Error loading authed user") }
                            .map(::AniListViewer)
                            .startWith(aniListSettings.aniListViewer.take(1).filterNotNull())
                            .distinctUntilChanged()
                    }
                }
                .onEach(aniListSettings.aniListViewer::emit)
                .collectLatest(authedUser::emit)
        }
    }

    open suspend fun userMediaList(
        userId: String,
        type: MediaType,
        status: MediaListStatus? = null,
    ) = queryCacheAndNetwork(
        UserMediaListQuery(
            userId = userId.toInt(),
            type = type,
            status = Optional.presentIfNotNull(status),
        )
    ).map { it.transformResult { it.mediaListCollection } }

    open suspend fun searchMedia(
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
        volumesGreater: Int?,
        volumesLesser: Int?,
        chaptersGreater: Int?,
        chaptersLesser: Int?,
        sourcesIn: List<MediaSource>?,
        minimumTagRank: Int?,
        licensedByIdIn: List<Int>?,
        includeDescription: Boolean,
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
                volumesGreater = Optional.presentIfNotNull(volumesGreater),
                volumesLesser = Optional.presentIfNotNull(volumesLesser),
                chaptersGreater = Optional.presentIfNotNull(chaptersGreater),
                chaptersLesser = Optional.presentIfNotNull(chaptersLesser),
                sourceIn = Optional.presentIfNotNull(sourcesIn?.ifEmpty { null }),
                minimumTagRank = Optional.presentIfNotNull(minimumTagRank),
                licensedByIdIn = Optional.presentIfNotNull(licensedByIdIn?.ifEmpty { null }),
                includeDescription = includeDescription,
            )
        )
    }

    open suspend fun genres() = query(GenresQuery())

    open suspend fun tags() = query(MediaTagsQuery())

    open suspend fun mediaDetails(id: String) = queryCacheAndNetwork(MediaDetailsQuery(id.toInt()))

    open suspend fun mediaDetailsCharactersPage(mediaId: String, page: Int, perPage: Int = 10) =
        query(
            MediaDetailsCharactersPageQuery(
                mediaId = mediaId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).media

    open suspend fun mediaDetailsStaffPage(mediaId: String, page: Int, perPage: Int = 10) =
        query(
            MediaDetailsStaffPageQuery(
                mediaId = mediaId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).media

    open suspend fun mediaTitlesAndImages(mediaIds: List<Int>) =
        query(MediaTitlesAndImagesQuery(ids = Optional.present(mediaIds)))
            .page?.media?.filterNotNull().orEmpty()

    open suspend fun mediaListEntry(id: String) = query(MediaListEntryQuery(id.toInt()))

    open suspend fun deleteMediaListEntry(id: String) =
        mutate(DeleteMediaListEntryMutation(id = id.toInt()))

    // TODO: Progress is broken for volume/chapter entries
    open suspend fun saveMediaListEntry(
        id: String?,
        mediaId: String,
        status: MediaListStatus?,
        scoreRaw: Int,
        progress: Int,
        progressVolumes: Int?,
        repeat: Int,
        priority: Int,
        private: Boolean,
        notes: String,
        startedAt: LocalDate?,
        completedAt: LocalDate?,
        hiddenFromStatusLists: Boolean,
    ) = mutate(
        SaveMediaListEntryMutation(
            id = Optional.presentIfNotNull(id?.toIntOrNull()),
            mediaId = mediaId.toInt(),
            status = Optional.present(status),
            scoreRaw = Optional.present(scoreRaw),
            progress = Optional.present(progress),
            progressVolumes = Optional.presentIfNotNull(progressVolumes),
            repeat = Optional.present(repeat),
            priority = Optional.present(priority),
            private = Optional.present(private),
            notes = Optional.present(notes),
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
            hiddenFromStatusLists = Optional.present(hiddenFromStatusLists),
        )
    ).saveMediaListEntry!!

    open suspend fun saveMediaListEntryProgressOnly(
        id: String?,
        mediaId: String,
        progress: Int,
    ) = mutate(
        SaveMediaListEntryMutation(
            id = Optional.presentIfNotNull(id?.toIntOrNull()),
            mediaId = mediaId.toInt(),
            progress = Optional.present(progress),
        )
    ).saveMediaListEntry!!

    open suspend fun mediaByIds(ids: List<Int>, includeDescription: Boolean = false) =
        query(MediaByIdsQuery(ids = ids, includeDescription = includeDescription))
            .page?.media?.filterNotNull()
            .orEmpty()

    open suspend fun searchCharacters(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        isBirthday: Boolean? = null,
        sort: List<CharacterSort>? = null,
    ) = query(
        CharacterAdvancedSearchQuery(
            search = Optional.presentIfNotNull(query.ifEmpty { null }),
            page = Optional.Present(page),
            perPage = Optional.Present(perPage),
            isBirthday = Optional.presentIfNotNull(isBirthday?.takeIf { it }),
            sort = Optional.presentIfNotNull(sort),
        )
    )

    open suspend fun characterDetails(id: String) =
        queryCacheAndNetwork(CharacterDetailsQuery(id.toInt()))

    open suspend fun characterDetailsMediaPage(characterId: String, page: Int, perPage: Int = 5) =
        query(
            CharacterDetailsMediaPageQuery(
                characterId = characterId.toInt(),
                page = page,
                perPage = perPage
            )
        )
            .character

    open suspend fun searchStaff(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        isBirthday: Boolean? = null,
        sort: List<StaffSort>? = null,
    ) = query(
        StaffSearchQuery(
            search = Optional.presentIfNotNull(query.ifEmpty { null }),
            page = Optional.Present(page),
            perPage = Optional.Present(perPage),
            isBirthday = Optional.presentIfNotNull(isBirthday?.takeIf { it }),
            sort = Optional.presentIfNotNull(sort),
        )
    )

    open suspend fun staffDetails(id: String) = query(StaffDetailsQuery(id.toInt())).staff!!

    open suspend fun staffDetailsCharactersPage(staffId: String, page: Int, perPage: Int = 10) =
        query(
            StaffDetailsCharactersPageQuery(
                staffId = staffId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).staff.characters

    open suspend fun staffDetailsCharacterMediaPagination(id: String, page: Int) =
        query(
            StaffDetailsCharacterMediaPaginationQuery(
                id = id.toInt(),
                page = page
            )
        ).staff?.characterMedia!!

    open suspend fun staffDetailsStaffMediaPagination(id: String, page: Int) =
        query(
            StaffDetailsStaffMediaPaginationQuery(
                id = id.toInt(),
                page = page
            )
        ).staff?.staffMedia!!

    open suspend fun searchUsers(
        query: String,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<UserSort>? = null,
        isModerator: Boolean? = null,
    ) = query(
        UserSearchQuery(
            search = Optional.presentIfNotNull(query.ifEmpty { null }),
            page = Optional.Present(page),
            perPage = Optional.Present(perPage),
            isModerator = Optional.presentIfNotNull(isModerator?.takeIf { it }),
            sort = Optional.presentIfNotNull(sort),
        )
    )

    open suspend fun user(id: String) = query(UserByIdQuery(id.toInt())).user

    open suspend fun userDetailsAnimePage(userId: String, page: Int, perPage: Int = 10) =
        query(
            UserDetailsAnimePageQuery(
                userId = userId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).user.favourites.anime

    open suspend fun userDetailsMangaPage(userId: String, page: Int, perPage: Int = 10) =
        query(
            UserDetailsMangaPageQuery(
                userId = userId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).user.favourites.manga

    open suspend fun userDetailsCharactersPage(userId: String, page: Int, perPage: Int = 10) =
        query(
            UserDetailsCharactersPageQuery(
                userId = userId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).user.favourites.characters

    open suspend fun userDetailsStaffPage(userId: String, page: Int, perPage: Int = 10) =
        query(
            UserDetailsStaffPageQuery(
                userId = userId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).user.favourites.staff

    open suspend fun userDetailsStudiosPage(userId: String, page: Int, perPage: Int = 10) =
        query(
            UserDetailsStudiosPageQuery(
                userId = userId.toInt(),
                page = page,
                perPage = perPage,
            )
        ).user.favourites.studios

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

    open suspend fun homeAnime(
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

    open suspend fun homeAnime2(perPage: Int = 10) = query(HomeAnime2Query(perPage = perPage))

    open suspend fun homeManga(perPage: Int = 10) = query(HomeMangaQuery(perPage = perPage))

    open suspend fun homeManga2(perPage: Int = 10) = query(HomeManga2Query(perPage = perPage))

    open suspend fun airingSchedule(
        startTime: Long,
        endTime: Long,
        sort: AiringSort,
        perPage: Int,
        page: Int,
    ) = query(
        AiringScheduleQuery(
            startTime = startTime.toInt(),
            endTime = endTime.toInt(),
            perPage = perPage,
            page = page,
            sort = listOf(sort),
        )
    )

    open suspend fun toggleFollow(userId: Int) = mutate(ToggleFollowMutation(userId)).toggleFollow

    open suspend fun userSocialFollowers(userId: String, page: Int, perPage: Int = 10) =
        query(UserSocialFollowersQuery(userId = userId.toInt(), perPage = perPage, page = page))

    open suspend fun userSocialFollowing(userId: String, page: Int, perPage: Int = 10) =
        query(UserSocialFollowingQuery(userId = userId.toInt(), perPage = perPage, page = page))

    open suspend fun userSocialActivity(
        isFollowing: Boolean,
        page: Int,
        perPage: Int = 10,
        sort: List<ActivitySort> = listOf(ActivitySort.PINNED, ActivitySort.ID_DESC),
        userId: String? = null,
        userIdNot: String? = null,
        typeIn: List<ActivityType>? = null,
        typeNotIn: List<ActivityType>? = null,
        hasReplies: Boolean? = null,
        createdAtGreater: Int? = null,
        createdAtLesser: Int? = null,
    ) = query(
        UserSocialActivityQuery(
            isFollowing = isFollowing,
            sort = sort,
            perPage = perPage,
            page = page,
            userId = Optional.presentIfNotNull(userId?.toInt()),
            userIdNotIn = Optional.presentIfNotNull(userIdNot?.toInt()?.let(::listOf)),
            typeIn = Optional.presentIfNotNull(typeIn),
            typeNotIn = Optional.presentIfNotNull(typeNotIn),
            hasReplies = Optional.presentIfNotNull(hasReplies),
            createdAtGreater = Optional.presentIfNotNull(createdAtGreater),
            createdAtLesser = Optional.presentIfNotNull(createdAtLesser),
        )
    )

    open suspend fun mediaAndCharacters(mediaId: String, charactersPerPage: Int = 10) = query(
        MediaAndCharactersQuery(
            mediaId = mediaId.toInt(),
            charactersPerPage = charactersPerPage,
        )
    ).media

    open suspend fun mediaAndCharactersPage(
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

    open suspend fun mediaAndReviews(
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

    open suspend fun mediaAndReviewsPage(
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

    open suspend fun reviewDetails(reviewId: String) =
        query(ReviewDetailsQuery(reviewId.toInt())).review

    open suspend fun rateReview(reviewId: String, rating: ReviewRating) =
        mutate(RateReviewMutation(id = reviewId.toInt(), rating = rating)).rateReview.userRating

    open suspend fun mediaAndRecommendations(
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

    open suspend fun mediaAndRecommendationsPage(
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

    open suspend fun characterAndMedias(
        characterId: String,
        sort: List<MediaSort>,
        mediasPerPage: Int = 10,
    ) = query(
        CharacterAndMediasQuery(
            characterId = characterId.toInt(),
            sort = sort,
            mediasPerPage = mediasPerPage,
        )
    ).character

    open suspend fun characterAndMediasPage(
        characterId: String,
        sort: List<MediaSort>,
        page: Int,
        mediasPerPage: Int = 10,
    ) = query(
        CharacterAndMediasPaginationQuery(
            characterId = characterId.toInt(),
            sort = sort,
            page = page,
            mediasPerPage = mediasPerPage,
        )
    )

    open suspend fun staffAndCharacters(
        staffId: String,
        sort: List<CharacterSort>,
        charactersPerPage: Int = 10,
    ) = query(
        StaffAndCharactersQuery(
            staffId = staffId.toInt(),
            sort = sort,
            charactersPerPage = charactersPerPage,
        )
    ).staff

    open suspend fun staffAndCharactersPage(
        staffId: String,
        sort: List<CharacterSort>,
        page: Int,
        charactersPerPage: Int = 10,
    ) = query(
        StaffAndCharactersPaginationQuery(
            staffId = staffId.toInt(),
            sort = sort,
            page = page,
            charactersPerPage = charactersPerPage,
        )
    )

    open suspend fun searchStudios(
        query: String,
        page: Int,
        perPage: Int,
        sort: List<StudioSort>,
    ) = query(
        StudioSearchQuery(
            search = Optional.presentIfNotNull(query.ifEmpty { null }),
            page = page,
            perPage = perPage,
            sort = sort,
        )
    )

    open suspend fun studioMedias(
        studioId: String,
        sort: List<MediaSort>,
        mediasPerPage: Int = 10,
    ) = query(
        StudioMediasQuery(
            studioId = studioId.toInt(),
            sort = sort,
            mediasPerPage = mediasPerPage,
        )
    ).studio

    open suspend fun studioMediasPage(
        studioId: String,
        sort: List<MediaSort>,
        page: Int,
        mediasPerPage: Int = 10,
    ) = query(
        StudioMediasPaginationQuery(
            studioId = studioId.toInt(),
            sort = sort,
            page = page,
            mediasPerPage = mediasPerPage,
        )
    )

    open suspend fun toggleAnimeFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleAnimeFavoriteMutation(id = idAsInt))
        return query(ToggleMediaResultQuery(id = idAsInt)).media.isFavourite
    }

    open suspend fun toggleMangaFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleMangaFavoriteMutation(id = idAsInt))
        return query(ToggleMediaResultQuery(id = idAsInt)).media.isFavourite
    }

    open suspend fun toggleCharacterFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleCharacterFavoriteMutation(id = idAsInt))
        return query(ToggleCharacterResultQuery(id = idAsInt)).character.isFavourite
    }

    open suspend fun toggleStaffFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleStaffFavoriteMutation(id = idAsInt))
        return query(ToggleStaffResultQuery(id = idAsInt)).staff.isFavourite
    }

    open suspend fun toggleStudioFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleStudioFavoriteMutation(id = idAsInt))
        return query(ToggleStudioResultQuery(id = idAsInt)).studio.isFavourite
    }

    open suspend fun mediaActivities(
        id: String,
        sort: List<ActivitySort>,
        activitiesPerPage: Int = 10,
    ) = query(
        MediaActivityQuery(
            mediaId = id.toInt(),
            sort = sort,
            activitiesPerPage = activitiesPerPage,
        )
    )

    open suspend fun mediaActivitiesPage(
        id: String,
        sort: List<ActivitySort>,
        page: Int,
        activitiesPerPage: Int = 10,
    ) = query(
        MediaActivityPageQuery(
            mediaId = id.toInt(),
            sort = sort,
            page = page,
            activitiesPerPage = activitiesPerPage,
        )
    )

    open suspend fun toggleActivityLike(id: String) =
        when (val result = mutate(ToggleActivityLikeMutation(id = id.toInt())).toggleLikeV2!!) {
            is ToggleActivityLikeMutation.Data.ListActivityToggleLikeV2 -> result.isLiked
            is ToggleActivityLikeMutation.Data.MessageActivityToggleLikeV2 -> result.isLiked
            is ToggleActivityLikeMutation.Data.TextActivityToggleLikeV2 -> result.isLiked
            is ToggleActivityLikeMutation.Data.OtherToggleLikeV2 -> false
        } ?: false

    open suspend fun toggleActivitySubscribe(id: String, subscribe: Boolean) = when (val result =
        mutate(
            ToggleActivitySubscribeMutation(
                id = id.toInt(),
                subscribe = subscribe
            )
        ).toggleActivitySubscription!!) {
        is ToggleActivitySubscribeMutation.Data.ListActivityToggleActivitySubscription -> result.isSubscribed
        is ToggleActivitySubscribeMutation.Data.MessageActivityToggleActivitySubscription -> result.isSubscribed
        is ToggleActivitySubscribeMutation.Data.TextActivityToggleActivitySubscription -> result.isSubscribed
        is ToggleActivitySubscribeMutation.Data.OtherToggleActivitySubscription -> false
    } ?: false

    open suspend fun activityDetails(id: String) =
        query(ActivityDetailsQuery(activityId = id.toInt()))

    open suspend fun activityReplies(id: String, page: Int, perPage: Int = 10) =
        query(ActivityDetailsRepliesQuery(activityId = id.toInt(), page = page, perPage = perPage))

    open suspend fun toggleActivityReplyLike(id: String) =
        mutate(ToggleActivityReplyLikeMutation(id = id.toInt()))
            .toggleLikeV2.asActivityReply()!!.isLiked

    open suspend fun deleteActivity(id: String) =
        mutate(DeleteActivityMutation(id = id.toInt())).deleteActivity.deleted

    open suspend fun deleteActivityReply(id: String) =
        mutate(DeleteActivityReplyMutation(id = id.toInt())).deleteActivityReply.deleted

    open suspend fun saveActivityReply(activityId: String, replyId: String?, text: String) =
        mutate(
            SaveActivityReplyMutation(
                activityId = activityId.toInt(),
                replyId = Optional.presentIfNotNull(replyId?.toInt()),
                text = text,
            )
        ).saveActivityReply

    open suspend fun licensors(mediaType: ExternalLinkMediaType) =
        query(LicensorsQuery(mediaType = mediaType)).externalLinkSourceCollection.filterNotNull()

    open suspend fun notifications(
        page: Int,
        perPage: Int = 10,
        typeIn: List<NotificationType>? = null,
        resetNotificationCount: Boolean = true,
    ) = query(
        NotificationsQuery(
            page = page,
            perPage = perPage,
            resetNotificationCount = resetNotificationCount,
            typeIn = Optional.presentIfNotNull(typeIn),
        )
    )

    open suspend fun notificationMediaAndActivity(
        mediaIds: List<String>,
        activityIds: List<String>,
    ) = query(
        NotificationMediaAndActivityQuery(
            mediaIds = mediaIds.map { it.toInt() },
            activityIds = activityIds.map { it.toInt() },
        )
    )

    open suspend fun forumRoot() = query(ForumRootQuery())

    open suspend fun forumThreadSearch(
        search: String?,
        subscribed: Boolean,
        categoryId: String?,
        mediaCategoryId: String?,
        sort: List<ThreadSort>?,
        page: Int,
        perPage: Int = 10,
    ) = query(
        ForumThreadSearchQuery(
            search = Optional.presentIfNotNull(search?.ifEmpty { null }),
            subscribed = Optional.presentIfNotNull(subscribed.takeIf { it }),
            categoryId = Optional.presentIfNotNull(categoryId?.toIntOrNull()),
            mediaCategoryId = Optional.presentIfNotNull(mediaCategoryId?.toIntOrNull()),
            sort = Optional.presentIfNotNull(sort?.ifEmpty { null }),
            page = page,
            perPage = perPage,
        )
    )

    open suspend fun forumThread(threadId: String) =
        query(ForumThreadDetailsQuery(threadId = threadId.toInt()))

    open suspend fun forumThreadComments(threadId: String, page: Int, perPage: Int = 10) =
        query(
            ForumThread_CommentsQuery(
                threadId = threadId.toInt(),
                page = page,
                perPage = perPage
            )
        )

    open suspend fun toggleForumThreadSubscribe(id: String, subscribe: Boolean) =
        mutate(ToggleForumThreadSubscribeMutation(id = id.toInt(), subscribe = subscribe))
            .toggleThreadSubscription.isLiked

    open suspend fun toggleForumThreadLike(id: String) =
        mutate(ToggleForumThreadLikeMutation(id = id.toInt()))
            .toggleLikeV2.asThread()!!.isLiked

    open suspend fun toggleForumThreadCommentLike(id: String) =
        mutate(ToggleForumThreadCommentLikeMutation(id = id.toInt()))
            .toggleLikeV2.asThreadComment()!!.isLiked

    open suspend fun deleteForumThreadComment(id: String) =
        mutate(DeleteForumThreadCommentMutation(id = id.toInt()))

    open suspend fun saveForumThreadComment(
        threadId: String,
        commentId: String?,
        parentCommentId: String?,
        text: String?,
    ) = mutate(
        SaveForumThreadCommentMutation(
            threadId = threadId.toInt(),
            commentId = Optional.presentIfNotNull(commentId?.toInt()),
            parentCommentId = Optional.presentIfNotNull(parentCommentId?.toInt()),
            text = text.orEmpty(),
        )
    )

    open suspend fun forumThreadSingleCommentTree(threadId: String, commentId: String) =
        query(
            ForumThreadCommentQuery(
                threadId = threadId.toInt(),
                commentId = commentId.toInt(),
            )
        )

    open suspend fun unreadNotificationCount() =
        query(UnreadNotificationCountQuery()).viewer.unreadNotificationCount

    open suspend fun saveRecommendationRating(
        mediaId: String,
        recommendationMediaId: String,
        rating: RecommendationRating,
    ) = mutate(
        SaveRecommendationRatingMutation(
            mediaId = mediaId.toInt(),
            recommendationMediaId = recommendationMediaId.toInt(),
            rating = rating,
        )
    ).saveRecommendation.userRating

    // TODO: Use queryCacheAndNetwork for everything
    private suspend fun <D : Query.Data> query(query: Query<D>) =
        apolloClient.query(query).fetchPolicy(FetchPolicy.NetworkFirst).execute().dataOrThrow()

    private suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>) =
        apolloClient.mutation(mutation).execute().dataOrThrow()

    private fun <D : Query.Data> queryCacheAndNetwork(query: Query<D>) = combine(
        // TODO: Cancel the cache query if network returns first
        apolloClient.query(query).fetchPolicy(FetchPolicy.CacheOnly).toFlow()
            .flowOn(CustomDispatchers.IO)
            .startWith(null),
        apolloClient.query(query).fetchPolicy(FetchPolicy.NetworkOnly).toFlow()
            .flowOn(CustomDispatchers.IO)
            .startWith(null),
    ) { cache, network ->
        val cacheHasErrors = cache?.exception != null
        if (network != null) {
            val networkHasErrors = network.exception != null
            val result = if (networkHasErrors) cache?.data else network.data
            LoadingResult(
                success = !networkHasErrors,
                result = result,
                error = if (networkHasErrors) {
                    UtilsStringR.error_loading_from_network to network.exception
                } else if (result == null && cacheHasErrors) {
                    UtilsStringR.error_loading_from_cache to cache?.exception
                } else {
                    null
                },
            )
        } else {
            LoadingResult(
                loading = true,
                success = !cacheHasErrors,
                result = cache?.data,
                error = null,
            )
        }
    }
}
