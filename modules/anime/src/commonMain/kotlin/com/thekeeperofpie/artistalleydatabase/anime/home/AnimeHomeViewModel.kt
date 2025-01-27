@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.stateInForCompose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import me.tatarka.inject.annotations.Inject

@Inject
class AnimeHomeViewModel(
    val newsController: AnimeNewsController,
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val recommendationStatusController: RecommendationStatusController,
    private val activityStatusController: ActivityStatusController,
    private val settings: AnimeSettings,
    monetizationController: MonetizationController,
    val notificationsController: NotificationsController,
) : ViewModel() {

    val unlocked = monetizationController.unlocked
    val preferredMediaType = settings.preferredMediaType.value
    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val refresh = RefreshFlow()
    val activity = refresh.updates
        .flatMapLatest { aniListApi.authedUser }
        .flatMapLatest { viewer ->
            AniListPager(perPage = 6, prefetchDistance = 1) {
                val result = aniListApi.userSocialActivity(
                    isFollowing = viewer != null,
                    page = it,
                    perPage = 6,
                    userIdNot = viewer?.id,
                )
                result.page?.pageInfo to
                        result.page?.activities?.filterNotNull().orEmpty()
            }
        }
        .cachedIn(viewModelScope)
        .flatMapLatest { pagingData ->
            combine(
                mediaListStatusController.allChanges(),
                ignoreController.updates(),
                settings.mediaFilteringData(),
            ) { mediaUpdates, ignoredIds, filteringData ->
                pagingData.mapOnIO {
                    ActivityEntry(
                        it.entryId,
                        it,
                        it.liked,
                        it.subscribed,
                        (it as? UserSocialActivityQuery.Data.Page.ListActivityActivity)
                            ?.media?.let { MediaCompactWithTagsEntry(media = it) }
                            ?.let {
                                applyMediaFiltering(
                                    statuses = mediaUpdates,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            },
                    )
                }
            }
        }
        .cachedIn(viewModelScope)
        .flatMapLatest { pagingData ->
            activityStatusController.allChanges()
                .mapLatest { updates ->
                    pagingData.mapOnIO {
                        val liked = updates[it.activityId.valueId]?.liked ?: it.liked
                        val subscribed =
                            updates[it.activityId.valueId]?.subscribed ?: it.subscribed
                        it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                            it.copy(liked = liked, subscribed = subscribed)
                        }
                    }
                }
        }
        .cachedIn(viewModelScope)
        .flowOn(CustomDispatchers.IO)
        .stateInForCompose()

    val recommendations = refresh.updates
        .flatMapLatest {
            AniListPager(perPage = 6, prefetchDistance = 1) {
                val result =
                    aniListApi.homeRecommendations(onList = true, page = it, perPage = 6)
                result.page.pageInfo to result.page.recommendations.filterNotNull()
            }
        }
        .mapLatest {
            it.mapOnIO {
                RecommendationEntry(
                    id = it.id.toString(),
                    user = it.user,
                    media = MediaCompactWithTagsEntry(it.media),
                    mediaRecommendation = MediaCompactWithTagsEntry(it.mediaRecommendation),
                    data = RecommendationData(
                        it.media.id.toString(),
                        it.mediaRecommendation.id.toString(),
                        it.rating ?: 0,
                        userRating = it.userRating ?: RecommendationRating.NO_RATING,
                    )
                )
            }
        }
        .cachedIn(viewModelScope)
        .flatMapLatest { pagingData ->
            combine(
                mediaListStatusController.allChanges(),
                recommendationStatusController.allChanges(),
                ignoreController.updates(),
                settings.mediaFilteringData(),
            ) { mediaStatusUpdates, recommendationUpdates, _, filteringData ->
                pagingData.mapNotNull {
                    val newMedia = applyMediaFiltering(
                        statuses = mediaStatusUpdates,
                        ignoreController = ignoreController,
                        filteringData = filteringData,
                        entry = it.media,
                        filterableData = it.media.mediaFilterable,
                        copy = { copy(mediaFilterable = it) },
                    ) ?: return@mapNotNull null
                    val newMediaRecommendation = applyMediaFiltering(
                        statuses = mediaStatusUpdates,
                        ignoreController = ignoreController,
                        filteringData = filteringData,
                        entry = it.mediaRecommendation,
                        filterableData = it.mediaRecommendation.mediaFilterable,
                        copy = { copy(mediaFilterable = it) },
                    ) ?: return@mapNotNull null
                    val filtered = it.copy(
                        media = newMedia,
                        mediaRecommendation = newMediaRecommendation,
                    )
                    val recommendationUpdate =
                        recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                    val userRating = recommendationUpdate?.rating
                        ?: it.data.userRating
                    filtered.transformIf(userRating != it.data.userRating) {
                        copy(data = data.copy(userRating = userRating))
                    }
                }
            }
        }
        .cachedIn(viewModelScope)
        .flowOn(CustomDispatchers.IO)
        .stateInForCompose()

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    fun refresh() {
        refresh.refresh()
        newsController.refresh()
        notificationsController.forceRefresh()
    }
}
