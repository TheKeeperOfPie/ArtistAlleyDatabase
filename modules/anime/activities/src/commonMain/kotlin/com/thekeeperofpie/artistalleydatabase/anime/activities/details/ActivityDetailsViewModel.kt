package com.thekeeperofpie.artistalleydatabase.anime.activities.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.activities.generated.resources.Res
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_details_error_loading
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_error_deleting
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_error_replying
import com.anilist.data.ActivityDetailsQuery
import com.anilist.data.fragment.MediaCompactWithTags
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityDestinations
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityReplyStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityReplyToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ActivityDetailsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val statusController: ActivityStatusController,
    private val mediaListStatusController: MediaListStatusController,
    private val replyStatusController: ActivityReplyStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<ActivityDestinations.ActivityDetails>(navigationTypeMap)
    val activityId = destination.activityId

    val viewer = aniListApi.authedUser
    val refresh = RefreshFlow()
    val state = ActivityDetailsScreen.State<MediaEntry>((activityId))

    val toggleHelper = ActivityToggleHelper(aniListApi, statusController, viewModelScope)
    val replyToggleHelper =
        ActivityReplyToggleHelper(aniListApi, replyStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh.updates,
                Res.string.anime_activity_details_error_loading
            ) {
                flowFromSuspend {
                    aniListApi.activityDetails(activityId).activity
                }
            }
                .flatMapLatest { activity ->
                    statusController.allChanges(activityId)
                        .mapLatest { activityUpdates ->
                            activity.transformResult {
                                val liked = activityUpdates?.liked ?: when (it) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> it.isLiked
                                    is ActivityDetailsQuery.Data.OtherActivity -> false
                                } ?: false
                                val subscribed = activityUpdates?.subscribed ?: when (it) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> it.isSubscribed
                                    is ActivityDetailsQuery.Data.OtherActivity -> false
                                } ?: false
                                ActivityDetailsScreen.Entry(
                                    activity = it,
                                    liked = liked,
                                    subscribed = subscribed,
                                    mediaEntry = (it as? ActivityDetailsQuery.Data.ListActivityActivity)
                                        ?.media
                                        ?.let(mediaEntryProvider::mediaEntry)
                                )
                            }
                        }
                        .flatMapLatest { result ->
                            combine(
                                if (activity.result is ActivityDetailsQuery.Data.ListActivityActivity) {
                                    mediaListStatusController.allChanges(activityId)
                                } else {
                                    flowOf(null)
                                },
                                ignoreController.updates(),
                                settings.mediaFilteringData(forceShowIgnored = true),
                            ) { statuses, _, filteringData ->
                                result.transformResult {
                                    if (it.mediaEntry == null) return@transformResult it
                                    val filterableData =
                                        mediaEntryProvider.mediaFilterable(it.mediaEntry)
                                    val mediaId = filterableData.mediaId
                                    applyMediaFiltering(
                                        statuses = if (statuses == null) {
                                            emptyMap()
                                        } else {
                                            mapOf(mediaId to statuses)
                                        },
                                        ignoreController = ignoreController,
                                        filteringData = filteringData,
                                        entry = it,
                                        filterableData = filterableData,
                                        copy = { newFilterableData ->
                                            copy(
                                                mediaEntry = mediaEntry?.let {
                                                    mediaEntryProvider
                                                        .copyMediaEntry(it, newFilterableData)
                                                }
                                            )
                                        },
                                    )
                                }
                            }
                        }
                }
                .catch {
                    emit(
                        LoadingResult.Companion.error(
                            Res.string.anime_activity_details_error_loading,
                            it
                        )
                    )
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { state.entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .flatMapLatest {
                    AniListPager {
                        val result = aniListApi.activityReplies(id = activityId, page = it)
                        result.page?.pageInfo to result.page?.activityReplies?.filterNotNull()
                            .orEmpty()
                    }
                }
                .enforceUniqueIntIds { it.id }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    replyStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.mapOnIO {
                                val liked = updates[it.id.toString()]?.liked ?: it.isLiked ?: false
                                ActivityDetailsScreen.ReplyEntry(reply = it, liked = liked)
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .collect(state.replies::emit)
        }
    }

    fun onEvent(event: ActivityDetailsScreen.Event) = when (event) {
        is ActivityDetailsScreen.Event.ActivityStatusChange -> toggleHelper.toggle(event.update)
        is ActivityDetailsScreen.Event.Delete -> delete(event.promptData)
        ActivityDetailsScreen.Event.Refresh -> refresh.refresh()
        is ActivityDetailsScreen.Event.ReplyStatusUpdate ->
            replyToggleHelper.toggleLike(event.activityReplyId, event.liked)
        is ActivityDetailsScreen.Event.SendReply -> sendReply(event.reply)
    }

    fun delete(deletePromptData: Either<Unit, ActivityDetailsScreen.ReplyEntry>) {
        if (state.deleting) return
        state.deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                if (deletePromptData is Either.Right) {
                    aniListApi.deleteActivityReply(deletePromptData.value.reply.id.toString())
                } else {
                    aniListApi.deleteActivity(activityId)
                }
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    state.deleting = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    state.error = Res.string.anime_activity_error_deleting to t
                    state.deleting = false
                }
            }
        }
    }

    fun sendReply(reply: String) {
        if (state.replying) return
        state.replying = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.saveActivityReply(activityId = activityId, replyId = null, text = reply)
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    state.replying = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    state.error = Res.string.anime_activity_error_replying to t
                    state.replying = false
                }
            }
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val statusController: ActivityStatusController,
        private val mediaListStatusController: MediaListStatusController,
        private val replyStatusController: ActivityReplyStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        fun <MediaEntry> create(mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>) =
            ActivityDetailsViewModel(
                aniListApi = aniListApi,
                statusController = statusController,
                mediaListStatusController = mediaListStatusController,
                replyStatusController = replyStatusController,
                ignoreController = ignoreController,
                settings = settings,
                navigationTypeMap = navigationTypeMap,
                savedStateHandle = savedStateHandle,
                mediaEntryProvider = mediaEntryProvider,
            )
    }
}
