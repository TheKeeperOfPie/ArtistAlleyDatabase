package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.anilist.ActivityDetailsQuery
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.UserSocialActivityQuery
import com.anilist.type.ActivitySort
import com.anilist.type.ActivityType
import com.anilist.type.AiringSort
import com.anilist.type.CharacterRole
import com.anilist.type.CharacterSort
import com.anilist.type.ExternalLinkMediaType
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
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import java.io.IOException
import java.time.LocalDate

/**
 * Strips isAdult media from API calls on release builds.
 */
class AuthedAniListApiWrapper(
    scopedApplication: ScopedApplication,
    oAuthStore: AniListOAuthStore,
    networkSettings: NetworkSettings,
    aniListSettings: AniListSettings,
    okHttpClient: OkHttpClient,
) : AuthedAniListApi(
    scopedApplication,
    oAuthStore,
    networkSettings,
    aniListSettings,
    okHttpClient,
) {

    override suspend fun viewerMediaList(
        userId: String,
        type: MediaType,
        status: MediaListStatus?,
        includeDescription: Boolean,
    ) = super.viewerMediaList(userId, type, status, includeDescription).map {
        it.transformResult {
            it.copy(lists = it.lists?.map {
                it?.copy(entries = it.entries?.filter { it?.media?.isAdult == false })
            })
        }
    }

    override suspend fun userMediaList(
        userId: String,
        type: MediaType,
        status: MediaListStatus?,
        includeDescription: Boolean,
    ) = super.userMediaList(userId, type, status, includeDescription).let {
        it.copy(lists = it.lists?.map {
            it?.copy(entries = it.entries?.filter { it?.media?.isAdult == false })
        })
    }

    override suspend fun searchMedia(
        query: String,
        mediaType: MediaType,
        page: Int?,
        perPage: Int?,
        sort: List<MediaSort>?,
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
    ) = super.searchMedia(
        query,
        mediaType,
        page,
        perPage,
        sort,
        genreIn,
        genreNotIn,
        tagIn,
        tagNotIn,
        statusIn,
        statusNotIn,
        formatIn,
        formatNotIn,
        showAdult = false,
        onList,
        season,
        seasonYear,
        startDateGreater,
        startDateLesser,
        averageScoreGreater,
        averageScoreLesser,
        episodesGreater,
        episodesLesser,
        volumesGreater,
        volumesLesser,
        chaptersGreater,
        chaptersLesser,
        sourcesIn,
        minimumTagRank,
        licensedByIdIn,
        includeDescription,
    ).let {
        it.copy(page = it.page.copy(media = it.page.media.filter { it?.isAdult == false }))
    }

    override suspend fun genres() = super.genres().let {
        it.copy(genreCollection = it.genreCollection?.filterNot {
            it.equals("Hentai", ignoreCase = true)
        })
    }

    override suspend fun tags() = super.tags().let {
        it.copy(mediaTagCollection = it.mediaTagCollection?.filter {
            it?.category?.contains("Sex", ignoreCase = true) != true
        })
    }

    override suspend fun mediaDetails(id: String) = super.mediaDetails(id).map {
        val media = it.result?.media
        if (media != null && media.isAdult != false) throw IOException("Cannot load this media")
        it.transformResult {
            it.copy(
                media = it.media?.copy(
                    relations = it.media?.relations?.copy(
                        edges = it.media?.relations?.edges?.filter { it?.node?.isAdult == false }),
                )
            )
        }
    }

    override suspend fun mediaDetails2(id: String) = super.mediaDetails2(id).map {
        val media = it.result?.media
        if (media != null && media.isAdult != false) throw IOException("Cannot load this media")
        it.transformResult {
            it.copy(
                media = it.media?.copy(
                    recommendations = it.media?.recommendations?.copy(
                        edges = it.media?.recommendations?.edges?.filter {
                            it?.node?.mediaRecommendation?.isAdult == false
                        }),
                )
            )
        }
    }

    override suspend fun mediaDetailsCharactersPage(
        mediaId: String,
        page: Int,
        perPage: Int,
    ) = super.mediaDetailsCharactersPage(mediaId, page, perPage).also {
        if (it.isAdult != false) throw IOException("Cannot load this media")
    }

    override suspend fun mediaDetailsStaffPage(mediaId: String, page: Int, perPage: Int) =
        super.mediaDetailsStaffPage(mediaId, page, perPage).also {
            if (it.isAdult != false) throw IOException("Cannot load this media")
        }

    override suspend fun mediaTitlesAndImages(mediaIds: List<Int>) =
        super.mediaTitlesAndImages(mediaIds).let {
            it.filter { it.isAdult == false }
        }

    override suspend fun mediaListEntry(id: String) = super.mediaListEntry(id).also {
        if (it.media.isAdult != false) throw IOException("Cannot load this media")
    }

    override suspend fun deleteMediaListEntry(id: String) = super.deleteMediaListEntry(id)

    override suspend fun saveMediaListEntry(
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
    ) = super.saveMediaListEntry(
        id,
        mediaId,
        status,
        scoreRaw,
        progress,
        progressVolumes,
        repeat,
        priority,
        private,
        notes,
        startedAt,
        completedAt,
        hiddenFromStatusLists
    ).also {
        if (it.media?.isAdult != false) throw IOException("Cannot load this media")
    }

    override suspend fun saveMediaListEntryProgressOnly(
        id: String?,
        mediaId: String,
        progress: Int,
    ) = super.saveMediaListEntryProgressOnly(id, mediaId, progress).also {
        if (it.media?.isAdult != false) throw IOException("Cannot load this media")
    }

    override suspend fun mediaByIds(
        ids: List<Int>,
        includeDescription: Boolean,
    ) = super.mediaByIds(ids, includeDescription).let {
        it.filter { it.isAdult == false }
    }

    override suspend fun searchCharacters(
        query: String,
        page: Int?,
        perPage: Int?,
        isBirthday: Boolean?,
        sort: List<CharacterSort>?,
    ) = super.searchCharacters(query, page, perPage, isBirthday, sort).let {
        it.copy(page = it.page.copy(characters = it.page.characters?.map {
            it?.copy(media = it.media?.copy(edges = it.media?.edges?.filter { it?.node?.isAdult == false }))
        }))
    }

    override suspend fun characterDetails(id: String) = super.characterDetails(id).map {
        it.transformResult {
            it.copy(
                character = it.character?.copy(
                    media = it.character?.media?.copy(
                        edges = it.character?.media?.edges?.filter { it?.node?.isAdult == false }
                    )
                )
            )
        }
    }

    override suspend fun characterDetailsMediaPage(
        characterId: String,
        page: Int,
        perPage: Int,
    ) = super.characterDetailsMediaPage(characterId, page, perPage).let {
        it.copy(media = it.media.copy(edges = it.media.edges.filter { it?.node?.isAdult == false }))
    }

    override suspend fun searchStaff(
        query: String,
        page: Int?,
        perPage: Int?,
        isBirthday: Boolean?,
        sort: List<StaffSort>?,
    ) = super.searchStaff(query, page, perPage, isBirthday, sort).let {
        it.copy(page = it.page.copy(staff = it.page.staff?.map {
            it?.copy(
                staffMedia = it.staffMedia?.copy(
                    nodes = it.staffMedia?.nodes?.filter { it?.isAdult == false })
            )
        }))
    }

    override suspend fun staffDetails(id: String) = super.staffDetails(id)

    override suspend fun staffDetailsCharactersPage(
        staffId: String,
        page: Int,
        perPage: Int,
    ) = super.staffDetailsCharactersPage(staffId, page, perPage)

    override suspend fun staffDetailsCharacterMediaPagination(
        id: String,
        page: Int,
    ) = super.staffDetailsCharacterMediaPagination(id, page)

    override suspend fun staffDetailsStaffMediaPagination(
        id: String,
        page: Int,
    ) = super.staffDetailsStaffMediaPagination(id, page).let {
        it.copy(edges = it.edges?.filter { it?.node?.isAdult == false })
    }

    override suspend fun searchUsers(
        query: String,
        page: Int?,
        perPage: Int?,
        sort: List<UserSort>?,
        isModerator: Boolean?,
    ) = super.searchUsers(query, page, perPage, sort, isModerator).let {
        it.copy(page = it.page.copy(users = it.page.users?.map {
            it?.copy(
                favourites = it.favourites?.copy(
                    anime = it.favourites?.anime?.copy(edges = it.favourites?.anime?.edges?.filter { it?.node?.isAdult == false }),
                    manga = it.favourites?.manga?.copy(edges = it.favourites?.manga?.edges?.filter { it?.node?.isAdult == false }),
                )
            )
        }))
    }

    override suspend fun user(id: String) = super.user(id).let {
        it?.copy(
            favourites = it.favourites?.copy(
                anime = it.favourites?.anime?.copy(nodes = it.favourites?.anime?.nodes?.filter { it?.isAdult == false }),
                manga = it.favourites?.manga?.copy(nodes = it.favourites?.manga?.nodes?.filter { it?.isAdult == false }),
            )
        )
    }

    override suspend fun userDetailsAnimePage(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userDetailsAnimePage(userId, page, perPage).let {
        it.copy(nodes = it.nodes.filter { it?.isAdult == false })
    }

    override suspend fun userDetailsMangaPage(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userDetailsMangaPage(userId, page, perPage).let {
        it.copy(nodes = it.nodes.filter { it?.isAdult == false })
    }

    override suspend fun userDetailsCharactersPage(userId: String, page: Int, perPage: Int) =
        super.userDetailsCharactersPage(userId, page, perPage)

    override suspend fun userDetailsStaffPage(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userDetailsStaffPage(userId, page, perPage)

    override suspend fun userDetailsStudiosPage(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userDetailsStudiosPage(userId, page, perPage)

    override suspend fun homeAnime(perPage: Int) = super.homeAnime(perPage).let {
        it.copy(
            trending = it.trending?.copy(media = it.trending?.media?.filter { it?.isAdult == false }),
            popularThisSeason = it.popularThisSeason?.copy(media = it.popularThisSeason?.media?.filter { it?.isAdult == false }),
            lastAdded = it.lastAdded?.copy(media = it.lastAdded?.media?.filter { it?.isAdult == false }),
            popularLastSeason = it.popularLastSeason?.copy(media = it.popularLastSeason?.media?.filter { it?.isAdult == false }),
            popularNextSeason = it.popularNextSeason?.copy(media = it.popularNextSeason?.media?.filter { it?.isAdult == false }),
        )
    }

    override suspend fun homeManga(perPage: Int) = super.homeManga(perPage).let {
        it.copy(
            trending = it.trending?.copy(media = it.trending?.media?.filter { it?.isAdult == false }),
            lastAdded = it.lastAdded?.copy(media = it.lastAdded?.media?.filter { it?.isAdult == false }),
        )
    }

    override suspend fun airingSchedule(
        startTime: Long,
        endTime: Long,
        sort: AiringSort,
        perPage: Int,
        page: Int,
    ) = super.airingSchedule(startTime, endTime, sort, perPage, page).let {
        it.copy(page = it.page?.copy(airingSchedules = it.page?.airingSchedules?.filter { it?.media?.isAdult == false }))
    }

    override suspend fun toggleFollow(userId: Int) = super.toggleFollow(userId)

    override suspend fun userSocialFollowers(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userSocialFollowers(userId, page, perPage)

    override suspend fun userSocialFollowing(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userSocialFollowing(userId, page, perPage)

    override suspend fun userSocialFollowersWithFavorites(
        userId: String,
        sort: List<UserSort>,
        page: Int,
        perPage: Int,
    ) = super.userSocialFollowersWithFavorites(userId, sort, page, perPage).let {
        it.copy(page = it.page?.copy(followers = it.page?.followers?.mapNotNull {
            it?.copy(
                favourites = it.favourites?.copy(
                    anime = it.favourites?.anime?.copy(edges = it.favourites?.anime?.edges?.filter {
                        it?.node?.isAdult == false
                    }),
                    manga = it.favourites?.manga?.copy(edges = it.favourites?.manga?.edges?.filter {
                        it?.node?.isAdult == false
                    }),
                )
            )
        }))
    }

    override suspend fun userSocialFollowingWithFavorites(
        userId: String,
        sort: List<UserSort>,
        page: Int,
        perPage: Int,
    ) = super.userSocialFollowingWithFavorites(userId, sort, page, perPage).let {
        it.copy(page = it.page?.copy(following = it.page?.following?.mapNotNull {
            it?.copy(
                favourites = it.favourites?.copy(
                    anime = it.favourites?.anime?.copy(edges = it.favourites?.anime?.edges?.filter {
                        it?.node?.isAdult == false
                    }),
                    manga = it.favourites?.manga?.copy(edges = it.favourites?.manga?.edges?.filter {
                        it?.node?.isAdult == false
                    }),
                )
            )
        }))
    }

    override suspend fun userSocialActivity(
        isFollowing: Boolean?,
        page: Int,
        perPage: Int,
        sort: List<ActivitySort>,
        userId: String?,
        userIdNot: String?,
        typeIn: List<ActivityType>?,
        typeNotIn: List<ActivityType>?,
        hasReplies: Boolean?,
        createdAtGreater: Int?,
        createdAtLesser: Int?,
        mediaId: String?,
    ) = super.userSocialActivity(
        isFollowing,
        page,
        perPage,
        sort,
        userId,
        userIdNot,
        typeIn,
        typeNotIn,
        hasReplies,
        createdAtGreater,
        createdAtLesser,
        mediaId,
    ).let {
        it.copy(page = it.page?.copy(activities = it.page?.activities?.filter {
            when (it) {
                is UserSocialActivityQuery.Data.Page.ListActivityActivity -> it.media?.isAdult == false
                is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> true
                is UserSocialActivityQuery.Data.Page.OtherActivity -> true
                is UserSocialActivityQuery.Data.Page.TextActivityActivity -> true
                null -> true
            }
        }))
    }

    override suspend fun mediaAndCharacters(mediaId: String) =
        super.mediaAndCharacters(mediaId).also {
            if (it.isAdult != false) throw IOException("Cannot load media")
        }

    override suspend fun mediaAndCharactersPage(
        mediaId: String,
        sort: List<CharacterSort>,
        role: CharacterRole?,
        page: Int,
        charactersPerPage: Int,
    ) = super.mediaAndCharactersPage(mediaId, sort, role, page, charactersPerPage).also {
        if (it.media.isAdult != false) throw IOException("Cannot load media")
    }

    override suspend fun mediaAndReviews(mediaId: String) = super.mediaAndReviews(mediaId).also {
        if (it.isAdult != false) throw IOException("Cannot load media")
    }

    override suspend fun mediaAndReviewsPage(
        mediaId: String,
        sort: List<ReviewSort>,
        page: Int,
        reviewsPerPage: Int,
    ) = super.mediaAndReviewsPage(mediaId, sort, page, reviewsPerPage).also {
        if (it.media.isAdult != false) throw IOException("Cannot load media")
    }

    override suspend fun reviewDetails(reviewId: String) = super.reviewDetails(reviewId).also {
        if (it.media?.isAdult != false) throw IOException("Cannot load media")
    }

    override suspend fun rateReview(reviewId: String, rating: ReviewRating) =
        super.rateReview(reviewId, rating)

    override suspend fun mediaAndRecommendations(mediaId: String) =
        super.mediaAndRecommendations(mediaId).also {
            if (it.isAdult != false) throw IOException("Cannot load media")
        }

    override suspend fun mediaAndRecommendationsPage(
        mediaId: String,
        sort: List<RecommendationSort>,
        page: Int,
        recommendationsPerPage: Int,
    ) = super.mediaAndRecommendationsPage(mediaId, sort, page, recommendationsPerPage).let {
        if (it.media.isAdult != false) throw IOException("Cannot load media")
        it.copy(
            media = it.media.copy(
                recommendations = it.media.recommendations.copy(
                    nodes = it.media.recommendations.nodes?.filter { it?.mediaRecommendation?.isAdult == false })
            )
        )
    }

    override suspend fun characterAndMedias(characterId: String) =
        super.characterAndMedias(characterId)

    override suspend fun characterAndMediasPage(
        characterId: String,
        sort: List<MediaSort>,
        onList: Boolean?,
        page: Int,
        mediasPerPage: Int,
    ) = super.characterAndMediasPage(characterId, sort, onList, page, mediasPerPage).let {
        it.copy(character = it.character.copy(media = it.character.media.copy(nodes = it.character.media.nodes?.filter { it?.isAdult == false })))
    }

    override suspend fun staffAndCharacters(staffId: String) = super.staffAndCharacters(staffId)

    override suspend fun staffAndCharactersPage(
        staffId: String,
        sort: List<CharacterSort>,
        page: Int,
        charactersPerPage: Int,
    ) = super.staffAndCharactersPage(staffId, sort, page, charactersPerPage).let {
        it.copy(staff = it.staff.copy(characters = it.staff.characters.copy(edges = it.staff.characters.edges?.map {
            it?.copy(node = it.node.copy(media = it.node.media?.copy(nodes = it.node.media?.nodes?.filter { it?.isAdult == false })))
        })))
    }

    override suspend fun searchStudios(
        query: String,
        page: Int,
        perPage: Int,
        sort: List<StudioSort>,
    ) = super.searchStudios(query, page, perPage, sort).let {
        it.copy(page = it.page.copy(studios = it.page.studios?.map {
            it?.copy(
                main = it.main?.copy(
                    nodes = it.main?.nodes?.filter { it?.isAdult == false }),
                nonMain = it.nonMain?.copy(
                    nodes = it.nonMain?.nodes?.filter { it?.isAdult == false }),
            )
        }))
    }

    override suspend fun studioMedias(studioId: String) = super.studioMedias(studioId)

    override suspend fun studioMediasPage(
        studioId: String,
        sort: List<MediaSort>,
        main: Boolean?,
        page: Int,
        mediasPerPage: Int,
    ) = super.studioMediasPage(studioId, sort, main, page, mediasPerPage).let {
        it.copy(studio = it.studio.copy(media = it.studio.media.copy(nodes = it.studio.media.nodes?.filter { it?.isAdult == false })))
    }

    override suspend fun toggleAnimeFavorite(id: String) = super.toggleAnimeFavorite(id)

    override suspend fun toggleMangaFavorite(id: String) = super.toggleMangaFavorite(id)

    override suspend fun toggleCharacterFavorite(id: String) = super.toggleCharacterFavorite(id)

    override suspend fun toggleStaffFavorite(id: String) = super.toggleStaffFavorite(id)

    override suspend fun toggleStudioFavorite(id: String) = super.toggleStudioFavorite(id)

    override suspend fun mediaActivities(
        id: String,
        sort: List<ActivitySort>,
        following: Boolean,
        activitiesPerPage: Int,
    ) = super.mediaActivities(id, sort, following, activitiesPerPage).also {
        if (it.media.isAdult != false) {
            throw IOException("Cannot load this media")
        }
    }

    override suspend fun mediaActivitiesPage(
        id: String,
        sort: List<ActivitySort>,
        following: Boolean,
        page: Int,
        activitiesPerPage: Int,
    ) = super.mediaActivitiesPage(id, sort, following, page, activitiesPerPage)

    override suspend fun toggleActivityLike(id: String) = super.toggleActivityLike(id)

    override suspend fun toggleActivitySubscribe(id: String, subscribe: Boolean) =
        super.toggleActivitySubscribe(id, subscribe)

    override suspend fun activityDetails(id: String) = super.activityDetails(id).also {
        val activity = it.activity
        if (activity is ActivityDetailsQuery.Data.ListActivityActivity
            && activity.media?.isAdult != false
        ) {
            throw IOException("Cannot load this media")
        }
    }

    override suspend fun activityReplies(
        id: String,
        page: Int,
        perPage: Int,
    ) = super.activityReplies(id, page, perPage)

    override suspend fun toggleActivityReplyLike(id: String) = super.toggleActivityReplyLike(id)

    override suspend fun deleteActivity(id: String) = super.deleteActivity(id)

    override suspend fun deleteActivityReply(id: String) = super.deleteActivityReply(id)

    override suspend fun saveActivityReply(
        activityId: String,
        replyId: String?,
        text: String,
    ) = super.saveActivityReply(activityId, replyId, text)

    override suspend fun licensors(mediaType: ExternalLinkMediaType) = super.licensors(mediaType)

    override suspend fun notifications(
        page: Int,
        perPage: Int,
        typeIn: List<NotificationType>?,
        resetNotificationCount: Boolean,
    ) = super.notifications(page, perPage, typeIn, resetNotificationCount)

    override suspend fun notificationMediaAndActivity(
        mediaIds: List<String>,
        activityIds: List<String>,
    ) = super.notificationMediaAndActivity(mediaIds, activityIds).let {
        it.copy(
            media = it.media?.copy(media = it.media?.media?.filter { it?.isAdult == false }),
            activity = it.activity?.copy(activities = it.activity?.activities?.filter {
                when (it) {
                    is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> it.media?.isAdult == false
                    is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity,
                    is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity,
                    is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity,
                    null,
                    -> true
                }
            })
        )
    }

    override suspend fun forumRoot() = super.forumRoot().let {
        it.copy(
            stickied = it.stickied.copy(threads = it.stickied.threads.filter { it?.mediaCategories?.none { it?.isAdult == true } != false }),
            active = it.active.copy(threads = it.active.threads.filter { it?.mediaCategories?.none { it?.isAdult == true } != false }),
            new = it.new.copy(threads = it.new.threads.filter { it?.mediaCategories?.none { it?.isAdult == true } != false }),
            releases = it.releases.copy(threads = it.releases.threads.filter { it?.mediaCategories?.none { it?.isAdult == true } != false }),
        )
    }

    override suspend fun forumThreadSearch(
        search: String?,
        subscribed: Boolean,
        categoryId: String?,
        mediaCategoryId: String?,
        sort: List<ThreadSort>?,
        page: Int,
        perPage: Int,
    ) = super.forumThreadSearch(
        search,
        subscribed,
        categoryId,
        mediaCategoryId,
        sort,
        page,
        perPage
    ).let {
        it.copy(
            page = it.page.copy(
                threads = it.page.threads
                    ?.filter { it?.mediaCategories?.none { it?.isAdult == true } != false })
        )
    }

    override suspend fun forumThread(threadId: String) = super.forumThread(threadId).also {
        if (it.thread.mediaCategories?.any { it?.isAdult == true } == true) {
            throw IOException("Cannot load this thread")
        }
    }

    override suspend fun forumThreadComments(
        threadId: String,
        page: Int,
        perPage: Int,
    ) = super.forumThreadComments(threadId, page, perPage)

    override suspend fun toggleForumThreadSubscribe(id: String, subscribe: Boolean) =
        super.toggleForumThreadSubscribe(id, subscribe)

    override suspend fun toggleForumThreadLike(id: String) = super.toggleForumThreadLike(id)

    override suspend fun toggleForumThreadCommentLike(id: String) =
        super.toggleForumThreadCommentLike(id)

    override suspend fun deleteForumThreadComment(id: String) = super.deleteForumThreadComment(id)

    override suspend fun saveForumThreadComment(
        threadId: String,
        commentId: String?,
        parentCommentId: String?,
        text: String?,
    ) = super.saveForumThreadComment(threadId, commentId, parentCommentId, text)

    override suspend fun forumThreadSingleCommentTree(threadId: String, commentId: String) =
        super.forumThreadSingleCommentTree(threadId, commentId).also {
            if (it.thread?.mediaCategories?.any { it?.isAdult == true } == true) {
                throw IOException("Cannot load this thread")
            }
        }

    override suspend fun unreadNotificationCount() = super.unreadNotificationCount()

    override suspend fun saveRecommendationRating(
        mediaId: String,
        recommendationMediaId: String,
        rating: RecommendationRating,
    ) = super.saveRecommendationRating(mediaId, recommendationMediaId, rating)

    override suspend fun homeReviews(
        mediaType: MediaType,
        page: Int,
        perPage: Int,
    ) = super.homeReviews(mediaType, page, perPage).let {
        it.copy(page = it.page.copy(reviews = it.page.reviews.filter { it?.media?.isAdult == false }))
    }

    override suspend fun homeRecommendations(
        onList: Boolean?,
        page: Int,
        perPage: Int,
    ) = super.homeRecommendations(onList, page, perPage).let {
        it.copy(page = it.page.copy(recommendations = it.page.recommendations.filter {
            it?.media?.isAdult == false && it.mediaRecommendation.isAdult == false
        }))
    }

    override suspend fun reviewSearch(
        sort: List<ReviewSort>,
        mediaType: MediaType,
        mediaId: String?,
        page: Int,
        perPage: Int,
    ) = super.reviewSearch(sort, mediaType, mediaId, page, perPage).let {
        it.copy(page = it.page.copy(reviews = it.page.reviews.filter { it?.media?.isAdult == false }))
    }

    override suspend fun mediaAutocomplete(
        query: String,
        isAdult: Boolean?,
        mediaType: MediaType?,
    ) = super.mediaAutocomplete(query, isAdult = false, mediaType).let {
        it.copy(page = it.page?.copy(media = it.page?.media?.filter { it?.isAdult == false }))
    }

    override suspend fun recommendationSearch(
        sort: List<RecommendationSort>,
        sourceMediaId: String?,
        targetMediaId: String?,
        ratingGreater: Int?,
        ratingLesser: Int?,
        onList: Boolean,
        page: Int,
        perPage: Int,
    ) = super.recommendationSearch(
        sort,
        sourceMediaId,
        targetMediaId,
        ratingGreater,
        ratingLesser,
        onList,
        page,
        perPage,
    ).let {
        it.copy(page = it.page.copy(recommendations = it.page.recommendations.filter {
            it?.media?.isAdult == false && it.mediaRecommendation.isAdult == false
        }))
    }

    override suspend fun userFavoritesAnime(
        userId: String,
        includeDescription: Boolean,
        page: Int,
        perPage: Int,
    ) = super.userFavoritesAnime(userId, includeDescription, page, perPage).let {
        it.copy(
            user = it.user?.copy(
                favourites = it.user?.favourites?.copy(
                    anime = it.user?.favourites?.anime?.copy(
                        nodes = it.user?.favourites?.anime?.nodes?.filter { it?.isAdult == false })
                )
            )
        )
    }

    override suspend fun userFavoritesManga(
        userId: String,
        includeDescription: Boolean,
        page: Int,
        perPage: Int,
    ) = super.userFavoritesManga(userId, includeDescription, page, perPage).let {
        it.copy(
            user = it.user?.copy(
                favourites = it.user?.favourites?.copy(
                    manga = it.user?.favourites?.manga?.copy(
                        nodes = it.user?.favourites?.manga?.nodes?.filter { it?.isAdult == false })
                )
            )
        )
    }

    override suspend fun userFavoritesCharacters(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userFavoritesCharacters(userId, page, perPage).let {
        it.copy(
            user = it.user?.copy(
                favourites = it.user?.favourites?.copy(
                    characters = it.user?.favourites?.characters?.copy(
                        nodes = it.user?.favourites?.characters?.nodes?.map {
                            it?.copy(media = it.media?.copy(edges = it.media?.edges?.filter { it?.node?.isAdult == false }))
                        })
                )
            )
        )
    }

    override suspend fun userFavoritesStaff(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userFavoritesStaff(userId, page, perPage).let {
        it.copy(
            user = it.user?.copy(
                favourites = it.user?.favourites?.copy(
                    staff = it.user?.favourites?.staff?.copy(
                        nodes = it.user?.favourites?.staff?.nodes?.map {
                            it?.copy(staffMedia = it.staffMedia?.copy(nodes = it.staffMedia?.nodes?.filter { it?.isAdult == false }))
                        })
                )
            )
        )
    }

    override suspend fun userFavoritesStudios(
        userId: String,
        page: Int,
        perPage: Int,
    ) = super.userFavoritesStudios(userId, page, perPage).let {
        it.copy(
            user = it.user?.copy(
                favourites = it.user?.favourites?.copy(
                    studios = it.user?.favourites?.studios?.copy(
                        nodes = it.user?.favourites?.studios?.nodes?.map {
                            it?.copy(
                                main = it.main?.copy(nodes = it.main?.nodes?.filter { it?.isAdult == false }),
                                nonMain = it.nonMain?.copy(nodes = it.nonMain?.nodes?.filter { it?.isAdult == false }),
                            )
                        })
                )
            )
        )
    }

    override suspend fun mediaDetailsActivity(
        mediaId: String,
        includeFollowing: Boolean,
    ) = super.mediaDetailsActivity(mediaId, includeFollowing)
}
