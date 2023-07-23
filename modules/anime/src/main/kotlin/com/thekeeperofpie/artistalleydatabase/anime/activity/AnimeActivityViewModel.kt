package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anilist.UserSocialActivityQuery
import com.anilist.UserSocialActivityQuery.Data.Page.ListActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.MessageActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.OtherActivity
import com.anilist.UserSocialActivityQuery.Data.Page.TextActivityActivity
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.isAdult
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeActivityViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val activityStatusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    private val globalActivity =
        MutableStateFlow(PagingData.empty<ActivityEntry>())
    private var globalActivityJob: Job? = null

    private val followingActivity =
        MutableStateFlow(PagingData.empty<ActivityEntry>())
    private var followingActivityJob: Job? = null

    fun followingActivity(): StateFlow<PagingData<ActivityEntry>> {
        if (followingActivityJob == null) {
            // TODO: React to user changes?
            followingActivityJob = activity(followingActivity, true)
        }

        return followingActivity
    }

    fun globalActivity(): StateFlow<PagingData<ActivityEntry>> {
        if (globalActivityJob == null) {
            globalActivityJob = activity(globalActivity, false)
        }

        return globalActivity
    }

    fun activity(
        target: MutableStateFlow<PagingData<ActivityEntry>>,
        following: Boolean,
    ) = viewModelScope.launch(CustomDispatchers.IO) {
        aniListApi.authedUser.flatMapLatest { viewer ->
            combine(
                settings.showAdult,
                refreshUptimeMillis,
                ::Pair
            ).flatMapLatest { (showAdult, _) ->
                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        val result = aniListApi.userSocialActivity(
                            isFollowing = following,
                            page = it,
                            userIdNot = viewer?.id,
                        )
                        result.page?.pageInfo to
                                result.page?.activities?.filterNotNull().orEmpty()
                    }
                }
                    .flow
                    .map { it.filter { showAdult || !it.isAdult() } }
            }
        }
            .enforceUniqueIntIds {
                when (it) {
                    is ListActivityActivity -> it.id
                    is MessageActivityActivity -> it.id
                    is TextActivityActivity -> it.id
                    is OtherActivity -> null
                }
            }
            .mapLatest { it.map(::ActivityEntry) }
            .cachedIn(viewModelScope)
            .flatMapLatest { pagingData ->
                combine(
                    mediaListStatusController.allChanges(),
                    activityStatusController.allChanges(),
                    ignoreList.updates,
                    settings.showAdult,
                    settings.showIgnored,
                    ::Params,
                ).mapLatest { (mediaListStatuses, activityStatuses, ignoredIds, showAdult, showIgnored) ->
                    pagingData.mapNotNull {
                        applyActivityFiltering(
                            mediaListStatuses = mediaListStatuses,
                            activityStatuses = activityStatuses,
                            ignoredIds = ignoredIds,
                            showAdult = showAdult,
                            showIgnored = showIgnored,
                            entry = it,
                            activityId = it.activityId.valueId,
                            activityLiked = it.liked,
                            activitySubscribed = it.subscribed,
                            media = (it.activity as? ListActivityActivity)?.media,
                            mediaStatusAware = it.media,
                            copyMedia = { status, progress, progressVolumes, ignored ->
                                copy(
                                    media = media?.copy(
                                        mediaListStatus = status,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        ignored = ignored,
                                    )
                                )
                            },
                            copyActivity = { liked, subscribed ->
                                copy(liked = liked, subscribed = subscribed)
                            }
                        )
                    }
                }
            }
            .collectLatest(target::emit)
    }

    private data class Params(
        val mediaListStatuses: Map<String, MediaListStatusController.Update>,
        val activityStatuses: Map<String, ActivityStatusController.Update>,
        val ignoredIds: Set<Int>,
        val showAdult: Boolean,
        val showIgnored: Boolean,
    )

    data class ActivityEntry(
        val activityId: EntryId,
        val activity: UserSocialActivityQuery.Data.Page.Activity,
        override val liked: Boolean,
        override val subscribed: Boolean,
        val media: MediaEntry?,
    ) : ActivityStatusAware {
        constructor(activity: UserSocialActivityQuery.Data.Page.Activity) : this(
            activityId = activity.entryId,
            activity = activity,
            liked = activity.liked,
            subscribed = activity.subscribed,
            media = (activity as? ListActivityActivity)?.media?.let(::MediaEntry),
        )

        data class MediaEntry(
            val media: ListActivityActivity.Media?,
            override val mediaListStatus: MediaListStatus? = media?.mediaListEntry?.status,
            override val progress: Int? = media?.mediaListEntry?.progress,
            override val progressVolumes: Int? = media?.mediaListEntry?.progressVolumes,
            override val ignored: Boolean = false,
        ) : MediaStatusAware
    }
}
