@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import artistalleydatabase.modules.utils_compose.generated.resources.error_loading_from_cache
import artistalleydatabase.modules.utils_compose.generated.resources.error_loading_from_network
import co.touchlab.kermit.Logger
import com.anilist.data.ActivityDetailsQuery
import com.anilist.data.ActivityDetailsRepliesQuery
import com.anilist.data.AiringScheduleQuery
import com.anilist.data.Anime2AnimeConnectionDetailsQuery
import com.anilist.data.Anime2AnimeConnectionsQuery
import com.anilist.data.Anime2AnimeCountQuery
import com.anilist.data.Anime2AnimeMediaCharactersQuery
import com.anilist.data.Anime2AnimeMediaQuery
import com.anilist.data.Anime2AnimeMediaStaffQuery
import com.anilist.data.Anime2AnimeRandomAnimeQuery
import com.anilist.data.AuthedUserQuery
import com.anilist.data.CharacterAdvancedSearchQuery
import com.anilist.data.CharacterAndMediasPaginationQuery
import com.anilist.data.CharacterAndMediasQuery
import com.anilist.data.CharacterDetailsMediaPageQuery
import com.anilist.data.CharacterDetailsQuery
import com.anilist.data.DeleteActivityMutation
import com.anilist.data.DeleteActivityReplyMutation
import com.anilist.data.DeleteForumThreadCommentMutation
import com.anilist.data.DeleteMediaListEntryMutation
import com.anilist.data.ForumRootQuery
import com.anilist.data.ForumThreadCommentQuery
import com.anilist.data.ForumThreadDetailsQuery
import com.anilist.data.ForumThreadSearchQuery
import com.anilist.data.ForumThread_CommentsQuery
import com.anilist.data.GenresQuery
import com.anilist.data.HomeAnimeQuery
import com.anilist.data.HomeMangaQuery
import com.anilist.data.HomeRecommendationsQuery
import com.anilist.data.HomeReviewsQuery
import com.anilist.data.LicensorsQuery
import com.anilist.data.MediaActivityPageQuery
import com.anilist.data.MediaActivityQuery
import com.anilist.data.MediaAdvancedSearchQuery
import com.anilist.data.MediaAndCharactersPaginationQuery
import com.anilist.data.MediaAndCharactersQuery
import com.anilist.data.MediaAndRecommendationsPaginationQuery
import com.anilist.data.MediaAndRecommendationsQuery
import com.anilist.data.MediaAndReviewsPaginationQuery
import com.anilist.data.MediaAndReviewsQuery
import com.anilist.data.MediaAutocompleteQuery
import com.anilist.data.MediaByIdsQuery
import com.anilist.data.MediaDetailsActivityQuery
import com.anilist.data.MediaDetailsCharactersPageQuery
import com.anilist.data.MediaDetailsQuery
import com.anilist.data.MediaDetailsStaffPageQuery
import com.anilist.data.MediaDetailsUserDataQuery
import com.anilist.data.MediaListEntryQuery
import com.anilist.data.MediaTagsQuery
import com.anilist.data.MediaTitlesAndImagesQuery
import com.anilist.data.NotificationMediaAndActivityQuery
import com.anilist.data.NotificationsQuery
import com.anilist.data.RateReviewMutation
import com.anilist.data.RecommendationSearchQuery
import com.anilist.data.ReviewDetailsQuery
import com.anilist.data.ReviewSearchQuery
import com.anilist.data.SaveActivityReplyMutation
import com.anilist.data.SaveForumThreadCommentMutation
import com.anilist.data.SaveMediaListEntryMutation
import com.anilist.data.SaveRecommendationRatingMutation
import com.anilist.data.StaffAndCharactersPaginationQuery
import com.anilist.data.StaffAndCharactersQuery
import com.anilist.data.StaffDetailsCharacterMediaPaginationQuery
import com.anilist.data.StaffDetailsCharactersPageQuery
import com.anilist.data.StaffDetailsQuery
import com.anilist.data.StaffDetailsStaffMediaPaginationQuery
import com.anilist.data.StaffSearchQuery
import com.anilist.data.StudioMediasPaginationQuery
import com.anilist.data.StudioMediasQuery
import com.anilist.data.StudioSearchQuery
import com.anilist.data.ToggleActivityLikeMutation
import com.anilist.data.ToggleActivityReplyLikeMutation
import com.anilist.data.ToggleActivityReplyLikeMutation.Data.ToggleLikeV2.Companion.asActivityReply
import com.anilist.data.ToggleActivitySubscribeMutation
import com.anilist.data.ToggleAnimeFavoriteMutation
import com.anilist.data.ToggleCharacterFavoriteMutation
import com.anilist.data.ToggleCharacterResultQuery
import com.anilist.data.ToggleFollowMutation
import com.anilist.data.ToggleForumThreadCommentLikeMutation
import com.anilist.data.ToggleForumThreadCommentLikeMutation.Data.ToggleLikeV2.Companion.asThreadComment
import com.anilist.data.ToggleForumThreadLikeMutation
import com.anilist.data.ToggleForumThreadLikeMutation.Data.ToggleLikeV2.Companion.asThread
import com.anilist.data.ToggleForumThreadSubscribeMutation
import com.anilist.data.ToggleMangaFavoriteMutation
import com.anilist.data.ToggleMediaResultQuery
import com.anilist.data.ToggleStaffFavoriteMutation
import com.anilist.data.ToggleStaffResultQuery
import com.anilist.data.ToggleStudioFavoriteMutation
import com.anilist.data.ToggleStudioResultQuery
import com.anilist.data.UnreadNotificationCountQuery
import com.anilist.data.UserByIdQuery
import com.anilist.data.UserDetailsAnimePageQuery
import com.anilist.data.UserDetailsCharactersPageQuery
import com.anilist.data.UserDetailsMangaPageQuery
import com.anilist.data.UserDetailsStaffPageQuery
import com.anilist.data.UserDetailsStudiosPageQuery
import com.anilist.data.UserFavoritesAnimeQuery
import com.anilist.data.UserFavoritesCharactersQuery
import com.anilist.data.UserFavoritesMangaQuery
import com.anilist.data.UserFavoritesStaffQuery
import com.anilist.data.UserFavoritesStudiosQuery
import com.anilist.data.UserMediaListQuery
import com.anilist.data.UserSearchQuery
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.UserSocialFollowersQuery
import com.anilist.data.UserSocialFollowersWithFavoritesQuery
import com.anilist.data.UserSocialFollowingQuery
import com.anilist.data.UserSocialFollowingWithFavoritesQuery
import com.anilist.data.ViewerMediaListQuery
import com.anilist.data.type.ActivitySort
import com.anilist.data.type.ActivityType
import com.anilist.data.type.AiringSort
import com.anilist.data.type.CharacterRole
import com.anilist.data.type.CharacterSort
import com.anilist.data.type.ExternalLinkMediaType
import com.anilist.data.type.FuzzyDateInput
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaSort
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.NotificationType
import com.anilist.data.type.RecommendationRating
import com.anilist.data.type.RecommendationSort
import com.anilist.data.type.ReviewRating
import com.anilist.data.type.ReviewSort
import com.anilist.data.type.StaffSort
import com.anilist.data.type.StudioSort
import com.anilist.data.type.ThreadSort
import com.anilist.data.type.UserSort
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.DefaultApolloException
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.apollo.utils.ApolloCache
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
open class AuthedAniListApi(
    private val scope: ApplicationScope,
    private val oAuthStore: AniListOAuthStore,
    aniListSettings: AniListSettings,
    private val httpClient: HttpClient,
    private val apolloClient: ApolloClient,
    private val cache: ApolloCache,
) {
    companion object {
        private const val TAG = "AuthedAniListApi"
    }

    val authedUser = MutableStateFlow<AniListViewer?>(null)

    init {
        scope.launch(CustomDispatchers.IO) {
            oAuthStore.authToken
                .flatMapLatest {
                    if (it == null) {
                        flowOf(null)
                    } else {
                        flowFromSuspend {
                            cache.query(AuthedUserQuery(), cacheTime = Duration.INFINITE)
                                .viewer
                                ?.let(::AniListViewer)
                        }
                            .filterNotNull()
                            .startWith(aniListSettings.aniListViewer.take(1).filterNotNull())
                            .distinctUntilChanged()
                    }
                }
                .catch { Logger.d(TAG, it) { "Error loading authed user" } }
                .onEach(aniListSettings.aniListViewer::emit)
                .collectLatest(authedUser::emit)
        }
    }

    open suspend fun viewerMediaList(
        userId: String,
        type: MediaType,
        status: MediaListStatus? = null,
        includeDescription: Boolean,
    ) = queryCacheAndNetwork(
        ViewerMediaListQuery(
            userId = userId.toInt(),
            type = type,
            status = Optional.presentIfNotNull(status),
            includeDescription = includeDescription,
        )
    ).map { it.transformResult { it.mediaListCollection } }

    open suspend fun userMediaList(
        userId: String,
        type: MediaType,
        status: MediaListStatus? = null,
        includeDescription: Boolean,
    ) = query(
        UserMediaListQuery(
            userId = userId.toInt(),
            type = type,
            status = Optional.presentIfNotNull(status),
            includeDescription = includeDescription,
        )
    ).mediaListCollection

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

    open suspend fun genres() = cache.query(GenresQuery(), cacheTime = 1.days)

    open suspend fun tags() = cache.query(MediaTagsQuery(), cacheTime = 1.days)

    open suspend fun mediaDetails(id: String, skipCache: Boolean) = queryLoadingResult {
        runCatching {
            cache.query(MediaDetailsQuery(id.toInt()), skipCache = skipCache) {
                // TODO: This doesn't work, updatedAt is far more frequent than expected
                // If the media was last updated in the distant past,
                // assume it won't change a lot and use an expiry of 7 days
                val updatedAt = it.media?.updatedAt
                if (updatedAt != null && Instant.fromEpochSeconds(updatedAt.toLong())
                        .plus(90.days) < Clock.System.now()
                ) {
                    7.days
                } else {
                    1.days
                }
            }
        }
    }

    open suspend fun mediaDetailsUserData(id: String) =
        queryLoadingResult(MediaDetailsUserDataQuery(id.toInt()), skipCache = true)

    open suspend fun mediaDetailsCharactersPage(
        mediaId: String,
        page: Int,
        perPage: Int = 5,
        skipCache: Boolean,
    ) = query(
        MediaDetailsCharactersPageQuery(
            mediaId = mediaId.toInt(),
            page = page,
            perPage = perPage,
        ),
        skipCache = skipCache,
    ).media

    open suspend fun mediaDetailsStaffPage(
        mediaId: String,
        page: Int,
        perPage: Int = 10,
        skipCache: Boolean,
    ) = query(
        MediaDetailsStaffPageQuery(
            mediaId = mediaId.toInt(),
            page = page,
            perPage = perPage,
        ),
        skipCache = skipCache,
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
                    month = Optional.present(it.month.value),
                    day = Optional.present(it.dayOfMonth),
                )
            }),
            completedAt = Optional.present(completedAt?.let {
                FuzzyDateInput(
                    year = Optional.present(it.year),
                    month = Optional.present(it.month.value),
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

    open suspend fun characterDetails(id: String, skipCache: Boolean) =
        queryLoadingResult(CharacterDetailsQuery(id.toInt()), skipCache)

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
        scope.launch(CustomDispatchers.IO) {
            try {
                // TODO: This doesn't actually work right now, API doesn't support revoking
                // TODO: Notify user that to really log out, they need to go to website
                val result = executeLogout()?.bodyAsText()
                Logger.d(TAG) { "Logging out response: $result" }
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Error logging out" }
            }

            oAuthStore.clearAuthToken()
            cache.evict(AuthedUserQuery())
        }
    }

    private suspend fun executeLogout(): HttpResponse? {
        val authHeader = oAuthStore.authHeader
        if (authHeader == null || oAuthStore.authToken.value == null) {
            return null
        }
        return httpClient.post {
            url(AniListUtils.GRAPHQL_API_URL)
            header("Authorization", authHeader)
            setBody("""{"query":"mutation{Logout}","variables":{}}""")
        }
    }

    open suspend fun homeAnime(
        perPage: Int = 10,
        skipCache: Boolean,
    ): LoadingResult<HomeAnimeQuery.Data> {
        val currentSeasonYear = AniListUtils.getCurrentSeasonYear()
        val nextSeasonYear = AniListUtils.getNextSeasonYear(currentSeasonYear)
        val lastSeasonYear = AniListUtils.getPreviousSeasonYear(currentSeasonYear)
        return queryLoadingResult(
            HomeAnimeQuery(
                currentSeason = currentSeasonYear.first,
                currentYear = currentSeasonYear.second,
                lastSeason = lastSeasonYear.first,
                lastYear = lastSeasonYear.second,
                nextSeason = nextSeasonYear.first,
                nextYear = nextSeasonYear.second,
                perPage = perPage,
            ),
            skipCache = skipCache,
        )
    }

    open suspend fun homeManga(perPage: Int = 10, skipCache: Boolean) = queryLoadingResult(
        HomeMangaQuery(
            perPage = perPage,
            thisYearStart = "${AniListUtils.getCurrentSeasonYear().second}0000",
        ),
        skipCache = skipCache,
    )

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

    open suspend fun userSocialFollowersWithFavorites(
        userId: String,
        sort: List<UserSort>,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserSocialFollowersWithFavoritesQuery(
            userId = userId.toInt(),
            sort = sort,
            perPage = perPage,
            page = page,
        )
    )

    open suspend fun userSocialFollowingWithFavorites(
        userId: String,
        sort: List<UserSort>,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserSocialFollowingWithFavoritesQuery(
            userId = userId.toInt(),
            sort = sort,
            perPage = perPage,
            page = page,
        )
    )

    open suspend fun userSocialActivity(
        isFollowing: Boolean?,
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
        mediaId: String? = null,
    ) = query(
        UserSocialActivityQuery(
            isFollowing = Optional.presentIfNotNull(isFollowing),
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
            mediaId = Optional.presentIfNotNull(mediaId?.toIntOrNull()),
        )
    )

    open suspend fun mediaAndCharacters(mediaId: String) =
        query(MediaAndCharactersQuery(mediaId = mediaId.toInt())).media

    open suspend fun mediaAndCharactersPage(
        mediaId: String,
        sort: List<CharacterSort>,
        role: CharacterRole?,
        page: Int,
        charactersPerPage: Int = 10,
    ) = query(
        MediaAndCharactersPaginationQuery(
            mediaId = mediaId.toInt(),
            page = page,
            sort = sort,
            role = Optional.presentIfNotNull(role),
            charactersPerPage = charactersPerPage,
        )
    )

    open suspend fun mediaAndReviews(mediaId: String) =
        query(MediaAndReviewsQuery(mediaId = mediaId.toInt())).media

    open suspend fun mediaAndReviewsPage(
        mediaId: String,
        sort: List<ReviewSort>,
        page: Int,
        reviewsPerPage: Int = 10,
    ) = query(
        MediaAndReviewsPaginationQuery(
            mediaId = mediaId.toInt(),
            sort = sort,
            page = page,
            reviewsPerPage = reviewsPerPage,
        )
    )

    open suspend fun reviewDetails(reviewId: String) =
        query(ReviewDetailsQuery(reviewId.toInt())).review

    open suspend fun rateReview(reviewId: String, rating: ReviewRating) =
        mutate(RateReviewMutation(id = reviewId.toInt(), rating = rating)).rateReview.userRating

    open suspend fun mediaAndRecommendations(mediaId: String) =
        query(MediaAndRecommendationsQuery(mediaId = mediaId.toInt())).media

    open suspend fun mediaAndRecommendationsPage(
        mediaId: String,
        sort: List<RecommendationSort>,
        page: Int,
        recommendationsPerPage: Int = 10,
    ) = query(
        MediaAndRecommendationsPaginationQuery(
            mediaId = mediaId.toInt(),
            sort = sort,
            page = page,
            recommendationsPerPage = recommendationsPerPage,
        )
    )

    open suspend fun characterAndMedias(characterId: String) =
        query(CharacterAndMediasQuery(characterId = characterId.toInt())).character

    open suspend fun characterAndMediasPage(
        characterId: String,
        sort: List<MediaSort>,
        onList: Boolean?,
        page: Int,
        mediasPerPage: Int = 10,
    ) = query(
        CharacterAndMediasPaginationQuery(
            characterId = characterId.toInt(),
            sort = sort,
            onList = Optional.presentIfNotNull(onList),
            page = page,
            mediasPerPage = mediasPerPage,
        )
    )

    open suspend fun staffAndCharacters(staffId: String) =
        query(StaffAndCharactersQuery(staffId = staffId.toInt())).staff

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

    open suspend fun studioMedias(studioId: String) =
        query(StudioMediasQuery(studioId = studioId.toInt())).studio

    open suspend fun studioMediasPage(
        studioId: String,
        sort: List<MediaSort>,
        main: Boolean?,
        page: Int,
        mediasPerPage: Int = 10,
    ) = query(
        StudioMediasPaginationQuery(
            studioId = studioId.toInt(),
            sort = sort,
            main = Optional.presentIfNotNull(main),
            page = page,
            mediasPerPage = mediasPerPage,
        )
    )

    open suspend fun toggleAnimeFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleAnimeFavoriteMutation(id = idAsInt))
        return query(ToggleMediaResultQuery(id = idAsInt), skipCache = true).media.isFavourite
    }

    open suspend fun toggleMangaFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleMangaFavoriteMutation(id = idAsInt))
        return query(ToggleMediaResultQuery(id = idAsInt), skipCache = true).media.isFavourite
    }

    open suspend fun toggleCharacterFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleCharacterFavoriteMutation(id = idAsInt))
        return query(ToggleCharacterResultQuery(id = idAsInt), skipCache = true)
            .character.isFavourite
    }

    open suspend fun toggleStaffFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleStaffFavoriteMutation(id = idAsInt))
        return query(ToggleStaffResultQuery(id = idAsInt), skipCache = true).staff.isFavourite
    }

    open suspend fun toggleStudioFavorite(id: String): Boolean {
        val idAsInt = id.toInt()
        mutate(ToggleStudioFavoriteMutation(id = idAsInt))
        return query(ToggleStudioResultQuery(id = idAsInt), skipCache = true).studio.isFavourite
    }

    open suspend fun mediaActivities(
        id: String,
        sort: List<ActivitySort>,
        following: Boolean,
        activitiesPerPage: Int = 10,
    ) = query(
        MediaActivityQuery(
            mediaId = id.toInt(),
            sort = sort,
            following = following,
            activitiesPerPage = activitiesPerPage,
        )
    )

    open suspend fun mediaActivitiesPage(
        id: String,
        sort: List<ActivitySort>,
        following: Boolean,
        page: Int,
        activitiesPerPage: Int = 10,
    ) = query(
        MediaActivityPageQuery(
            mediaId = id.toInt(),
            sort = sort,
            following = following,
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
        cache.query(LicensorsQuery(mediaType = mediaType), cacheTime = 1.days)
            .externalLinkSourceCollection.filterNotNull()

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

    open suspend fun homeReviews(mediaType: MediaType, page: Int, perPage: Int = 10) =
        query(HomeReviewsQuery(mediaType = mediaType, page = page, perPage = perPage))

    open suspend fun homeRecommendations(onList: Boolean?, page: Int, perPage: Int) =
        query(
            HomeRecommendationsQuery(
                onList = Optional.presentIfNotNull(onList),
                page = page,
                perPage = perPage,
            )
        )

    open suspend fun reviewSearch(
        sort: List<ReviewSort>,
        mediaType: MediaType,
        mediaId: String?,
        page: Int,
        perPage: Int = 10,
    ) =
        query(
            ReviewSearchQuery(
                sort = Optional.presentIfNotNull(sort.ifEmpty { null }),
                mediaType = mediaType,
                mediaId = Optional.presentIfNotNull(mediaId?.toIntOrNull()),
                page = page,
                perPage = perPage
            )
        )

    open suspend fun mediaAutocomplete(query: String, isAdult: Boolean?, mediaType: MediaType?) =
        query(
            MediaAutocompleteQuery(
                search = query,
                mediaType = Optional.presentIfNotNull(mediaType),
                isAdult = Optional.presentIfNotNull(isAdult),
                page = 1,
                perPage = 10,
            )
        )

    open suspend fun recommendationSearch(
        sort: List<RecommendationSort>,
        sourceMediaId: String?,
        targetMediaId: String?,
        ratingGreater: Int?,
        ratingLesser: Int?,
        onList: Boolean,
        page: Int,
        perPage: Int = 10,
    ) = query(
        RecommendationSearchQuery(
            sort = sort,
            sourceMediaId = Optional.presentIfNotNull(sourceMediaId?.toIntOrNull()),
            targetMediaId = Optional.presentIfNotNull(targetMediaId?.toIntOrNull()),
            ratingGreater = Optional.presentIfNotNull(ratingGreater),
            ratingLesser = Optional.presentIfNotNull(ratingLesser),
            onList = Optional.presentIfNotNull(onList.takeIf { it }),
            page = page,
            perPage = perPage,
        )
    )

    open suspend fun userFavoritesAnime(
        userId: String,
        includeDescription: Boolean,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserFavoritesAnimeQuery(
            userId = userId.toInt(),
            includeDescription = includeDescription,
            page = page,
            perPage = perPage
        )
    )

    open suspend fun userFavoritesManga(
        userId: String,
        includeDescription: Boolean,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserFavoritesMangaQuery(
            userId = userId.toInt(),
            includeDescription = includeDescription,
            page = page,
            perPage = perPage
        )
    )

    open suspend fun userFavoritesCharacters(
        userId: String,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserFavoritesCharactersQuery(
            userId = userId.toInt(),
            page = page,
            perPage = perPage
        )
    )

    open suspend fun userFavoritesStaff(
        userId: String,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserFavoritesStaffQuery(
            userId = userId.toInt(),
            page = page,
            perPage = perPage
        )
    )

    open suspend fun userFavoritesStudios(
        userId: String,
        page: Int,
        perPage: Int = 10,
    ) = query(
        UserFavoritesStudiosQuery(
            userId = userId.toInt(),
            page = page,
            perPage = perPage
        )
    )

    open suspend fun mediaDetailsActivity(mediaId: String, includeFollowing: Boolean) =
        query(
            MediaDetailsActivityQuery(
                mediaId = mediaId.toInt(),
                includeFollowing = includeFollowing,
            )
        )

    open suspend fun anime2AnimeCount() =
        queryResult(Anime2AnimeCountQuery()) { it.siteStatistics?.anime?.nodes?.firstOrNull() }

    open suspend fun anime2AnimeRandomAnime(page: Int, minStaffAndCharactersCount: Int) =
        queryResult(
            Anime2AnimeRandomAnimeQuery(
                page = page,
                // TODO: This technically forces count + 1
                minStaffAndCharactersCount = minStaffAndCharactersCount,
            )
        ) { it.page?.media }

    open suspend fun anime2AnimeMedia(mediaId: String) =
        queryResult(Anime2AnimeMediaQuery(mediaId = mediaId.toInt())) { it.media }

    open suspend fun anime2AnimeConnections(mediaId: String) =
        query(Anime2AnimeConnectionsQuery(mediaId = mediaId.toInt()))

    open suspend fun anime2AnimeConnectionDetails(
        mediaId: String,
        characterIds: List<String>,
        voiceActorIds: List<String>,
        staffIds: List<String>,
    ) = query(
        Anime2AnimeConnectionDetailsQuery(
            mediaId = mediaId.toInt(),
            includeCharacter = characterIds.isNotEmpty(),
            characterIds = characterIds.map(String::toInt),
            includeVoiceActor = voiceActorIds.isNotEmpty(),
            voiceActorIds = voiceActorIds.map(String::toInt),
            includeStaff = staffIds.isNotEmpty(),
            staffIds = staffIds.map(String::toInt),
        )
    )

    open suspend fun anime2AnimeMediaCharacters(mediaId: String, page: Int, perPage: Int) =
        queryResult(
            Anime2AnimeMediaCharactersQuery(
                mediaId = mediaId.toInt(),
                page = page,
                perPage = perPage,
            )
        ) { it.media }

    open suspend fun anime2AnimeMediaStaff(mediaId: String, page: Int, perPage: Int) =
        queryResult(
            Anime2AnimeMediaStaffQuery(
                mediaId = mediaId.toInt(),
                page = page,
                perPage = perPage,
            )
        ) { it.media }

    // TODO: Use queryCacheAndNetwork for everything
    // TODO: Use a result object to avoid throwing
    private suspend fun <D : Query.Data> query(query: Query<D>, skipCache: Boolean = false) =
        apolloClient.query(query)
            .fetchPolicy(FetchPolicy.NetworkFirst)
            // TODO: This breaks mutate -> refreshes, like when replying/commenting
//            .fetchPolicy(if (skipCache) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst)
            .execute()
            .dataOrError

    private suspend fun <Data : Query.Data> queryLoadingResult(
        query: Query<Data>,
        skipCache: Boolean,
    ): LoadingResult<Data> {
        val fetchPolicy = if (skipCache) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst
        val result = apolloClient.query(query)
            .fetchPolicy(fetchPolicy)
            .execute()
        val errors = result.errors
        val error =  if (errors.isNullOrEmpty()) {
            LoadingResult.Error(
                message = UtilsStrings.error_loading_from_network,
                throwable = result.exception,
            )
        } else {
            LoadingResult.Error(
                message = errors.joinToString { it.message },
                throwable = result.exception,
            )
        }
        return LoadingResult(
            loading = false,
            success = result.data != null,
            result = result.data,
            error = error,
        )
    }

    private suspend fun <Data : Query.Data> queryLoadingResult(
        query: suspend () -> Result<Data>,
    ): LoadingResult<Data> {
        val result = query()
        val data = result.getOrNull()
        if (result.isFailure || data == null) {
            return LoadingResult.error(
                error = UtilsStrings.error_loading_from_network,
                throwable = result.exceptionOrNull()
            )
        }
        return LoadingResult.success(data)
    }

    private suspend fun <Data : Query.Data, Output> queryResult(
        query: Query<Data>,
        fetchPolicy: FetchPolicy = FetchPolicy.CacheFirst,
        transform: (Data) -> Output,
    ) = try {
        Result.success(
            transform(
                apolloClient.query(query)
                    .fetchPolicy(fetchPolicy)
                    .execute()
                    .dataOrThrow()
            )
        )
    } catch (t: Throwable) {
        Result.failure(t)
    }

    private suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>) =
        apolloClient.mutation(mutation).execute().dataOrThrow()

    private fun <D : Query.Data> queryCacheAndNetwork(query: Query<D>) = flow {
        coroutineScope {
            val cache = async {
                apolloClient.query(query).fetchPolicy(FetchPolicy.CacheOnly).execute()
            }
            val network = async {
                apolloClient.query(query).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            }
            select {
                cache.onAwait {
                    emit(it to null)
                    emit(it to network.await())
                }
                network.onAwait {
                    emit(null to it)
                    if (it.hasErrors()) {
                        emit(cache.await() to it)
                    } else {
                        cache.cancel()
                    }
                }
            }
        }
    }.flowOn(CustomDispatchers.IO)
        .mapLatestNotNull { (cache, network) ->
            val cacheHasErrors = cache?.exception != null
            if (network != null) {
                val networkHasErrors = network.exception != null
                val result = if (networkHasErrors) cache?.data else network.data
                LoadingResult(
                    success = !networkHasErrors,
                    result = result,
                    error = if (networkHasErrors) {
                        LoadingResult.Error(UtilsStrings.error_loading_from_network, network.exception)
                    } else if (result == null && cacheHasErrors) {
                        LoadingResult.Error(UtilsStrings.error_loading_from_cache, cache.exception)
                    } else {
                        null
                    },
                )
            } else if (cacheHasErrors) {
                null
            } else {
                LoadingResult(
                    loading = true,
                    success = true,
                    result = cache?.data,
                    error = null,
                )
            }
        }

    val <D : Operation.Data> ApolloResponse<D>.dataOrError get() = when {
        data != null -> data!!
        hasErrors() -> throw IOException(errors!!.joinToString { it.message })
        exception != null -> throw DefaultApolloException("An exception happened", exception)
        else -> dataOrThrow()
    }
}
