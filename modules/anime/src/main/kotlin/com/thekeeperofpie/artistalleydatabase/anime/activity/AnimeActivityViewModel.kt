package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.UserSocialActivityQuery.Data.Page.ListActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.MessageActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.OtherActivity
import com.anilist.UserSocialActivityQuery.Data.Page.TextActivityActivity
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeActivityViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val activityStatusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val sortFilterController = ActivitySortFilterController(
        screenKey = AnimeNavDestinations.ACTIVITY.id,
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    private val globalActivity =
        MutableStateFlow(PagingData.empty<ActivityEntry>())
    private var globalActivityJob: Job? = null

    private val followingActivity =
        MutableStateFlow(PagingData.empty<ActivityEntry>())
    private var followingActivityJob: Job? = null

    private val ownActivity =
        MutableStateFlow(PagingData.empty<ActivityEntry>())
    private var ownActivityJob: Job? = null

    private val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())

    fun ownActivity(): StateFlow<PagingData<ActivityEntry>> {
        if (ownActivityJob == null) {
            // TODO: React to user changes?
            ownActivityJob = activity(ownActivity, following = false, filterToViewer = true)
        }

        return ownActivity
    }

    fun followingActivity(): StateFlow<PagingData<ActivityEntry>> {
        if (followingActivityJob == null) {
            // TODO: React to user changes?
            followingActivityJob = activity(followingActivity, following = true)
        }

        return followingActivity
    }

    fun globalActivity(): StateFlow<PagingData<ActivityEntry>> {
        if (globalActivityJob == null) {
            globalActivityJob = activity(globalActivity, following = false)
        }

        return globalActivity
    }

    fun activity(
        target: MutableStateFlow<PagingData<ActivityEntry>>,
        following: Boolean,
        filterToViewer: Boolean = false,
    ) = viewModelScope.launch(CustomDispatchers.IO) {
        aniListApi.authedUser.flatMapLatest { viewer ->
            combine(
                sortFilterController.filterParams(),
                refreshUptimeMillis,
                ::Pair
            ).flatMapLatest { (filterParams) ->
                AniListPager {
                    val result = aniListApi.userSocialActivity(
                        isFollowing = following,
                        page = it,
                        userId = if (filterToViewer) viewer?.id else null,
                        userIdNot = if (filterToViewer) null else viewer?.id,
                        sort = filterParams.sort
                            .selectedOption(ActivitySortOption.NEWEST)
                            .toApiValue(),
                        typeIn = filterParams.type
                            .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                            .map { it.value }
                            .ifEmpty { null },
                        typeNotIn = filterParams.type
                            .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                            .map { it.value }
                            .ifEmpty { null },
                        hasReplies = if (filterParams.hasReplies) true else null,
                        createdAtGreater = filterParams.date.startDate
                            ?.atStartOfDay()
                            ?.toEpochSecond(offset)
                            ?.toInt(),
                        createdAtLesser = filterParams.date.endDate
                            ?.plus(1, ChronoUnit.DAYS)
                            ?.atStartOfDay()
                            ?.toEpochSecond(offset)
                            ?.toInt(),
                        mediaId = filterParams.mediaId,
                    )
                    result.page?.pageInfo to
                            result.page?.activities?.filterNotNull().orEmpty()
                }
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
            .mapLatest { it.mapOnIO(::ActivityEntry) }
            .cachedIn(viewModelScope)
            .flatMapLatest { pagingData ->
                combine(
                    mediaListStatusController.allChanges(),
                    activityStatusController.allChanges(),
                    ignoreController.updates(),
                    settings.showAdult,
                    settings.showIgnored,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { mediaListStatuses, activityStatuses, ignoredIds, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                    pagingData.mapNotNull {
                        applyActivityFiltering(
                            mediaListStatuses = mediaListStatuses,
                            activityStatuses = activityStatuses,
                            ignoreController = ignoreController,
                            showAdult = showAdult,
                            showIgnored = showIgnored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                            entry = it,
                            activityId = it.activityId.valueId,
                            activityStatusAware = it,
                            media = (it.activity as? ListActivityActivity)?.media,
                            mediaStatusAware = it.media,
                            copyMedia = { status, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                copy(
                                    media = media?.copy(
                                        mediaListStatus = status,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        ignored = ignored,
                                        showLessImportantTags = showLessImportantTags,
                                        showSpoilerTags = showSpoilerTags,
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
            .cachedIn(viewModelScope)
            .collectLatest(target::emit)
    }

}
