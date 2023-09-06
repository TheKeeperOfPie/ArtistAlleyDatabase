@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.UserSocialActivityQuery
import com.anilist.type.RecommendationRating
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
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

    private val refresh = MutableStateFlow(-1L)
    val activity = MutableStateFlow(PagingData.empty<ActivityEntry>())
    val recommendations = MutableStateFlow(PagingData.empty<RecommendationEntry>())

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    init {
        collectRecommendations()
        collectActivity()
    }

    private fun collectActivity() {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refresh, aniListApi.authedUser, ::Pair)
                .flatMapLatest { (_, viewer) ->
                    Pager(config = PagingConfig(pageSize = 3, initialLoadSize = 3)) {
                        AniListPagingSource {
                            val result = aniListApi.userSocialActivity(
                                isFollowing = viewer != null,
                                page = it,
                                perPage = 3,
                                userIdNot = viewer?.id,
                            )
                            result.page?.pageInfo to
                                    result.page?.activities?.filterNotNull().orEmpty()
                        }
                    }.flow.cachedIn(viewModelScope)
                }
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaUpdates, ignoredIds, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapOnIO {
                            ActivityEntry(
                                it.entryId,
                                it,
                                it.liked,
                                it.subscribed,
                                (it as? UserSocialActivityQuery.Data.Page.ListActivityActivity)
                                    ?.media?.let {
                                        MediaCompactWithTagsEntry(
                                            media = it,
                                            ignored = ignoreController.isIgnored(it.id.toString()),
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    }?.let {
                                        applyMediaFiltering(
                                            statuses = mediaUpdates,
                                            ignoreController = ignoreController,
                                            showAdult = showAdult,
                                            showIgnored = showIgnored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                            entry = it,
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
                .collectLatest(activity::emit)
        }
    }

    private fun collectRecommendations() {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest {
                Pager(config = PagingConfig(pageSize = 3, initialLoadSize = 3)) {
                    AniListPagingSource(perPage = 3) {
                        val result =
                            aniListApi.homeRecommendations(onList = true, page = it, perPage = 3)
                        result.page.pageInfo to result.page.recommendations.filterNotNull()
                    }
                }.flow
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
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaStatusUpdates, recommendationUpdates, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapNotNull {
                            applyMediaFiltering(
                                statuses = mediaStatusUpdates,
                                ignoreController = ignoreController,
                                showAdult = showAdult,
                                showIgnored = showIgnored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                transform = { it.media },
                                media = it.media.media,
                                copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        media = media.copy(
                                            mediaListStatus = mediaListStatus,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            scoreRaw = scoreRaw,
                                            ignored = ignored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    )
                                }
                            )?.let {
                                applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it.mediaRecommendation },
                                    media = it.mediaRecommendation.media,
                                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                        copy(
                                            mediaRecommendation = mediaRecommendation.copy(
                                                mediaListStatus = mediaListStatus,
                                                progress = progress,
                                                progressVolumes = progressVolumes,
                                                scoreRaw = scoreRaw,
                                                ignored = ignored,
                                                showLessImportantTags = showLessImportantTags,
                                                showSpoilerTags = showSpoilerTags,
                                            )
                                        )
                                    }
                                )
                            }?.let {
                                val recommendationUpdate =
                                    recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                                val userRating = recommendationUpdate?.rating
                                    ?: it.data.userRating
                                it.transformIf(userRating != it.data.userRating) {
                                    copy(
                                        data = data.copy(
                                            userRating = userRating
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(recommendations::emit)
        }
    }

    fun refresh() {
        newsController.refresh()
        refresh.value = SystemClock.uptimeMillis()
        notificationsController.forceRefresh()
    }
}
